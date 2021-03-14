package com.replaymod.replaystudio.us.myles.ViaVersion;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaInjector;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaPlatform;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.ViaCommandHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.update.UpdateUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViaManager {
  private final Map<UUID, UserConnection> portedPlayers = new ConcurrentHashMap<>();
  
  private ViaPlatform platform;
  
  public Map<UUID, UserConnection> getPortedPlayers() {
    return this.portedPlayers;
  }
  
  public ViaPlatform getPlatform() {
    return this.platform;
  }
  
  private ViaProviders providers = new ViaProviders();
  
  public ViaProviders getProviders() {
    return this.providers;
  }
  
  private boolean debug = false;
  
  private ViaInjector injector;
  
  private ViaCommandHandler commandHandler;
  
  private ViaPlatformLoader loader;
  
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  
  public boolean isDebug() {
    return this.debug;
  }
  
  public ViaInjector getInjector() {
    return this.injector;
  }
  
  public ViaCommandHandler getCommandHandler() {
    return this.commandHandler;
  }
  
  public ViaPlatformLoader getLoader() {
    return this.loader;
  }
  
  public static ViaManagerBuilder builder() {
    return new ViaManagerBuilder();
  }
  
  public static class ViaManagerBuilder {
    private ViaPlatform platform;
    
    private ViaInjector injector;
    
    private ViaCommandHandler commandHandler;
    
    private ViaPlatformLoader loader;
    
    public ViaManagerBuilder platform(ViaPlatform platform) {
      this.platform = platform;
      return this;
    }
    
    public ViaManagerBuilder injector(ViaInjector injector) {
      this.injector = injector;
      return this;
    }
    
    public ViaManagerBuilder commandHandler(ViaCommandHandler commandHandler) {
      this.commandHandler = commandHandler;
      return this;
    }
    
    public ViaManagerBuilder loader(ViaPlatformLoader loader) {
      this.loader = loader;
      return this;
    }
    
    public ViaManager build() {
      return new ViaManager(this.platform, this.injector, this.commandHandler, this.loader);
    }
    
    public String toString() {
      return "ViaManager.ViaManagerBuilder(platform=" + this.platform + ", injector=" + this.injector + ", commandHandler=" + this.commandHandler + ", loader=" + this.loader + ")";
    }
  }
  
  public ViaManager(ViaPlatform platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader) {
    this.platform = platform;
    this.injector = injector;
    this.commandHandler = commandHandler;
    this.loader = loader;
  }
  
  public void init() {
    if (System.getProperty("ViaVersion") != null)
      this.platform.onReload(); 
    if (this.platform.getConf().isCheckForUpdates())
      UpdateUtil.sendUpdateMessage(); 
    ProtocolRegistry.getSupportedVersions();
    try {
      this.injector.inject();
    } catch (Exception e) {
      getPlatform().getLogger().severe("ViaVersion failed to inject:");
      e.printStackTrace();
      return;
    } 
    System.setProperty("ViaVersion", getPlatform().getPluginVersion());
    this.platform.runSync(new Runnable() {
          public void run() {
            ViaManager.this.onServerLoaded();
          }
        });
  }
  
  public void onServerLoaded() {
    try {
      ProtocolRegistry.SERVER_PROTOCOL = this.injector.getServerProtocolVersion();
    } catch (Exception e) {
      getPlatform().getLogger().severe("ViaVersion failed to get the server protocol!");
      e.printStackTrace();
    } 
    if (ProtocolRegistry.SERVER_PROTOCOL != -1) {
      getPlatform().getLogger().info("ViaVersion detected server version: " + ProtocolVersion.getProtocol(ProtocolRegistry.SERVER_PROTOCOL));
      if (!ProtocolRegistry.isWorkingPipe())
        getPlatform().getLogger().warning("ViaVersion does not have any compatible versions for this server version, please read our resource page carefully."); 
    } 
    ProtocolRegistry.onServerLoaded();
    this.loader.load();
    ProtocolRegistry.refreshVersions();
  }
  
  public void destroy() {
    getPlatform().getLogger().info("ViaVersion is disabling, if this is a reload and you experience issues consider rebooting.");
    try {
      this.injector.uninject();
    } catch (Exception e) {
      getPlatform().getLogger().severe("ViaVersion failed to uninject:");
      e.printStackTrace();
    } 
    this.loader.unload();
  }
  
  public void addPortedClient(UserConnection info) {
    this.portedPlayers.put(((ProtocolInfo)info.get(ProtocolInfo.class)).getUuid(), info);
  }
  
  public void removePortedClient(UUID clientID) {
    this.portedPlayers.remove(clientID);
  }
  
  public UserConnection getConnection(UUID playerUUID) {
    return this.portedPlayers.get(playerUUID);
  }
}
