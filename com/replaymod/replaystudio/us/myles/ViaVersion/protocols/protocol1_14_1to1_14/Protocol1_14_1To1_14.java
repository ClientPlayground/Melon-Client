package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.storage.EntityTracker;

public class Protocol1_14_1To1_14 extends Protocol {
  protected void registerPackets() {
    EntityPackets.register(this);
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
  }
}
