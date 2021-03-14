package com.replaymod.replaystudio.us.myles.ViaVersion.commands;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaCommandSender;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaSubCommand;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.command.ViaVersionCommand;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.AutoTeamSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.DebugSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.DisplayLeaksSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.DontBugMeSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.DumpSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.HelpSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.ListSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.PPSSubCmd;
import com.replaymod.replaystudio.us.myles.ViaVersion.commands.defaultsubs.ReloadSubCmd;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;

public abstract class ViaCommandHandler implements ViaVersionCommand {
  private final Map<String, ViaSubCommand> commandMap = new HashMap<>();
  
  public ViaCommandHandler() {
    try {
      registerDefaults();
    } catch (Exception exception) {}
  }
  
  public void registerSubCommand(@NonNull ViaSubCommand command) throws Exception {
    if (command == null)
      throw new NullPointerException("command is marked @NonNull but is null"); 
    Preconditions.checkArgument(command.name().matches("^[a-z0-9_-]{3,15}$"), command.name() + " is not a valid sub-command name.");
    if (hasSubCommand(command.name()))
      throw new Exception("ViaSubCommand " + command.name() + " does already exists!"); 
    this.commandMap.put(command.name().toLowerCase(Locale.ROOT), command);
  }
  
  public boolean hasSubCommand(String name) {
    return this.commandMap.containsKey(name.toLowerCase(Locale.ROOT));
  }
  
  public ViaSubCommand getSubCommand(String name) {
    return this.commandMap.get(name.toLowerCase(Locale.ROOT));
  }
  
  public boolean onCommand(ViaCommandSender sender, String[] args) {
    if (args.length == 0) {
      showHelp(sender);
      return false;
    } 
    if (!hasSubCommand(args[0])) {
      sender.sendMessage(color("&cThis command does not exist."));
      showHelp(sender);
      return false;
    } 
    ViaSubCommand handler = getSubCommand(args[0]);
    if (!hasPermission(sender, handler.permission())) {
      sender.sendMessage(color("&cYou are not allowed to use this command!"));
      return false;
    } 
    String[] subArgs = Arrays.<String>copyOfRange(args, 1, args.length);
    boolean result = handler.execute(sender, subArgs);
    if (!result)
      sender.sendMessage("Usage: /viaversion " + handler.usage()); 
    return result;
  }
  
  public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
    Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
    List<String> output = new ArrayList<>();
    if (args.length == 1) {
      if (!args[0].isEmpty()) {
        for (ViaSubCommand sub : allowed) {
          if (sub.name().toLowerCase().startsWith(args[0].toLowerCase(Locale.ROOT)))
            output.add(sub.name()); 
        } 
      } else {
        for (ViaSubCommand sub : allowed)
          output.add(sub.name()); 
      } 
    } else if (args.length >= 2 && 
      getSubCommand(args[0]) != null) {
      ViaSubCommand sub = getSubCommand(args[0]);
      if (!allowed.contains(sub))
        return output; 
      String[] subArgs = Arrays.<String>copyOfRange(args, 1, args.length);
      List<String> tab = sub.onTabComplete(sender, subArgs);
      Collections.sort(tab);
      return tab;
    } 
    return output;
  }
  
  public void showHelp(ViaCommandSender sender) {
    Set<ViaSubCommand> allowed = calculateAllowedCommands(sender);
    if (allowed.size() == 0) {
      sender.sendMessage(color("&cYou are not allowed to use these commands!"));
      return;
    } 
    sender.sendMessage(color("&aViaVersion &c" + Via.getPlatform().getPluginVersion()));
    sender.sendMessage(color("&6Commands:"));
    for (ViaSubCommand cmd : allowed) {
      sender.sendMessage(color(String.format("&2/viaversion %s &7- &6%s", new Object[] { cmd.usage(), cmd.description() })));
    } 
    allowed.clear();
  }
  
  private Set<ViaSubCommand> calculateAllowedCommands(ViaCommandSender sender) {
    Set<ViaSubCommand> cmds = new HashSet<>();
    for (ViaSubCommand sub : this.commandMap.values()) {
      if (hasPermission(sender, sub.permission()))
        cmds.add(sub); 
    } 
    return cmds;
  }
  
  private boolean hasPermission(ViaCommandSender sender, String permission) {
    return (permission == null || sender.hasPermission(permission));
  }
  
  private void registerDefaults() throws Exception {
    registerSubCommand((ViaSubCommand)new ListSubCmd());
    registerSubCommand((ViaSubCommand)new PPSSubCmd());
    registerSubCommand((ViaSubCommand)new DebugSubCmd());
    registerSubCommand((ViaSubCommand)new DumpSubCmd());
    registerSubCommand((ViaSubCommand)new DisplayLeaksSubCmd());
    registerSubCommand((ViaSubCommand)new DontBugMeSubCmd());
    registerSubCommand((ViaSubCommand)new AutoTeamSubCmd());
    registerSubCommand((ViaSubCommand)new HelpSubCmd());
    registerSubCommand((ViaSubCommand)new ReloadSubCmd());
  }
  
  public static String color(String string) {
    try {
      string = ChatColor.translateAlternateColorCodes('&', string);
    } catch (Exception exception) {}
    return string;
  }
  
  public static void sendMessage(@NonNull ViaCommandSender sender, String message, Object... args) {
    if (sender == null)
      throw new NullPointerException("sender is marked @NonNull but is null"); 
    sender.sendMessage(color((args == null) ? message : String.format(message, args)));
  }
}
