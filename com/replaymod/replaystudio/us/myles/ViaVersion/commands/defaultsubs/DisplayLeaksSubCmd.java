package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;

public class DisplayLeaksSubCmd extends ViaSubCommand {
  public String name() {
    return "displayleaks";
  }
  
  public String description() {
    return "Try to hunt memory leaks!";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    if (ResourceLeakDetector.getLevel() != ResourceLeakDetector.Level.ADVANCED) {
      ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    } else {
      ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
    } 
    sendMessage(sender, "&6Leak detector is now %s", new Object[] { (ResourceLeakDetector.getLevel() == ResourceLeakDetector.Level.ADVANCED) ? "&aenabled" : "&cdisabled" });
    return true;
  }
}
