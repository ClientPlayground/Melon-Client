package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;

public class VersionProvider implements Provider {
  public int getServerProtocol(UserConnection connection) throws Exception {
    return ProtocolRegistry.SERVER_PROTOCOL;
  }
}
