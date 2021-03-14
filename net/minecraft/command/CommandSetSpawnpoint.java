package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandSetSpawnpoint extends CommandBase {
  public String getCommandName() {
    return "spawnpoint";
  }
  
  public int getRequiredPermissionLevel() {
    return 2;
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.spawnpoint.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    if (args.length > 1 && args.length < 4)
      throw new WrongUsageException("commands.spawnpoint.usage", new Object[0]); 
    EntityPlayerMP entityplayermp = (args.length > 0) ? getPlayer(sender, args[0]) : getCommandSenderAsPlayer(sender);
    BlockPos blockpos = (args.length > 3) ? parseBlockPos(sender, args, 1, true) : entityplayermp.getPosition();
    if (entityplayermp.worldObj != null) {
      entityplayermp.setSpawnPoint(blockpos, true);
      notifyOperators(sender, this, "commands.spawnpoint.success", new Object[] { entityplayermp.getCommandSenderName(), Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ()) });
    } 
  }
  
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    return (args.length == 1) ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : ((args.length > 1 && args.length <= 4) ? func_175771_a(args, 1, pos) : null);
  }
  
  public boolean isUsernameIndex(String[] args, int index) {
    return (index == 0);
  }
}
