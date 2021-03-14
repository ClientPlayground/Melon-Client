package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityFireball extends Entity {
  private int xTile = -1;
  
  private int yTile = -1;
  
  private int zTile = -1;
  
  private Block inTile;
  
  private boolean inGround;
  
  public EntityLivingBase shootingEntity;
  
  private int ticksAlive;
  
  private int ticksInAir;
  
  public double accelerationX;
  
  public double accelerationY;
  
  public double accelerationZ;
  
  public EntityFireball(World worldIn) {
    super(worldIn);
    setSize(1.0F, 1.0F);
  }
  
  protected void entityInit() {}
  
  public boolean isInRangeToRenderDist(double distance) {
    double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
    if (Double.isNaN(d0))
      d0 = 4.0D; 
    d0 *= 64.0D;
    return (distance < d0 * d0);
  }
  
  public EntityFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
    super(worldIn);
    setSize(1.0F, 1.0F);
    setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
    setPosition(x, y, z);
    double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
    this.accelerationX = accelX / d0 * 0.1D;
    this.accelerationY = accelY / d0 * 0.1D;
    this.accelerationZ = accelZ / d0 * 0.1D;
  }
  
  public EntityFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
    super(worldIn);
    this.shootingEntity = shooter;
    setSize(1.0F, 1.0F);
    setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
    setPosition(this.posX, this.posY, this.posZ);
    this.motionX = this.motionY = this.motionZ = 0.0D;
    accelX += this.rand.nextGaussian() * 0.4D;
    accelY += this.rand.nextGaussian() * 0.4D;
    accelZ += this.rand.nextGaussian() * 0.4D;
    double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
    this.accelerationX = accelX / d0 * 0.1D;
    this.accelerationY = accelY / d0 * 0.1D;
    this.accelerationZ = accelZ / d0 * 0.1D;
  }
  
  public void onUpdate() {
    if (this.worldObj.isRemote || ((this.shootingEntity == null || !this.shootingEntity.isDead) && this.worldObj.isBlockLoaded(new BlockPos(this)))) {
      super.onUpdate();
      setFire(1);
      if (this.inGround) {
        if (this.worldObj.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
          this.ticksAlive++;
          if (this.ticksAlive == 600)
            setDead(); 
          return;
        } 
        this.inGround = false;
        this.motionX *= (this.rand.nextFloat() * 0.2F);
        this.motionY *= (this.rand.nextFloat() * 0.2F);
        this.motionZ *= (this.rand.nextFloat() * 0.2F);
        this.ticksAlive = 0;
        this.ticksInAir = 0;
      } else {
        this.ticksInAir++;
      } 
      Vec3 vec3 = new Vec3(this.posX, this.posY, this.posZ);
      Vec3 vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
      vec3 = new Vec3(this.posX, this.posY, this.posZ);
      vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (movingobjectposition != null)
        vec31 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord); 
      Entity entity = null;
      List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
      double d0 = 0.0D;
      for (int i = 0; i < list.size(); i++) {
        Entity entity1 = list.get(i);
        if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual((Entity)this.shootingEntity) || this.ticksInAir >= 25)) {
          float f = 0.3F;
          AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
          MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);
          if (movingobjectposition1 != null) {
            double d1 = vec3.squareDistanceTo(movingobjectposition1.hitVec);
            if (d1 < d0 || d0 == 0.0D) {
              entity = entity1;
              d0 = d1;
            } 
          } 
        } 
      } 
      if (entity != null)
        movingobjectposition = new MovingObjectPosition(entity); 
      if (movingobjectposition != null)
        onImpact(movingobjectposition); 
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;
      for (this.rotationPitch = (float)(MathHelper.atan2(f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
      while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        this.prevRotationPitch += 360.0F; 
      while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        this.prevRotationYaw -= 360.0F; 
      while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        this.prevRotationYaw += 360.0F; 
      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      float f2 = getMotionFactor();
      if (isInWater()) {
        for (int j = 0; j < 4; j++) {
          float f3 = 0.25F;
          this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f3, this.posY - this.motionY * f3, this.posZ - this.motionZ * f3, this.motionX, this.motionY, this.motionZ, new int[0]);
        } 
        f2 = 0.8F;
      } 
      this.motionX += this.accelerationX;
      this.motionY += this.accelerationY;
      this.motionZ += this.accelerationZ;
      this.motionX *= f2;
      this.motionY *= f2;
      this.motionZ *= f2;
      this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
      setPosition(this.posX, this.posY, this.posZ);
    } else {
      setDead();
    } 
  }
  
  protected float getMotionFactor() {
    return 0.95F;
  }
  
  protected abstract void onImpact(MovingObjectPosition paramMovingObjectPosition);
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    tagCompound.setShort("xTile", (short)this.xTile);
    tagCompound.setShort("yTile", (short)this.yTile);
    tagCompound.setShort("zTile", (short)this.zTile);
    ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.inTile);
    tagCompound.setString("inTile", (resourcelocation == null) ? "" : resourcelocation.toString());
    tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    tagCompound.setTag("direction", (NBTBase)newDoubleNBTList(new double[] { this.motionX, this.motionY, this.motionZ }));
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    this.xTile = tagCompund.getShort("xTile");
    this.yTile = tagCompund.getShort("yTile");
    this.zTile = tagCompund.getShort("zTile");
    if (tagCompund.hasKey("inTile", 8)) {
      this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
    } else {
      this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 0xFF);
    } 
    this.inGround = (tagCompund.getByte("inGround") == 1);
    if (tagCompund.hasKey("direction", 9)) {
      NBTTagList nbttaglist = tagCompund.getTagList("direction", 6);
      this.motionX = nbttaglist.getDoubleAt(0);
      this.motionY = nbttaglist.getDoubleAt(1);
      this.motionZ = nbttaglist.getDoubleAt(2);
    } else {
      setDead();
    } 
  }
  
  public boolean canBeCollidedWith() {
    return true;
  }
  
  public float getCollisionBorderSize() {
    return 1.0F;
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    setBeenAttacked();
    if (source.getEntity() != null) {
      Vec3 vec3 = source.getEntity().getLookVec();
      if (vec3 != null) {
        this.motionX = vec3.xCoord;
        this.motionY = vec3.yCoord;
        this.motionZ = vec3.zCoord;
        this.accelerationX = this.motionX * 0.1D;
        this.accelerationY = this.motionY * 0.1D;
        this.accelerationZ = this.motionZ * 0.1D;
      } 
      if (source.getEntity() instanceof EntityLivingBase)
        this.shootingEntity = (EntityLivingBase)source.getEntity(); 
      return true;
    } 
    return false;
  }
  
  public float getBrightness(float partialTicks) {
    return 1.0F;
  }
  
  public int getBrightnessForRender(float partialTicks) {
    return 15728880;
  }
}
