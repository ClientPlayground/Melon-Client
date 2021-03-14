package com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PPSSubCmd extends ViaSubCommand {
  public String name() {
    return "pps";
  }
  
  public String description() {
    return "Shows the packets per second of online players";
  }
  
  public String usage() {
    return "pps";
  }
  
  public boolean execute(ViaCommandSender sender, String[] args) {
    Map<Integer, Set<String>> playerVersions = new HashMap<>();
    int totalPackets = 0;
    int clients = 0;
    long max = 0L;
    for (ViaCommandSender p : Via.getPlatform().getOnlinePlayers()) {
      int playerVersion = Via.getAPI().getPlayerVersion(p.getUUID());
      if (!playerVersions.containsKey(Integer.valueOf(playerVersion)))
        playerVersions.put(Integer.valueOf(playerVersion), new HashSet<>()); 
      UserConnection uc = Via.getManager().getConnection(p.getUUID());
      if (uc != null && uc.getPacketsPerSecond() > -1L) {
        ((Set<String>)playerVersions.get(Integer.valueOf(playerVersion))).add(p.getName() + " (" + uc.getPacketsPerSecond() + " PPS)");
        totalPackets = (int)(totalPackets + uc.getPacketsPerSecond());
        if (uc.getPacketsPerSecond() > max)
          max = uc.getPacketsPerSecond(); 
        clients++;
      } 
    } 
    Map<Integer, Set<String>> sorted = new TreeMap<>(playerVersions);
    sendMessage(sender, "&4Live Packets Per Second", new Object[0]);
    if (clients > 1) {
      sendMessage(sender, "&cAverage: &f" + (totalPackets / clients), new Object[0]);
      sendMessage(sender, "&cHighest: &f" + max, new Object[0]);
    } 
    if (clients == 0)
      sendMessage(sender, "&cNo clients to display.", new Object[0]); 
    for (Map.Entry<Integer, Set<String>> entry : sorted.entrySet()) {
      sendMessage(sender, "&8[&6%s&8]: &b%s", new Object[] { ProtocolVersion.getProtocol(((Integer)entry.getKey()).intValue()).getName(), entry.getValue() });
    } 
    sorted.clear();
    return true;
  }
}
