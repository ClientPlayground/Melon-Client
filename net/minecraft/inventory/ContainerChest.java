package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerChest extends Container {
  private IInventory lowerChestInventory;
  
  private int numRows;
  
  public ContainerChest(IInventory playerInventory, IInventory chestInventory, EntityPlayer player) {
    this.lowerChestInventory = chestInventory;
    this.numRows = chestInventory.getSizeInventory() / 9;
    chestInventory.openInventory(player);
    int i = (this.numRows - 4) * 18;
    for (int j = 0; j < this.numRows; j++) {
      for (int k = 0; k < 9; k++)
        addSlotToContainer(new Slot(chestInventory, k + j * 9, 8 + k * 18, 18 + j * 18)); 
    } 
    for (int l = 0; l < 3; l++) {
      for (int j1 = 0; j1 < 9; j1++)
        addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i)); 
    } 
    for (int i1 = 0; i1 < 9; i1++)
      addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i)); 
  }
  
  public boolean canInteractWith(EntityPlayer playerIn) {
    return this.lowerChestInventory.isUseableByPlayer(playerIn);
  }
  
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
    ItemStack itemstack = null;
    Slot slot = this.inventorySlots.get(index);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      if (index < this.numRows * 9) {
        if (!mergeItemStack(itemstack1, this.numRows * 9, this.inventorySlots.size(), true))
          return null; 
      } else if (!mergeItemStack(itemstack1, 0, this.numRows * 9, false)) {
        return null;
      } 
      if (itemstack1.stackSize == 0) {
        slot.putStack((ItemStack)null);
      } else {
        slot.onSlotChanged();
      } 
    } 
    return itemstack;
  }
  
  public void onContainerClosed(EntityPlayer playerIn) {
    super.onContainerClosed(playerIn);
    this.lowerChestInventory.closeInventory(playerIn);
  }
  
  public IInventory getLowerChestInventory() {
    return this.lowerChestInventory;
  }
}
