package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;

public class HelpSubCmd extends ViaSubCommand {
  public String name() {
    return "help";
  }
  
  public String description() {
    return "You are looking at it right now!";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    Via.getManager().getCommandHandler().showHelp(sender);
    return true;
  }
}
