package net.minecraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface ISidedInventory extends IInventory {
  int[] getSlotsForFace(EnumFacing paramEnumFacing);
  
  boolean canInsertItem(int paramInt, ItemStack paramItemStack, EnumFacing paramEnumFacing);
  
  boolean canExtractItem(int paramInt, ItemStack paramItemStack, EnumFacing paramEnumFacing);
}
