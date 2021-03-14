package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class WorldPackets {
  public static void register(Protocol protocol) {
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
                    if (id == 27)
                      wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM)); 
                  }
                });
          }
        });
  }
}
