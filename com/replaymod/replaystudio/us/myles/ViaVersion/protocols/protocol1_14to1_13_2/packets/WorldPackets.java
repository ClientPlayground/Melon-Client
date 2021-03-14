package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_14Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import java.util.Arrays;

public class WorldPackets {
  private static final int AIR = MappingData.blockStateMappings.getNewBlock(0);
  
  private static final int VOID_AIR = MappingData.blockStateMappings.getNewBlock(8591);
  
  private static final int CAVE_AIR = MappingData.blockStateMappings.getNewBlock(8592);
  
  public static final int SERVERSIDE_VIEW_DISTANCE = 64;
  
  private static final Byte[] FULL_LIGHT = new Byte[2048];
  
  static {
    Arrays.fill((Object[])FULL_LIGHT, Byte.valueOf((byte)-1));
  }
  
  public static void register(final Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 8, 8, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.POSITION, Type.POSITION1_14);
            map(Type.BYTE);
          }
        });
    protocol.registerOutgoing(State.PLAY, 9, 9, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION, Type.POSITION1_14);
          }
        });
    protocol.registerOutgoing(State.PLAY, 10, 10, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION, Type.POSITION1_14);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 11, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION, Type.POSITION1_14);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_14To1_13_2.getNewBlockStateId(id)));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 13, 13, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 15, 15, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.INT);
            map(Type.BLOCK_CHANGE_RECORD_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    for (BlockChangeRecord record : (BlockChangeRecord[])wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                      int id = record.getBlockId();
                      record.setBlockId(Protocol1_14To1_13_2.getNewBlockStateId(id));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 30, 28, new PacketRemapper() {
          public void registerMap() {
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    for (int i = 0; i < 3; i++) {
                      float coord = ((Float)wrapper.get(Type.FLOAT, i)).floatValue();
                      if (coord < 0.0F) {
                        coord = (int)coord;
                        wrapper.set(Type.FLOAT, i, Float.valueOf(coord));
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 34, 33, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk chunk = (Chunk)wrapper.read((Type)new Chunk1_13Type(clientWorld));
                    wrapper.write((Type)new Chunk1_14Type(clientWorld), chunk);
                    int[] motionBlocking = new int[256];
                    int[] worldSurface = new int[256];
                    for (int s = 0; s < 16; s++) {
                      ChunkSection section = chunk.getSections()[s];
                      if (section != null) {
                        boolean hasBlock = false;
                        for (int j = 0; j < section.getPaletteSize(); j++) {
                          int old = section.getPaletteEntry(j);
                          int newId = Protocol1_14To1_13_2.getNewBlockStateId(old);
                          if (!hasBlock && newId != WorldPackets.AIR && newId != WorldPackets.VOID_AIR && newId != WorldPackets.CAVE_AIR)
                            hasBlock = true; 
                          section.setPaletteEntry(j, newId);
                        } 
                        if (!hasBlock) {
                          section.setNonAirBlocksCount(0);
                        } else {
                          int nonAirBlockCount = 0;
                          for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                              for (int z = 0; z < 16; z++) {
                                int id = section.getFlatBlock(x, y, z);
                                if (id != WorldPackets.AIR && id != WorldPackets.VOID_AIR && id != WorldPackets.CAVE_AIR) {
                                  nonAirBlockCount++;
                                  worldSurface[x + z * 16] = y + s * 16 + 1;
                                } 
                                if (MappingData.motionBlocking.contains(Integer.valueOf(id)))
                                  motionBlocking[x + z * 16] = y + s * 16 + 1; 
                                if (Via.getConfig().isNonFullBlockLightFix() && MappingData.nonFullBlocks.contains(Integer.valueOf(id)))
                                  WorldPackets.setNonFullLight(chunk, section, s, x, y, z); 
                              } 
                            } 
                          } 
                          section.setNonAirBlocksCount(nonAirBlockCount);
                        } 
                      } 
                    } 
                    CompoundTag heightMap = new CompoundTag("");
                    heightMap.put((Tag)new LongArrayTag("MOTION_BLOCKING", WorldPackets.encodeHeightMap(motionBlocking)));
                    heightMap.put((Tag)new LongArrayTag("WORLD_SURFACE", WorldPackets.encodeHeightMap(worldSurface)));
                    chunk.setHeightMap(heightMap);
                    PacketWrapper lightPacket = wrapper.create(36);
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(chunk.getX()));
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(chunk.getZ()));
                    int skyLightMask = chunk.isGroundUp() ? 262143 : 0;
                    int blockLightMask = 0;
                    for (int i = 0; i < (chunk.getSections()).length; i++) {
                      ChunkSection sec = chunk.getSections()[i];
                      if (sec != null) {
                        if (!chunk.isGroundUp() && sec.hasSkyLight())
                          skyLightMask |= 1 << i + 1; 
                        blockLightMask |= 1 << i + 1;
                      } 
                    } 
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(skyLightMask));
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(blockLightMask));
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(0));
                    lightPacket.write(Type.VAR_INT, Integer.valueOf(0));
                    if (chunk.isGroundUp())
                      lightPacket.write(Type.BYTE_ARRAY, WorldPackets.FULL_LIGHT); 
                    for (ChunkSection section : chunk.getSections()) {
                      if (section == null || !section.hasSkyLight()) {
                        if (chunk.isGroundUp())
                          lightPacket.write(Type.BYTE_ARRAY, WorldPackets.FULL_LIGHT); 
                      } else {
                        lightPacket.write(Type.BYTE_ARRAY, fromPrimitiveArray(section.getSkyLight()));
                      } 
                    } 
                    if (chunk.isGroundUp())
                      lightPacket.write(Type.BYTE_ARRAY, WorldPackets.FULL_LIGHT); 
                    for (ChunkSection section : chunk.getSections()) {
                      if (section != null)
                        lightPacket.write(Type.BYTE_ARRAY, fromPrimitiveArray(section.getBlockLight())); 
                    } 
                    EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    int diffX = Math.abs(entityTracker.getChunkCenterX() - chunk.getX());
                    int diffZ = Math.abs(entityTracker.getChunkCenterZ() - chunk.getZ());
                    if (entityTracker.isForceSendCenterChunk() || diffX >= 64 || diffZ >= 64) {
                      PacketWrapper fakePosLook = wrapper.create(64);
                      fakePosLook.write(Type.VAR_INT, Integer.valueOf(chunk.getX()));
                      fakePosLook.write(Type.VAR_INT, Integer.valueOf(chunk.getZ()));
                      fakePosLook.send(Protocol1_14To1_13_2.class, true, true);
                      entityTracker.setChunkCenterX(chunk.getX());
                      entityTracker.setChunkCenterZ(chunk.getZ());
                    } 
                    lightPacket.send(Protocol1_14To1_13_2.class, true, true);
                  }
                  
                  private Byte[] fromPrimitiveArray(byte[] bytes) {
                    Byte[] newArray = new Byte[bytes.length];
                    for (int i = 0; i < bytes.length; i++)
                      newArray[i] = Byte.valueOf(bytes[i]); 
                    return newArray;
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 35, 34, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.POSITION, Type.POSITION1_14);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    int data = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    if (id == 1010) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = InventoryPackets.getNewItemId(data)));
                    } else if (id == 2001) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = Protocol1_14To1_13_2.getNewBlockStateId(data)));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 36, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.BOOLEAN);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    if (id == 3 || id == 20) {
                      int data = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_14To1_13_2.getNewBlockStateId(data)));
                    } else if (id == 27) {
                      InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                    } 
                    int newId = MetadataRewriter.getNewParticleId(id);
                    if (newId != id)
                      wrapper.set(Type.INT, 0, Integer.valueOf(newId)); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 37, 37, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientChunks = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    clientChunks.setEnvironment(dimensionId);
                    int entityId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    Entity1_14Types.EntityType entType = Entity1_14Types.EntityType.PLAYER;
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.addEntity(entityId, entType);
                    tracker.setClientEntityId(entityId);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    short difficulty = ((Short)wrapper.read(Type.UNSIGNED_BYTE)).shortValue();
                    PacketWrapper difficultyPacket = wrapper.create(13);
                    difficultyPacket.write(Type.UNSIGNED_BYTE, Short.valueOf(difficulty));
                    difficultyPacket.write(Type.BOOLEAN, Boolean.valueOf(false));
                    difficultyPacket.send(protocol.getClass());
                    wrapper.passthrough(Type.UNSIGNED_BYTE);
                    wrapper.passthrough(Type.STRING);
                    wrapper.write(Type.VAR_INT, Integer.valueOf(64));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 38, 38, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.BYTE);
            map(Type.BOOLEAN);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 56, 58, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    clientWorld.setEnvironment(dimensionId);
                    EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    entityTracker.setForceSendCenterChunk(true);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    short difficulty = ((Short)wrapper.read(Type.UNSIGNED_BYTE)).shortValue();
                    PacketWrapper difficultyPacket = wrapper.create(13);
                    difficultyPacket.write(Type.UNSIGNED_BYTE, Short.valueOf(difficulty));
                    difficultyPacket.write(Type.BOOLEAN, Boolean.valueOf(false));
                    difficultyPacket.send(protocol.getClass());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 73, 77, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION, Type.POSITION1_14);
          }
        });
  }
  
  private static long[] encodeHeightMap(int[] heightMap) {
    int bitsPerBlock = 9;
    long maxEntryValue = 511L;
    int length = (int)Math.ceil((heightMap.length * 9) / 64.0D);
    long[] data = new long[length];
    for (int index = 0; index < heightMap.length; index++) {
      int value = heightMap[index];
      int bitIndex = index * 9;
      int startIndex = bitIndex / 64;
      int endIndex = ((index + 1) * 9 - 1) / 64;
      int startBitSubIndex = bitIndex % 64;
      data[startIndex] = data[startIndex] & (maxEntryValue << startBitSubIndex ^ 0xFFFFFFFFFFFFFFFFL) | (value & maxEntryValue) << startBitSubIndex;
      if (startIndex != endIndex) {
        int endBitSubIndex = 64 - startBitSubIndex;
        data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | (value & maxEntryValue) >> endBitSubIndex;
      } 
    } 
    return data;
  }
  
  private static void setNonFullLight(Chunk chunk, ChunkSection section, int ySection, int x, int y, int z) {
    int skyLight = 0;
    int blockLight = 0;
    for (BlockFace blockFace : BlockFace.values()) {
      NibbleArray skyLightArray = section.getSkyLightNibbleArray();
      NibbleArray blockLightArray = section.getBlockLightNibbleArray();
      int neighbourX = x + blockFace.getModX();
      int neighbourY = y + blockFace.getModY();
      int neighbourZ = z + blockFace.getModZ();
      if (blockFace.getModX() != 0) {
        if (neighbourX == 16 || neighbourX == -1)
          continue; 
      } else if (blockFace.getModY() != 0) {
        if (neighbourY == 16 || neighbourY == -1) {
          if (neighbourY == 16) {
            ySection++;
            neighbourY = 0;
          } else {
            ySection--;
            neighbourY = 15;
          } 
          if (ySection == 16 || ySection == -1)
            continue; 
          ChunkSection newSection = chunk.getSections()[ySection];
          if (newSection == null)
            continue; 
          skyLightArray = newSection.getSkyLightNibbleArray();
          blockLightArray = newSection.getBlockLightNibbleArray();
        } 
      } else if (blockFace.getModZ() != 0) {
        if (neighbourZ == 16 || neighbourZ == -1)
          continue; 
      } 
      if (blockLightArray != null && blockLight != 15) {
        int neighbourBlockLight = blockLightArray.get(neighbourX, neighbourY, neighbourZ);
        if (neighbourBlockLight == 15) {
          blockLight = 14;
        } else if (neighbourBlockLight > blockLight) {
          blockLight = neighbourBlockLight - 1;
        } 
      } 
      if (skyLightArray != null && skyLight != 15) {
        int neighbourSkyLight = skyLightArray.get(neighbourX, neighbourY, neighbourZ);
        if (neighbourSkyLight == 15) {
          if (blockFace.getModY() == 1) {
            skyLight = 15;
          } else {
            skyLight = 14;
          } 
        } else if (neighbourSkyLight > skyLight) {
          skyLight = neighbourSkyLight - 1;
        } 
      } 
      continue;
    } 
    if (skyLight != 0) {
      if (!section.hasSkyLight()) {
        byte[] newSkyLight = new byte[2028];
        section.setSkyLight(newSkyLight);
      } 
      section.getSkyLightNibbleArray().set(x, y, z, skyLight);
    } 
    if (blockLight != 0)
      section.getBlockLightNibbleArray().set(x, y, z, blockLight); 
  }
  
  private static long getChunkIndex(int x, int z) {
    return (x & 0x3FFFFFFL) << 38L | z & 0x3FFFFFFL;
  }
}
