package com.replaymod.replaystudio.viaversion;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.CancelException;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.Direction;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ViaVersionPacketConverter {
  private final UserConnection user;
  
  private final CustomViaAPI viaAPI;
  
  private final ProtocolPipeline pipeline;
  
  @Deprecated
  public static ViaVersionPacketConverter createForFileVersion(int input, int output) {
    return createForFileVersion(input, 0, ((Integer)ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(output))).intValue());
  }
  
  public static ViaVersionPacketConverter createForFileVersion(int fileVersion, int fileProtocol, int outputProtocol) {
    if (!ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.containsKey(Integer.valueOf(fileVersion)) && fileVersion < 10)
      throw new IllegalArgumentException("Unknown file version"); 
    return createForProtocolVersion((fileVersion < 10) ? ((Integer)ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(fileVersion))).intValue() : fileProtocol, outputProtocol);
  }
  
  public static ViaVersionPacketConverter createForProtocolVersion(int input, int output) {
    return new ViaVersionPacketConverter(input, output);
  }
  
  @Deprecated
  public static boolean isFileVersionSupported(int input, int output) {
    return (ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.containsKey(Integer.valueOf(input)) && ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT
      .containsKey(Integer.valueOf(output)) && 
      isProtocolVersionSupported(((Integer)ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(input))).intValue(), ((Integer)ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(output))).intValue()));
  }
  
  public static boolean isFileVersionSupported(int fileVersion, int fileProtocol, int outputProtocol) {
    if (fileVersion < 10) {
      if (!ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.containsKey(Integer.valueOf(fileVersion)))
        return false; 
      fileProtocol = ((Integer)ReplayMetaData.PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(fileVersion))).intValue();
    } 
    return isProtocolVersionSupported(fileProtocol, outputProtocol);
  }
  
  public static boolean isProtocolVersionSupported(int input, int output) {
    if (input == output)
      return true; 
    CustomViaManager.initialize();
    return (ProtocolRegistry.getProtocolPath(output, input) != null);
  }
  
  private List<ByteBuf> out = new ArrayList<>();
  
  private ViaVersionPacketConverter(int inputProtocol, int outputProtocol) {
    CustomViaManager.initialize();
    List<Pair<Integer, Protocol>> path = ProtocolRegistry.getProtocolPath(outputProtocol, inputProtocol);
    if (path != null) {
      this.user = new DummyUserConnection();
      this.viaAPI = new CustomViaAPI(inputProtocol, this.user);
      this.pipeline = new ProtocolPipeline(this.user);
      ProtocolInfo protocolInfo = (ProtocolInfo)this.user.get(ProtocolInfo.class);
      protocolInfo.setState(State.PLAY);
      protocolInfo.setUsername("$Camera$");
      protocolInfo.setUuid(UUID.randomUUID());
      path.stream().map(Pair::getValue).forEachOrdered(this.pipeline::add);
    } else {
      this.user = null;
      this.viaAPI = null;
      this.pipeline = null;
    } 
  }
  
  @Deprecated
  public List<ByteBuf> convertPacket(ByteBuf buf) throws IOException {
    return convertPacket(buf, State.PLAY);
  }
  
  public List<ByteBuf> convertPacket(ByteBuf buf, State state) throws IOException {
    if (this.user == null) {
      buf.retain();
      return Collections.singletonList(buf);
    } 
    CustomViaAPI.INSTANCE.set(this.viaAPI);
    try {
      int packetId = (new ByteBufNetInput(buf)).readVarInt();
      PacketWrapper packetWrapper = new PacketWrapper(packetId, buf, this.user);
      try {
        this.pipeline.transform(Direction.OUTGOING, state, packetWrapper);
      } catch (CancelException e) {
        if (!this.out.isEmpty())
          return popOut(); 
        return (List)Collections.emptyList();
      } 
      ByteBuf result = buf.alloc().buffer();
      packetWrapper.writeToBuffer(result);
      if (!this.out.isEmpty()) {
        this.out.add(0, result);
        return popOut();
      } 
      return Collections.singletonList(result);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Exception during ViaVersion conversion:", e);
    } finally {
      CustomViaAPI.INSTANCE.remove();
    } 
  }
  
  private List<ByteBuf> popOut() {
    try {
      return this.out;
    } finally {
      this.out = new ArrayList<>();
    } 
  }
  
  private final class DummyUserConnection extends UserConnection {
    DummyUserConnection() {
      super((Channel)new EmbeddedChannel());
    }
    
    public void sendRawPacket(ByteBuf packet, boolean currentThread) {
      ViaVersionPacketConverter.this.out.add(packet);
    }
    
    public ChannelFuture sendRawPacketFuture(ByteBuf packet) {
      throw new UnsupportedOperationException();
    }
  }
}
