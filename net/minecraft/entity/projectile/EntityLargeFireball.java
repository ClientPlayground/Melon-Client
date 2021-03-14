package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityLargeFireball extends EntityFireball {
  public int explosionPower = 1;
  
  public EntityLargeFireball(World worldIn) {
    super(worldIn);
  }
  
  public EntityLargeFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
    super(worldIn, x, y, z, accelX, accelY, accelZ);
  }
  
  public EntityLargeFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
    super(worldIn, shooter, accelX, accelY, accelZ);
  }
  
  protected void onImpact(MovingObjectPosition movingObject) {
    if (!this.worldObj.isRemote) {
      if (movingObject.entityHit != null) {
        movingObject.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, (Entity)this.shootingEntity), 6.0F);
        applyEnchantments(this.shootingEntity, movingObject.entityHit);
      } 
      boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
      this.worldObj.newExplosion((Entity)null, this.posX, this.posY, this.posZ, this.explosionPower, flag, flag);
      setDead();
    } 
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("ExplosionPower", this.explosionPower);
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    if (tagCompund.hasKey("ExplosionPower", 99))
      this.explosionPower = tagCompund.getInteger("ExplosionPower"); 
  }
}
