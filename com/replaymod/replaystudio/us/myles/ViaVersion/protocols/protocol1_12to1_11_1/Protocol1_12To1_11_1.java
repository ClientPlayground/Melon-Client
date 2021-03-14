package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_12Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_12;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import java.util.List;

public class Protocol1_12To1_11_1 extends Protocol {
  protected void registerPackets() {
    InventoryPackets.register(this);
    registerOutgoing(State.PLAY, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    byte type = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    Entity1_12Types.EntityType entType = Entity1_12Types.getTypeFromId(type, true);
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 3, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.VAR_INT);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Types1_12.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Entity1_12Types.EntityType entType = Entity1_12Types.getTypeFromId(type, false);
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List<Metadata>)wrapper.get(Types1_12.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 7, 7, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int count = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    int removed = 0;
                    for (int i = 0; i < count; i++) {
                      String name = (String)wrapper.read(Type.STRING);
                      int value = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                      if (name.startsWith("achievement.")) {
                        removed++;
                      } else {
                        wrapper.write(Type.STRING, name);
                        wrapper.write(Type.VAR_INT, Integer.valueOf(value));
                      } 
                    } 
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(count - removed));
                    if (count == removed)
                      wrapper.cancel(); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 15, 15, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (!Via.getConfig().is1_12NBTArrayFix())
                      return; 
                    try {
                      JsonElement obj = (new JsonParser()).parse((String)wrapper.get(Type.STRING, 0));
                      if (!TranslateRewriter.toClient(obj, wrapper.user())) {
                        wrapper.cancel();
                        return;
                      } 
                      ChatItemRewriter.toClient(obj, wrapper.user());
                      wrapper.set(Type.STRING, 0, obj.toString());
                    } catch (Exception e) {
                      e.printStackTrace();
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 32, 32, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk1_9_3_4Type type = new Chunk1_9_3_4Type(clientWorld);
                    Chunk chunk = (Chunk)wrapper.passthrough((Type)type);
                    for (int i = 0; i < (chunk.getSections()).length; i++) {
                      ChunkSection section = chunk.getSections()[i];
                      if (section != null)
                        for (int y = 0; y < 16; y++) {
                          for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                              int block = section.getBlockId(x, y, z);
                              if (block == 26) {
                                CompoundTag tag = new CompoundTag("");
                                tag.put((Tag)new IntTag("color", 14));
                                tag.put((Tag)new IntTag("x", x + (chunk.getX() << 4)));
                                tag.put((Tag)new IntTag("y", y + (i << 4)));
                                tag.put((Tag)new IntTag("z", z + (chunk.getZ() << 4)));
                                tag.put((Tag)new StringTag("id", "minecraft:bed"));
                                chunk.getBlockEntities().add(tag);
                              } 
                            } 
                          } 
                        }  
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 35, 35, new PacketRemapper() {
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
    registerOutgoing(State.PLAY, 40, 37);
    registerOutgoing(State.PLAY, 37, 38);
    registerOutgoing(State.PLAY, 38, 39);
    registerOutgoing(State.PLAY, 39, 40);
    registerOutgoing(State.PLAY, 48, 49, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Integer[] arrayOfInteger;
                    int i;
                    byte b;
                    for (arrayOfInteger = (Integer[])wrapper.get(Type.VAR_INT_ARRAY, 0), i = arrayOfInteger.length, b = 0; b < i; ) {
                      int entity = arrayOfInteger[b].intValue();
                      ((EntityTracker)wrapper.user().get(EntityTracker.class)).removeEntity(entity);
                      b++;
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 49, 50);
    registerOutgoing(State.PLAY, 50, 51);
    registerOutgoing(State.PLAY, 51, 52, new PacketRemapper() {
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
    registerOutgoing(State.PLAY, 52, 53);
    registerOutgoing(State.PLAY, 53, 55);
    registerOutgoing(State.PLAY, 54, 56);
    registerOutgoing(State.PLAY, 55, 57);
    registerOutgoing(State.PLAY, 56, 58);
    registerOutgoing(State.PLAY, 57, 59, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_12.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Optional<Entity1_12Types.EntityType> type = ((EntityTracker)wrapper.user().get(EntityTracker.class)).get(entityId);
                    if (!type.isPresent())
                      return; 
                    MetadataRewriter.handleMetadata(entityId, (Entity1_12Types.EntityType)type.get(), (List<Metadata>)wrapper.get(Types1_12.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 58, 60);
    registerOutgoing(State.PLAY, 59, 61);
    registerOutgoing(State.PLAY, 61, 63);
    registerOutgoing(State.PLAY, 62, 64);
    registerOutgoing(State.PLAY, 63, 65);
    registerOutgoing(State.PLAY, 64, 66);
    registerOutgoing(State.PLAY, 65, 67);
    registerOutgoing(State.PLAY, 66, 68);
    registerOutgoing(State.PLAY, 67, 69);
    registerOutgoing(State.PLAY, 68, 70);
    registerOutgoing(State.PLAY, 69, 71);
    registerOutgoing(State.PLAY, 70, 72, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.FLOAT);
            map(Type.FLOAT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    id = Protocol1_12To1_11_1.this.getNewSoundId(id);
                    if (id == -1)
                      wrapper.cancel(); 
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(id));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 71, 73);
    registerOutgoing(State.PLAY, 72, 74);
    registerOutgoing(State.PLAY, 73, 75);
    registerOutgoing(State.PLAY, 74, 77);
    registerOutgoing(State.PLAY, 75, 78);
    registerIncoming(State.PLAY, 1, 1, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 1, 2);
    registerIncoming(State.PLAY, 2, 3);
    registerIncoming(State.PLAY, 3, 4);
    registerIncoming(State.PLAY, 4, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BYTE);
            map(Type.VAR_INT);
            map(Type.BOOLEAN);
            map(Type.UNSIGNED_BYTE);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String locale = (String)wrapper.get(Type.STRING, 0);
                    if (locale.length() > 7)
                      wrapper.set(Type.STRING, 0, locale.substring(0, 7)); 
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 5, 6);
    registerIncoming(State.PLAY, 6, 7);
    registerIncoming(State.PLAY, 8, 9);
    registerIncoming(State.PLAY, 9, 10);
    registerIncoming(State.PLAY, 10, 11);
    registerIncoming(State.PLAY, 11, 12);
    registerIncoming(State.PLAY, 15, 13);
    registerIncoming(State.PLAY, 12, 14);
    registerIncoming(State.PLAY, 13, 15);
    registerIncoming(State.PLAY, 14, 16);
    registerIncoming(State.PLAY, 16, 17);
    registerIncoming(State.PLAY, 17, 18);
    registerIncoming(State.PLAY, 18, 19);
    registerIncoming(State.PLAY, 19, 20);
    registerIncoming(State.PLAY, 20, 21);
    registerIncoming(State.PLAY, 21, 22);
    registerIncoming(State.PLAY, 23, 23, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 22, 24);
    registerIncoming(State.PLAY, 25, 25, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 23, 26);
    registerIncoming(State.PLAY, 25, 28);
    registerIncoming(State.PLAY, 26, 29);
    registerIncoming(State.PLAY, 27, 30);
    registerIncoming(State.PLAY, 28, 31);
    registerIncoming(State.PLAY, 29, 32);
  }
  
  private int getNewSoundId(int id) {
    int newId = id;
    if (id >= 26)
      newId += 2; 
    if (id >= 70)
      newId += 4; 
    if (id >= 74)
      newId++; 
    if (id >= 143)
      newId += 3; 
    if (id >= 185)
      newId++; 
    if (id >= 263)
      newId += 7; 
    if (id >= 301)
      newId += 33; 
    if (id >= 317)
      newId += 2; 
    if (id >= 491)
      newId += 3; 
    return newId;
  }
  
  protected void register(ViaProviders providers) {
    providers.register(InventoryQuickMoveProvider.class, (Provider)new InventoryQuickMoveProvider());
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
  }
}
