package net.minecraft.command;

import java.util.List;
import java.util.Map;
import net.minecraft.util.BlockPos;

public interface ICommandManager {
  int executeCommand(ICommandSender paramICommandSender, String paramString);
  
  List<String> getTabCompletionOptions(ICommandSender paramICommandSender, String paramString, BlockPos paramBlockPos);
  
  List<ICommand> getPossibleCommands(ICommandSender paramICommandSender);
  
  Map<String, ICommand> getCommands();
}
