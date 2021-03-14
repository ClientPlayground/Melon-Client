package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.util.UUID;

public class ProtocolInfo extends StoredObject {
  public void setState(State state) {
    this.state = state;
  }
  
  public void setProtocolVersion(int protocolVersion) {
    this.protocolVersion = protocolVersion;
  }
  
  public void setServerProtocolVersion(int serverProtocolVersion) {
    this.serverProtocolVersion = serverProtocolVersion;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }
  
  public void setPipeline(ProtocolPipeline pipeline) {
    this.pipeline = pipeline;
  }
  
  private State state = State.HANDSHAKE;
  
  public State getState() {
    return this.state;
  }
  
  private int protocolVersion = -1;
  
  public int getProtocolVersion() {
    return this.protocolVersion;
  }
  
  private int serverProtocolVersion = -1;
  
  private String username;
  
  private UUID uuid;
  
  private ProtocolPipeline pipeline;
  
  public int getServerProtocolVersion() {
    return this.serverProtocolVersion;
  }
  
  public String getUsername() {
    return this.username;
  }
  
  public UUID getUuid() {
    return this.uuid;
  }
  
  public ProtocolPipeline getPipeline() {
    return this.pipeline;
  }
  
  public ProtocolInfo(UserConnection user) {
    super(user);
  }
}
