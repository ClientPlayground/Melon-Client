package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12_1to1_12;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class Protocol1_12_1To1_12 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, -1, 43);
    registerOutgoing(State.PLAY, 43, 44);
    registerOutgoing(State.PLAY, 44, 45);
    registerOutgoing(State.PLAY, 45, 46);
    registerOutgoing(State.PLAY, 46, 47);
    registerOutgoing(State.PLAY, 47, 48);
    registerOutgoing(State.PLAY, 48, 49);
    registerOutgoing(State.PLAY, 49, 50);
    registerOutgoing(State.PLAY, 50, 51);
    registerOutgoing(State.PLAY, 51, 52);
    registerOutgoing(State.PLAY, 52, 53);
    registerOutgoing(State.PLAY, 53, 54);
    registerOutgoing(State.PLAY, 54, 55);
    registerOutgoing(State.PLAY, 55, 56);
    registerOutgoing(State.PLAY, 56, 57);
    registerOutgoing(State.PLAY, 57, 58);
    registerOutgoing(State.PLAY, 58, 59);
    registerOutgoing(State.PLAY, 59, 60);
    registerOutgoing(State.PLAY, 60, 61);
    registerOutgoing(State.PLAY, 61, 62);
    registerOutgoing(State.PLAY, 62, 63);
    registerOutgoing(State.PLAY, 63, 64);
    registerOutgoing(State.PLAY, 64, 65);
    registerOutgoing(State.PLAY, 65, 66);
    registerOutgoing(State.PLAY, 66, 67);
    registerOutgoing(State.PLAY, 67, 68);
    registerOutgoing(State.PLAY, 68, 69);
    registerOutgoing(State.PLAY, 69, 70);
    registerOutgoing(State.PLAY, 70, 71);
    registerOutgoing(State.PLAY, 71, 72);
    registerOutgoing(State.PLAY, 72, 73);
    registerOutgoing(State.PLAY, 73, 74);
    registerOutgoing(State.PLAY, 74, 75);
    registerOutgoing(State.PLAY, 75, 76);
    registerOutgoing(State.PLAY, 76, 77);
    registerOutgoing(State.PLAY, 77, 78);
    registerOutgoing(State.PLAY, 78, 79);
    registerIncoming(State.PLAY, 1, -1);
    registerIncoming(State.PLAY, 2, 1);
    registerIncoming(State.PLAY, 3, 2);
    registerIncoming(State.PLAY, 4, 3);
    registerIncoming(State.PLAY, 5, 4);
    registerIncoming(State.PLAY, 6, 5);
    registerIncoming(State.PLAY, 7, 6);
    registerIncoming(State.PLAY, 8, 7);
    registerIncoming(State.PLAY, 9, 8);
    registerIncoming(State.PLAY, 10, 9);
    registerIncoming(State.PLAY, 11, 10);
    registerIncoming(State.PLAY, 12, 11);
    registerIncoming(State.PLAY, 13, 12);
    registerIncoming(State.PLAY, 14, 13);
    registerIncoming(State.PLAY, 15, 14);
    registerIncoming(State.PLAY, 16, 15);
    registerIncoming(State.PLAY, 17, 16);
    registerIncoming(State.PLAY, 18, 17);
    registerIncoming(State.PLAY, -1, 18, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.VAR_INT);
            map(Type.BOOLEAN);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
}
