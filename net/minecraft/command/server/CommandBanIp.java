package net.minecraft.command.server;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public class CommandBanIp extends CommandBase {
  public static final Pattern field_147211_a = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
  
  public String getCommandName() {
    return "ban-ip";
  }
  
  public int getRequiredPermissionLevel() {
    return 3;
  }
  
  public boolean canCommandSenderUseCommand(ICommandSender sender) {
    return (MinecraftServer.getServer().getConfigurationManager().getBannedIPs().isLanServer() && super.canCommandSenderUseCommand(sender));
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.banip.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    if (args.length >= 1 && args[0].length() > 1) {
      IChatComponent ichatcomponent = (args.length >= 2) ? getChatComponentFromNthArg(sender, args, 1) : null;
      Matcher matcher = field_147211_a.matcher(args[0]);
      if (matcher.matches()) {
        func_147210_a(sender, args[0], (ichatcomponent == null) ? null : ichatcomponent.getUnformattedText());
      } else {
        EntityPlayerMP entityplayermp = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[0]);
        if (entityplayermp == null)
          throw new PlayerNotFoundException("commands.banip.invalid", new Object[0]); 
        func_147210_a(sender, entityplayermp.getPlayerIP(), (ichatcomponent == null) ? null : ichatcomponent.getUnformattedText());
      } 
    } else {
      throw new WrongUsageException("commands.banip.usage", new Object[0]);
    } 
  }
  
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    return (args.length == 1) ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
  }
  
  protected void func_147210_a(ICommandSender sender, String address, String reason) {
    IPBanEntry ipbanentry = new IPBanEntry(address, (Date)null, sender.getCommandSenderName(), (Date)null, reason);
    MinecraftServer.getServer().getConfigurationManager().getBannedIPs().addEntry((UserListEntry)ipbanentry);
    List<EntityPlayerMP> list = MinecraftServer.getServer().getConfigurationManager().getPlayersMatchingAddress(address);
    String[] astring = new String[list.size()];
    int i = 0;
    for (EntityPlayerMP entityplayermp : list) {
      entityplayermp.playerNetServerHandler.kickPlayerFromServer("You have been IP banned.");
      astring[i++] = entityplayermp.getCommandSenderName();
    } 
    if (list.isEmpty()) {
      notifyOperators(sender, (ICommand)this, "commands.banip.success", new Object[] { address });
    } else {
      notifyOperators(sender, (ICommand)this, "commands.banip.success.players", new Object[] { address, joinNiceString((Object[])astring) });
    } 
  }
}
