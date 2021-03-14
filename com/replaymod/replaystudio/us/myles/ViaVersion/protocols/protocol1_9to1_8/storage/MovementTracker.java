package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;

public class MovementTracker extends StoredObject {
  private static final long IDLE_PACKET_DELAY = 50L;
  
  private static final long IDLE_PACKET_LIMIT = 20L;
  
  private long nextIdlePacket = 0L;
  
  public long getNextIdlePacket() {
    return this.nextIdlePacket;
  }
  
  private boolean ground = true;
  
  public boolean isGround() {
    return this.ground;
  }
  
  public void setGround(boolean ground) {
    this.ground = ground;
  }
  
  public MovementTracker(UserConnection user) {
    super(user);
  }
  
  public void incrementIdlePacket() {
    this.nextIdlePacket = Math.max(this.nextIdlePacket + 50L, System.currentTimeMillis() - 1000L);
  }
}
