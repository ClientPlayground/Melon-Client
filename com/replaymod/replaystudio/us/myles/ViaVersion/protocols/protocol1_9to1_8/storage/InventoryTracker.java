package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;

public class InventoryTracker extends StoredObject {
  private String inventory;
  
  public void setInventory(String inventory) {
    this.inventory = inventory;
  }
  
  public String getInventory() {
    return this.inventory;
  }
  
  public InventoryTracker(UserConnection user) {
    super(user);
  }
}
