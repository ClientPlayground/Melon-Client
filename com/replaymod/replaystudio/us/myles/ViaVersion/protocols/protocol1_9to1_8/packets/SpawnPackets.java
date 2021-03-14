package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_10Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import java.util.ArrayList;
import java.util.List;

public class SpawnPackets {
  public static final ValueTransformer<Integer, Double> toNewDouble = new ValueTransformer<Integer, Double>(Type.DOUBLE) {
      public Double transform(PacketWrapper wrapper, Integer inputValue) {
        return Double.valueOf(inputValue.intValue() / 32.0D);
      }
    };
  
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 14, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                  }
                });
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int typeID = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.getTypeFromId(typeID, true));
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.INT);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    int data = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    short vX = 0, vY = 0, vZ = 0;
                    if (data > 0) {
                      vX = ((Short)wrapper.read(Type.SHORT)).shortValue();
                      vY = ((Short)wrapper.read(Type.SHORT)).shortValue();
                      vZ = ((Short)wrapper.read(Type.SHORT)).shortValue();
                    } 
                    wrapper.write(Type.SHORT, Short.valueOf(vX));
                    wrapper.write(Type.SHORT, Short.valueOf(vY));
                    wrapper.write(Type.SHORT, Short.valueOf(vZ));
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    final int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    final int data = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    int typeID = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    if (Entity1_10Types.getTypeFromId(typeID, true) == Entity1_10Types.EntityType.SPLASH_POTION) {
                      PacketWrapper metaPacket = wrapper.create(57, new ValueCreator() {
                            public void write(PacketWrapper wrapper) throws Exception {
                              wrapper.write(Type.VAR_INT, Integer.valueOf(entityID));
                              List<Metadata> meta = new ArrayList<>();
                              Item item = new Item((short)373, (byte)1, (short)data, null);
                              ItemRewriter.toClient(item);
                              Metadata potion = new Metadata(5, (MetaType)MetaType1_9.Slot, item);
                              meta.add(potion);
                              wrapper.write(Types1_9.METADATA_LIST, meta);
                            }
                          });
                      metaPacket.send(Protocol1_9To1_8.class);
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 17, 1, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.EntityType.EXPERIENCE_ORB);
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.SHORT);
          }
        });
    protocol.registerOutgoing(State.PLAY, 44, 2, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.EntityType.LIGHTNING);
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
          }
        });
    protocol.registerOutgoing(State.PLAY, 15, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                  }
                });
            map(Type.UNSIGNED_BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int typeID = ((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.getTypeFromId(typeID, false));
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    List<Metadata> metadataList = (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    Entity1_10Types.EntityType type = (Entity1_10Types.EntityType)tracker.getClientEntityTypes().get(Integer.valueOf(entityID));
                    if (type != null) {
                      MetadataRewriter.transform(type, metadataList);
                    } else {
                      Via.getPlatform().getLogger().warning("Unable to find entity for metadata, entity ID: " + entityID);
                      metadataList.clear();
                    } 
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    List<Metadata> metadataList = (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.handleMetadata(entityID, metadataList);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 16, 4, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.EntityType.PAINTING);
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                  }
                });
            map(Type.STRING);
            map(Type.POSITION);
            map(Type.BYTE);
          }
        });
    protocol.registerOutgoing(State.PLAY, 12, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.EntityType.PLAYER);
                    tracker.sendMetadataBuffer(entityID);
                  }
                });
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.INT, SpawnPackets.toNewDouble);
            map(Type.BYTE);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    short item = ((Short)wrapper.read(Type.SHORT)).shortValue();
                    if (item != 0) {
                      PacketWrapper packet = new PacketWrapper(60, null, wrapper.user());
                      packet.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
                      packet.write(Type.VAR_INT, Integer.valueOf(0));
                      packet.write(Type.ITEM, new Item(item, (byte)1, (short)0, null));
                      try {
                        packet.send(Protocol1_9To1_8.class, true, true);
                      } catch (Exception e) {
                        e.printStackTrace();
                      } 
                    } 
                  }
                });
            map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    List<Metadata> metadataList = (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    Entity1_10Types.EntityType type = (Entity1_10Types.EntityType)tracker.getClientEntityTypes().get(Integer.valueOf(entityID));
                    if (type != null) {
                      MetadataRewriter.transform(type, metadataList);
                    } else {
                      Via.getPlatform().getLogger().warning("Unable to find entity for metadata, entity ID: " + entityID);
                      metadataList.clear();
                    } 
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    List<Metadata> metadataList = (List<Metadata>)wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.handleMetadata(entityID, metadataList);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 19, 48, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Integer[] entities = (Integer[])wrapper.get(Type.VAR_INT_ARRAY, 0);
                    for (Integer entity : entities)
                      ((EntityTracker)wrapper.user().get(EntityTracker.class)).removeEntity(entity); 
                  }
                });
          }
        });
  }
}
