package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBoat extends Entity {
  private boolean isBoatEmpty;
  
  private double speedMultiplier;
  
  private int boatPosRotationIncrements;
  
  private double boatX;
  
  private double boatY;
  
  private double boatZ;
  
  private double boatYaw;
  
  private double boatPitch;
  
  private double velocityX;
  
  private double velocityY;
  
  private double velocityZ;
  
  public EntityBoat(World worldIn) {
    super(worldIn);
    this.isBoatEmpty = true;
    this.speedMultiplier = 0.07D;
    this.preventEntitySpawning = true;
    setSize(1.5F, 0.6F);
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  protected void entityInit() {
    this.dataWatcher.addObject(17, new Integer(0));
    this.dataWatcher.addObject(18, new Integer(1));
    this.dataWatcher.addObject(19, new Float(0.0F));
  }
  
  public AxisAlignedBB getCollisionBox(Entity entityIn) {
    return entityIn.getEntityBoundingBox();
  }
  
  public AxisAlignedBB getCollisionBoundingBox() {
    return getEntityBoundingBox();
  }
  
  public boolean canBePushed() {
    return true;
  }
  
  public EntityBoat(World worldIn, double p_i1705_2_, double p_i1705_4_, double p_i1705_6_) {
    this(worldIn);
    setPosition(p_i1705_2_, p_i1705_4_, p_i1705_6_);
    this.motionX = 0.0D;
    this.motionY = 0.0D;
    this.motionZ = 0.0D;
    this.prevPosX = p_i1705_2_;
    this.prevPosY = p_i1705_4_;
    this.prevPosZ = p_i1705_6_;
  }
  
  public double getMountedYOffset() {
    return -0.3D;
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    if (!this.worldObj.isRemote && !this.isDead) {
      if (this.riddenByEntity != null && this.riddenByEntity == source.getEntity() && source instanceof net.minecraft.util.EntityDamageSourceIndirect)
        return false; 
      setForwardDirection(-getForwardDirection());
      setTimeSinceHit(10);
      setDamageTaken(getDamageTaken() + amount * 10.0F);
      setBeenAttacked();
      boolean flag = (source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode);
      if (flag || getDamageTaken() > 40.0F) {
        if (this.riddenByEntity != null)
          this.riddenByEntity.mountEntity(this); 
        if (!flag && this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops"))
          dropItemWithOffset(Items.boat, 1, 0.0F); 
        setDead();
      } 
      return true;
    } 
    return true;
  }
  
  public void performHurtAnimation() {
    setForwardDirection(-getForwardDirection());
    setTimeSinceHit(10);
    setDamageTaken(getDamageTaken() * 11.0F);
  }
  
  public boolean canBeCollidedWith() {
    return !this.isDead;
  }
  
  public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
    this.prevPosX = this.posX = x;
    this.prevPosY = this.posY = y;
    this.prevPosZ = this.posZ = z;
    this.rotationYaw = yaw;
    this.rotationPitch = pitch;
    this.boatPosRotationIncrements = 0;
    setPosition(x, y, z);
    this.motionX = this.velocityX = 0.0D;
    this.motionY = this.velocityY = 0.0D;
    this.motionZ = this.velocityZ = 0.0D;
    if (this.isBoatEmpty) {
      this.boatPosRotationIncrements = posRotationIncrements + 5;
    } else {
      double d0 = x - this.posX;
      double d1 = y - this.posY;
      double d2 = z - this.posZ;
      double d3 = d0 * d0 + d1 * d1 + d2 * d2;
      if (d3 <= 1.0D)
        return; 
      this.boatPosRotationIncrements = 3;
    } 
    this.boatX = x;
    this.boatY = y;
    this.boatZ = z;
    this.boatYaw = yaw;
    this.boatPitch = pitch;
    this.motionX = this.velocityX;
    this.motionY = this.velocityY;
    this.motionZ = this.velocityZ;
  }
  
  public void setVelocity(double x, double y, double z) {
    this.velocityX = this.motionX = x;
    this.velocityY = this.motionY = y;
    this.velocityZ = this.motionZ = z;
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (getTimeSinceHit() > 0)
      setTimeSinceHit(getTimeSinceHit() - 1); 
    if (getDamageTaken() > 0.0F)
      setDamageTaken(getDamageTaken() - 1.0F); 
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    int i = 5;
    double d0 = 0.0D;
    for (int j = 0; j < i; j++) {
      double d1 = (getEntityBoundingBox()).minY + ((getEntityBoundingBox()).maxY - (getEntityBoundingBox()).minY) * (j + 0) / i - 0.125D;
      double d3 = (getEntityBoundingBox()).minY + ((getEntityBoundingBox()).maxY - (getEntityBoundingBox()).minY) * (j + 1) / i - 0.125D;
      AxisAlignedBB axisalignedbb = new AxisAlignedBB((getEntityBoundingBox()).minX, d1, (getEntityBoundingBox()).minZ, (getEntityBoundingBox()).maxX, d3, (getEntityBoundingBox()).maxZ);
      if (this.worldObj.isAABBInMaterial(axisalignedbb, Material.water))
        d0 += 1.0D / i; 
    } 
    double d9 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    if (d9 > 0.2975D) {
      double d2 = Math.cos(this.rotationYaw * Math.PI / 180.0D);
      double d4 = Math.sin(this.rotationYaw * Math.PI / 180.0D);
      for (int k = 0; k < 1.0D + d9 * 60.0D; k++) {
        double d5 = (this.rand.nextFloat() * 2.0F - 1.0F);
        double d6 = (this.rand.nextInt(2) * 2 - 1) * 0.7D;
        if (this.rand.nextBoolean()) {
          double d7 = this.posX - d2 * d5 * 0.8D + d4 * d6;
          double d8 = this.posZ - d4 * d5 * 0.8D - d2 * d6;
          this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, d7, this.posY - 0.125D, d8, this.motionX, this.motionY, this.motionZ, new int[0]);
        } else {
          double d24 = this.posX + d2 + d4 * d5 * 0.7D;
          double d25 = this.posZ + d4 - d2 * d5 * 0.7D;
          this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, d24, this.posY - 0.125D, d25, this.motionX, this.motionY, this.motionZ, new int[0]);
        } 
      } 
    } 
    if (this.worldObj.isRemote && this.isBoatEmpty) {
      if (this.boatPosRotationIncrements > 0) {
        double d12 = this.posX + (this.boatX - this.posX) / this.boatPosRotationIncrements;
        double d16 = this.posY + (this.boatY - this.posY) / this.boatPosRotationIncrements;
        double d19 = this.posZ + (this.boatZ - this.posZ) / this.boatPosRotationIncrements;
        double d22 = MathHelper.wrapAngleTo180_double(this.boatYaw - this.rotationYaw);
        this.rotationYaw = (float)(this.rotationYaw + d22 / this.boatPosRotationIncrements);
        this.rotationPitch = (float)(this.rotationPitch + (this.boatPitch - this.rotationPitch) / this.boatPosRotationIncrements);
        this.boatPosRotationIncrements--;
        setPosition(d12, d16, d19);
        setRotation(this.rotationYaw, this.rotationPitch);
      } else {
        double d13 = this.posX + this.motionX;
        double d17 = this.posY + this.motionY;
        double d20 = this.posZ + this.motionZ;
        setPosition(d13, d17, d20);
        if (this.onGround) {
          this.motionX *= 0.5D;
          this.motionY *= 0.5D;
          this.motionZ *= 0.5D;
        } 
        this.motionX *= 0.9900000095367432D;
        this.motionY *= 0.949999988079071D;
        this.motionZ *= 0.9900000095367432D;
      } 
    } else {
      if (d0 < 1.0D) {
        double d10 = d0 * 2.0D - 1.0D;
        this.motionY += 0.03999999910593033D * d10;
      } else {
        if (this.motionY < 0.0D)
          this.motionY /= 2.0D; 
        this.motionY += 0.007000000216066837D;
      } 
      if (this.riddenByEntity instanceof EntityLivingBase) {
        EntityLivingBase entitylivingbase = (EntityLivingBase)this.riddenByEntity;
        float f = this.riddenByEntity.rotationYaw + -entitylivingbase.moveStrafing * 90.0F;
        this.motionX += -Math.sin((f * 3.1415927F / 180.0F)) * this.speedMultiplier * entitylivingbase.moveForward * 0.05000000074505806D;
        this.motionZ += Math.cos((f * 3.1415927F / 180.0F)) * this.speedMultiplier * entitylivingbase.moveForward * 0.05000000074505806D;
      } 
      double d11 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (d11 > 0.35D) {
        double d14 = 0.35D / d11;
        this.motionX *= d14;
        this.motionZ *= d14;
        d11 = 0.35D;
      } 
      if (d11 > d9 && this.speedMultiplier < 0.35D) {
        this.speedMultiplier += (0.35D - this.speedMultiplier) / 35.0D;
        if (this.speedMultiplier > 0.35D)
          this.speedMultiplier = 0.35D; 
      } else {
        this.speedMultiplier -= (this.speedMultiplier - 0.07D) / 35.0D;
        if (this.speedMultiplier < 0.07D)
          this.speedMultiplier = 0.07D; 
      } 
      for (int i1 = 0; i1 < 4; i1++) {
        int l1 = MathHelper.floor_double(this.posX + ((i1 % 2) - 0.5D) * 0.8D);
        int i2 = MathHelper.floor_double(this.posZ + ((i1 / 2) - 0.5D) * 0.8D);
        for (int j2 = 0; j2 < 2; j2++) {
          int l = MathHelper.floor_double(this.posY) + j2;
          BlockPos blockpos = new BlockPos(l1, l, i2);
          Block block = this.worldObj.getBlockState(blockpos).getBlock();
          if (block == Blocks.snow_layer) {
            this.worldObj.setBlockToAir(blockpos);
            this.isCollidedHorizontally = false;
          } else if (block == Blocks.waterlily) {
            this.worldObj.destroyBlock(blockpos, true);
            this.isCollidedHorizontally = false;
          } 
        } 
      } 
      if (this.onGround) {
        this.motionX *= 0.5D;
        this.motionY *= 0.5D;
        this.motionZ *= 0.5D;
      } 
      moveEntity(this.motionX, this.motionY, this.motionZ);
      if (this.isCollidedHorizontally && d9 > 0.2975D) {
        if (!this.worldObj.isRemote && !this.isDead) {
          setDead();
          if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
            for (int j1 = 0; j1 < 3; j1++)
              dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F); 
            for (int k1 = 0; k1 < 2; k1++)
              dropItemWithOffset(Items.stick, 1, 0.0F); 
          } 
        } 
      } else {
        this.motionX *= 0.9900000095367432D;
        this.motionY *= 0.949999988079071D;
        this.motionZ *= 0.9900000095367432D;
      } 
      this.rotationPitch = 0.0F;
      double d15 = this.rotationYaw;
      double d18 = this.prevPosX - this.posX;
      double d21 = this.prevPosZ - this.posZ;
      if (d18 * d18 + d21 * d21 > 0.001D)
        d15 = (float)(MathHelper.atan2(d21, d18) * 180.0D / Math.PI); 
      double d23 = MathHelper.wrapAngleTo180_double(d15 - this.rotationYaw);
      if (d23 > 20.0D)
        d23 = 20.0D; 
      if (d23 < -20.0D)
        d23 = -20.0D; 
      this.rotationYaw = (float)(this.rotationYaw + d23);
      setRotation(this.rotationYaw, this.rotationPitch);
      if (!this.worldObj.isRemote) {
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        if (list != null && !list.isEmpty())
          for (int k2 = 0; k2 < list.size(); k2++) {
            Entity entity = list.get(k2);
            if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityBoat)
              entity.applyEntityCollision(this); 
          }  
        if (this.riddenByEntity != null && this.riddenByEntity.isDead)
          this.riddenByEntity = null; 
      } 
    } 
  }
  
  public void updateRiderPosition() {
    if (this.riddenByEntity != null) {
      double d0 = Math.cos(this.rotationYaw * Math.PI / 180.0D) * 0.4D;
      double d1 = Math.sin(this.rotationYaw * Math.PI / 180.0D) * 0.4D;
      this.riddenByEntity.setPosition(this.posX + d0, this.posY + getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + d1);
    } 
  }
  
  protected void writeEntityToNBT(NBTTagCompound tagCompound) {}
  
  protected void readEntityFromNBT(NBTTagCompound tagCompund) {}
  
  public boolean interactFirst(EntityPlayer playerIn) {
    if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != playerIn)
      return true; 
    if (!this.worldObj.isRemote)
      playerIn.mountEntity(this); 
    return true;
  }
  
  protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
    if (onGroundIn) {
      if (this.fallDistance > 3.0F) {
        fall(this.fallDistance, 1.0F);
        if (!this.worldObj.isRemote && !this.isDead) {
          setDead();
          if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
            for (int i = 0; i < 3; i++)
              dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F); 
            for (int j = 0; j < 2; j++)
              dropItemWithOffset(Items.stick, 1, 0.0F); 
          } 
        } 
        this.fallDistance = 0.0F;
      } 
    } else if (this.worldObj.getBlockState((new BlockPos(this)).down()).getBlock().getMaterial() != Material.water && y < 0.0D) {
      this.fallDistance = (float)(this.fallDistance - y);
    } 
  }
  
  public void setDamageTaken(float p_70266_1_) {
    this.dataWatcher.updateObject(19, Float.valueOf(p_70266_1_));
  }
  
  public float getDamageTaken() {
    return this.dataWatcher.getWatchableObjectFloat(19);
  }
  
  public void setTimeSinceHit(int p_70265_1_) {
    this.dataWatcher.updateObject(17, Integer.valueOf(p_70265_1_));
  }
  
  public int getTimeSinceHit() {
    return this.dataWatcher.getWatchableObjectInt(17);
  }
  
  public void setForwardDirection(int p_70269_1_) {
    this.dataWatcher.updateObject(18, Integer.valueOf(p_70269_1_));
  }
  
  public int getForwardDirection() {
    return this.dataWatcher.getWatchableObjectInt(18);
  }
  
  public void setIsBoatEmpty(boolean p_70270_1_) {
    this.isBoatEmpty = p_70270_1_;
  }
}
