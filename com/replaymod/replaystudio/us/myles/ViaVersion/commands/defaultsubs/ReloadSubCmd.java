package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;

public class ReloadSubCmd extends ViaSubCommand {
  public String name() {
    return "reload";
  }
  
  public String description() {
    return "Reload the config from the disk";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    Via.getPlatform().getConfigurationProvider().reloadConfig();
    sendMessage(sender, "&6Configuration successfully reloaded! Some features may need a restart.", new Object[0]);
    return true;
  }
}
