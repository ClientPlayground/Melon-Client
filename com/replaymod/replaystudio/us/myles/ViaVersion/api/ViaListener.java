package com.replaymod.replaystudio.us.myles.ViaVersion.api;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.util.UUID;
import lombok.NonNull;

public abstract class ViaListener {
  private final Class<? extends Protocol> requiredPipeline;
  
  protected void setRegistered(boolean registered) {
    this.registered = registered;
  }
  
  public ViaListener(Class<? extends Protocol> requiredPipeline) {
    this.requiredPipeline = requiredPipeline;
  }
  
  protected Class<? extends Protocol> getRequiredPipeline() {
    return this.requiredPipeline;
  }
  
  private boolean registered = false;
  
  protected boolean isRegistered() {
    return this.registered;
  }
  
  protected UserConnection getUserConnection(@NonNull UUID uuid) {
    if (uuid == null)
      throw new NullPointerException("uuid is marked @NonNull but is null"); 
    return Via.getManager().getConnection(uuid);
  }
  
  protected boolean isOnPipe(UUID uuid) {
    UserConnection userConnection = getUserConnection(uuid);
    return (userConnection != null && (this.requiredPipeline == null || ((ProtocolInfo)userConnection
      .get(ProtocolInfo.class)).getPipeline().contains(this.requiredPipeline)));
  }
  
  public abstract void register();
}
