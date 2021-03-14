package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage;

public class ItemTransaction {
  private short windowId;
  
  private short slotId;
  
  private short actionId;
  
  public ItemTransaction(short windowId, short slotId, short actionId) {
    this.windowId = windowId;
    this.slotId = slotId;
    this.actionId = actionId;
  }
  
  public String toString() {
    return "ItemTransaction(windowId=" + getWindowId() + ", slotId=" + getSlotId() + ", actionId=" + getActionId() + ")";
  }
  
  public short getWindowId() {
    return this.windowId;
  }
  
  public short getSlotId() {
    return this.slotId;
  }
  
  public short getActionId() {
    return this.actionId;
  }
}
