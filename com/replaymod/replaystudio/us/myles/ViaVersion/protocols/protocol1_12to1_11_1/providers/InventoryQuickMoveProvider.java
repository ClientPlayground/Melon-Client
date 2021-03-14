package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;

public class InventoryQuickMoveProvider implements Provider {
  public boolean registerQuickMoveAction(short windowId, short slotId, short actionId, UserConnection userConnection) {
    return false;
  }
}
