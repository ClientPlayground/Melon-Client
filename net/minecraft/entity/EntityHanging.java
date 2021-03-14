package net.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

public abstract class EntityHanging extends Entity {
  private int tickCounter1;
  
  protected BlockPos hangingPosition;
  
  public EnumFacing facingDirection;
  
  public EntityHanging(World worldIn) {
    super(worldIn);
    setSize(0.5F, 0.5F);
  }
  
  public EntityHanging(World worldIn, BlockPos hangingPositionIn) {
    this(worldIn);
    this.hangingPosition = hangingPositionIn;
  }
  
  protected void entityInit() {}
  
  protected void updateFacingWithBoundingBox(EnumFacing facingDirectionIn) {
    Validate.notNull(facingDirectionIn);
    Validate.isTrue(facingDirectionIn.getAxis().isHorizontal());
    this.facingDirection = facingDirectionIn;
    this.prevRotationYaw = this.rotationYaw = (this.facingDirection.getHorizontalIndex() * 90);
    updateBoundingBox();
  }
  
  private void updateBoundingBox() {
    if (this.facingDirection != null) {
      double d0 = this.hangingPosition.getX() + 0.5D;
      double d1 = this.hangingPosition.getY() + 0.5D;
      double d2 = this.hangingPosition.getZ() + 0.5D;
      double d3 = 0.46875D;
      double d4 = func_174858_a(getWidthPixels());
      double d5 = func_174858_a(getHeightPixels());
      d0 -= this.facingDirection.getFrontOffsetX() * 0.46875D;
      d2 -= this.facingDirection.getFrontOffsetZ() * 0.46875D;
      d1 += d5;
      EnumFacing enumfacing = this.facingDirection.rotateYCCW();
      d0 += d4 * enumfacing.getFrontOffsetX();
      d2 += d4 * enumfacing.getFrontOffsetZ();
      this.posX = d0;
      this.posY = d1;
      this.posZ = d2;
      double d6 = getWidthPixels();
      double d7 = getHeightPixels();
      double d8 = getWidthPixels();
      if (this.facingDirection.getAxis() == EnumFacing.Axis.Z) {
        d8 = 1.0D;
      } else {
        d6 = 1.0D;
      } 
      d6 /= 32.0D;
      d7 /= 32.0D;
      d8 /= 32.0D;
      setEntityBoundingBox(new AxisAlignedBB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
    } 
  }
  
  private double func_174858_a(int p_174858_1_) {
    return (p_174858_1_ % 32 == 0) ? 0.5D : 0.0D;
  }
  
  public void onUpdate() {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    if (this.tickCounter1++ == 100 && !this.worldObj.isRemote) {
      this.tickCounter1 = 0;
      if (!this.isDead && !onValidSurface()) {
        setDead();
        onBroken((Entity)null);
      } 
    } 
  }
  
  public boolean onValidSurface() {
    if (!this.worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty())
      return false; 
    int i = Math.max(1, getWidthPixels() / 16);
    int j = Math.max(1, getHeightPixels() / 16);
    BlockPos blockpos = this.hangingPosition.offset(this.facingDirection.getOpposite());
    EnumFacing enumfacing = this.facingDirection.rotateYCCW();
    for (int k = 0; k < i; k++) {
      for (int l = 0; l < j; l++) {
        BlockPos blockpos1 = blockpos.offset(enumfacing, k).up(l);
        Block block = this.worldObj.getBlockState(blockpos1).getBlock();
        if (!block.getMaterial().isSolid() && !BlockRedstoneDiode.isRedstoneRepeaterBlockID(block))
          return false; 
      } 
    } 
    for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox())) {
      if (entity instanceof EntityHanging)
        return false; 
    } 
    return true;
  }
  
  public boolean canBeCollidedWith() {
    return true;
  }
  
  public boolean hitByEntity(Entity entityIn) {
    return (entityIn instanceof EntityPlayer) ? attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entityIn), 0.0F) : false;
  }
  
  public EnumFacing getHorizontalFacing() {
    return this.facingDirection;
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    if (!this.isDead && !this.worldObj.isRemote) {
      setDead();
      setBeenAttacked();
      onBroken(source.getEntity());
    } 
    return true;
  }
  
  public void moveEntity(double x, double y, double z) {
    if (!this.worldObj.isRemote && !this.isDead && x * x + y * y + z * z > 0.0D) {
      setDead();
      onBroken((Entity)null);
    } 
  }
  
  public void addVelocity(double x, double y, double z) {
    if (!this.worldObj.isRemote && !this.isDead && x * x + y * y + z * z > 0.0D) {
      setDead();
      onBroken((Entity)null);
    } 
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    tagCompound.setByte("Facing", (byte)this.facingDirection.getHorizontalIndex());
    tagCompound.setInteger("TileX", getHangingPosition().getX());
    tagCompound.setInteger("TileY", getHangingPosition().getY());
    tagCompound.setInteger("TileZ", getHangingPosition().getZ());
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    EnumFacing enumfacing;
    this.hangingPosition = new BlockPos(tagCompund.getInteger("TileX"), tagCompund.getInteger("TileY"), tagCompund.getInteger("TileZ"));
    if (tagCompund.hasKey("Direction", 99)) {
      enumfacing = EnumFacing.getHorizontal(tagCompund.getByte("Direction"));
      this.hangingPosition = this.hangingPosition.offset(enumfacing);
    } else if (tagCompund.hasKey("Facing", 99)) {
      enumfacing = EnumFacing.getHorizontal(tagCompund.getByte("Facing"));
    } else {
      enumfacing = EnumFacing.getHorizontal(tagCompund.getByte("Dir"));
    } 
    updateFacingWithBoundingBox(enumfacing);
  }
  
  public abstract int getWidthPixels();
  
  public abstract int getHeightPixels();
  
  public abstract void onBroken(Entity paramEntity);
  
  protected boolean shouldSetPosAfterLoading() {
    return false;
  }
  
  public void setPosition(double x, double y, double z) {
    this.posX = x;
    this.posY = y;
    this.posZ = z;
    BlockPos blockpos = this.hangingPosition;
    this.hangingPosition = new BlockPos(x, y, z);
    if (!this.hangingPosition.equals(blockpos)) {
      updateBoundingBox();
      this.isAirBorne = true;
    } 
  }
  
  public BlockPos getHangingPosition() {
    return this.hangingPosition;
  }
}
