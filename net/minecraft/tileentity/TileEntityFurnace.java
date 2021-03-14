package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
  private static final int[] slotsTop = new int[] { 0 };
  
  private static final int[] slotsBottom = new int[] { 2, 1 };
  
  private static final int[] slotsSides = new int[] { 1 };
  
  private ItemStack[] furnaceItemStacks = new ItemStack[3];
  
  private int furnaceBurnTime;
  
  private int currentItemBurnTime;
  
  private int cookTime;
  
  private int totalCookTime;
  
  private String furnaceCustomName;
  
  public int getSizeInventory() {
    return this.furnaceItemStacks.length;
  }
  
  public ItemStack getStackInSlot(int index) {
    return this.furnaceItemStacks[index];
  }
  
  public ItemStack decrStackSize(int index, int count) {
    if (this.furnaceItemStacks[index] != null) {
      if ((this.furnaceItemStacks[index]).stackSize <= count) {
        ItemStack itemstack1 = this.furnaceItemStacks[index];
        this.furnaceItemStacks[index] = null;
        return itemstack1;
      } 
      ItemStack itemstack = this.furnaceItemStacks[index].splitStack(count);
      if ((this.furnaceItemStacks[index]).stackSize == 0)
        this.furnaceItemStacks[index] = null; 
      return itemstack;
    } 
    return null;
  }
  
  public ItemStack getStackInSlotOnClosing(int index) {
    if (this.furnaceItemStacks[index] != null) {
      ItemStack itemstack = this.furnaceItemStacks[index];
      this.furnaceItemStacks[index] = null;
      return itemstack;
    } 
    return null;
  }
  
  public void setInventorySlotContents(int index, ItemStack stack) {
    boolean flag = (stack != null && stack.isItemEqual(this.furnaceItemStacks[index]) && ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks[index]));
    this.furnaceItemStacks[index] = stack;
    if (stack != null && stack.stackSize > getInventoryStackLimit())
      stack.stackSize = getInventoryStackLimit(); 
    if (index == 0 && !flag) {
      this.totalCookTime = getCookTime(stack);
      this.cookTime = 0;
      markDirty();
    } 
  }
  
  public String getCommandSenderName() {
    return hasCustomName() ? this.furnaceCustomName : "container.furnace";
  }
  
  public boolean hasCustomName() {
    return (this.furnaceCustomName != null && this.furnaceCustomName.length() > 0);
  }
  
  public void setCustomInventoryName(String p_145951_1_) {
    this.furnaceCustomName = p_145951_1_;
  }
  
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    NBTTagList nbttaglist = compound.getTagList("Items", 10);
    this.furnaceItemStacks = new ItemStack[getSizeInventory()];
    for (int i = 0; i < nbttaglist.tagCount(); i++) {
      NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
      int j = nbttagcompound.getByte("Slot");
      if (j >= 0 && j < this.furnaceItemStacks.length)
        this.furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound); 
    } 
    this.furnaceBurnTime = compound.getShort("BurnTime");
    this.cookTime = compound.getShort("CookTime");
    this.totalCookTime = compound.getShort("CookTimeTotal");
    this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
    if (compound.hasKey("CustomName", 8))
      this.furnaceCustomName = compound.getString("CustomName"); 
  }
  
  public void writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setShort("BurnTime", (short)this.furnaceBurnTime);
    compound.setShort("CookTime", (short)this.cookTime);
    compound.setShort("CookTimeTotal", (short)this.totalCookTime);
    NBTTagList nbttaglist = new NBTTagList();
    for (int i = 0; i < this.furnaceItemStacks.length; i++) {
      if (this.furnaceItemStacks[i] != null) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setByte("Slot", (byte)i);
        this.furnaceItemStacks[i].writeToNBT(nbttagcompound);
        nbttaglist.appendTag((NBTBase)nbttagcompound);
      } 
    } 
    compound.setTag("Items", (NBTBase)nbttaglist);
    if (hasCustomName())
      compound.setString("CustomName", this.furnaceCustomName); 
  }
  
  public int getInventoryStackLimit() {
    return 64;
  }
  
  public boolean isBurning() {
    return (this.furnaceBurnTime > 0);
  }
  
  public static boolean isBurning(IInventory p_174903_0_) {
    return (p_174903_0_.getField(0) > 0);
  }
  
  public void update() {
    boolean flag = isBurning();
    boolean flag1 = false;
    if (isBurning())
      this.furnaceBurnTime--; 
    if (!this.worldObj.isRemote) {
      if (isBurning() || (this.furnaceItemStacks[1] != null && this.furnaceItemStacks[0] != null)) {
        if (!isBurning() && canSmelt()) {
          this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
          if (isBurning()) {
            flag1 = true;
            if (this.furnaceItemStacks[1] != null) {
              (this.furnaceItemStacks[1]).stackSize--;
              if ((this.furnaceItemStacks[1]).stackSize == 0) {
                Item item = this.furnaceItemStacks[1].getItem().getContainerItem();
                this.furnaceItemStacks[1] = (item != null) ? new ItemStack(item) : null;
              } 
            } 
          } 
        } 
        if (isBurning() && canSmelt()) {
          this.cookTime++;
          if (this.cookTime == this.totalCookTime) {
            this.cookTime = 0;
            this.totalCookTime = getCookTime(this.furnaceItemStacks[0]);
            smeltItem();
            flag1 = true;
          } 
        } else {
          this.cookTime = 0;
        } 
      } else if (!isBurning() && this.cookTime > 0) {
        this.cookTime = MathHelper.clamp_int(this.cookTime - 2, 0, this.totalCookTime);
      } 
      if (flag != isBurning()) {
        flag1 = true;
        BlockFurnace.setState(isBurning(), this.worldObj, this.pos);
      } 
    } 
    if (flag1)
      markDirty(); 
  }
  
  public int getCookTime(ItemStack stack) {
    return 200;
  }
  
  private boolean canSmelt() {
    if (this.furnaceItemStacks[0] == null)
      return false; 
    ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
    return (itemstack == null) ? false : ((this.furnaceItemStacks[2] == null) ? true : (!this.furnaceItemStacks[2].isItemEqual(itemstack) ? false : (((this.furnaceItemStacks[2]).stackSize < getInventoryStackLimit() && (this.furnaceItemStacks[2]).stackSize < this.furnaceItemStacks[2].getMaxStackSize()) ? true : (((this.furnaceItemStacks[2]).stackSize < itemstack.getMaxStackSize())))));
  }
  
  public void smeltItem() {
    if (canSmelt()) {
      ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
      if (this.furnaceItemStacks[2] == null) {
        this.furnaceItemStacks[2] = itemstack.copy();
      } else if (this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
        (this.furnaceItemStacks[2]).stackSize++;
      } 
      if (this.furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.sponge) && this.furnaceItemStacks[0].getMetadata() == 1 && this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].getItem() == Items.bucket)
        this.furnaceItemStacks[1] = new ItemStack(Items.water_bucket); 
      (this.furnaceItemStacks[0]).stackSize--;
      if ((this.furnaceItemStacks[0]).stackSize <= 0)
        this.furnaceItemStacks[0] = null; 
    } 
  }
  
  public static int getItemBurnTime(ItemStack p_145952_0_) {
    if (p_145952_0_ == null)
      return 0; 
    Item item = p_145952_0_.getItem();
    if (item instanceof net.minecraft.item.ItemBlock && Block.getBlockFromItem(item) != Blocks.air) {
      Block block = Block.getBlockFromItem(item);
      if (block == Blocks.wooden_slab)
        return 150; 
      if (block.getMaterial() == Material.wood)
        return 300; 
      if (block == Blocks.coal_block)
        return 16000; 
    } 
    return (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) ? 200 : ((item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) ? 200 : ((item instanceof ItemHoe && ((ItemHoe)item).getMaterialName().equals("WOOD")) ? 200 : ((item == Items.stick) ? 100 : ((item == Items.coal) ? 1600 : ((item == Items.lava_bucket) ? 20000 : ((item == Item.getItemFromBlock(Blocks.sapling)) ? 100 : ((item == Items.blaze_rod) ? 2400 : 0)))))));
  }
  
  public static boolean isItemFuel(ItemStack p_145954_0_) {
    return (getItemBurnTime(p_145954_0_) > 0);
  }
  
  public boolean isUseableByPlayer(EntityPlayer player) {
    return (this.worldObj.getTileEntity(this.pos) != this) ? false : ((player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D));
  }
  
  public void openInventory(EntityPlayer player) {}
  
  public void closeInventory(EntityPlayer player) {}
  
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    return (index == 2) ? false : ((index != 1) ? true : ((isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack))));
  }
  
  public int[] getSlotsForFace(EnumFacing side) {
    return (side == EnumFacing.DOWN) ? slotsBottom : ((side == EnumFacing.UP) ? slotsTop : slotsSides);
  }
  
  public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
    return isItemValidForSlot(index, itemStackIn);
  }
  
  public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
    if (direction == EnumFacing.DOWN && index == 1) {
      Item item = stack.getItem();
      if (item != Items.water_bucket && item != Items.bucket)
        return false; 
    } 
    return true;
  }
  
  public String getGuiID() {
    return "minecraft:furnace";
  }
  
  public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
    return (Container)new ContainerFurnace(playerInventory, (IInventory)this);
  }
  
  public int getField(int id) {
    switch (id) {
      case 0:
        return this.furnaceBurnTime;
      case 1:
        return this.currentItemBurnTime;
      case 2:
        return this.cookTime;
      case 3:
        return this.totalCookTime;
    } 
    return 0;
  }
  
  public void setField(int id, int value) {
    switch (id) {
      case 0:
        this.furnaceBurnTime = value;
        break;
      case 1:
        this.currentItemBurnTime = value;
        break;
      case 2:
        this.cookTime = value;
        break;
      case 3:
        this.totalCookTime = value;
        break;
    } 
  }
  
  public int getFieldCount() {
    return 4;
  }
  
  public void clear() {
    for (int i = 0; i < this.furnaceItemStacks.length; i++)
      this.furnaceItemStacks[i] = null; 
  }
}
