package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_13Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_14Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.EntityTypeRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class EntityPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.BYTE, Type.VAR_INT);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.INT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    UUID uuid = (UUID)wrapper.get(Type.UUID, 0);
                    int typeId = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Entity1_13Types.EntityType type1_13 = Entity1_13Types.getTypeFromId(typeId, true);
                    typeId = ((Integer)EntityTypeRewriter.getNewId(type1_13.getId()).or(Integer.valueOf(type1_13.getId()))).intValue();
                    Entity1_14Types.EntityType type1_14 = Entity1_14Types.getTypeFromId(typeId);
                    if (type1_14 != null) {
                      int data = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                      if (type1_14.is(Entity1_14Types.EntityType.FALLING_BLOCK)) {
                        wrapper.set(Type.INT, 0, Integer.valueOf(Protocol1_14To1_13_2.getNewBlockStateId(data)));
                      } else if (type1_14.is(Entity1_14Types.EntityType.MINECART)) {
                        switch (data) {
                          case 1:
                            typeId = Entity1_14Types.EntityType.CHEST_MINECART.getId();
                            break;
                          case 2:
                            typeId = Entity1_14Types.EntityType.FURNACE_MINECART.getId();
                            break;
                          case 3:
                            typeId = Entity1_14Types.EntityType.TNT_MINECART.getId();
                            break;
                          case 4:
                            typeId = Entity1_14Types.EntityType.SPAWNER_MINECART.getId();
                            break;
                          case 5:
                            typeId = Entity1_14Types.EntityType.HOPPER_MINECART.getId();
                            break;
                          case 6:
                            typeId = Entity1_14Types.EntityType.COMMANDBLOCK_MINECART.getId();
                            break;
                        } 
                      } else if ((type1_14.is(Entity1_14Types.EntityType.ITEM) && data > 0) || type1_14
                        .isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ARROW)) {
                        if (type1_14.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ARROW))
                          wrapper.set(Type.INT, 0, Integer.valueOf(data - 1)); 
                        PacketWrapper velocity = wrapper.create(69);
                        velocity.write(Type.VAR_INT, Integer.valueOf(entityId));
                        velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 0));
                        velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 1));
                        velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 2));
                        velocity.send(Protocol1_14To1_13_2.class);
                      } 
                    } 
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(typeId));
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, type1_14);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 3, 3, new PacketRemapper() {
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
            map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    UUID uuid = (UUID)wrapper.get(Type.UUID, 0);
                    type = ((Integer)EntityTypeRewriter.getNewId(type).or(Integer.valueOf(type))).intValue();
                    Entity1_14Types.EntityType entType = Entity1_14Types.getTypeFromId(type);
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(type));
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 4, 4, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.VAR_INT);
            map(Type.POSITION, Type.POSITION1_14);
            map(Type.BYTE);
          }
        });
    protocol.registerOutgoing(State.PLAY, 5, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    UUID uuid = (UUID)wrapper.get(Type.UUID, 0);
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Entity1_14Types.EntityType entType = Entity1_14Types.EntityType.PLAYER;
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 6, 6, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    short animation = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                    if (animation == 2) {
                      EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                      int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                      tracker.setSleeping(entityId, false);
                      PacketWrapper metadataPacket = wrapper.create(67);
                      metadataPacket.write(Type.VAR_INT, Integer.valueOf(entityId));
                      List<Metadata> metadataList = new LinkedList<>();
                      if (tracker.getClientEntityId() != entityId)
                        metadataList.add(new Metadata(6, (MetaType)MetaType1_14.Pose, Integer.valueOf(MetadataRewriter.recalculatePlayerPose(entityId, tracker)))); 
                      metadataList.add(new Metadata(12, (MetaType)MetaType1_14.OptPosition, null));
                      metadataPacket.write(Types1_14.METADATA_LIST, metadataList);
                      metadataPacket.send(Protocol1_14To1_13_2.class);
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 51, 67, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    tracker.setSleeping(entityId, true);
                    Position position = (Position)wrapper.read(Type.POSITION);
                    List<Metadata> metadataList = new LinkedList<>();
                    metadataList.add(new Metadata(12, (MetaType)MetaType1_14.OptPosition, position));
                    if (tracker.getClientEntityId() != entityId)
                      metadataList.add(new Metadata(6, (MetaType)MetaType1_14.Pose, Integer.valueOf(MetadataRewriter.recalculatePlayerPose(entityId, tracker)))); 
                    wrapper.write(Types1_14.METADATA_LIST, metadataList);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 53, 55, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 63, 67, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Optional<Entity1_14Types.EntityType> type = ((EntityTracker)wrapper.user().get(EntityTracker.class)).get(entityId);
                    MetadataRewriter.handleMetadata(entityId, (Entity1_14Types.EntityType)type.orNull(), (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
  }
}
