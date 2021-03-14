package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.CustomByteType;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BulkChunkTranslatorProvider implements Provider {
  public List<Object> transformMapChunkBulk(Object packet, ClientChunks clientChunks) throws Exception {
    if (!(packet instanceof PacketWrapper))
      throw new IllegalArgumentException("The default packet has to be a PacketWrapper for transformMapChunkBulk, unexpected " + packet.getClass()); 
    List<Object> packets = new ArrayList();
    PacketWrapper wrapper = (PacketWrapper)packet;
    boolean skyLight = ((Boolean)wrapper.read(Type.BOOLEAN)).booleanValue();
    int count = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
    ChunkBulkSection[] metas = new ChunkBulkSection[count];
    for (int i = 0; i < count; i++)
      metas[i] = ChunkBulkSection.read(wrapper, skyLight); 
    for (ChunkBulkSection meta : metas) {
      CustomByteType customByteType = new CustomByteType(Integer.valueOf(meta.getLength()));
      meta.setData((byte[])wrapper.read((Type)customByteType));
      PacketWrapper chunkPacket = new PacketWrapper(33, null, wrapper.user());
      chunkPacket.write(Type.INT, Integer.valueOf(meta.getX()));
      chunkPacket.write(Type.INT, Integer.valueOf(meta.getZ()));
      chunkPacket.write(Type.BOOLEAN, Boolean.valueOf(true));
      chunkPacket.write(Type.UNSIGNED_SHORT, Integer.valueOf(meta.getBitMask()));
      chunkPacket.write(Type.VAR_INT, Integer.valueOf(meta.getLength()));
      chunkPacket.write((Type)customByteType, meta.getData());
      clientChunks.getBulkChunks().add(Long.valueOf(ClientChunks.toLong(meta.getX(), meta.getZ())));
      packets.add(chunkPacket);
    } 
    return packets;
  }
  
  public boolean isFiltered(Class<?> packet) {
    return false;
  }
  
  public boolean isPacketLevel() {
    return true;
  }
  
  private static class ChunkBulkSection {
    private int x;
    
    private int z;
    
    private int bitMask;
    
    private int length;
    
    private byte[] data;
    
    public void setX(int x) {
      this.x = x;
    }
    
    public void setZ(int z) {
      this.z = z;
    }
    
    public void setBitMask(int bitMask) {
      this.bitMask = bitMask;
    }
    
    public void setLength(int length) {
      this.length = length;
    }
    
    public void setData(byte[] data) {
      this.data = data;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof ChunkBulkSection))
        return false; 
      ChunkBulkSection other = (ChunkBulkSection)o;
      return !other.canEqual(this) ? false : ((getX() != other.getX()) ? false : ((getZ() != other.getZ()) ? false : ((getBitMask() != other.getBitMask()) ? false : ((getLength() != other.getLength()) ? false : (!!Arrays.equals(getData(), other.getData()))))));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof ChunkBulkSection;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      result = result * 59 + getX();
      result = result * 59 + getZ();
      result = result * 59 + getBitMask();
      result = result * 59 + getLength();
      return result * 59 + Arrays.hashCode(getData());
    }
    
    public String toString() {
      return "BulkChunkTranslatorProvider.ChunkBulkSection(x=" + getX() + ", z=" + getZ() + ", bitMask=" + getBitMask() + ", length=" + getLength() + ", data=" + Arrays.toString(getData()) + ")";
    }
    
    public int getX() {
      return this.x;
    }
    
    public int getZ() {
      return this.z;
    }
    
    public int getBitMask() {
      return this.bitMask;
    }
    
    public int getLength() {
      return this.length;
    }
    
    public byte[] getData() {
      return this.data;
    }
    
    public static ChunkBulkSection read(PacketWrapper wrapper, boolean skylight) throws Exception {
      ChunkBulkSection bulkSection = new ChunkBulkSection();
      bulkSection.setX(((Integer)wrapper.read(Type.INT)).intValue());
      bulkSection.setZ(((Integer)wrapper.read(Type.INT)).intValue());
      bulkSection.setBitMask(((Integer)wrapper.read(Type.UNSIGNED_SHORT)).intValue());
      int bitCount = Integer.bitCount(bulkSection.getBitMask());
      bulkSection.setLength(bitCount * 10240 + (skylight ? (bitCount * 2048) : 0) + 256);
      return bulkSection;
    }
  }
}
