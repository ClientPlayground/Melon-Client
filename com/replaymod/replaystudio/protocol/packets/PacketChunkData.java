package com.replaymod.replaystudio.protocol.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.io.stream.StreamNetInput;
import com.github.steveice10.packetlib.io.stream.StreamNetOutput;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class PacketChunkData {
  private Column column;
  
  private boolean isUnload;
  
  private int unloadX;
  
  private int unloadZ;
  
  public static PacketChunkData read(Packet packet) throws IOException {
    PacketChunkData chunkData = new PacketChunkData();
    try (Packet.Reader reader = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_9)) {
        if (packet.getType() == PacketType.UnloadChunk) {
          chunkData.readUnload((NetInput)reader);
        } else {
          chunkData.readLoad(packet, reader);
        } 
      } else {
        chunkData.readLoad(packet, reader);
      } 
    } 
    return chunkData;
  }
  
  public Packet write(PacketTypeRegistry registry) throws IOException {
    PacketType packetType;
    boolean atLeastV1_9 = (ProtocolVersion.getIndex(registry.getVersion()) >= ProtocolVersion.getIndex(ProtocolVersion.v1_9));
    if (atLeastV1_9) {
      packetType = this.isUnload ? PacketType.UnloadChunk : PacketType.ChunkData;
    } else {
      packetType = PacketType.ChunkData;
    } 
    Packet packet = new Packet(registry, packetType);
    try (Packet.Writer writer = packet.overwrite()) {
      if (atLeastV1_9) {
        if (this.isUnload) {
          writeUnload((NetOutput)writer);
        } else {
          writeLoad(packet, writer);
        } 
      } else {
        writeLoad(packet, writer);
      } 
    } 
    return packet;
  }
  
  public static List<Column> readBulk(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8))
        return readBulkV1_8(packet, in); 
      return readBulkV1_7(packet, in);
    } 
  }
  
  private static List<Column> readBulkV1_8(Packet packet, Packet.Reader in) throws IOException {
    List<Column> result = new ArrayList<>();
    boolean skylight = in.readBoolean();
    int columns = in.readVarInt();
    int[] xs = new int[columns];
    int[] zs = new int[columns];
    int[] masks = new int[columns];
    int[] lengths = new int[columns];
    int column;
    for (column = 0; column < columns; column++) {
      xs[column] = in.readInt();
      zs[column] = in.readInt();
      masks[column] = in.readUnsignedShort();
      int nChunks = Integer.bitCount(masks[column]);
      int length = nChunks * 10240 + (skylight ? (nChunks * 2048) : 0) + 256;
      lengths[column] = length;
    } 
    for (column = 0; column < columns; column++) {
      byte[] buf = new byte[lengths[column]];
      in.readBytes(buf);
      result.add(readColumn(packet, buf, xs[column], zs[column], true, skylight, masks[column], 0, null, null, null));
    } 
    return result;
  }
  
  private static List<Column> readBulkV1_7(Packet packet, Packet.Reader in) throws IOException {
    List<Column> result = new ArrayList<>();
    short columns = in.readShort();
    int deflatedLength = in.readInt();
    boolean skylight = in.readBoolean();
    byte[] deflatedBytes = in.readBytes(deflatedLength);
    byte[] inflated = new byte[196864 * columns];
    Inflater inflater = new Inflater();
    inflater.setInput(deflatedBytes, 0, deflatedLength);
    try {
      inflater.inflate(inflated);
    } catch (DataFormatException e) {
      throw new IOException("Bad compressed data format");
    } finally {
      inflater.end();
    } 
    int pos = 0;
    for (int count = 0; count < columns; count++) {
      int x = in.readInt();
      int z = in.readInt();
      int chunkMask = in.readShort();
      int extendedChunkMask = in.readShort();
      int chunks = 0;
      int extended = 0;
      for (int ch = 0; ch < 16; ch++) {
        chunks += chunkMask >> ch & 0x1;
        extended += extendedChunkMask >> ch & 0x1;
      } 
      int length = 8192 * chunks + 256 + 2048 * extended;
      if (skylight)
        length += 2048 * chunks; 
      byte[] buf = new byte[length];
      System.arraycopy(inflated, pos, buf, 0, length);
      result.add(readColumn(packet, buf, x, z, true, skylight, chunkMask, extendedChunkMask, null, null, null));
      pos += length;
    } 
    return result;
  }
  
  public static PacketChunkData load(Column column) {
    PacketChunkData chunkData = new PacketChunkData();
    chunkData.column = column;
    return chunkData;
  }
  
  public static PacketChunkData unload(int chunkX, int chunkZ) {
    PacketChunkData chunkData = new PacketChunkData();
    chunkData.isUnload = true;
    chunkData.unloadX = chunkX;
    chunkData.unloadZ = chunkZ;
    chunkData.column = new Column(chunkX, chunkZ, new Chunk[16], new byte[256], null, null, null);
    return chunkData;
  }
  
  public Column getColumn() {
    return this.column;
  }
  
  public boolean isUnload() {
    return this.isUnload;
  }
  
  public int getUnloadX() {
    return this.unloadX;
  }
  
  public int getUnloadZ() {
    return this.unloadZ;
  }
  
  private void readUnload(NetInput in) throws IOException {
    this.isUnload = true;
    this.unloadX = in.readInt();
    this.unloadZ = in.readInt();
  }
  
  private void writeUnload(NetOutput out) throws IOException {
    out.writeInt(this.unloadX);
    out.writeInt(this.unloadZ);
  }
  
  private void readLoad(Packet packet, Packet.Reader in) throws IOException {
    byte[] data;
    int x = in.readInt();
    int z = in.readInt();
    boolean fullChunk = in.readBoolean();
    int chunkMask = packet.atLeast(ProtocolVersion.v1_9) ? in.readVarInt() : in.readUnsignedShort();
    int extendedChunkMask = 0;
    if (!packet.atLeast(ProtocolVersion.v1_8))
      extendedChunkMask = in.readUnsignedShort(); 
    CompoundTag heightmaps = null;
    if (packet.atLeast(ProtocolVersion.v1_14))
      heightmaps = in.readNBT(); 
    int[] biomes = null;
    if (packet.atLeast(ProtocolVersion.v1_15) && fullChunk)
      biomes = in.readInts(1024); 
    if (packet.atLeast(ProtocolVersion.v1_8)) {
      data = in.readBytes(in.readVarInt());
    } else {
      byte[] deflated = in.readBytes(in.readInt());
      int len = 12288 * Integer.bitCount(chunkMask);
      if (fullChunk)
        len += 256; 
      data = new byte[len];
      Inflater inflater = new Inflater();
      inflater.setInput(deflated, 0, deflated.length);
      try {
        inflater.inflate(data);
      } catch (DataFormatException e) {
        throw new IOException("Bad compressed data format");
      } finally {
        inflater.end();
      } 
    } 
    CompoundTag[] tileEntities = null;
    if (packet.atLeast(ProtocolVersion.v1_9_3)) {
      tileEntities = new CompoundTag[in.readVarInt()];
      for (int i = 0; i < tileEntities.length; i++)
        tileEntities[i] = in.readNBT(); 
    } 
    this.column = readColumn(packet, data, x, z, fullChunk, false, chunkMask, extendedChunkMask, tileEntities, heightmaps, biomes);
    if (packet.atMost(ProtocolVersion.v1_8) && fullChunk && chunkMask == 0) {
      this.isUnload = true;
      this.unloadX = x;
      this.unloadZ = z;
    } 
  }
  
  private void writeLoad(Packet packet, Packet.Writer out) throws IOException {
    int len;
    byte[] data;
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    StreamNetOutput streamNetOutput = new StreamNetOutput(byteOut);
    Pair<Integer, Integer> masks = writeColumn(packet, (NetOutput)streamNetOutput, this.column, this.column.isFull());
    int mask = ((Integer)masks.getKey()).intValue();
    int extendedMask = ((Integer)masks.getValue()).intValue();
    out.writeInt(this.column.x);
    out.writeInt(this.column.z);
    out.writeBoolean(this.column.isFull());
    if (packet.atLeast(ProtocolVersion.v1_9)) {
      out.writeVarInt(mask);
    } else {
      out.writeShort(mask);
    } 
    if (!packet.atLeast(ProtocolVersion.v1_8))
      out.writeShort(extendedMask); 
    if (packet.atLeast(ProtocolVersion.v1_14))
      out.writeNBT(this.column.heightMaps); 
    if (packet.atLeast(ProtocolVersion.v1_15) && this.column.biomes != null)
      out.writeInts(this.column.biomes); 
    if (packet.atLeast(ProtocolVersion.v1_8)) {
      len = byteOut.size();
      data = byteOut.toByteArray();
    } else {
      Deflater deflater = new Deflater(-1);
      len = byteOut.size();
      data = new byte[len];
      try {
        deflater.setInput(byteOut.toByteArray(), 0, len);
        deflater.finish();
        len = deflater.deflate(data);
      } finally {
        deflater.end();
      } 
    } 
    out.writeVarInt(len);
    out.writeBytes(data, len);
    if (packet.atLeast(ProtocolVersion.v1_9_3)) {
      out.writeVarInt(this.column.tileEntities.length);
      for (CompoundTag tag : this.column.tileEntities)
        out.writeNBT(tag); 
    } 
  }
  
  private static Column readColumn(Packet packet, byte[] data, int x, int z, boolean fullChunk, boolean hasSkylight, int mask, int extendedMask, CompoundTag[] tileEntities, CompoundTag heightmaps, int[] biomes) throws IOException {
    StreamNetInput streamNetInput = new StreamNetInput(new ByteArrayInputStream(data));
    Throwable ex = null;
    Column column = null;
    try {
      Chunk[] chunks = new Chunk[16];
      int index;
      for (index = 0; index < chunks.length; index++) {
        if ((mask & 1 << index) != 0) {
          Chunk chunk = new Chunk();
          if (packet.atLeast(ProtocolVersion.v1_9)) {
            chunk.blocks = new BlockStorage(packet, (NetInput)streamNetInput);
            if (packet.atMost(ProtocolVersion.v1_13_2)) {
              chunk.blockLight = streamNetInput.readBytes(2048);
              chunk.skyLight = hasSkylight ? streamNetInput.readBytes(2048) : null;
            } 
          } else {
            chunk.blocks = new BlockStorage(packet);
          } 
          chunks[index] = chunk;
        } 
      } 
      if (!packet.atLeast(ProtocolVersion.v1_9)) {
        if (packet.atLeast(ProtocolVersion.v1_8)) {
          for (Chunk chunk : chunks) {
            if (chunk != null)
              chunk.blocks.storage = new FlexibleStorage(0, streamNetInput.readLongs(1024)); 
          } 
        } else {
          for (Chunk chunk : chunks) {
            if (chunk != null)
              chunk.blocks.storage = new FlexibleStorage(0, streamNetInput.readLongs(512)); 
          } 
          for (Chunk chunk : chunks) {
            if (chunk != null)
              chunk.blocks.metadata = streamNetInput.readLongs(256); 
          } 
        } 
        for (Chunk chunk : chunks) {
          if (chunk != null)
            chunk.blockLight = streamNetInput.readBytes(2048); 
        } 
        if (hasSkylight)
          for (Chunk chunk : chunks) {
            if (chunk != null)
              chunk.skyLight = streamNetInput.readBytes(2048); 
          }  
        for (index = 0; index < chunks.length; index++) {
          if ((extendedMask & 1 << index) != 0)
            if (chunks[index] == null) {
              streamNetInput.readLongs(256);
            } else {
              (chunks[index]).blocks.extended = streamNetInput.readLongs(256);
            }  
        } 
      } 
      byte[] biomeData = null;
      if (fullChunk && !packet.atLeast(ProtocolVersion.v1_15))
        biomeData = streamNetInput.readBytes(packet.atLeast(ProtocolVersion.v1_13) ? 1024 : 256); 
      column = new Column(x, z, chunks, biomeData, tileEntities, heightmaps, biomes);
    } catch (Throwable e) {
      ex = e;
    } 
    if ((streamNetInput.available() > 0 || ex != null) && !hasSkylight)
      return readColumn(packet, data, x, z, fullChunk, true, mask, extendedMask, tileEntities, heightmaps, biomes); 
    if (ex != null)
      throw new IOException("Failed to read chunk data.", ex); 
    return column;
  }
  
  private static Pair<Integer, Integer> writeColumn(Packet packet, NetOutput out, Column column, boolean fullChunk) throws IOException {
    int mask = 0;
    int extendedMask = 0;
    Chunk[] chunks = column.chunks;
    int index;
    for (index = 0; index < chunks.length; index++) {
      Chunk chunk = chunks[index];
      if (chunk != null) {
        mask |= 1 << index;
        if (packet.atLeast(ProtocolVersion.v1_9)) {
          chunk.blocks.write(packet, out);
          if (packet.atMost(ProtocolVersion.v1_13_2)) {
            out.writeBytes(chunk.blockLight);
            if (chunk.skyLight != null)
              out.writeBytes(chunk.skyLight); 
          } 
        } 
      } 
    } 
    if (!packet.atLeast(ProtocolVersion.v1_9)) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        for (Chunk chunk : chunks) {
          if (chunk != null)
            out.writeLongs(chunk.blocks.storage.data); 
        } 
      } else {
        for (Chunk chunk : chunks) {
          if (chunk != null)
            out.writeLongs(chunk.blocks.storage.data); 
        } 
        for (Chunk chunk : chunks) {
          if (chunk != null)
            out.writeLongs(chunk.blocks.metadata); 
        } 
      } 
      for (Chunk chunk : chunks) {
        if (chunk != null)
          out.writeBytes(chunk.blockLight); 
      } 
      for (Chunk chunk : chunks) {
        if (chunk != null && chunk.skyLight != null)
          out.writeBytes(chunk.skyLight); 
      } 
      for (index = 0; index < chunks.length; index++) {
        if (chunks[index] != null && (chunks[index]).blocks.extended != null) {
          extendedMask |= 1 << index;
          out.writeLongs((chunks[index]).blocks.extended);
        } 
      } 
    } 
    if (fullChunk && !packet.atLeast(ProtocolVersion.v1_15))
      out.writeBytes(column.biomeData); 
    return new Pair(Integer.valueOf(mask), Integer.valueOf(extendedMask));
  }
  
  public static class Column {
    public int x;
    
    public int z;
    
    public PacketChunkData.Chunk[] chunks;
    
    public byte[] biomeData;
    
    public CompoundTag[] tileEntities;
    
    public CompoundTag heightMaps;
    
    public int[] biomes;
    
    public Column(int x, int z, PacketChunkData.Chunk[] chunks, byte[] biomeData, CompoundTag[] tileEntities, CompoundTag heightmaps, int[] biomes) {
      this.x = x;
      this.z = z;
      this.chunks = chunks;
      this.biomeData = biomeData;
      this.tileEntities = tileEntities;
      this.heightMaps = heightmaps;
      this.biomes = biomes;
    }
    
    public boolean isFull() {
      return (this.biomeData != null || this.biomes != null);
    }
  }
  
  public static class Chunk {
    public PacketChunkData.BlockStorage blocks;
    
    public byte[] blockLight;
    
    public byte[] skyLight;
    
    public Chunk copy() {
      Chunk copy = new Chunk();
      copy.blocks = (this.blocks != null) ? this.blocks.copy() : null;
      copy.blockLight = (this.blockLight != null) ? (byte[])this.blockLight.clone() : null;
      copy.skyLight = (this.skyLight != null) ? (byte[])this.skyLight.clone() : null;
      return copy;
    }
  }
  
  public static class BlockStorage {
    private int blockCount;
    
    private int bitsPerEntry;
    
    private List<Integer> states;
    
    private PacketChunkData.FlexibleStorage storage;
    
    private long[] metadata;
    
    private long[] extended;
    
    private BlockStorage(BlockStorage from) {
      this.blockCount = from.blockCount;
      this.bitsPerEntry = from.bitsPerEntry;
      if (from.states != null)
        this.states = new ArrayList<>(from.states); 
      if (from.storage != null)
        this.storage = new PacketChunkData.FlexibleStorage(this.bitsPerEntry, (long[])from.storage.data.clone()); 
      if (from.metadata != null)
        this.metadata = (long[])from.metadata.clone(); 
      if (from.extended != null)
        this.extended = (long[])from.extended.clone(); 
    }
    
    public BlockStorage() {
      this.blockCount = 0;
      this.bitsPerEntry = 4;
      this.states = new ArrayList<>();
      this.states.add(Integer.valueOf(0));
      this.storage = new PacketChunkData.FlexibleStorage(this.bitsPerEntry, 4096);
    }
    
    BlockStorage(Packet packet) {}
    
    BlockStorage(Packet packet, NetInput in) throws IOException {
      if (packet.atLeast(ProtocolVersion.v1_14))
        this.blockCount = in.readShort(); 
      this.bitsPerEntry = in.readUnsignedByte();
      this.states = new ArrayList<>();
      int stateCount = (this.bitsPerEntry > 8 && packet.atLeast(ProtocolVersion.v1_13)) ? 0 : in.readVarInt();
      for (int i = 0; i < stateCount; i++)
        this.states.add(Integer.valueOf(in.readVarInt())); 
      this.storage = new PacketChunkData.FlexibleStorage(this.bitsPerEntry, in.readLongs(in.readVarInt()));
    }
    
    void write(Packet packet, NetOutput out) throws IOException {
      if (packet.atLeast(ProtocolVersion.v1_14))
        out.writeShort(this.blockCount); 
      out.writeByte(this.bitsPerEntry);
      if (this.bitsPerEntry <= 8 || !packet.atLeast(ProtocolVersion.v1_13)) {
        out.writeVarInt(this.states.size());
        for (Integer state : this.states)
          out.writeVarInt(state.intValue()); 
      } 
      out.writeVarInt(this.storage.data.length);
      out.writeLongs(this.storage.data);
    }
    
    private static int index(int x, int y, int z) {
      return y << 8 | z << 4 | x;
    }
    
    public int get(int x, int y, int z) {
      int id = this.storage.get(index(x, y, z));
      return (this.bitsPerEntry <= 8) ? ((id >= 0 && id < this.states.size()) ? ((Integer)this.states.get(id)).intValue() : 0) : id;
    }
    
    public void set(int x, int y, int z, int state) {
      int id = (this.bitsPerEntry <= 8) ? this.states.indexOf(Integer.valueOf(state)) : state;
      if (id == -1) {
        this.states.add(Integer.valueOf(state));
        if (this.states.size() > 1 << this.bitsPerEntry) {
          this.bitsPerEntry++;
          List<Integer> oldStates = this.states;
          if (this.bitsPerEntry > 8) {
            oldStates = new ArrayList<>(this.states);
            this.states.clear();
            this.bitsPerEntry = 13;
          } 
          PacketChunkData.FlexibleStorage oldStorage = this.storage;
          this.storage = new PacketChunkData.FlexibleStorage(this.bitsPerEntry, this.storage.getSize());
          for (int index = 0; index < this.storage.getSize(); index++)
            this.storage.set(index, (this.bitsPerEntry <= 8) ? oldStorage.get(index) : ((Integer)oldStates.get(index)).intValue()); 
        } 
        id = (this.bitsPerEntry <= 8) ? this.states.indexOf(Integer.valueOf(state)) : state;
      } 
      int ind = index(x, y, z);
      int curr = this.storage.get(ind);
      if (state != 0 && curr == 0) {
        this.blockCount++;
      } else if (state == 0 && curr != 0) {
        this.blockCount--;
      } 
      this.storage.set(ind, id);
    }
    
    public BlockStorage copy() {
      return new BlockStorage(this);
    }
  }
  
  private static class FlexibleStorage {
    private final long[] data;
    
    private final int bitsPerEntry;
    
    private final int size;
    
    private final long maxEntryValue;
    
    public FlexibleStorage(int bitsPerEntry, int size) {
      this(bitsPerEntry, new long[roundToNearest(size * bitsPerEntry, 64) / 64]);
    }
    
    public FlexibleStorage(int bitsPerEntry, long[] data) {
      if (bitsPerEntry < 4)
        bitsPerEntry = 4; 
      this.bitsPerEntry = bitsPerEntry;
      this.data = data;
      this.size = this.data.length * 64 / this.bitsPerEntry;
      this.maxEntryValue = (1L << this.bitsPerEntry) - 1L;
    }
    
    private static int roundToNearest(int value, int roundTo) {
      if (roundTo == 0)
        return 0; 
      if (value == 0)
        return roundTo; 
      if (value < 0)
        roundTo *= -1; 
      int remainder = value % roundTo;
      return (remainder != 0) ? (value + roundTo - remainder) : value;
    }
    
    public long[] getData() {
      return this.data;
    }
    
    public int getBitsPerEntry() {
      return this.bitsPerEntry;
    }
    
    public int getSize() {
      return this.size;
    }
    
    public int get(int index) {
      if (index < 0 || index > this.size - 1)
        throw new IndexOutOfBoundsException(); 
      int bitIndex = index * this.bitsPerEntry;
      int startIndex = bitIndex / 64;
      int endIndex = ((index + 1) * this.bitsPerEntry - 1) / 64;
      int startBitSubIndex = bitIndex % 64;
      if (startIndex == endIndex)
        return (int)(this.data[startIndex] >>> startBitSubIndex & this.maxEntryValue); 
      int endBitSubIndex = 64 - startBitSubIndex;
      return (int)((this.data[startIndex] >>> startBitSubIndex | this.data[endIndex] << endBitSubIndex) & this.maxEntryValue);
    }
    
    public void set(int index, int value) {
      if (index < 0 || index > this.size - 1)
        throw new IndexOutOfBoundsException(); 
      if (value < 0 || value > this.maxEntryValue)
        throw new IllegalArgumentException("Value cannot be outside of accepted range."); 
      int bitIndex = index * this.bitsPerEntry;
      int startIndex = bitIndex / 64;
      int endIndex = ((index + 1) * this.bitsPerEntry - 1) / 64;
      int startBitSubIndex = bitIndex % 64;
      this.data[startIndex] = this.data[startIndex] & (this.maxEntryValue << startBitSubIndex ^ 0xFFFFFFFFFFFFFFFFL) | (value & this.maxEntryValue) << startBitSubIndex;
      if (startIndex != endIndex) {
        int endBitSubIndex = 64 - startBitSubIndex;
        this.data[endIndex] = this.data[endIndex] >>> endBitSubIndex << endBitSubIndex | (value & this.maxEntryValue) >> endBitSubIndex;
      } 
    }
  }
}
