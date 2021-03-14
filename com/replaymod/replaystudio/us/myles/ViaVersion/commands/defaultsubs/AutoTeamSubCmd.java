package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.configuration.ConfigurationProvider;

public class AutoTeamSubCmd extends ViaSubCommand {
  public String name() {
    return "autoteam";
  }
  
  public String description() {
    return "Toggle automatically teaming to prevent colliding.";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    ConfigurationProvider provider = Via.getPlatform().getConfigurationProvider();
    boolean newValue = !Via.getConfig().isAutoTeam();
    provider.set("auto-team", Boolean.valueOf(newValue));
    provider.saveConfig();
    sendMessage(sender, "&6We will %s", new Object[] { newValue ? "&aautomatically team players" : "&cno longer auto team players" });
    sendMessage(sender, "&6All players will need to re-login for the change to take place.", new Object[0]);
    return true;
  }
}
