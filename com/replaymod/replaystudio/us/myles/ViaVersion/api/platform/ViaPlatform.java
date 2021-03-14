package com.replaymod.replaystudio.us.myles.ViaVersion.api.platform;

import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaAPI;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaVersionConfig;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import java.util.UUID;
import java.util.logging.Logger;

public interface ViaPlatform<T> {
  Logger getLogger();
  
  String getPlatformName();
  
  String getPlatformVersion();
  
  String getPluginVersion();
  
  TaskId runAsync(Runnable paramRunnable);
  
  TaskId runSync(Runnable paramRunnable);
  
  TaskId runSync(Runnable paramRunnable, Long paramLong);
  
  TaskId runRepeatingSync(Runnable paramRunnable, Long paramLong);
  
  void cancelTask(TaskId paramTaskId);
  
  ViaCommandSender[] getOnlinePlayers();
  
  void sendMessage(UUID paramUUID, String paramString);
  
  boolean kickPlayer(UUID paramUUID, String paramString);
  
  boolean isPluginEnabled();
  
  ViaAPI<T> getApi();
  
  ViaVersionConfig getConf();
  
  ConfigurationProvider getConfigurationProvider();
  
  void onReload();
  
  JsonObject getDump();
  
  boolean isOldClientsAllowed();
}
