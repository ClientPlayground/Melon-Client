package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

public abstract class EntityAITarget extends EntityAIBase {
  protected final EntityCreature taskOwner;
  
  protected boolean shouldCheckSight;
  
  private boolean nearbyOnly;
  
  private int targetSearchStatus;
  
  private int targetSearchDelay;
  
  private int targetUnseenTicks;
  
  public EntityAITarget(EntityCreature creature, boolean checkSight) {
    this(creature, checkSight, false);
  }
  
  public EntityAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
    this.taskOwner = creature;
    this.shouldCheckSight = checkSight;
    this.nearbyOnly = onlyNearby;
  }
  
  public boolean continueExecuting() {
    EntityLivingBase entitylivingbase = this.taskOwner.getAttackTarget();
    if (entitylivingbase == null)
      return false; 
    if (!entitylivingbase.isEntityAlive())
      return false; 
    Team team = this.taskOwner.getTeam();
    Team team1 = entitylivingbase.getTeam();
    if (team != null && team1 == team)
      return false; 
    double d0 = getTargetDistance();
    if (this.taskOwner.getDistanceSqToEntity((Entity)entitylivingbase) > d0 * d0)
      return false; 
    if (this.shouldCheckSight)
      if (this.taskOwner.getEntitySenses().canSee((Entity)entitylivingbase)) {
        this.targetUnseenTicks = 0;
      } else if (++this.targetUnseenTicks > 60) {
        return false;
      }  
    return (!(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).capabilities.disableDamage);
  }
  
  protected double getTargetDistance() {
    IAttributeInstance iattributeinstance = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange);
    return (iattributeinstance == null) ? 16.0D : iattributeinstance.getAttributeValue();
  }
  
  public void startExecuting() {
    this.targetSearchStatus = 0;
    this.targetSearchDelay = 0;
    this.targetUnseenTicks = 0;
  }
  
  public void resetTask() {
    this.taskOwner.setAttackTarget((EntityLivingBase)null);
  }
  
  public static boolean isSuitableTarget(EntityLiving attacker, EntityLivingBase target, boolean includeInvincibles, boolean checkSight) {
    if (target == null)
      return false; 
    if (target == attacker)
      return false; 
    if (!target.isEntityAlive())
      return false; 
    if (!attacker.canAttackClass(target.getClass()))
      return false; 
    Team team = attacker.getTeam();
    Team team1 = target.getTeam();
    if (team != null && team1 == team)
      return false; 
    if (attacker instanceof IEntityOwnable && StringUtils.isNotEmpty(((IEntityOwnable)attacker).getOwnerId())) {
      if (target instanceof IEntityOwnable && ((IEntityOwnable)attacker).getOwnerId().equals(((IEntityOwnable)target).getOwnerId()))
        return false; 
      if (target == ((IEntityOwnable)attacker).getOwner())
        return false; 
    } else if (target instanceof EntityPlayer && !includeInvincibles && ((EntityPlayer)target).capabilities.disableDamage) {
      return false;
    } 
    return (!checkSight || attacker.getEntitySenses().canSee((Entity)target));
  }
  
  protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
    if (!isSuitableTarget((EntityLiving)this.taskOwner, target, includeInvincibles, this.shouldCheckSight))
      return false; 
    if (!this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos((Entity)target)))
      return false; 
    if (this.nearbyOnly) {
      if (--this.targetSearchDelay <= 0)
        this.targetSearchStatus = 0; 
      if (this.targetSearchStatus == 0)
        this.targetSearchStatus = canEasilyReach(target) ? 1 : 2; 
      if (this.targetSearchStatus == 2)
        return false; 
    } 
    return true;
  }
  
  private boolean canEasilyReach(EntityLivingBase target) {
    this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
    PathEntity pathentity = this.taskOwner.getNavigator().getPathToEntityLiving((Entity)target);
    if (pathentity == null)
      return false; 
    PathPoint pathpoint = pathentity.getFinalPathPoint();
    if (pathpoint == null)
      return false; 
    int i = pathpoint.xCoord - MathHelper.floor_double(target.posX);
    int j = pathpoint.zCoord - MathHelper.floor_double(target.posZ);
    return ((i * i + j * j) <= 2.25D);
  }
}
