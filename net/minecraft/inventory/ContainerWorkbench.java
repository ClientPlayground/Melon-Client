package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ContainerWorkbench extends Container {
  public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
  
  public IInventory craftResult = new InventoryCraftResult();
  
  private World worldObj;
  
  private BlockPos pos;
  
  public ContainerWorkbench(InventoryPlayer playerInventory, World worldIn, BlockPos posIn) {
    this.worldObj = worldIn;
    this.pos = posIn;
    addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35));
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++)
        addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18)); 
    } 
    for (int k = 0; k < 3; k++) {
      for (int i1 = 0; i1 < 9; i1++)
        addSlotToContainer(new Slot((IInventory)playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18)); 
    } 
    for (int l = 0; l < 9; l++)
      addSlotToContainer(new Slot((IInventory)playerInventory, l, 8 + l * 18, 142)); 
    onCraftMatrixChanged(this.craftMatrix);
  }
  
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
  }
  
  public void onContainerClosed(EntityPlayer playerIn) {
    super.onContainerClosed(playerIn);
    if (!this.worldObj.isRemote)
      for (int i = 0; i < 9; i++) {
        ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
        if (itemstack != null)
          playerIn.dropPlayerItemWithRandomChoice(itemstack, false); 
      }  
  }
  
  public boolean canInteractWith(EntityPlayer playerIn) {
    return (this.worldObj.getBlockState(this.pos).getBlock() != Blocks.crafting_table) ? false : ((playerIn.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D));
  }
  
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
    ItemStack itemstack = null;
    Slot slot = this.inventorySlots.get(index);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      if (index == 0) {
        if (!mergeItemStack(itemstack1, 10, 46, true))
          return null; 
        slot.onSlotChange(itemstack1, itemstack);
      } else if (index >= 10 && index < 37) {
        if (!mergeItemStack(itemstack1, 37, 46, false))
          return null; 
      } else if (index >= 37 && index < 46) {
        if (!mergeItemStack(itemstack1, 10, 37, false))
          return null; 
      } else if (!mergeItemStack(itemstack1, 10, 46, false)) {
        return null;
      } 
      if (itemstack1.stackSize == 0) {
        slot.putStack((ItemStack)null);
      } else {
        slot.onSlotChanged();
      } 
      if (itemstack1.stackSize == itemstack.stackSize)
        return null; 
      slot.onPickupFromSlot(playerIn, itemstack1);
    } 
    return itemstack;
  }
  
  public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
    return (slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn));
  }
}
