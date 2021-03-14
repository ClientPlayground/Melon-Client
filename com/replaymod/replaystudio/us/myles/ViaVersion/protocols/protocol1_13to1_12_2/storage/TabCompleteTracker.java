package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

public class TabCompleteTracker extends StoredObject {
  private int transactionId;
  
  private String input;
  
  private String lastTabComplete;
  
  private long timeToSend;
  
  public void setTransactionId(int transactionId) {
    this.transactionId = transactionId;
  }
  
  public void setInput(String input) {
    this.input = input;
  }
  
  public void setLastTabComplete(String lastTabComplete) {
    this.lastTabComplete = lastTabComplete;
  }
  
  public void setTimeToSend(long timeToSend) {
    this.timeToSend = timeToSend;
  }
  
  public int getTransactionId() {
    return this.transactionId;
  }
  
  public String getInput() {
    return this.input;
  }
  
  public String getLastTabComplete() {
    return this.lastTabComplete;
  }
  
  public long getTimeToSend() {
    return this.timeToSend;
  }
  
  public TabCompleteTracker(UserConnection user) {
    super(user);
  }
  
  public void sendPacketToServer() {
    if (this.lastTabComplete == null || this.timeToSend > System.currentTimeMillis())
      return; 
    PacketWrapper wrapper = new PacketWrapper(1, null, getUser());
    wrapper.write(Type.STRING, this.lastTabComplete);
    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
    wrapper.write(Type.OPTIONAL_POSITION, null);
    try {
      wrapper.sendToServer(Protocol1_13To1_12_2.class);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    this.lastTabComplete = null;
  }
}
