package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;

public class PlayerMovementMapper extends PacketHandler {
  public void handle(PacketWrapper wrapper) throws Exception {
    MovementTracker tracker = (MovementTracker)wrapper.user().get(MovementTracker.class);
    tracker.incrementIdlePacket();
    if (wrapper.is(Type.BOOLEAN, 0))
      tracker.setGround(((Boolean)wrapper.get(Type.BOOLEAN, 0)).booleanValue()); 
  }
}
