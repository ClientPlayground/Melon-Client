package com.replaymod.replaystudio.us.myles.ViaVersion.api.data;

public class StoredObject {
  private UserConnection user;
  
  public StoredObject(UserConnection user) {
    this.user = user;
  }
  
  public UserConnection getUser() {
    return this.user;
  }
}
