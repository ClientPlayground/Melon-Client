package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.GameRules;

public class CommandGameRule extends CommandBase {
  public String getCommandName() {
    return "gamerule";
  }
  
  public int getRequiredPermissionLevel() {
    return 2;
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.gamerule.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    String s2;
    GameRules gamerules = getGameRules();
    String s = (args.length > 0) ? args[0] : "";
    String s1 = (args.length > 1) ? buildString(args, 1) : "";
    switch (args.length) {
      case 0:
        sender.addChatMessage((IChatComponent)new ChatComponentText(joinNiceString((Object[])gamerules.getRules())));
        return;
      case 1:
        if (!gamerules.hasRule(s))
          throw new CommandException("commands.gamerule.norule", new Object[] { s }); 
        s2 = gamerules.getGameRuleStringValue(s);
        sender.addChatMessage((new ChatComponentText(s)).appendText(" = ").appendText(s2));
        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gamerules.getInt(s));
        return;
    } 
    if (gamerules.areSameType(s, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(s1) && !"false".equals(s1))
      throw new CommandException("commands.generic.boolean.invalid", new Object[] { s1 }); 
    gamerules.setOrCreateGameRule(s, s1);
    func_175773_a(gamerules, s);
    notifyOperators(sender, this, "commands.gamerule.success", new Object[0]);
  }
  
  public static void func_175773_a(GameRules rules, String p_175773_1_) {
    if ("reducedDebugInfo".equals(p_175773_1_)) {
      byte b0 = (byte)(rules.getGameRuleBooleanValue(p_175773_1_) ? 22 : 23);
      for (EntityPlayerMP entityplayermp : MinecraftServer.getServer().getConfigurationManager().getPlayerList())
        entityplayermp.playerNetServerHandler.sendPacket((Packet)new S19PacketEntityStatus((Entity)entityplayermp, b0)); 
    } 
  }
  
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 1)
      return getListOfStringsMatchingLastWord(args, getGameRules().getRules()); 
    if (args.length == 2) {
      GameRules gamerules = getGameRules();
      if (gamerules.areSameType(args[0], GameRules.ValueType.BOOLEAN_VALUE))
        return getListOfStringsMatchingLastWord(args, new String[] { "true", "false" }); 
    } 
    return null;
  }
  
  private GameRules getGameRules() {
    return MinecraftServer.getServer().worldServerForDimension(0).getGameRules();
  }
}
