package net.minecraft.command;

import java.util.List;
import net.minecraft.util.BlockPos;

public interface ICommand extends Comparable<ICommand> {
  String getCommandName();
  
  String getCommandUsage(ICommandSender paramICommandSender);
  
  List<String> getCommandAliases();
  
  void processCommand(ICommandSender paramICommandSender, String[] paramArrayOfString) throws CommandException;
  
  boolean canCommandSenderUseCommand(ICommandSender paramICommandSender);
  
  List<String> addTabCompletionOptions(ICommandSender paramICommandSender, String[] paramArrayOfString, BlockPos paramBlockPos);
  
  boolean isUsernameIndex(String[] paramArrayOfString, int paramInt);
}
