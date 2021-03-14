package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;

public class InventoryEnderChest extends InventoryBasic {
  private TileEntityEnderChest associatedChest;
  
  public InventoryEnderChest() {
    super("container.enderchest", false, 27);
  }
  
  public void setChestTileEntity(TileEntityEnderChest chestTileEntity) {
    this.associatedChest = chestTileEntity;
  }
  
  public void loadInventoryFromNBT(NBTTagList p_70486_1_) {
    for (int i = 0; i < getSizeInventory(); i++)
      setInventorySlotContents(i, (ItemStack)null); 
    for (int k = 0; k < p_70486_1_.tagCount(); k++) {
      NBTTagCompound nbttagcompound = p_70486_1_.getCompoundTagAt(k);
      int j = nbttagcompound.getByte("Slot") & 0xFF;
      if (j >= 0 && j < getSizeInventory())
        setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound)); 
    } 
  }
  
  public NBTTagList saveInventoryToNBT() {
    NBTTagList nbttaglist = new NBTTagList();
    for (int i = 0; i < getSizeInventory(); i++) {
      ItemStack itemstack = getStackInSlot(i);
      if (itemstack != null) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setByte("Slot", (byte)i);
        itemstack.writeToNBT(nbttagcompound);
        nbttaglist.appendTag((NBTBase)nbttagcompound);
      } 
    } 
    return nbttaglist;
  }
  
  public boolean isUseableByPlayer(EntityPlayer player) {
    return (this.associatedChest != null && !this.associatedChest.canBeUsed(player)) ? false : super.isUseableByPlayer(player);
  }
  
  public void openInventory(EntityPlayer player) {
    if (this.associatedChest != null)
      this.associatedChest.openChest(); 
    super.openInventory(player);
  }
  
  public void closeInventory(EntityPlayer player) {
    if (this.associatedChest != null)
      this.associatedChest.closeChest(); 
    super.closeInventory(player);
    this.associatedChest = null;
  }
}
