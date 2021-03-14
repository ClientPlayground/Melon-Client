package com.replaymod.replaystudio.protocol;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.util.IPosition;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Packet {
  private final PacketTypeRegistry registry;
  
  private final int id;
  
  private final PacketType type;
  
  private final ByteBuf buf;
  
  public Packet(PacketTypeRegistry registry, PacketType type) {
    this(registry, type, Unpooled.buffer());
  }
  
  public Packet(PacketTypeRegistry registry, PacketType type, ByteBuf buf) {
    this(registry, registry.getId(type).intValue(), type, buf);
  }
  
  public Packet(PacketTypeRegistry registry, int packetId, ByteBuf buf) {
    this(registry, packetId, registry.getType(packetId), buf);
  }
  
  public Packet(PacketTypeRegistry registry, int id, PacketType type, ByteBuf buf) {
    this.registry = registry;
    this.id = id;
    this.type = type;
    this.buf = buf;
  }
  
  public PacketTypeRegistry getRegistry() {
    return this.registry;
  }
  
  public ProtocolVersion getProtocolVersion() {
    return this.registry.getVersion();
  }
  
  public int getId() {
    return this.id;
  }
  
  public PacketType getType() {
    return this.type;
  }
  
  public ByteBuf getBuf() {
    return this.buf;
  }
  
  public Packet retain() {
    this.buf.retain();
    return this;
  }
  
  public Packet copy() {
    return new Packet(this.registry, this.id, this.type, this.buf.retainedSlice());
  }
  
  public boolean release() {
    return this.buf.release();
  }
  
  public Reader reader() {
    return new Reader(this, this.buf);
  }
  
  public Writer overwrite() {
    this.buf.writerIndex(this.buf.readerIndex());
    return new Writer(this, this.buf);
  }
  
  public boolean atLeast(ProtocolVersion protocolVersion) {
    return this.registry.atLeast(protocolVersion);
  }
  
  public boolean atMost(ProtocolVersion protocolVersion) {
    return this.registry.atMost(protocolVersion);
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    Packet packet = (Packet)o;
    return (this.id == packet.id && this.registry
      .equals(packet.registry) && this.buf
      .equals(packet.buf));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.registry, Integer.valueOf(this.id), this.buf });
  }
  
  public static class Reader extends ByteBufNetInput implements AutoCloseable {
    private final Packet packet;
    
    private final ByteBuf buf;
    
    private int orgReaderIndex;
    
    Reader(Packet packet, ByteBuf buf) {
      super(buf);
      this.packet = packet;
      this.buf = buf;
      this.orgReaderIndex = buf.readerIndex();
    }
    
    public void close() {
      this.buf.readerIndex(this.orgReaderIndex);
    }
    
    public IPosition readPosition() throws IOException {
      return readPosition(this.packet.registry, (NetInput)this);
    }
    
    public static IPosition readPosition(PacketTypeRegistry registry, NetInput in) throws IOException {
      long val = in.readLong();
      int x = (int)(val >> 38L);
      int y = (int)(val & 0xFFFL);
      int z = (int)(val << 26L >> 38L);
      return new IPosition(x, y, z);
    }
    
    public CompoundTag readNBT() throws IOException {
      return readNBT(this.packet.registry, (NetInput)this);
    }
    
    public static CompoundTag readNBT(PacketTypeRegistry registry, final NetInput in) throws IOException {
      if (registry.atLeast(ProtocolVersion.v1_8)) {
        final byte b = in.readByte();
        if (b == 0)
          return null; 
        return (CompoundTag)NBTIO.readTag(new InputStream() {
              private boolean first = true;
              
              public int read() throws IOException {
                if (this.first) {
                  this.first = false;
                  return b;
                } 
                return in.readUnsignedByte();
              }
            });
      } 
      short length = in.readShort();
      if (length < 0)
        return null; 
      return (CompoundTag)NBTIO.readTag(new GZIPInputStream(new ByteArrayInputStream(in.readBytes(length))));
    }
  }
  
  public static class Writer extends ByteBufNetOutput implements AutoCloseable {
    private final Packet packet;
    
    private Writer(Packet packet, ByteBuf buf) {
      super(buf);
      this.packet = packet;
    }
    
    public void close() {}
    
    public void writePosition(IPosition pos) throws IOException {
      writePosition((NetOutput)this, pos);
    }
    
    public static void writePosition(NetOutput out, IPosition pos) throws IOException {
      long x = (pos.getX() & 0x3FFFFFF);
      long y = (pos.getY() & 0xFFF);
      long z = (pos.getZ() & 0x3FFFFFF);
      out.writeLong(x << 38L | z << 12L | y);
    }
    
    public void writeNBT(CompoundTag tag) throws IOException {
      writeNBT(this.packet.registry, (NetOutput)this, tag);
    }
    
    public static void writeNBT(PacketTypeRegistry registry, final NetOutput out, CompoundTag tag) throws IOException {
      if (registry.atLeast(ProtocolVersion.v1_8)) {
        if (tag == null) {
          out.writeByte(0);
        } else {
          NBTIO.writeTag(new OutputStream() {
                public void write(int i) throws IOException {
                  out.writeByte(i);
                }
              },  (Tag)tag);
        } 
      } else {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(output);
        NBTIO.writeTag(gzip, (Tag)tag);
        gzip.close();
        output.close();
        byte[] bytes = output.toByteArray();
        out.writeShort(bytes.length);
        out.writeBytes(bytes);
      } 
    }
  }
}
