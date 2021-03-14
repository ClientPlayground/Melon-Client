package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets;

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
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 34, 34, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk chunk = (Chunk)wrapper.passthrough((Type)new Chunk1_13Type(clientWorld));
                    for (ChunkSection section : chunk.getSections()) {
                      if (section != null)
                        for (int i = 0; i < section.getPaletteSize(); i++)
                          section.setPaletteEntry(i, Protocol1_13_1To1_13.getNewBlockStateId(section.getPaletteEntry(i)));  
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 10, 10, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_13_1To1_13.getNewBlockId(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 11, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_13_1To1_13.getNewBlockStateId(id)));
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
                      record.setBlockId(Protocol1_13_1To1_13.getNewBlockStateId(id));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 35, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.POSITION);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    int data = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    if (id == 1010) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = InventoryPackets.getNewItemId(data)));
                    } else if (id == 2001) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = Protocol1_13_1To1_13.getNewBlockStateId(data)));
                    } 
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
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 56, 56, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    clientWorld.setEnvironment(dimensionId);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 36, 36, new PacketRemapper() {
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
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_13_1To1_13.getNewBlockStateId(data)));
                    } else if (id == 27) {
                      InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                    } 
                  }
                });
          }
        });
  }
}
