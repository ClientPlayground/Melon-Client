package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types.Chunk1_14Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.types.Chunk1_15Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 92, 8, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int blockState = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_15To1_14_4.getNewBlockStateId(blockState)));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 10, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_15To1_14_4.getNewBlockId(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 11, 12, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_15To1_14_4.getNewBlockStateId(id)));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 15, 16, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.INT);
            map(Type.BLOCK_CHANGE_RECORD_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    for (BlockChangeRecord record : (BlockChangeRecord[])wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                      int id = record.getBlockId();
                      record.setBlockId(Protocol1_15To1_14_4.getNewBlockStateId(id));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 33, 34, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk chunk = (Chunk)wrapper.read((Type)new Chunk1_14Type(clientWorld));
                    wrapper.write((Type)new Chunk1_15Type(clientWorld), chunk);
                    if (chunk.isGroundUp()) {
                      int[] biomeData = chunk.getBiomeData();
                      int[] newBiomeData = new int[1024];
                      if (biomeData != null) {
                        int i;
                        for (i = 0; i < 4; i++) {
                          for (int j = 0; j < 4; j++) {
                            int x = (j << 2) + 2;
                            int z = (i << 2) + 2;
                            int oldIndex = z << 4 | x;
                            newBiomeData[i << 2 | j] = biomeData[oldIndex];
                          } 
                        } 
                        for (i = 1; i < 64; i++)
                          System.arraycopy(newBiomeData, 0, newBiomeData, i * 16, 16); 
                      } 
                      chunk.setBiomeData(newBiomeData);
                    } 
                    for (int s = 0; s < 16; s++) {
                      ChunkSection section = chunk.getSections()[s];
                      if (section != null)
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                          int old = section.getPaletteEntry(i);
                          int newId = Protocol1_15To1_14_4.getNewBlockStateId(old);
                          section.setPaletteEntry(i, newId);
                        }  
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 34, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.POSITION1_14);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    int data = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    if (id == 1010) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(InventoryPackets.getNewItemId(data)));
                    } else if (id == 2001) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = Protocol1_15To1_14_4.getNewBlockStateId(data)));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 35, 36, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.BOOLEAN);
            map(Type.FLOAT, Type.DOUBLE);
            map(Type.FLOAT, Type.DOUBLE);
            map(Type.FLOAT, Type.DOUBLE);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    if (id == 3 || id == 23) {
                      int data = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_15To1_14_4.getNewBlockStateId(data)));
                    } else if (id == 32) {
                      InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                    } 
                  }
                });
          }
        });
  }
}
