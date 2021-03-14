package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_9_1;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class Protocol1_9To1_9_1 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 35, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.INT, Type.BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            map(Type.BOOLEAN);
          }
        });
    registerOutgoing(State.PLAY, 71, 71, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int sound = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (sound == 415) {
                      wrapper.cancel();
                    } else if (sound >= 416) {
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(sound - 1));
                    } 
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
}
