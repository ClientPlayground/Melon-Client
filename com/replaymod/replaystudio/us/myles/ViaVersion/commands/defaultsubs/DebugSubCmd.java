package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;

public class DebugSubCmd extends ViaSubCommand {
  public String name() {
    return "debug";
  }
  
  public String description() {
    return "Toggle debug mode";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    Via.getManager().setDebug(!Via.getManager().isDebug());
    sendMessage(sender, "&6Debug mode is now %s", new Object[] { Via.getManager().isDebug() ? "&aenabled" : "&cdisabled" });
    return true;
  }
}
