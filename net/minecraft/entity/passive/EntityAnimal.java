package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAnimal extends EntityAgeable implements IAnimals {
  protected Block spawnableBlock = (Block)Blocks.grass;
  
  private int inLove;
  
  private EntityPlayer playerInLove;
  
  public EntityAnimal(World worldIn) {
    super(worldIn);
  }
  
  protected void updateAITasks() {
    if (getGrowingAge() != 0)
      this.inLove = 0; 
    super.updateAITasks();
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (getGrowingAge() != 0)
      this.inLove = 0; 
    if (this.inLove > 0) {
      this.inLove--;
      if (this.inLove % 10 == 0) {
        double d0 = this.rand.nextGaussian() * 0.02D;
        double d1 = this.rand.nextGaussian() * 0.02D;
        double d2 = this.rand.nextGaussian() * 0.02D;
        this.worldObj.spawnParticle(EnumParticleTypes.HEART, this.posX + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.posY + 0.5D + (this.rand.nextFloat() * this.height), this.posZ + (this.rand.nextFloat() * this.width * 2.0F) - this.width, d0, d1, d2, new int[0]);
      } 
    } 
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    this.inLove = 0;
    return super.attackEntityFrom(source, amount);
  }
  
  public float getBlockPathWeight(BlockPos pos) {
    return (this.worldObj.getBlockState(pos.down()).getBlock() == Blocks.grass) ? 10.0F : (this.worldObj.getLightBrightness(pos) - 0.5F);
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("InLove", this.inLove);
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    this.inLove = tagCompund.getInteger("InLove");
  }
  
  public boolean getCanSpawnHere() {
    int i = MathHelper.floor_double(this.posX);
    int j = MathHelper.floor_double((getEntityBoundingBox()).minY);
    int k = MathHelper.floor_double(this.posZ);
    BlockPos blockpos = new BlockPos(i, j, k);
    return (this.worldObj.getBlockState(blockpos.down()).getBlock() == this.spawnableBlock && this.worldObj.getLight(blockpos) > 8 && super.getCanSpawnHere());
  }
  
  public int getTalkInterval() {
    return 120;
  }
  
  protected boolean canDespawn() {
    return false;
  }
  
  protected int getExperiencePoints(EntityPlayer player) {
    return 1 + this.worldObj.rand.nextInt(3);
  }
  
  public boolean isBreedingItem(ItemStack stack) {
    return (stack == null) ? false : ((stack.getItem() == Items.wheat));
  }
  
  public boolean interact(EntityPlayer player) {
    ItemStack itemstack = player.inventory.getCurrentItem();
    if (itemstack != null) {
      if (isBreedingItem(itemstack) && getGrowingAge() == 0 && this.inLove <= 0) {
        consumeItemFromStack(player, itemstack);
        setInLove(player);
        return true;
      } 
      if (isChild() && isBreedingItem(itemstack)) {
        consumeItemFromStack(player, itemstack);
        func_175501_a((int)((-getGrowingAge() / 20) * 0.1F), true);
        return true;
      } 
    } 
    return super.interact(player);
  }
  
  protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
    if (!player.capabilities.isCreativeMode) {
      stack.stackSize--;
      if (stack.stackSize <= 0)
        player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null); 
    } 
  }
  
  public void setInLove(EntityPlayer player) {
    this.inLove = 600;
    this.playerInLove = player;
    this.worldObj.setEntityState((Entity)this, (byte)18);
  }
  
  public EntityPlayer getPlayerInLove() {
    return this.playerInLove;
  }
  
  public boolean isInLove() {
    return (this.inLove > 0);
  }
  
  public void resetInLove() {
    this.inLove = 0;
  }
  
  public boolean canMateWith(EntityAnimal otherAnimal) {
    return (otherAnimal == this) ? false : ((otherAnimal.getClass() != getClass()) ? false : ((isInLove() && otherAnimal.isInLove())));
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 18) {
      for (int i = 0; i < 7; i++) {
        double d0 = this.rand.nextGaussian() * 0.02D;
        double d1 = this.rand.nextGaussian() * 0.02D;
        double d2 = this.rand.nextGaussian() * 0.02D;
        this.worldObj.spawnParticle(EnumParticleTypes.HEART, this.posX + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.posY + 0.5D + (this.rand.nextFloat() * this.height), this.posZ + (this.rand.nextFloat() * this.width * 2.0F) - this.width, d0, d1, d2, new int[0]);
      } 
    } else {
      super.handleHealthUpdate(id);
    } 
  }
}
