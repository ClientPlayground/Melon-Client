package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIDefendVillage;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookAtVillager;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityIronGolem extends EntityGolem {
  private int homeCheckTimer;
  
  Village villageObj;
  
  private int attackTimer;
  
  private int holdRoseTick;
  
  public EntityIronGolem(World worldIn) {
    super(worldIn);
    setSize(1.4F, 2.9F);
    ((PathNavigateGround)getNavigator()).setAvoidsWater(true);
    this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackOnCollide(this, 1.0D, true));
    this.tasks.addTask(2, (EntityAIBase)new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
    this.tasks.addTask(3, (EntityAIBase)new EntityAIMoveThroughVillage(this, 0.6D, true));
    this.tasks.addTask(4, (EntityAIBase)new EntityAIMoveTowardsRestriction(this, 1.0D));
    this.tasks.addTask(5, (EntityAIBase)new EntityAILookAtVillager(this));
    this.tasks.addTask(6, (EntityAIBase)new EntityAIWander(this, 0.6D));
    this.tasks.addTask(7, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0F));
    this.tasks.addTask(8, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    this.targetTasks.addTask(1, (EntityAIBase)new EntityAIDefendVillage(this));
    this.targetTasks.addTask(2, (EntityAIBase)new EntityAIHurtByTarget(this, false, new Class[0]));
    this.targetTasks.addTask(3, (EntityAIBase)new AINearestAttackableTargetNonCreeper(this, EntityLiving.class, 10, false, true, IMob.VISIBLE_MOB_SELECTOR));
  }
  
  protected void entityInit() {
    super.entityInit();
    this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
  }
  
  protected void updateAITasks() {
    if (--this.homeCheckTimer <= 0) {
      this.homeCheckTimer = 70 + this.rand.nextInt(50);
      this.villageObj = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos((Entity)this), 32);
      if (this.villageObj == null) {
        detachHome();
      } else {
        BlockPos blockpos = this.villageObj.getCenter();
        setHomePosAndDistance(blockpos, (int)(this.villageObj.getVillageRadius() * 0.6F));
      } 
    } 
    super.updateAITasks();
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
  }
  
  protected int decreaseAirSupply(int p_70682_1_) {
    return p_70682_1_;
  }
  
  protected void collideWithEntity(Entity entityIn) {
    if (entityIn instanceof IMob && !(entityIn instanceof EntityCreeper) && getRNG().nextInt(20) == 0)
      setAttackTarget((EntityLivingBase)entityIn); 
    super.collideWithEntity(entityIn);
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (this.attackTimer > 0)
      this.attackTimer--; 
    if (this.holdRoseTick > 0)
      this.holdRoseTick--; 
    if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7D && this.rand.nextInt(5) == 0) {
      int i = MathHelper.floor_double(this.posX);
      int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
      int k = MathHelper.floor_double(this.posZ);
      IBlockState iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k));
      Block block = iblockstate.getBlock();
      if (block.getMaterial() != Material.air)
        this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + (this.rand.nextFloat() - 0.5D) * this.width, (getEntityBoundingBox()).minY + 0.1D, this.posZ + (this.rand.nextFloat() - 0.5D) * this.width, 4.0D * (this.rand.nextFloat() - 0.5D), 0.5D, (this.rand.nextFloat() - 0.5D) * 4.0D, new int[] { Block.getStateId(iblockstate) }); 
    } 
  }
  
  public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
    return (isPlayerCreated() && EntityPlayer.class.isAssignableFrom(cls)) ? false : ((cls == EntityCreeper.class) ? false : super.canAttackClass(cls));
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setBoolean("PlayerCreated", isPlayerCreated());
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    setPlayerCreated(tagCompund.getBoolean("PlayerCreated"));
  }
  
  public boolean attackEntityAsMob(Entity entityIn) {
    this.attackTimer = 10;
    this.worldObj.setEntityState((Entity)this, (byte)4);
    boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)this), (7 + this.rand.nextInt(15)));
    if (flag) {
      entityIn.motionY += 0.4000000059604645D;
      applyEnchantments((EntityLivingBase)this, entityIn);
    } 
    playSound("mob.irongolem.throw", 1.0F, 1.0F);
    return flag;
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 4) {
      this.attackTimer = 10;
      playSound("mob.irongolem.throw", 1.0F, 1.0F);
    } else if (id == 11) {
      this.holdRoseTick = 400;
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  public Village getVillage() {
    return this.villageObj;
  }
  
  public int getAttackTimer() {
    return this.attackTimer;
  }
  
  public void setHoldingRose(boolean p_70851_1_) {
    this.holdRoseTick = p_70851_1_ ? 400 : 0;
    this.worldObj.setEntityState((Entity)this, (byte)11);
  }
  
  protected String getHurtSound() {
    return "mob.irongolem.hit";
  }
  
  protected String getDeathSound() {
    return "mob.irongolem.death";
  }
  
  protected void playStepSound(BlockPos pos, Block blockIn) {
    playSound("mob.irongolem.walk", 1.0F, 1.0F);
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    int i = this.rand.nextInt(3);
    for (int j = 0; j < i; j++)
      dropItemWithOffset(Item.getItemFromBlock((Block)Blocks.red_flower), 1, BlockFlower.EnumFlowerType.POPPY.getMeta()); 
    int l = 3 + this.rand.nextInt(3);
    for (int k = 0; k < l; k++)
      dropItem(Items.iron_ingot, 1); 
  }
  
  public int getHoldRoseTick() {
    return this.holdRoseTick;
  }
  
  public boolean isPlayerCreated() {
    return ((this.dataWatcher.getWatchableObjectByte(16) & 0x1) != 0);
  }
  
  public void setPlayerCreated(boolean p_70849_1_) {
    byte b0 = this.dataWatcher.getWatchableObjectByte(16);
    if (p_70849_1_) {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 0x1)));
    } else {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 0xFFFFFFFE)));
    } 
  }
  
  public void onDeath(DamageSource cause) {
    if (!isPlayerCreated() && this.attackingPlayer != null && this.villageObj != null)
      this.villageObj.setReputationForPlayer(this.attackingPlayer.getCommandSenderName(), -5); 
    super.onDeath(cause);
  }
  
  static class AINearestAttackableTargetNonCreeper<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
    public AINearestAttackableTargetNonCreeper(final EntityCreature creature, Class<T> classTarget, int chance, boolean p_i45858_4_, boolean p_i45858_5_, final Predicate<? super T> p_i45858_6_) {
      super(creature, classTarget, chance, p_i45858_4_, p_i45858_5_, p_i45858_6_);
      this.targetEntitySelector = new Predicate<T>() {
          public boolean apply(T p_apply_1_) {
            if (p_i45858_6_ != null && !p_i45858_6_.apply(p_apply_1_))
              return false; 
            if (p_apply_1_ instanceof EntityCreeper)
              return false; 
            if (p_apply_1_ instanceof EntityPlayer) {
              double d0 = EntityIronGolem.AINearestAttackableTargetNonCreeper.this.getTargetDistance();
              if (p_apply_1_.isSneaking())
                d0 *= 0.800000011920929D; 
              if (p_apply_1_.isInvisible()) {
                float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();
                if (f < 0.1F)
                  f = 0.1F; 
                d0 *= (0.7F * f);
              } 
              if (p_apply_1_.getDistanceToEntity((Entity)creature) > d0)
                return false; 
            } 
            return EntityIronGolem.AINearestAttackableTargetNonCreeper.this.isSuitableTarget((EntityLivingBase)p_apply_1_, false);
          }
        };
    }
  }
}
