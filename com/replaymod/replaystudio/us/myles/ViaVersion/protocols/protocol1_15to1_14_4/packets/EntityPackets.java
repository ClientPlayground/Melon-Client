package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_15Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;
import java.util.List;
import java.util.UUID;

public class EntityPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.VAR_INT);
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
                    Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(EntityPackets.getNewEntityId(typeId));
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entityType);
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(entityType.getId()));
                    if (entityType == Entity1_15Types.EntityType.FALLING_BLOCK)
                      wrapper.set(Type.INT, 0, Integer.valueOf(Protocol1_15To1_14_4.getNewBlockStateId(((Integer)wrapper.get(Type.INT, 0)).intValue()))); 
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
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int typeId = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(EntityPackets.getNewEntityId(typeId));
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entityType);
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(entityType.getId()));
                    List<Metadata> metadata = (List<Metadata>)wrapper.read(Types1_14.METADATA_LIST);
                    MetadataRewriter.handleMetadata(entityId, entityType, metadata, wrapper.user());
                    PacketWrapper metadataUpdate = wrapper.create(68);
                    metadataUpdate.write(Type.VAR_INT, Integer.valueOf(entityId));
                    metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                    metadataUpdate.send(Protocol1_15To1_14_4.class);
                  }
                });
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
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Entity1_15Types.EntityType entityType = Entity1_15Types.EntityType.PLAYER;
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entityType);
                    List<Metadata> metadata = (List<Metadata>)wrapper.read(Types1_14.METADATA_LIST);
                    MetadataRewriter.handleMetadata(entityId, entityType, metadata, wrapper.user());
                    PacketWrapper metadataUpdate = wrapper.create(68);
                    metadataUpdate.write(Type.VAR_INT, Integer.valueOf(entityId));
                    metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                    metadataUpdate.send(Protocol1_15To1_14_4.class);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 67, 68, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Optional<Entity1_15Types.EntityType> type = ((EntityTracker)wrapper.user().get(EntityTracker.class)).get(entityId);
                    MetadataRewriter.handleMetadata(entityId, (Entity1_15Types.EntityType)type.orNull(), (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 55, 56, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    Integer[] arrayOfInteger;
                    int i;
                    byte b;
                    for (arrayOfInteger = (Integer[])wrapper.get(Type.VAR_INT_ARRAY, 0), i = arrayOfInteger.length, b = 0; b < i; ) {
                      int entity = arrayOfInteger[b].intValue();
                      entityTracker.removeEntity(entity);
                      b++;
                    } 
                  }
                });
          }
        });
  }
  
  public static int getNewEntityId(int oldId) {
    return (oldId >= 4) ? (oldId + 1) : oldId;
  }
}
