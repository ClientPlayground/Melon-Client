package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_13Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_12;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import java.util.List;

public class EntityPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.BYTE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    byte type = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    Entity1_13Types.EntityType entType = Entity1_13Types.getTypeFromId(type, true);
                    if (entType != null) {
                      if (entType.is(Entity1_13Types.EntityType.FALLING_BLOCK)) {
                        int oldId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                        int combined = (oldId & 0xFFF) << 4 | oldId >> 12 & 0xF;
                        wrapper.set(Type.INT, 0, Integer.valueOf(WorldPackets.toNewId(combined)));
                      } 
                      if (entType.is(Entity1_13Types.EntityType.ITEM_FRAME)) {
                        int data = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                        switch (data) {
                          case 0:
                            data = 3;
                            break;
                          case 1:
                            data = 4;
                            break;
                          case 3:
                            data = 5;
                            break;
                        } 
                        wrapper.set(Type.INT, 0, Integer.valueOf(data));
                      } 
                    } 
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
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
            map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    Optional<Integer> optNewType = EntityTypeRewriter.getNewId(type);
                    type = ((Integer)optNewType.or(Integer.valueOf(type))).intValue();
                    Entity1_13Types.EntityType entType = Entity1_13Types.getTypeFromId(type, false);
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(type));
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
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
            map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Entity1_13Types.EntityType entType = Entity1_13Types.EntityType.PLAYER;
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, entType);
                    MetadataRewriter.handleMetadata(entityId, entType, (List)wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 50, 53, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 60, 63, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    Optional<Entity1_13Types.EntityType> type = ((EntityTracker)wrapper.user().get(EntityTracker.class)).get(entityId);
                    MetadataRewriter.handleMetadata(entityId, (Entity1_13Types.EntityType)type.orNull(), (List)wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
                  }
                });
          }
        });
  }
}
