package com.replaymod.replaystudio.io;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.PooledByteBufAllocator;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.protocol.packets.PacketLoginSuccess;
import com.replaymod.replaystudio.stream.PacketStream;
import com.replaymod.replaystudio.studio.StudioPacketStream;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.util.Utils;
import com.replaymod.replaystudio.viaversion.ViaVersionPacketConverter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class ReplayInputStream extends InputStream {
  private static final ByteBufAllocator ALLOC = (ByteBufAllocator)PooledByteBufAllocator.DEFAULT;
  
  private PacketTypeRegistry registry;
  
  private final InputStream in;
  
  private ViaVersionPacketConverter viaVersionConverter;
  
  private boolean loginPhase;
  
  private boolean outputLoginPhase;
  
  private Queue<PacketData> buffer = new ArrayDeque<>();
  
  public ReplayInputStream(PacketTypeRegistry registry, InputStream in, int fileFormatVersion, int fileProtocol) throws IOException {
    boolean includeLoginPhase = (fileFormatVersion >= 14);
    this.registry = registry;
    this.loginPhase = includeLoginPhase;
    this.outputLoginPhase = (registry.getState() == State.LOGIN);
    if (!includeLoginPhase && this.outputLoginPhase) {
      this.buffer.offer(new PacketData(0L, (new PacketLoginSuccess(UUID.nameUUIDFromBytes(new byte[0]).toString(), "Player")).write(registry)));
      this.registry = PacketTypeRegistry.get(registry.getVersion(), State.PLAY);
    } else if (includeLoginPhase && !this.outputLoginPhase) {
      this.registry = PacketTypeRegistry.get(registry.getVersion(), State.LOGIN);
    } 
    this.in = in;
    this.viaVersionConverter = ViaVersionPacketConverter.createForFileVersion(fileFormatVersion, fileProtocol, registry.getVersion().getId());
  }
  
  public int read() throws IOException {
    return this.in.read();
  }
  
  public void close() throws IOException {
    this.in.close();
  }
  
  public PacketTypeRegistry getRegistry() {
    return this.registry;
  }
  
  public PacketData readPacket() throws IOException {
    fillBuffer();
    return this.buffer.poll();
  }
  
  private void fillBuffer() throws IOException {
    while (this.buffer.isEmpty()) {
      int next = Utils.readInt(this.in);
      int length = Utils.readInt(this.in);
      if (next == -1 || length == -1)
        break; 
      if (length == 0)
        continue; 
      ByteBuf buf = ALLOC.buffer(length);
      while (length > 0) {
        int read = buf.writeBytes(this.in, length);
        if (read == -1)
          throw new EOFException(); 
        length -= read;
      } 
      List<Packet> decoded = new LinkedList<>();
      try {
        for (ByteBuf packet : this.viaVersionConverter.convertPacket(buf, this.loginPhase ? State.LOGIN : State.PLAY)) {
          int packetId = (new ByteBufNetInput(packet)).readVarInt();
          decoded.add(new Packet(this.registry, packetId, this.registry.getType(packetId), packet));
        } 
      } catch (Exception e) {
        throw (e instanceof IOException) ? (IOException)e : new IOException("decoding", e);
      } 
      buf.release();
      for (Packet packet : decoded) {
        PacketType type = packet.getType();
        if (type == PacketType.KeepAlive) {
          packet.release();
          continue;
        } 
        if (type == PacketType.LoginSuccess) {
          this.loginPhase = false;
          this.registry = PacketTypeRegistry.get(this.registry.getVersion(), State.PLAY);
        } 
        if ((this.loginPhase || type == PacketType.LoginSuccess) && !this.outputLoginPhase) {
          packet.release();
          continue;
        } 
        this.buffer.offer(new PacketData(next, packet));
      } 
    } 
  }
  
  public PacketStream asPacketStream() {
    return (PacketStream)new StudioPacketStream(this);
  }
}
