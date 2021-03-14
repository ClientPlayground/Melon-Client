package com.replaymod.replaystudio.us.myles.ViaVersion.api.command;

import java.util.UUID;

public interface ViaCommandSender {
  boolean hasPermission(String paramString);
  
  void sendMessage(String paramString);
  
  UUID getUUID();
  
  String getName();
}
