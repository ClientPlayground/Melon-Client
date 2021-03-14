package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EntitySilverfish extends EntityMob {
  private AISummonSilverfish summonSilverfish;
  
  public EntitySilverfish(World worldIn) {
    super(worldIn);
    setSize(0.4F, 0.3F);
    this.tasks.addTask(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    this.tasks.addTask(3, this.summonSilverfish = new AISummonSilverfish(this));
    this.tasks.addTask(4, (EntityAIBase)new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
    this.tasks.addTask(5, (EntityAIBase)new AIHideInStone(this));
    this.targetTasks.addTask(1, (EntityAIBase)new EntityAIHurtByTarget(this, true, new Class[0]));
    this.targetTasks.addTask(2, (EntityAIBase)new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
  }
  
  public double getYOffset() {
    return 0.2D;
  }
  
  public float getEyeHeight() {
    return 0.1F;
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  protected String getLivingSound() {
    return "mob.silverfish.say";
  }
  
  protected String getHurtSound() {
    return "mob.silverfish.hit";
  }
  
  protected String getDeathSound() {
    return "mob.silverfish.kill";
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (isEntityInvulnerable(source))
      return false; 
    if (source instanceof net.minecraft.util.EntityDamageSource || source == DamageSource.magic)
      this.summonSilverfish.func_179462_f(); 
    return super.attackEntityFrom(source, amount);
  }
  
  protected void playStepSound(BlockPos pos, Block blockIn) {
    playSound("mob.silverfish.step", 0.15F, 1.0F);
  }
  
  protected Item getDropItem() {
    return null;
  }
  
  public void onUpdate() {
    this.renderYawOffset = this.rotationYaw;
    super.onUpdate();
  }
  
  public float getBlockPathWeight(BlockPos pos) {
    return (this.worldObj.getBlockState(pos.down()).getBlock() == Blocks.stone) ? 10.0F : super.getBlockPathWeight(pos);
  }
  
  protected boolean isValidLightLevel() {
    return true;
  }
  
  public boolean getCanSpawnHere() {
    if (super.getCanSpawnHere()) {
      EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity((Entity)this, 5.0D);
      return (entityplayer == null);
    } 
    return false;
  }
  
  public EnumCreatureAttribute getCreatureAttribute() {
    return EnumCreatureAttribute.ARTHROPOD;
  }
  
  static class AIHideInStone extends EntityAIWander {
    private final EntitySilverfish silverfish;
    
    private EnumFacing facing;
    
    private boolean field_179484_c;
    
    public AIHideInStone(EntitySilverfish silverfishIn) {
      super(silverfishIn, 1.0D, 10);
      this.silverfish = silverfishIn;
      setMutexBits(1);
    }
    
    public boolean shouldExecute() {
      if (this.silverfish.getAttackTarget() != null)
        return false; 
      if (!this.silverfish.getNavigator().noPath())
        return false; 
      Random random = this.silverfish.getRNG();
      if (random.nextInt(10) == 0) {
        this.facing = EnumFacing.random(random);
        BlockPos blockpos = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
        IBlockState iblockstate = this.silverfish.worldObj.getBlockState(blockpos);
        if (BlockSilverfish.canContainSilverfish(iblockstate)) {
          this.field_179484_c = true;
          return true;
        } 
      } 
      this.field_179484_c = false;
      return super.shouldExecute();
    }
    
    public boolean continueExecuting() {
      return this.field_179484_c ? false : super.continueExecuting();
    }
    
    public void startExecuting() {
      if (!this.field_179484_c) {
        super.startExecuting();
      } else {
        World world = this.silverfish.worldObj;
        BlockPos blockpos = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
        IBlockState iblockstate = world.getBlockState(blockpos);
        if (BlockSilverfish.canContainSilverfish(iblockstate)) {
          world.setBlockState(blockpos, Blocks.monster_egg.getDefaultState().withProperty((IProperty)BlockSilverfish.VARIANT, (Comparable)BlockSilverfish.EnumType.forModelBlock(iblockstate)), 3);
          this.silverfish.spawnExplosionParticle();
          this.silverfish.setDead();
        } 
      } 
    }
  }
  
  static class AISummonSilverfish extends EntityAIBase {
    private EntitySilverfish silverfish;
    
    private int field_179463_b;
    
    public AISummonSilverfish(EntitySilverfish silverfishIn) {
      this.silverfish = silverfishIn;
    }
    
    public void func_179462_f() {
      if (this.field_179463_b == 0)
        this.field_179463_b = 20; 
    }
    
    public boolean shouldExecute() {
      return (this.field_179463_b > 0);
    }
    
    public void updateTask() {
      this.field_179463_b--;
      if (this.field_179463_b <= 0) {
        World world = this.silverfish.worldObj;
        Random random = this.silverfish.getRNG();
        BlockPos blockpos = new BlockPos((Entity)this.silverfish);
        int i;
        for (i = 0; i <= 5 && i >= -5; i = (i <= 0) ? (1 - i) : (0 - i)) {
          int j;
          for (j = 0; j <= 10 && j >= -10; j = (j <= 0) ? (1 - j) : (0 - j)) {
            int k;
            for (k = 0; k <= 10 && k >= -10; k = (k <= 0) ? (1 - k) : (0 - k)) {
              BlockPos blockpos1 = blockpos.add(j, i, k);
              IBlockState iblockstate = world.getBlockState(blockpos1);
              if (iblockstate.getBlock() == Blocks.monster_egg) {
                if (world.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
                  world.destroyBlock(blockpos1, true);
                } else {
                  world.setBlockState(blockpos1, ((BlockSilverfish.EnumType)iblockstate.getValue((IProperty)BlockSilverfish.VARIANT)).getModelBlock(), 3);
                } 
                if (random.nextBoolean())
                  return; 
              } 
            } 
          } 
        } 
      } 
    }
  }
}
