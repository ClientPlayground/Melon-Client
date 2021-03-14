package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;

public class SlotCrafting extends Slot {
  private final InventoryCrafting craftMatrix;
  
  private final EntityPlayer thePlayer;
  
  private int amountCrafted;
  
  public SlotCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory p_i45790_3_, int slotIndex, int xPosition, int yPosition) {
    super(p_i45790_3_, slotIndex, xPosition, yPosition);
    this.thePlayer = player;
    this.craftMatrix = craftingInventory;
  }
  
  public boolean isItemValid(ItemStack stack) {
    return false;
  }
  
  public ItemStack decrStackSize(int amount) {
    if (getHasStack())
      this.amountCrafted += Math.min(amount, (getStack()).stackSize); 
    return super.decrStackSize(amount);
  }
  
  protected void onCrafting(ItemStack stack, int amount) {
    this.amountCrafted += amount;
    onCrafting(stack);
  }
  
  protected void onCrafting(ItemStack stack) {
    if (this.amountCrafted > 0)
      stack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted); 
    this.amountCrafted = 0;
    if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table))
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildWorkBench); 
    if (stack.getItem() instanceof ItemPickaxe)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildPickaxe); 
    if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace))
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildFurnace); 
    if (stack.getItem() instanceof net.minecraft.item.ItemHoe)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildHoe); 
    if (stack.getItem() == Items.bread)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.makeBread); 
    if (stack.getItem() == Items.cake)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.bakeCake); 
    if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe)stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildBetterPickaxe); 
    if (stack.getItem() instanceof net.minecraft.item.ItemSword)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.buildSword); 
    if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table))
      this.thePlayer.triggerAchievement((StatBase)AchievementList.enchantments); 
    if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf))
      this.thePlayer.triggerAchievement((StatBase)AchievementList.bookcase); 
    if (stack.getItem() == Items.golden_apple && stack.getMetadata() == 1)
      this.thePlayer.triggerAchievement((StatBase)AchievementList.overpowered); 
  }
  
  public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
    onCrafting(stack);
    ItemStack[] aitemstack = CraftingManager.getInstance().func_180303_b(this.craftMatrix, playerIn.worldObj);
    for (int i = 0; i < aitemstack.length; i++) {
      ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
      ItemStack itemstack1 = aitemstack[i];
      if (itemstack != null)
        this.craftMatrix.decrStackSize(i, 1); 
      if (itemstack1 != null)
        if (this.craftMatrix.getStackInSlot(i) == null) {
          this.craftMatrix.setInventorySlotContents(i, itemstack1);
        } else if (!this.thePlayer.inventory.addItemStackToInventory(itemstack1)) {
          this.thePlayer.dropPlayerItemWithRandomChoice(itemstack1, false);
        }  
    } 
  }
}
