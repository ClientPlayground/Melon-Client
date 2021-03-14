package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityEnderPearl extends EntityThrowable {
  private EntityLivingBase field_181555_c;
  
  public EntityEnderPearl(World worldIn) {
    super(worldIn);
  }
  
  public EntityEnderPearl(World worldIn, EntityLivingBase p_i1783_2_) {
    super(worldIn, p_i1783_2_);
    this.field_181555_c = p_i1783_2_;
  }
  
  public EntityEnderPearl(World worldIn, double x, double y, double z) {
    super(worldIn, x, y, z);
  }
  
  protected void onImpact(MovingObjectPosition p_70184_1_) {
    EntityLivingBase entitylivingbase = getThrower();
    if (p_70184_1_.entityHit != null) {
      if (p_70184_1_.entityHit == this.field_181555_c)
        return; 
      p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage((Entity)this, (Entity)entitylivingbase), 0.0F);
    } 
    for (int i = 0; i < 32; i++)
      this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian(), new int[0]); 
    if (!this.worldObj.isRemote) {
      if (entitylivingbase instanceof EntityPlayerMP) {
        EntityPlayerMP entityplayermp = (EntityPlayerMP)entitylivingbase;
        if (entityplayermp.playerNetServerHandler.getNetworkManager().isChannelOpen() && entityplayermp.worldObj == this.worldObj && !entityplayermp.isPlayerSleeping()) {
          if (this.rand.nextFloat() < 0.05F && this.worldObj.getGameRules().getGameRuleBooleanValue("doMobSpawning")) {
            EntityEndermite entityendermite = new EntityEndermite(this.worldObj);
            entityendermite.setSpawnedByPlayer(true);
            entityendermite.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
            this.worldObj.spawnEntityInWorld((Entity)entityendermite);
          } 
          if (entitylivingbase.isRiding())
            entitylivingbase.mountEntity((Entity)null); 
          entitylivingbase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
          entitylivingbase.fallDistance = 0.0F;
          entitylivingbase.attackEntityFrom(DamageSource.fall, 5.0F);
        } 
      } else if (entitylivingbase != null) {
        entitylivingbase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
        entitylivingbase.fallDistance = 0.0F;
      } 
      setDead();
    } 
  }
  
  public void onUpdate() {
    EntityLivingBase entitylivingbase = getThrower();
    if (entitylivingbase != null && entitylivingbase instanceof net.minecraft.entity.player.EntityPlayer && !entitylivingbase.isEntityAlive()) {
      setDead();
    } else {
      super.onUpdate();
    } 
  }
}
