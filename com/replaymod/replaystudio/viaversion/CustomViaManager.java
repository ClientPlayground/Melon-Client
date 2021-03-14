package com.replaymod.replaystudio.viaversion;

import com.replaymod.replaystudio.us.myles.ViaVersion.ViaManager;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class CustomViaManager extends ViaManager {
  public static synchronized void initialize() {
    if (Via.getPlatform() != null)
      return; 
    Via.init(new CustomViaManager());
  }
  
  private CustomViaManager() {
    super(new CustomViaPlatform(), new CustomViaInjector(), null, null);
  }
  
  public Map<UUID, UserConnection> getPortedPlayers() {
    UserConnection user = ((CustomViaAPI)CustomViaAPI.INSTANCE.get()).user();
    UUID uuid = ((ProtocolInfo)user.get(ProtocolInfo.class)).getUuid();
    return Collections.singletonMap(uuid, user);
  }
  
  public UserConnection getConnection(UUID playerUUID) {
    UserConnection user = ((CustomViaAPI)CustomViaAPI.INSTANCE.get()).user();
    if (playerUUID.equals(((ProtocolInfo)user.get(ProtocolInfo.class)).getUuid()))
      return user; 
    throw new UnsupportedOperationException();
  }
}
