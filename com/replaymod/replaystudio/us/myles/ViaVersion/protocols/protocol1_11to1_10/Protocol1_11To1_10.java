package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_11Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import java.util.List;

public class Protocol1_11To1_10 extends Protocol {
  private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<Float, Short>(Type.UNSIGNED_BYTE) {
      public Short transform(PacketWrapper wrapper, Float inputValue) throws Exception {
        return Short.valueOf((short)(int)(inputValue.floatValue() * 16.0F));
      }
    };
  
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
                    Entity1_11Types.EntityType entType = Entity1_11Types.getTypeFromId(type, true);
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 3, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.UNSIGNED_BYTE, Type.VAR_INT);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Types1_9.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Entity1_11Types.EntityType entType = MetadataRewriter.rewriteEntityType(type, (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0));
                    if (entType != null)
                      wrapper.set(Type.VAR_INT, 1, Integer.valueOf(entType.getId())); 
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 70, 70, new PacketRemapper() {
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
                    id = Protocol1_11To1_10.this.getNewSoundId(id);
                    if (id == -1)
                      wrapper.cancel(); 
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(id));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 72, 72, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.VAR_INT, Integer.valueOf(1));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 57, 57, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_9.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Optional<Entity1_11Types.EntityType> type = ((EntityTracker)wrapper.user().get(EntityTracker.class)).get(entityId);
                    if (!type.isPresent())
                      return; 
                    MetadataRewriter.handleMetadata(entityId, (Entity1_11Types.EntityType)type.get(), (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 73, 73, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BOOLEAN);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (Via.getConfig().isHologramPatch()) {
                      EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                      if (tracker.isHologram(entityID)) {
                        Double newValue = (Double)wrapper.get(Type.DOUBLE, 1);
                        newValue = Double.valueOf(newValue.doubleValue() - Via.getConfig().getHologramYOffset());
                        wrapper.set(Type.DOUBLE, 1, newValue);
                      } 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 48, 48, new PacketRemapper() {
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
    registerOutgoing(State.PLAY, 69, 69, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (action >= 2)
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(action + 1)); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 10, 10, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper actionWrapper) throws Exception {
                    if (Via.getConfig().isPistonAnimationPatch()) {
                      int id = ((Integer)actionWrapper.get(Type.VAR_INT, 0)).intValue();
                      if (id == 33 || id == 29)
                        actionWrapper.cancel(); 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 9, 9, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.UNSIGNED_BYTE);
            map(Type.NBT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    CompoundTag tag = (CompoundTag)wrapper.get(Type.NBT, 0);
                    if (((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue() == 1)
                      EntityIdRewriter.toClientSpawner(tag); 
                    if (tag.contains("id"))
                      ((StringTag)tag.get("id")).setValue(BlockEntityRewriter.toNewIdentifier((String)tag.get("id").getValue())); 
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
                    wrapper.clearInputBuffer();
                    if (chunk.getBlockEntities() == null)
                      return; 
                    for (CompoundTag tag : chunk.getBlockEntities()) {
                      if (tag.contains("id")) {
                        String identifier = ((StringTag)tag.get("id")).getValue();
                        if (identifier.equals("MobSpawner"))
                          EntityIdRewriter.toClientSpawner(tag); 
                        ((StringTag)tag.get("id")).setValue(BlockEntityRewriter.toNewIdentifier(identifier));
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
    registerOutgoing(State.PLAY, 51, 51, new PacketRemapper() {
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
    registerIncoming(State.PLAY, 28, 28, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.FLOAT, Protocol1_11To1_10.toOldByte);
            map(Type.FLOAT, Protocol1_11To1_10.toOldByte);
            map(Type.FLOAT, Protocol1_11To1_10.toOldByte);
          }
        });
    registerIncoming(State.PLAY, 2, 2, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String msg = (String)wrapper.get(Type.STRING, 0);
                    if (msg.length() > 100)
                      wrapper.set(Type.STRING, 0, msg.substring(0, 100)); 
                  }
                });
          }
        });
  }
  
  private int getNewSoundId(int id) {
    if (id == 196)
      return -1; 
    if (id >= 85)
      id += 2; 
    if (id >= 176)
      id++; 
    if (id >= 197)
      id += 8; 
    if (id >= 196)
      id--; 
    if (id >= 279)
      id += 9; 
    if (id >= 296)
      id++; 
    if (id >= 390)
      id += 4; 
    if (id >= 400)
      id += 3; 
    if (id >= 450)
      id++; 
    if (id >= 455)
      id++; 
    if (id >= 470)
      id++; 
    return id;
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
  }
}
