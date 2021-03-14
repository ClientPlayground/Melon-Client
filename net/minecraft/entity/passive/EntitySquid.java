package net.minecraft.entity.passive;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySquid extends EntityWaterMob {
  public float squidPitch;
  
  public float prevSquidPitch;
  
  public float squidYaw;
  
  public float prevSquidYaw;
  
  public float squidRotation;
  
  public float prevSquidRotation;
  
  public float tentacleAngle;
  
  public float lastTentacleAngle;
  
  private float randomMotionSpeed;
  
  private float rotationVelocity;
  
  private float field_70871_bB;
  
  private float randomMotionVecX;
  
  private float randomMotionVecY;
  
  private float randomMotionVecZ;
  
  public EntitySquid(World worldIn) {
    super(worldIn);
    setSize(0.95F, 0.95F);
    this.rand.setSeed((1 + getEntityId()));
    this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
    this.tasks.addTask(0, new AIMoveRandom(this));
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
  }
  
  public float getEyeHeight() {
    return this.height * 0.5F;
  }
  
  protected String getLivingSound() {
    return null;
  }
  
  protected String getHurtSound() {
    return null;
  }
  
  protected String getDeathSound() {
    return null;
  }
  
  protected float getSoundVolume() {
    return 0.4F;
  }
  
  protected Item getDropItem() {
    return null;
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    int i = this.rand.nextInt(3 + lootingModifier) + 1;
    for (int j = 0; j < i; j++)
      entityDropItem(new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), 0.0F); 
  }
  
  public boolean isInWater() {
    return this.worldObj.handleMaterialAcceleration(getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.water, (Entity)this);
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    this.prevSquidPitch = this.squidPitch;
    this.prevSquidYaw = this.squidYaw;
    this.prevSquidRotation = this.squidRotation;
    this.lastTentacleAngle = this.tentacleAngle;
    this.squidRotation += this.rotationVelocity;
    if (this.squidRotation > 6.283185307179586D)
      if (this.worldObj.isRemote) {
        this.squidRotation = 6.2831855F;
      } else {
        this.squidRotation = (float)(this.squidRotation - 6.283185307179586D);
        if (this.rand.nextInt(10) == 0)
          this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F; 
        this.worldObj.setEntityState((Entity)this, (byte)19);
      }  
    if (this.inWater) {
      if (this.squidRotation < 3.1415927F) {
        float f = this.squidRotation / 3.1415927F;
        this.tentacleAngle = MathHelper.sin(f * f * 3.1415927F) * 3.1415927F * 0.25F;
        if (f > 0.75D) {
          this.randomMotionSpeed = 1.0F;
          this.field_70871_bB = 1.0F;
        } else {
          this.field_70871_bB *= 0.8F;
        } 
      } else {
        this.tentacleAngle = 0.0F;
        this.randomMotionSpeed *= 0.9F;
        this.field_70871_bB *= 0.99F;
      } 
      if (!this.worldObj.isRemote) {
        this.motionX = (this.randomMotionVecX * this.randomMotionSpeed);
        this.motionY = (this.randomMotionVecY * this.randomMotionSpeed);
        this.motionZ = (this.randomMotionVecZ * this.randomMotionSpeed);
      } 
      float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.renderYawOffset += (-((float)MathHelper.atan2(this.motionX, this.motionZ)) * 180.0F / 3.1415927F - this.renderYawOffset) * 0.1F;
      this.rotationYaw = this.renderYawOffset;
      this.squidYaw = (float)(this.squidYaw + Math.PI * this.field_70871_bB * 1.5D);
      this.squidPitch += (-((float)MathHelper.atan2(f1, this.motionY)) * 180.0F / 3.1415927F - this.squidPitch) * 0.1F;
    } else {
      this.tentacleAngle = MathHelper.abs(MathHelper.sin(this.squidRotation)) * 3.1415927F * 0.25F;
      if (!this.worldObj.isRemote) {
        this.motionX = 0.0D;
        this.motionY -= 0.08D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ = 0.0D;
      } 
      this.squidPitch = (float)(this.squidPitch + (-90.0F - this.squidPitch) * 0.02D);
    } 
  }
  
  public void moveEntityWithHeading(float strafe, float forward) {
    moveEntity(this.motionX, this.motionY, this.motionZ);
  }
  
  public boolean getCanSpawnHere() {
    return (this.posY > 45.0D && this.posY < this.worldObj.getSeaLevel() && super.getCanSpawnHere());
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 19) {
      this.squidRotation = 0.0F;
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  public void func_175568_b(float randomMotionVecXIn, float randomMotionVecYIn, float randomMotionVecZIn) {
    this.randomMotionVecX = randomMotionVecXIn;
    this.randomMotionVecY = randomMotionVecYIn;
    this.randomMotionVecZ = randomMotionVecZIn;
  }
  
  public boolean func_175567_n() {
    return (this.randomMotionVecX != 0.0F || this.randomMotionVecY != 0.0F || this.randomMotionVecZ != 0.0F);
  }
  
  static class AIMoveRandom extends EntityAIBase {
    private EntitySquid squid;
    
    public AIMoveRandom(EntitySquid p_i45859_1_) {
      this.squid = p_i45859_1_;
    }
    
    public boolean shouldExecute() {
      return true;
    }
    
    public void updateTask() {
      int i = this.squid.getAge();
      if (i > 100) {
        this.squid.func_175568_b(0.0F, 0.0F, 0.0F);
      } else if (this.squid.getRNG().nextInt(50) == 0 || !this.squid.inWater || !this.squid.func_175567_n()) {
        float f = this.squid.getRNG().nextFloat() * 3.1415927F * 2.0F;
        float f1 = MathHelper.cos(f) * 0.2F;
        float f2 = -0.1F + this.squid.getRNG().nextFloat() * 0.2F;
        float f3 = MathHelper.sin(f) * 0.2F;
        this.squid.func_175568_b(f1, f2, f3);
      } 
    }
  }
}
