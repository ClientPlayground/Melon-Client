package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1to1_9;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class Protocol1_9_1To1_9 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 35, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.BYTE, Type.INT);
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
                    if (sound >= 415)
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(sound + 1)); 
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
}
