package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12_2to1_12_1;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class Protocol1_12_2To1_12_1 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 31, 31, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT, Type.LONG);
          }
        });
    registerIncoming(State.PLAY, 11, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.LONG, Type.VAR_INT);
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
}
