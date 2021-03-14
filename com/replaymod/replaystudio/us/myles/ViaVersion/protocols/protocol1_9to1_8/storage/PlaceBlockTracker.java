package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;

public class PlaceBlockTracker extends StoredObject {
  private long lastPlaceTimestamp = 0L;
  
  public long getLastPlaceTimestamp() {
    return this.lastPlaceTimestamp;
  }
  
  private Position lastPlacedPosition = null;
  
  public void setLastPlacedPosition(Position lastPlacedPosition) {
    this.lastPlacedPosition = lastPlacedPosition;
  }
  
  public Position getLastPlacedPosition() {
    return this.lastPlacedPosition;
  }
  
  public PlaceBlockTracker(UserConnection user) {
    super(user);
  }
  
  public boolean isExpired(int ms) {
    return (System.currentTimeMillis() > this.lastPlaceTimestamp + ms);
  }
  
  public void updateTime() {
    this.lastPlaceTimestamp = System.currentTimeMillis();
  }
}
