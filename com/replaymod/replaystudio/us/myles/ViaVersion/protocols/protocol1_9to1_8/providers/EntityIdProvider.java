package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class EntityIdProvider implements Provider {
  public int getEntityId(UserConnection user) throws Exception {
    return ((EntityTracker)user.get(EntityTracker.class)).getEntityID();
  }
}
