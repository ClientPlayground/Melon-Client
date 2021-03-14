package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_10to1_9_3.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;

public class ResourcePackTracker extends StoredObject {
  public void setLastHash(String lastHash) {
    this.lastHash = lastHash;
  }
  
  public String toString() {
    return "ResourcePackTracker(lastHash=" + getLastHash() + ")";
  }
  
  private String lastHash = "";
  
  public String getLastHash() {
    return this.lastHash;
  }
  
  public ResourcePackTracker(UserConnection user) {
    super(user);
  }
}
