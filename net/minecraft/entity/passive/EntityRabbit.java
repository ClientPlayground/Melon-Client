package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityRabbit extends EntityAnimal {
  private AIAvoidEntity<EntityWolf> aiAvoidWolves;
  
  private int field_175540_bm = 0;
  
  private int field_175535_bn = 0;
  
  private boolean field_175536_bo = false;
  
  private boolean field_175537_bp = false;
  
  private int currentMoveTypeDuration = 0;
  
  private EnumMoveType moveType = EnumMoveType.HOP;
  
  private int carrotTicks = 0;
  
  private EntityPlayer field_175543_bt = null;
  
  public EntityRabbit(World worldIn) {
    super(worldIn);
    setSize(0.6F, 0.7F);
    this.jumpHelper = new RabbitJumpHelper(this);
    this.moveHelper = new RabbitMoveHelper(this);
    ((PathNavigateGround)getNavigator()).setAvoidsWater(true);
    this.navigator.setHeightRequirement(2.5F);
    this.tasks.addTask(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    this.tasks.addTask(1, (EntityAIBase)new AIPanic(this, 1.33D));
    this.tasks.addTask(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.0D, Items.carrot, false));
    this.tasks.addTask(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.0D, Items.golden_carrot, false));
    this.tasks.addTask(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.0D, Item.getItemFromBlock((Block)Blocks.yellow_flower), false));
    this.tasks.addTask(3, (EntityAIBase)new EntityAIMate(this, 0.8D));
    this.tasks.addTask(5, (EntityAIBase)new AIRaidFarm(this));
    this.tasks.addTask(5, (EntityAIBase)new EntityAIWander((EntityCreature)this, 0.6D));
    this.tasks.addTask(11, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 10.0F));
    this.aiAvoidWolves = new AIAvoidEntity<>(this, EntityWolf.class, 16.0F, 1.33D, 1.33D);
    this.tasks.addTask(4, (EntityAIBase)this.aiAvoidWolves);
    setMovementSpeed(0.0D);
  }
  
  protected float getJumpUpwardsMotion() {
    return (this.moveHelper.isUpdating() && this.moveHelper.getY() > this.posY + 0.5D) ? 0.5F : this.moveType.func_180074_b();
  }
  
  public void setMoveType(EnumMoveType type) {
    this.moveType = type;
  }
  
  public float func_175521_o(float p_175521_1_) {
    return (this.field_175535_bn == 0) ? 0.0F : ((this.field_175540_bm + p_175521_1_) / this.field_175535_bn);
  }
  
  public void setMovementSpeed(double newSpeed) {
    getNavigator().setSpeed(newSpeed);
    this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), newSpeed);
  }
  
  public void setJumping(boolean jump, EnumMoveType moveTypeIn) {
    setJumping(jump);
    if (!jump) {
      if (this.moveType == EnumMoveType.ATTACK)
        this.moveType = EnumMoveType.HOP; 
    } else {
      setMovementSpeed(1.5D * moveTypeIn.getSpeed());
      playSound(getJumpingSound(), getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
    } 
    this.field_175536_bo = jump;
  }
  
  public void doMovementAction(EnumMoveType movetype) {
    setJumping(true, movetype);
    this.field_175535_bn = movetype.func_180073_d();
    this.field_175540_bm = 0;
  }
  
  public boolean func_175523_cj() {
    return this.field_175536_bo;
  }
  
  protected void entityInit() {
    super.entityInit();
    this.dataWatcher.addObject(18, Byte.valueOf((byte)0));
  }
  
  public void updateAITasks() {
    if (this.moveHelper.getSpeed() > 0.8D) {
      setMoveType(EnumMoveType.SPRINT);
    } else if (this.moveType != EnumMoveType.ATTACK) {
      setMoveType(EnumMoveType.HOP);
    } 
    if (this.currentMoveTypeDuration > 0)
      this.currentMoveTypeDuration--; 
    if (this.carrotTicks > 0) {
      this.carrotTicks -= this.rand.nextInt(3);
      if (this.carrotTicks < 0)
        this.carrotTicks = 0; 
    } 
    if (this.onGround) {
      if (!this.field_175537_bp) {
        setJumping(false, EnumMoveType.NONE);
        func_175517_cu();
      } 
      if (getRabbitType() == 99 && this.currentMoveTypeDuration == 0) {
        EntityLivingBase entitylivingbase = getAttackTarget();
        if (entitylivingbase != null && getDistanceSqToEntity((Entity)entitylivingbase) < 16.0D) {
          calculateRotationYaw(entitylivingbase.posX, entitylivingbase.posZ);
          this.moveHelper.setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, this.moveHelper.getSpeed());
          doMovementAction(EnumMoveType.ATTACK);
          this.field_175537_bp = true;
        } 
      } 
      RabbitJumpHelper entityrabbit$rabbitjumphelper = (RabbitJumpHelper)this.jumpHelper;
      if (!entityrabbit$rabbitjumphelper.getIsJumping()) {
        if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
          PathEntity pathentity = this.navigator.getPath();
          Vec3 vec3 = new Vec3(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());
          if (pathentity != null && pathentity.getCurrentPathIndex() < pathentity.getCurrentPathLength())
            vec3 = pathentity.getPosition((Entity)this); 
          calculateRotationYaw(vec3.xCoord, vec3.zCoord);
          doMovementAction(this.moveType);
        } 
      } else if (!entityrabbit$rabbitjumphelper.func_180065_d()) {
        func_175518_cr();
      } 
    } 
    this.field_175537_bp = this.onGround;
  }
  
  public void spawnRunningParticles() {}
  
  private void calculateRotationYaw(double x, double z) {
    this.rotationYaw = (float)(MathHelper.atan2(z - this.posZ, x - this.posX) * 180.0D / Math.PI) - 90.0F;
  }
  
  private void func_175518_cr() {
    ((RabbitJumpHelper)this.jumpHelper).func_180066_a(true);
  }
  
  private void func_175520_cs() {
    ((RabbitJumpHelper)this.jumpHelper).func_180066_a(false);
  }
  
  private void updateMoveTypeDuration() {
    this.currentMoveTypeDuration = getMoveTypeDuration();
  }
  
  private void func_175517_cu() {
    updateMoveTypeDuration();
    func_175520_cs();
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (this.field_175540_bm != this.field_175535_bn) {
      if (this.field_175540_bm == 0 && !this.worldObj.isRemote)
        this.worldObj.setEntityState((Entity)this, (byte)1); 
      this.field_175540_bm++;
    } else if (this.field_175535_bn != 0) {
      this.field_175540_bm = 0;
      this.field_175535_bn = 0;
    } 
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("RabbitType", getRabbitType());
    tagCompound.setInteger("MoreCarrotTicks", this.carrotTicks);
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    setRabbitType(tagCompund.getInteger("RabbitType"));
    this.carrotTicks = tagCompund.getInteger("MoreCarrotTicks");
  }
  
  protected String getJumpingSound() {
    return "mob.rabbit.hop";
  }
  
  protected String getLivingSound() {
    return "mob.rabbit.idle";
  }
  
  protected String getHurtSound() {
    return "mob.rabbit.hurt";
  }
  
  protected String getDeathSound() {
    return "mob.rabbit.death";
  }
  
  public boolean attackEntityAsMob(Entity entityIn) {
    if (getRabbitType() == 99) {
      playSound("mob.attack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
      return entityIn.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)this), 8.0F);
    } 
    return entityIn.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)this), 3.0F);
  }
  
  public int getTotalArmorValue() {
    return (getRabbitType() == 99) ? 8 : super.getTotalArmorValue();
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    return isEntityInvulnerable(source) ? false : super.attackEntityFrom(source, amount);
  }
  
  protected void addRandomDrop() {
    entityDropItem(new ItemStack(Items.rabbit_foot, 1), 0.0F);
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    int i = this.rand.nextInt(2) + this.rand.nextInt(1 + lootingModifier);
    for (int j = 0; j < i; j++)
      dropItem(Items.rabbit_hide, 1); 
    i = this.rand.nextInt(2);
    for (int k = 0; k < i; k++) {
      if (isBurning()) {
        dropItem(Items.cooked_rabbit, 1);
      } else {
        dropItem(Items.rabbit, 1);
      } 
    } 
  }
  
  private boolean isRabbitBreedingItem(Item itemIn) {
    return (itemIn == Items.carrot || itemIn == Items.golden_carrot || itemIn == Item.getItemFromBlock((Block)Blocks.yellow_flower));
  }
  
  public EntityRabbit createChild(EntityAgeable ageable) {
    EntityRabbit entityrabbit = new EntityRabbit(this.worldObj);
    if (ageable instanceof EntityRabbit)
      entityrabbit.setRabbitType(this.rand.nextBoolean() ? getRabbitType() : ((EntityRabbit)ageable).getRabbitType()); 
    return entityrabbit;
  }
  
  public boolean isBreedingItem(ItemStack stack) {
    return (stack != null && isRabbitBreedingItem(stack.getItem()));
  }
  
  public int getRabbitType() {
    return this.dataWatcher.getWatchableObjectByte(18);
  }
  
  public void setRabbitType(int rabbitTypeId) {
    if (rabbitTypeId == 99) {
      this.tasks.removeTask((EntityAIBase)this.aiAvoidWolves);
      this.tasks.addTask(4, (EntityAIBase)new AIEvilAttack(this));
      this.targetTasks.addTask(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, true));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityWolf.class, true));
      if (!hasCustomName())
        setCustomNameTag(StatCollector.translateToLocal("entity.KillerBunny.name")); 
    } 
    this.dataWatcher.updateObject(18, Byte.valueOf((byte)rabbitTypeId));
  }
  
  public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
    livingdata = super.onInitialSpawn(difficulty, livingdata);
    int i = this.rand.nextInt(6);
    boolean flag = false;
    if (livingdata instanceof RabbitTypeData) {
      i = ((RabbitTypeData)livingdata).typeData;
      flag = true;
    } else {
      livingdata = new RabbitTypeData(i);
    } 
    setRabbitType(i);
    if (flag)
      setGrowingAge(-24000); 
    return livingdata;
  }
  
  private boolean isCarrotEaten() {
    return (this.carrotTicks == 0);
  }
  
  protected int getMoveTypeDuration() {
    return this.moveType.getDuration();
  }
  
  protected void createEatingParticles() {
    this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.posY + 0.5D + (this.rand.nextFloat() * this.height), this.posZ + (this.rand.nextFloat() * this.width * 2.0F) - this.width, 0.0D, 0.0D, 0.0D, new int[] { Block.getStateId(Blocks.carrots.getStateFromMeta(7)) });
    this.carrotTicks = 100;
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 1) {
      createRunningParticles();
      this.field_175535_bn = 10;
      this.field_175540_bm = 0;
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  static class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {
    private EntityRabbit entityInstance;
    
    public AIAvoidEntity(EntityRabbit rabbit, Class<T> p_i46403_2_, float p_i46403_3_, double p_i46403_4_, double p_i46403_6_) {
      super((EntityCreature)rabbit, p_i46403_2_, p_i46403_3_, p_i46403_4_, p_i46403_6_);
      this.entityInstance = rabbit;
    }
    
    public void updateTask() {
      super.updateTask();
    }
  }
  
  static class AIEvilAttack extends EntityAIAttackOnCollide {
    public AIEvilAttack(EntityRabbit rabbit) {
      super((EntityCreature)rabbit, EntityLivingBase.class, 1.4D, true);
    }
    
    protected double func_179512_a(EntityLivingBase attackTarget) {
      return (4.0F + attackTarget.width);
    }
  }
  
  static class AIPanic extends EntityAIPanic {
    private EntityRabbit theEntity;
    
    public AIPanic(EntityRabbit rabbit, double speedIn) {
      super((EntityCreature)rabbit, speedIn);
      this.theEntity = rabbit;
    }
    
    public void updateTask() {
      super.updateTask();
      this.theEntity.setMovementSpeed(this.speed);
    }
  }
  
  static class AIRaidFarm extends EntityAIMoveToBlock {
    private final EntityRabbit rabbit;
    
    private boolean field_179498_d;
    
    private boolean field_179499_e = false;
    
    public AIRaidFarm(EntityRabbit rabbitIn) {
      super((EntityCreature)rabbitIn, 0.699999988079071D, 16);
      this.rabbit = rabbitIn;
    }
    
    public boolean shouldExecute() {
      if (this.runDelay <= 0) {
        if (!this.rabbit.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
          return false; 
        this.field_179499_e = false;
        this.field_179498_d = this.rabbit.isCarrotEaten();
      } 
      return super.shouldExecute();
    }
    
    public boolean continueExecuting() {
      return (this.field_179499_e && super.continueExecuting());
    }
    
    public void startExecuting() {
      super.startExecuting();
    }
    
    public void resetTask() {
      super.resetTask();
    }
    
    public void updateTask() {
      super.updateTask();
      this.rabbit.getLookHelper().setLookPosition(this.destinationBlock.getX() + 0.5D, (this.destinationBlock.getY() + 1), this.destinationBlock.getZ() + 0.5D, 10.0F, this.rabbit.getVerticalFaceSpeed());
      if (getIsAboveDestination()) {
        World world = this.rabbit.worldObj;
        BlockPos blockpos = this.destinationBlock.up();
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        if (this.field_179499_e && block instanceof BlockCarrot && ((Integer)iblockstate.getValue((IProperty)BlockCarrot.AGE)).intValue() == 7) {
          world.setBlockState(blockpos, Blocks.air.getDefaultState(), 2);
          world.destroyBlock(blockpos, true);
          this.rabbit.createEatingParticles();
        } 
        this.field_179499_e = false;
        this.runDelay = 10;
      } 
    }
    
    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
      Block block = worldIn.getBlockState(pos).getBlock();
      if (block == Blocks.farmland) {
        pos = pos.up();
        IBlockState iblockstate = worldIn.getBlockState(pos);
        block = iblockstate.getBlock();
        if (block instanceof BlockCarrot && ((Integer)iblockstate.getValue((IProperty)BlockCarrot.AGE)).intValue() == 7 && this.field_179498_d && !this.field_179499_e) {
          this.field_179499_e = true;
          return true;
        } 
      } 
      return false;
    }
  }
  
  enum EnumMoveType {
    NONE(0.0F, 0.0F, 30, 1),
    HOP(0.8F, 0.2F, 20, 10),
    STEP(1.0F, 0.45F, 14, 14),
    SPRINT(1.75F, 0.4F, 1, 8),
    ATTACK(2.0F, 0.7F, 7, 8);
    
    private final float speed;
    
    private final float field_180077_g;
    
    private final int duration;
    
    private final int field_180085_i;
    
    EnumMoveType(float typeSpeed, float p_i45866_4_, int typeDuration, int p_i45866_6_) {
      this.speed = typeSpeed;
      this.field_180077_g = p_i45866_4_;
      this.duration = typeDuration;
      this.field_180085_i = p_i45866_6_;
    }
    
    public float getSpeed() {
      return this.speed;
    }
    
    public float func_180074_b() {
      return this.field_180077_g;
    }
    
    public int getDuration() {
      return this.duration;
    }
    
    public int func_180073_d() {
      return this.field_180085_i;
    }
  }
  
  public class RabbitJumpHelper extends EntityJumpHelper {
    private EntityRabbit theEntity;
    
    private boolean field_180068_d = false;
    
    public RabbitJumpHelper(EntityRabbit rabbit) {
      super((EntityLiving)rabbit);
      this.theEntity = rabbit;
    }
    
    public boolean getIsJumping() {
      return this.isJumping;
    }
    
    public boolean func_180065_d() {
      return this.field_180068_d;
    }
    
    public void func_180066_a(boolean p_180066_1_) {
      this.field_180068_d = p_180066_1_;
    }
    
    public void doJump() {
      if (this.isJumping) {
        this.theEntity.doMovementAction(EntityRabbit.EnumMoveType.STEP);
        this.isJumping = false;
      } 
    }
  }
  
  static class RabbitMoveHelper extends EntityMoveHelper {
    private EntityRabbit theEntity;
    
    public RabbitMoveHelper(EntityRabbit rabbit) {
      super((EntityLiving)rabbit);
      this.theEntity = rabbit;
    }
    
    public void onUpdateMoveHelper() {
      if (this.theEntity.onGround && !this.theEntity.func_175523_cj())
        this.theEntity.setMovementSpeed(0.0D); 
      super.onUpdateMoveHelper();
    }
  }
  
  public static class RabbitTypeData implements IEntityLivingData {
    public int typeData;
    
    public RabbitTypeData(int type) {
      this.typeData = type;
    }
  }
}
