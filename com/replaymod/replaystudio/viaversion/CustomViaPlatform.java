package com.replaymod.replaystudio.viaversion;

import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaAPI;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaVersionConfig;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.TaskId;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaPlatform;
import java.util.UUID;
import java.util.logging.Logger;

public class CustomViaPlatform implements ViaPlatform {
  private CustomViaConfig config = new CustomViaConfig();
  
  public Logger getLogger() {
    return Logger.getLogger(CustomViaPlatform.class.getName());
  }
  
  public String getPlatformName() {
    return "ReplayStudio";
  }
  
  public String getPlatformVersion() {
    return null;
  }
  
  public String getPluginVersion() {
    return "1.0";
  }
  
  public TaskId runAsync(Runnable runnable) {
    throw new UnsupportedOperationException();
  }
  
  public TaskId runSync(Runnable runnable) {
    throw new UnsupportedOperationException();
  }
  
  public TaskId runSync(Runnable runnable, Long aLong) {
    throw new UnsupportedOperationException();
  }
  
  public TaskId runRepeatingSync(Runnable runnable, Long aLong) {
    throw new UnsupportedOperationException();
  }
  
  public void cancelTask(TaskId taskId) {
    throw new UnsupportedOperationException();
  }
  
  public ViaCommandSender[] getOnlinePlayers() {
    throw new UnsupportedOperationException();
  }
  
  public void sendMessage(UUID uuid, String s) {
    throw new UnsupportedOperationException();
  }
  
  public boolean kickPlayer(UUID uuid, String s) {
    throw new UnsupportedOperationException();
  }
  
  public boolean isPluginEnabled() {
    return true;
  }
  
  public ViaAPI getApi() {
    return CustomViaAPI.INSTANCE.get();
  }
  
  public ViaVersionConfig getConf() {
    return this.config;
  }
  
  public ConfigurationProvider getConfigurationProvider() {
    throw new UnsupportedOperationException();
  }
  
  public void onReload() {}
  
  public JsonObject getDump() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isOldClientsAllowed() {
    return false;
  }
}
