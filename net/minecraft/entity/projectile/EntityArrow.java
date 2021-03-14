package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EntityArrow extends Entity implements IProjectile {
  private int xTile = -1;
  
  private int yTile = -1;
  
  private int zTile = -1;
  
  private Block inTile;
  
  private int inData;
  
  private boolean inGround;
  
  public int canBePickedUp;
  
  public int arrowShake;
  
  public Entity shootingEntity;
  
  private int ticksInGround;
  
  private int ticksInAir;
  
  private double damage = 2.0D;
  
  private int knockbackStrength;
  
  public EntityArrow(World worldIn) {
    super(worldIn);
    this.renderDistanceWeight = 10.0D;
    setSize(0.5F, 0.5F);
  }
  
  public EntityArrow(World worldIn, double x, double y, double z) {
    super(worldIn);
    this.renderDistanceWeight = 10.0D;
    setSize(0.5F, 0.5F);
    setPosition(x, y, z);
  }
  
  public EntityArrow(World worldIn, EntityLivingBase shooter, EntityLivingBase target, float velocity, float innacuracy) {
    super(worldIn);
    this.renderDistanceWeight = 10.0D;
    this.shootingEntity = (Entity)shooter;
    if (shooter instanceof EntityPlayer)
      this.canBePickedUp = 1; 
    this.posY = shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D;
    double d0 = target.posX - shooter.posX;
    double d1 = (target.getEntityBoundingBox()).minY + (target.height / 3.0F) - this.posY;
    double d2 = target.posZ - shooter.posZ;
    double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);
    if (d3 >= 1.0E-7D) {
      float f = (float)(MathHelper.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
      float f1 = (float)-(MathHelper.atan2(d1, d3) * 180.0D / Math.PI);
      double d4 = d0 / d3;
      double d5 = d2 / d3;
      setLocationAndAngles(shooter.posX + d4, this.posY, shooter.posZ + d5, f, f1);
      float f2 = (float)(d3 * 0.20000000298023224D);
      setThrowableHeading(d0, d1 + f2, d2, velocity, innacuracy);
    } 
  }
  
  public EntityArrow(World worldIn, EntityLivingBase shooter, float velocity) {
    super(worldIn);
    this.renderDistanceWeight = 10.0D;
    this.shootingEntity = (Entity)shooter;
    if (shooter instanceof EntityPlayer)
      this.canBePickedUp = 1; 
    setSize(0.5F, 0.5F);
    setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
    this.posX -= (MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
    this.posY -= 0.10000000149011612D;
    this.posZ -= (MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
    setPosition(this.posX, this.posY, this.posZ);
    this.motionX = (-MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F));
    this.motionZ = (MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F));
    this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F);
    setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity * 1.5F, 1.0F);
  }
  
  protected void entityInit() {
    this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
  }
  
  public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
    float f = MathHelper.sqrt_double(x * x + y * y + z * z);
    x /= f;
    y /= f;
    z /= f;
    x += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : true) * 0.007499999832361937D * inaccuracy;
    y += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : true) * 0.007499999832361937D * inaccuracy;
    z += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : true) * 0.007499999832361937D * inaccuracy;
    x *= velocity;
    y *= velocity;
    z *= velocity;
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;
    float f1 = MathHelper.sqrt_double(x * x + z * z);
    this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(x, z) * 180.0D / Math.PI);
    this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(y, f1) * 180.0D / Math.PI);
    this.ticksInGround = 0;
  }
  
  public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
    setPosition(x, y, z);
    setRotation(yaw, pitch);
  }
  
  public void setVelocity(double x, double y, double z) {
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;
    if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
      float f = MathHelper.sqrt_double(x * x + z * z);
      this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(x, z) * 180.0D / Math.PI);
      this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(y, f) * 180.0D / Math.PI);
      this.prevRotationPitch = this.rotationPitch;
      this.prevRotationYaw = this.rotationYaw;
      setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      this.ticksInGround = 0;
    } 
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
      float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
      this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f) * 180.0D / Math.PI);
    } 
    BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
    IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
    Block block = iblockstate.getBlock();
    if (block.getMaterial() != Material.air) {
      block.setBlockBoundsBasedOnState((IBlockAccess)this.worldObj, blockpos);
      AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(this.worldObj, blockpos, iblockstate);
      if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(this.posX, this.posY, this.posZ)))
        this.inGround = true; 
    } 
    if (this.arrowShake > 0)
      this.arrowShake--; 
    if (this.inGround) {
      int j = block.getMetaFromState(iblockstate);
      if (block == this.inTile && j == this.inData) {
        this.ticksInGround++;
        if (this.ticksInGround >= 1200)
          setDead(); 
      } else {
        this.inGround = false;
        this.motionX *= (this.rand.nextFloat() * 0.2F);
        this.motionY *= (this.rand.nextFloat() * 0.2F);
        this.motionZ *= (this.rand.nextFloat() * 0.2F);
        this.ticksInGround = 0;
        this.ticksInAir = 0;
      } 
    } else {
      this.ticksInAir++;
      Vec3 vec31 = new Vec3(this.posX, this.posY, this.posZ);
      Vec3 vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3, false, true, false);
      vec31 = new Vec3(this.posX, this.posY, this.posZ);
      vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (movingobjectposition != null)
        vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord); 
      Entity entity = null;
      List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
      double d0 = 0.0D;
      for (int i = 0; i < list.size(); i++) {
        Entity entity1 = list.get(i);
        if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5)) {
          float f1 = 0.3F;
          AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand(f1, f1, f1);
          MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);
          if (movingobjectposition1 != null) {
            double d1 = vec31.squareDistanceTo(movingobjectposition1.hitVec);
            if (d1 < d0 || d0 == 0.0D) {
              entity = entity1;
              d0 = d1;
            } 
          } 
        } 
      } 
      if (entity != null)
        movingobjectposition = new MovingObjectPosition(entity); 
      if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer) {
        EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;
        if (entityplayer.capabilities.disableDamage || (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityplayer)))
          movingobjectposition = null; 
      } 
      if (movingobjectposition != null)
        if (movingobjectposition.entityHit != null) {
          DamageSource damagesource;
          float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
          int l = MathHelper.ceiling_double_int(f2 * this.damage);
          if (getIsCritical())
            l += this.rand.nextInt(l / 2 + 2); 
          if (this.shootingEntity == null) {
            damagesource = DamageSource.causeArrowDamage(this, this);
          } else {
            damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);
          } 
          if (isBurning() && !(movingobjectposition.entityHit instanceof net.minecraft.entity.monster.EntityEnderman))
            movingobjectposition.entityHit.setFire(5); 
          if (movingobjectposition.entityHit.attackEntityFrom(damagesource, l)) {
            if (movingobjectposition.entityHit instanceof EntityLivingBase) {
              EntityLivingBase entitylivingbase = (EntityLivingBase)movingobjectposition.entityHit;
              if (!this.worldObj.isRemote)
                entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1); 
              if (this.knockbackStrength > 0) {
                float f7 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                if (f7 > 0.0F)
                  movingobjectposition.entityHit.addVelocity(this.motionX * this.knockbackStrength * 0.6000000238418579D / f7, 0.1D, this.motionZ * this.knockbackStrength * 0.6000000238418579D / f7); 
              } 
              if (this.shootingEntity instanceof EntityLivingBase) {
                EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, (Entity)entitylivingbase);
              } 
              if (this.shootingEntity != null && movingobjectposition.entityHit != this.shootingEntity && movingobjectposition.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP)
                ((EntityPlayerMP)this.shootingEntity).playerNetServerHandler.sendPacket((Packet)new S2BPacketChangeGameState(6, 0.0F)); 
            } 
            playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (!(movingobjectposition.entityHit instanceof net.minecraft.entity.monster.EntityEnderman))
              setDead(); 
          } else {
            this.motionX *= -0.10000000149011612D;
            this.motionY *= -0.10000000149011612D;
            this.motionZ *= -0.10000000149011612D;
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            this.ticksInAir = 0;
          } 
        } else {
          BlockPos blockpos1 = movingobjectposition.getBlockPos();
          this.xTile = blockpos1.getX();
          this.yTile = blockpos1.getY();
          this.zTile = blockpos1.getZ();
          IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
          this.inTile = iblockstate1.getBlock();
          this.inData = this.inTile.getMetaFromState(iblockstate1);
          this.motionX = (float)(movingobjectposition.hitVec.xCoord - this.posX);
          this.motionY = (float)(movingobjectposition.hitVec.yCoord - this.posY);
          this.motionZ = (float)(movingobjectposition.hitVec.zCoord - this.posZ);
          float f5 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
          this.posX -= this.motionX / f5 * 0.05000000074505806D;
          this.posY -= this.motionY / f5 * 0.05000000074505806D;
          this.posZ -= this.motionZ / f5 * 0.05000000074505806D;
          playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
          this.inGround = true;
          this.arrowShake = 7;
          setIsCritical(false);
          if (this.inTile.getMaterial() != Material.air)
            this.inTile.onEntityCollidedWithBlock(this.worldObj, blockpos1, iblockstate1, this); 
        }  
      if (getIsCritical())
        for (int k = 0; k < 4; k++)
          this.worldObj.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * k / 4.0D, this.posY + this.motionY * k / 4.0D, this.posZ + this.motionZ * k / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ, new int[0]);  
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float f3 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
      for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f3) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
      while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        this.prevRotationPitch += 360.0F; 
      while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        this.prevRotationYaw -= 360.0F; 
      while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        this.prevRotationYaw += 360.0F; 
      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      float f4 = 0.99F;
      float f6 = 0.05F;
      if (isInWater()) {
        for (int i1 = 0; i1 < 4; i1++) {
          float f8 = 0.25F;
          this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f8, this.posY - this.motionY * f8, this.posZ - this.motionZ * f8, this.motionX, this.motionY, this.motionZ, new int[0]);
        } 
        f4 = 0.6F;
      } 
      if (isWet())
        extinguish(); 
      this.motionX *= f4;
      this.motionY *= f4;
      this.motionZ *= f4;
      this.motionY -= f6;
      setPosition(this.posX, this.posY, this.posZ);
      doBlockCollisions();
    } 
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    tagCompound.setShort("xTile", (short)this.xTile);
    tagCompound.setShort("yTile", (short)this.yTile);
    tagCompound.setShort("zTile", (short)this.zTile);
    tagCompound.setShort("life", (short)this.ticksInGround);
    ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.inTile);
    tagCompound.setString("inTile", (resourcelocation == null) ? "" : resourcelocation.toString());
    tagCompound.setByte("inData", (byte)this.inData);
    tagCompound.setByte("shake", (byte)this.arrowShake);
    tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    tagCompound.setByte("pickup", (byte)this.canBePickedUp);
    tagCompound.setDouble("damage", this.damage);
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    this.xTile = tagCompund.getShort("xTile");
    this.yTile = tagCompund.getShort("yTile");
    this.zTile = tagCompund.getShort("zTile");
    this.ticksInGround = tagCompund.getShort("life");
    if (tagCompund.hasKey("inTile", 8)) {
      this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
    } else {
      this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 0xFF);
    } 
    this.inData = tagCompund.getByte("inData") & 0xFF;
    this.arrowShake = tagCompund.getByte("shake") & 0xFF;
    this.inGround = (tagCompund.getByte("inGround") == 1);
    if (tagCompund.hasKey("damage", 99))
      this.damage = tagCompund.getDouble("damage"); 
    if (tagCompund.hasKey("pickup", 99)) {
      this.canBePickedUp = tagCompund.getByte("pickup");
    } else if (tagCompund.hasKey("player", 99)) {
      this.canBePickedUp = tagCompund.getBoolean("player") ? 1 : 0;
    } 
  }
  
  public void onCollideWithPlayer(EntityPlayer entityIn) {
    if (!this.worldObj.isRemote && this.inGround && this.arrowShake <= 0) {
      boolean flag = (this.canBePickedUp == 1 || (this.canBePickedUp == 2 && entityIn.capabilities.isCreativeMode));
      if (this.canBePickedUp == 1 && !entityIn.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1)))
        flag = false; 
      if (flag) {
        playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        entityIn.onItemPickup(this, 1);
        setDead();
      } 
    } 
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  public void setDamage(double damageIn) {
    this.damage = damageIn;
  }
  
  public double getDamage() {
    return this.damage;
  }
  
  public void setKnockbackStrength(int knockbackStrengthIn) {
    this.knockbackStrength = knockbackStrengthIn;
  }
  
  public boolean canAttackWithItem() {
    return false;
  }
  
  public float getEyeHeight() {
    return 0.0F;
  }
  
  public void setIsCritical(boolean critical) {
    byte b0 = this.dataWatcher.getWatchableObjectByte(16);
    if (critical) {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 0x1)));
    } else {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 0xFFFFFFFE)));
    } 
  }
  
  public boolean getIsCritical() {
    byte b0 = this.dataWatcher.getWatchableObjectByte(16);
    return ((b0 & 0x1) != 0);
  }
}
