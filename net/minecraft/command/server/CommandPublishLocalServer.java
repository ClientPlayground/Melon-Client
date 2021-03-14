package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;

public class CommandPublishLocalServer extends CommandBase {
  public String getCommandName() {
    return "publish";
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.publish.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    String s = MinecraftServer.getServer().shareToLAN(WorldSettings.GameType.SURVIVAL, false);
    if (s != null) {
      notifyOperators(sender, (ICommand)this, "commands.publish.started", new Object[] { s });
    } else {
      notifyOperators(sender, (ICommand)this, "commands.publish.failed", new Object[0]);
    } 
  }
}
