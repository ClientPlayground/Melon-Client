package com.replaymod.replaystudio.us.myles.ViaVersion.api.command;

import com.replaymod.replaystudio.us.myles.ViaVersion.commands.ViaCommandHandler;
import java.util.Collections;
import java.util.List;

public abstract class ViaSubCommand {
  public abstract String name();
  
  public abstract String description();
  
  public String usage() {
    return name();
  }
  
  public String permission() {
    return "viaversion.admin";
  }
  
  public abstract boolean execute(ViaCommandSender paramViaCommandSender, String[] paramArrayOfString);
  
  public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
    return Collections.emptyList();
  }
  
  public String color(String s) {
    return ViaCommandHandler.color(s);
  }
  
  public void sendMessage(ViaCommandSender sender, String message, Object... args) {
    ViaCommandHandler.sendMessage(sender, message, args);
  }
}
