package net.minecraft.entity.monster;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityPigZombie extends EntityZombie {
  private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
  
  private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = (new AttributeModifier(ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.05D, 0)).setSaved(false);
  
  private int angerLevel;
  
  private int randomSoundDelay;
  
  private UUID angerTargetUUID;
  
  public EntityPigZombie(World worldIn) {
    super(worldIn);
    this.isImmuneToFire = true;
  }
  
  public void setRevengeTarget(EntityLivingBase livingBase) {
    super.setRevengeTarget(livingBase);
    if (livingBase != null)
      this.angerTargetUUID = livingBase.getUniqueID(); 
  }
  
  protected void applyEntityAI() {
    this.targetTasks.addTask(1, (EntityAIBase)new AIHurtByAggressor(this));
    this.targetTasks.addTask(2, (EntityAIBase)new AITargetAggressor(this));
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(reinforcementChance).setBaseValue(0.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
    getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0D);
  }
  
  public void onUpdate() {
    super.onUpdate();
  }
  
  protected void updateAITasks() {
    IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
    if (isAngry()) {
      if (!isChild() && !iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER))
        iattributeinstance.applyModifier(ATTACK_SPEED_BOOST_MODIFIER); 
      this.angerLevel--;
    } else if (iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
      iattributeinstance.removeModifier(ATTACK_SPEED_BOOST_MODIFIER);
    } 
    if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0)
      playSound("mob.zombiepig.zpigangry", getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F); 
    if (this.angerLevel > 0 && this.angerTargetUUID != null && getAITarget() == null) {
      EntityPlayer entityplayer = this.worldObj.getPlayerEntityByUUID(this.angerTargetUUID);
      setRevengeTarget((EntityLivingBase)entityplayer);
      this.attackingPlayer = entityplayer;
      this.recentlyHit = getRevengeTimer();
    } 
    super.updateAITasks();
  }
  
  public boolean getCanSpawnHere() {
    return (this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL);
  }
  
  public boolean isNotColliding() {
    return (this.worldObj.checkNoEntityCollision(getEntityBoundingBox(), (Entity)this) && this.worldObj.getCollidingBoundingBoxes((Entity)this, getEntityBoundingBox()).isEmpty() && !this.worldObj.isAnyLiquid(getEntityBoundingBox()));
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setShort("Anger", (short)this.angerLevel);
    if (this.angerTargetUUID != null) {
      tagCompound.setString("HurtBy", this.angerTargetUUID.toString());
    } else {
      tagCompound.setString("HurtBy", "");
    } 
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    this.angerLevel = tagCompund.getShort("Anger");
    String s = tagCompund.getString("HurtBy");
    if (s.length() > 0) {
      this.angerTargetUUID = UUID.fromString(s);
      EntityPlayer entityplayer = this.worldObj.getPlayerEntityByUUID(this.angerTargetUUID);
      setRevengeTarget((EntityLivingBase)entityplayer);
      if (entityplayer != null) {
        this.attackingPlayer = entityplayer;
        this.recentlyHit = getRevengeTimer();
      } 
    } 
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    Entity entity = source.getEntity();
    if (entity instanceof EntityPlayer)
      becomeAngryAt(entity); 
    return super.attackEntityFrom(source, amount);
  }
  
  private void becomeAngryAt(Entity p_70835_1_) {
    this.angerLevel = 400 + this.rand.nextInt(400);
    this.randomSoundDelay = this.rand.nextInt(40);
    if (p_70835_1_ instanceof EntityLivingBase)
      setRevengeTarget((EntityLivingBase)p_70835_1_); 
  }
  
  public boolean isAngry() {
    return (this.angerLevel > 0);
  }
  
  protected String getLivingSound() {
    return "mob.zombiepig.zpig";
  }
  
  protected String getHurtSound() {
    return "mob.zombiepig.zpighurt";
  }
  
  protected String getDeathSound() {
    return "mob.zombiepig.zpigdeath";
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    int i = this.rand.nextInt(2 + lootingModifier);
    for (int j = 0; j < i; j++)
      dropItem(Items.rotten_flesh, 1); 
    i = this.rand.nextInt(2 + lootingModifier);
    for (int k = 0; k < i; k++)
      dropItem(Items.gold_nugget, 1); 
  }
  
  public boolean interact(EntityPlayer player) {
    return false;
  }
  
  protected void addRandomDrop() {
    dropItem(Items.gold_ingot, 1);
  }
  
  protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
    setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
  }
  
  public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
    super.onInitialSpawn(difficulty, livingdata);
    setVillager(false);
    return livingdata;
  }
  
  static class AIHurtByAggressor extends EntityAIHurtByTarget {
    public AIHurtByAggressor(EntityPigZombie p_i45828_1_) {
      super(p_i45828_1_, true, new Class[0]);
    }
    
    protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn) {
      super.setEntityAttackTarget(creatureIn, entityLivingBaseIn);
      if (creatureIn instanceof EntityPigZombie)
        ((EntityPigZombie)creatureIn).becomeAngryAt((Entity)entityLivingBaseIn); 
    }
  }
  
  static class AITargetAggressor extends EntityAINearestAttackableTarget<EntityPlayer> {
    public AITargetAggressor(EntityPigZombie p_i45829_1_) {
      super(p_i45829_1_, EntityPlayer.class, true);
    }
    
    public boolean shouldExecute() {
      return (((EntityPigZombie)this.taskOwner).isAngry() && super.shouldExecute());
    }
  }
}
