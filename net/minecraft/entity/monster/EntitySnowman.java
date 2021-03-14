package net.minecraft.entity.monster;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySnowman extends EntityGolem implements IRangedAttackMob {
  public EntitySnowman(World worldIn) {
    super(worldIn);
    setSize(0.7F, 1.9F);
    ((PathNavigateGround)getNavigator()).setAvoidsWater(true);
    this.tasks.addTask(1, (EntityAIBase)new EntityAIArrowAttack(this, 1.25D, 20, 10.0F));
    this.tasks.addTask(2, (EntityAIBase)new EntityAIWander(this, 1.0D));
    this.tasks.addTask(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0F));
    this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    this.targetTasks.addTask(1, (EntityAIBase)new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, IMob.mobSelector));
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (!this.worldObj.isRemote) {
      int i = MathHelper.floor_double(this.posX);
      int j = MathHelper.floor_double(this.posY);
      int k = MathHelper.floor_double(this.posZ);
      if (isWet())
        attackEntityFrom(DamageSource.drown, 1.0F); 
      if (this.worldObj.getBiomeGenForCoords(new BlockPos(i, 0, k)).getFloatTemperature(new BlockPos(i, j, k)) > 1.0F)
        attackEntityFrom(DamageSource.onFire, 1.0F); 
      for (int l = 0; l < 4; l++) {
        i = MathHelper.floor_double(this.posX + ((l % 2 * 2 - 1) * 0.25F));
        j = MathHelper.floor_double(this.posY);
        k = MathHelper.floor_double(this.posZ + ((l / 2 % 2 * 2 - 1) * 0.25F));
        BlockPos blockpos = new BlockPos(i, j, k);
        if (this.worldObj.getBlockState(blockpos).getBlock().getMaterial() == Material.air && this.worldObj.getBiomeGenForCoords(new BlockPos(i, 0, k)).getFloatTemperature(blockpos) < 0.8F && Blocks.snow_layer.canPlaceBlockAt(this.worldObj, blockpos))
          this.worldObj.setBlockState(blockpos, Blocks.snow_layer.getDefaultState()); 
      } 
    } 
  }
  
  protected Item getDropItem() {
    return Items.snowball;
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    int i = this.rand.nextInt(16);
    for (int j = 0; j < i; j++)
      dropItem(Items.snowball, 1); 
  }
  
  public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
    EntitySnowball entitysnowball = new EntitySnowball(this.worldObj, (EntityLivingBase)this);
    double d0 = target.posY + target.getEyeHeight() - 1.100000023841858D;
    double d1 = target.posX - this.posX;
    double d2 = d0 - entitysnowball.posY;
    double d3 = target.posZ - this.posZ;
    float f = MathHelper.sqrt_double(d1 * d1 + d3 * d3) * 0.2F;
    entitysnowball.setThrowableHeading(d1, d2 + f, d3, 1.6F, 12.0F);
    playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
    this.worldObj.spawnEntityInWorld((Entity)entitysnowball);
  }
  
  public float getEyeHeight() {
    return 1.7F;
  }
}
