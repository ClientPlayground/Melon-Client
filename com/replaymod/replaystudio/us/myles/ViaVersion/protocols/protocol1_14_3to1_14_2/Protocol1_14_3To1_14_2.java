package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_3to1_14_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class Protocol1_14_3To1_14_2 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 39, 39, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                    for (int i = 0; i < size; i++) {
                      wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                      wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                      if (((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue())
                        wrapper.passthrough(Type.FLAT_VAR_INT_ITEM); 
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.FLOAT);
                    } 
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.VAR_INT);
                    boolean regularVillager = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(regularVillager));
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
}
