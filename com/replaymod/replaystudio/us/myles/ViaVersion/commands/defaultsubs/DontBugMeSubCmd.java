package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.configuration.ConfigurationProvider;

public class DontBugMeSubCmd extends ViaSubCommand {
  public String name() {
    return "dontbugme";
  }
  
  public String description() {
    return "Toggle checking for updates";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    ConfigurationProvider provider = Via.getPlatform().getConfigurationProvider();
    boolean newValue = !Via.getConfig().isCheckForUpdates();
    provider.set("checkforupdates", Boolean.valueOf(newValue));
    provider.saveConfig();
    sendMessage(sender, "&6We will %snotify you about updates.", new Object[] { newValue ? "&a" : "&cnot " });
    return true;
  }
}
