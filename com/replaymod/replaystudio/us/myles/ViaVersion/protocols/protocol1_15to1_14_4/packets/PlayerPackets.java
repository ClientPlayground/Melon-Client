package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_15Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class PlayerPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 58, 59, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    clientWorld.setEnvironment(dimensionId);
                  }
                });
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.LONG, Long.valueOf(0L));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 37, 38, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Entity1_15Types.EntityType entType = Entity1_15Types.EntityType.PLAYER;
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.addEntity(((Integer)wrapper.get(Type.INT, 0)).intValue(), entType);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientChunks = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    clientChunks.setEnvironment(dimensionId);
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    int entityId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    tracker.addEntity(entityId, Entity1_15Types.EntityType.PLAYER);
                  }
                });
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.LONG, Long.valueOf(0L));
                  }
                });
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            map(Type.VAR_INT);
            map(Type.BOOLEAN);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(!Via.getConfig().is1_15InstantRespawn()));
                  }
                });
          }
        });
  }
}
