package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIFindEntityNearestPlayer extends EntityAIBase {
  private static final Logger LOGGER = LogManager.getLogger();
  
  private EntityLiving entityLiving;
  
  private final Predicate<Entity> predicate;
  
  private final EntityAINearestAttackableTarget.Sorter sorter;
  
  private EntityLivingBase entityTarget;
  
  public EntityAIFindEntityNearestPlayer(EntityLiving entityLivingIn) {
    this.entityLiving = entityLivingIn;
    if (entityLivingIn instanceof net.minecraft.entity.EntityCreature)
      LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!"); 
    this.predicate = new Predicate<Entity>() {
        public boolean apply(Entity p_apply_1_) {
          if (!(p_apply_1_ instanceof EntityPlayer))
            return false; 
          if (((EntityPlayer)p_apply_1_).capabilities.disableDamage)
            return false; 
          double d0 = EntityAIFindEntityNearestPlayer.this.maxTargetRange();
          if (p_apply_1_.isSneaking())
            d0 *= 0.800000011920929D; 
          if (p_apply_1_.isInvisible()) {
            float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();
            if (f < 0.1F)
              f = 0.1F; 
            d0 *= (0.7F * f);
          } 
          return (p_apply_1_.getDistanceToEntity((Entity)EntityAIFindEntityNearestPlayer.this.entityLiving) > d0) ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearestPlayer.this.entityLiving, (EntityLivingBase)p_apply_1_, false, true);
        }
      };
    this.sorter = new EntityAINearestAttackableTarget.Sorter((Entity)entityLivingIn);
  }
  
  public boolean shouldExecute() {
    double d0 = maxTargetRange();
    List<EntityPlayer> list = this.entityLiving.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.entityLiving.getEntityBoundingBox().expand(d0, 4.0D, d0), this.predicate);
    Collections.sort(list, this.sorter);
    if (list.isEmpty())
      return false; 
    this.entityTarget = (EntityLivingBase)list.get(0);
    return true;
  }
  
  public boolean continueExecuting() {
    EntityLivingBase entitylivingbase = this.entityLiving.getAttackTarget();
    if (entitylivingbase == null)
      return false; 
    if (!entitylivingbase.isEntityAlive())
      return false; 
    if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).capabilities.disableDamage)
      return false; 
    Team team = this.entityLiving.getTeam();
    Team team1 = entitylivingbase.getTeam();
    if (team != null && team1 == team)
      return false; 
    double d0 = maxTargetRange();
    return (this.entityLiving.getDistanceSqToEntity((Entity)entitylivingbase) > d0 * d0) ? false : ((!(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP)entitylivingbase).theItemInWorldManager.isCreative()));
  }
  
  public void startExecuting() {
    this.entityLiving.setAttackTarget(this.entityTarget);
    super.startExecuting();
  }
  
  public void resetTask() {
    this.entityLiving.setAttackTarget((EntityLivingBase)null);
    super.startExecuting();
  }
  
  protected double maxTargetRange() {
    IAttributeInstance iattributeinstance = this.entityLiving.getEntityAttribute(SharedMonsterAttributes.followRange);
    return (iattributeinstance == null) ? 16.0D : iattributeinstance.getAttributeValue();
  }
}
