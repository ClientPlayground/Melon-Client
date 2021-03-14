package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;

public class HandItemProvider implements Provider {
  public Item getHandItem(UserConnection info) {
    return new Item((short)0, (byte)0, (short)0, null);
  }
}
