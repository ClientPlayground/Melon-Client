package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.util.MathHelper;

public class EntityAIArrowAttack extends EntityAIBase {
  private final EntityLiving entityHost;
  
  private final IRangedAttackMob rangedAttackEntityHost;
  
  private EntityLivingBase attackTarget;
  
  public EntityAIArrowAttack(IRangedAttackMob attacker, double movespeed, int p_i1649_4_, float p_i1649_5_) {
    this(attacker, movespeed, p_i1649_4_, p_i1649_4_, p_i1649_5_);
  }
  
  private int rangedAttackTime = -1;
  
  private double entityMoveSpeed;
  
  private int field_75318_f;
  
  private int field_96561_g;
  
  private int maxRangedAttackTime;
  
  private float field_96562_i;
  
  private float maxAttackDistance;
  
  public EntityAIArrowAttack(IRangedAttackMob attacker, double movespeed, int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn) {
    if (!(attacker instanceof EntityLivingBase))
      throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob"); 
    this.rangedAttackEntityHost = attacker;
    this.entityHost = (EntityLiving)attacker;
    this.entityMoveSpeed = movespeed;
    this.field_96561_g = p_i1650_4_;
    this.maxRangedAttackTime = maxAttackTime;
    this.field_96562_i = maxAttackDistanceIn;
    this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
    setMutexBits(3);
  }
  
  public boolean shouldExecute() {
    EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();
    if (entitylivingbase == null)
      return false; 
    this.attackTarget = entitylivingbase;
    return true;
  }
  
  public boolean continueExecuting() {
    return (shouldExecute() || !this.entityHost.getNavigator().noPath());
  }
  
  public void resetTask() {
    this.attackTarget = null;
    this.field_75318_f = 0;
    this.rangedAttackTime = -1;
  }
  
  public void updateTask() {
    double d0 = this.entityHost.getDistanceSq(this.attackTarget.posX, (this.attackTarget.getEntityBoundingBox()).minY, this.attackTarget.posZ);
    boolean flag = this.entityHost.getEntitySenses().canSee((Entity)this.attackTarget);
    if (flag) {
      this.field_75318_f++;
    } else {
      this.field_75318_f = 0;
    } 
    if (d0 <= this.maxAttackDistance && this.field_75318_f >= 20) {
      this.entityHost.getNavigator().clearPathEntity();
    } else {
      this.entityHost.getNavigator().tryMoveToEntityLiving((Entity)this.attackTarget, this.entityMoveSpeed);
    } 
    this.entityHost.getLookHelper().setLookPositionWithEntity((Entity)this.attackTarget, 30.0F, 30.0F);
    if (--this.rangedAttackTime == 0) {
      if (d0 > this.maxAttackDistance || !flag)
        return; 
      float f = MathHelper.sqrt_double(d0) / this.field_96562_i;
      float lvt_5_1_ = MathHelper.clamp_float(f, 0.1F, 1.0F);
      this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, lvt_5_1_);
      this.rangedAttackTime = MathHelper.floor_float(f * (this.maxRangedAttackTime - this.field_96561_g) + this.field_96561_g);
    } else if (this.rangedAttackTime < 0) {
      float f2 = MathHelper.sqrt_double(d0) / this.field_96562_i;
      this.rangedAttackTime = MathHelper.floor_float(f2 * (this.maxRangedAttackTime - this.field_96561_g) + this.field_96561_g);
    } 
  }
}
