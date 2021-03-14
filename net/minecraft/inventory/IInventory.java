package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldNameable;

public interface IInventory extends IWorldNameable {
  int getSizeInventory();
  
  ItemStack getStackInSlot(int paramInt);
  
  ItemStack decrStackSize(int paramInt1, int paramInt2);
  
  ItemStack getStackInSlotOnClosing(int paramInt);
  
  void setInventorySlotContents(int paramInt, ItemStack paramItemStack);
  
  int getInventoryStackLimit();
  
  void markDirty();
  
  boolean isUseableByPlayer(EntityPlayer paramEntityPlayer);
  
  void openInventory(EntityPlayer paramEntityPlayer);
  
  void closeInventory(EntityPlayer paramEntityPlayer);
  
  boolean isItemValidForSlot(int paramInt, ItemStack paramItemStack);
  
  int getField(int paramInt);
  
  void setField(int paramInt1, int paramInt2);
  
  int getFieldCount();
  
  void clear();
}
