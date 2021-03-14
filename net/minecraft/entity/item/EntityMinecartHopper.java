package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper {
  private boolean isBlocked = true;
  
  private int transferTicker = -1;
  
  private BlockPos field_174900_c = BlockPos.ORIGIN;
  
  public EntityMinecartHopper(World worldIn) {
    super(worldIn);
  }
  
  public EntityMinecartHopper(World worldIn, double x, double y, double z) {
    super(worldIn, x, y, z);
  }
  
  public EntityMinecart.EnumMinecartType getMinecartType() {
    return EntityMinecart.EnumMinecartType.HOPPER;
  }
  
  public IBlockState getDefaultDisplayTile() {
    return Blocks.hopper.getDefaultState();
  }
  
  public int getDefaultDisplayTileOffset() {
    return 1;
  }
  
  public int getSizeInventory() {
    return 5;
  }
  
  public boolean interactFirst(EntityPlayer playerIn) {
    if (!this.worldObj.isRemote)
      playerIn.displayGUIChest((IInventory)this); 
    return true;
  }
  
  public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
    boolean flag = !receivingPower;
    if (flag != getBlocked())
      setBlocked(flag); 
  }
  
  public boolean getBlocked() {
    return this.isBlocked;
  }
  
  public void setBlocked(boolean p_96110_1_) {
    this.isBlocked = p_96110_1_;
  }
  
  public World getWorld() {
    return this.worldObj;
  }
  
  public double getXPos() {
    return this.posX;
  }
  
  public double getYPos() {
    return this.posY + 0.5D;
  }
  
  public double getZPos() {
    return this.posZ;
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (!this.worldObj.isRemote && isEntityAlive() && getBlocked()) {
      BlockPos blockpos = new BlockPos(this);
      if (blockpos.equals(this.field_174900_c)) {
        this.transferTicker--;
      } else {
        setTransferTicker(0);
      } 
      if (!canTransfer()) {
        setTransferTicker(0);
        if (func_96112_aD()) {
          setTransferTicker(4);
          markDirty();
        } 
      } 
    } 
  }
  
  public boolean func_96112_aD() {
    if (TileEntityHopper.captureDroppedItems(this))
      return true; 
    List<EntityItem> list = this.worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(0.25D, 0.0D, 0.25D), EntitySelectors.selectAnything);
    if (list.size() > 0)
      TileEntityHopper.putDropInInventoryAllSlots((IInventory)this, list.get(0)); 
    return false;
  }
  
  public void killMinecart(DamageSource source) {
    super.killMinecart(source);
    if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops"))
      dropItemWithOffset(Item.getItemFromBlock((Block)Blocks.hopper), 1, 0.0F); 
  }
  
  protected void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("TransferCooldown", this.transferTicker);
  }
  
  protected void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    this.transferTicker = tagCompund.getInteger("TransferCooldown");
  }
  
  public void setTransferTicker(int p_98042_1_) {
    this.transferTicker = p_98042_1_;
  }
  
  public boolean canTransfer() {
    return (this.transferTicker > 0);
  }
  
  public String getGuiID() {
    return "minecraft:hopper";
  }
  
  public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
    return (Container)new ContainerHopper(playerInventory, (IInventory)this, playerIn);
  }
}
