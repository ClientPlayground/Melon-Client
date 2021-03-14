package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandGive extends CommandBase {
  public String getCommandName() {
    return "give";
  }
  
  public int getRequiredPermissionLevel() {
    return 2;
  }
  
  public String getCommandUsage(ICommandSender sender) {
    return "commands.give.usage";
  }
  
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    if (args.length < 2)
      throw new WrongUsageException("commands.give.usage", new Object[0]); 
    EntityPlayerMP entityPlayerMP = getPlayer(sender, args[0]);
    Item item = getItemByText(sender, args[1]);
    int i = (args.length >= 3) ? parseInt(args[2], 1, 64) : 1;
    int j = (args.length >= 4) ? parseInt(args[3]) : 0;
    ItemStack itemstack = new ItemStack(item, i, j);
    if (args.length >= 5) {
      String s = getChatComponentFromNthArg(sender, args, 4).getUnformattedText();
      try {
        itemstack.setTagCompound(JsonToNBT.getTagFromJson(s));
      } catch (NBTException nbtexception) {
        throw new CommandException("commands.give.tagError", new Object[] { nbtexception.getMessage() });
      } 
    } 
    boolean flag = ((EntityPlayer)entityPlayerMP).inventory.addItemStackToInventory(itemstack);
    if (flag) {
      ((EntityPlayer)entityPlayerMP).worldObj.playSoundAtEntity((Entity)entityPlayerMP, "random.pop", 0.2F, ((entityPlayerMP.getRNG().nextFloat() - entityPlayerMP.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
      ((EntityPlayer)entityPlayerMP).inventoryContainer.detectAndSendChanges();
    } 
    if (flag && itemstack.stackSize <= 0) {
      itemstack.stackSize = 1;
      sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, i);
      EntityItem entityitem1 = entityPlayerMP.dropPlayerItemWithRandomChoice(itemstack, false);
      if (entityitem1 != null)
        entityitem1.func_174870_v(); 
    } else {
      sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, i - itemstack.stackSize);
      EntityItem entityitem = entityPlayerMP.dropPlayerItemWithRandomChoice(itemstack, false);
      if (entityitem != null) {
        entityitem.setNoPickupDelay();
        entityitem.setOwner(entityPlayerMP.getCommandSenderName());
      } 
    } 
    notifyOperators(sender, this, "commands.give.success", new Object[] { itemstack.getChatComponent(), Integer.valueOf(i), entityPlayerMP.getCommandSenderName() });
  }
  
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    return (args.length == 1) ? getListOfStringsMatchingLastWord(args, getPlayers()) : ((args.length == 2) ? getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys()) : null);
  }
  
  protected String[] getPlayers() {
    return MinecraftServer.getServer().getAllUsernames();
  }
  
  public boolean isUsernameIndex(String[] args, int index) {
    return (index == 0);
  }
}
