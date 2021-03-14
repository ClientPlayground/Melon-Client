package net.minecraft.command;

public interface IAdminCommand {
  void notifyOperators(ICommandSender paramICommandSender, ICommand paramICommand, int paramInt, String paramString, Object... paramVarArgs);
}
