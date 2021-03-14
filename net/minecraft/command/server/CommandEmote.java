package net.minecraft.command.server;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class CommandEmote extends CommandBase {
  public String getCommandName() {
    return "me";
  }
  
  public int getRequiredPermissionLevel() {
    return 0;
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.me.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    if (args.length <= 0)
      throw new WrongUsageException("commands.me.usage", new Object[0]); 
    IChatComponent ichatcomponent = getChatComponentFromNthArg(sender, args, 0, !(sender instanceof net.minecraft.entity.player.EntityPlayer));
    MinecraftServer.getServer().getConfigurationManager().sendChatMsg((IChatComponent)new ChatComponentTranslation("chat.type.emote", new Object[] { sender.getDisplayName(), ichatcomponent }));
  }
  
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
  }
}
