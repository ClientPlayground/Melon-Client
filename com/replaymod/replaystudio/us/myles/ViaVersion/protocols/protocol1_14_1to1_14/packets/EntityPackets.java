package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.packets;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_14Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.storage.EntityTracker;
import java.util.List;

public class EntityPackets {
  public static void register(Protocol protocol) {
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
            map(Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Entity1_14Types.EntityType entType = Entity1_14Types.getTypeFromId(type);
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
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
            map(Types1_14.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Entity1_14Types.EntityType entType = Entity1_14Types.EntityType.PLAYER;
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 55, 55, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 67, 67, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_14.METADATA_LIST);
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
