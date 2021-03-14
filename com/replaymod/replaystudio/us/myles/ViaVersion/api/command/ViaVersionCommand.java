package com.replaymod.replaystudio.us.myles.ViaVersion.api.command;

import java.util.List;

public interface ViaVersionCommand {
  void registerSubCommand(ViaSubCommand paramViaSubCommand) throws Exception;
  
  boolean hasSubCommand(String paramString);
  
  ViaSubCommand getSubCommand(String paramString);
  
  boolean onCommand(ViaCommandSender paramViaCommandSender, String[] paramArrayOfString);
  
  List<String> onTabComplete(ViaCommandSender paramViaCommandSender, String[] paramArrayOfString);
}
