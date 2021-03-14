package com.replaymod.replaystudio.us.myles.ViaVersion.api;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.ViaManager;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaPlatform;

public class Via {
  private static ViaPlatform platform;
  
  private static ViaManager manager;
  
  public static ViaPlatform getPlatform() {
    return platform;
  }
  
  public static ViaManager getManager() {
    return manager;
  }
  
  public static void init(ViaManager viaManager) {
    Preconditions.checkArgument((manager == null), "ViaManager is already set");
    platform = viaManager.getPlatform();
    manager = viaManager;
  }
  
  public static ViaAPI getAPI() {
    Preconditions.checkArgument((platform != null), "ViaVersion has not loaded the Platform");
    return platform.getApi();
  }
  
  public static ViaVersionConfig getConfig() {
    Preconditions.checkArgument((platform != null), "ViaVersion has not loaded the Platform");
    return platform.getConf();
  }
}
