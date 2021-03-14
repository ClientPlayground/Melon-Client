package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAILookIdle extends EntityAIBase {
  private EntityLiving idleEntity;
  
  private double lookX;
  
  private double lookZ;
  
  private int idleTime;
  
  public EntityAILookIdle(EntityLiving entitylivingIn) {
    this.idleEntity = entitylivingIn;
    setMutexBits(3);
  }
  
  public boolean shouldExecute() {
    return (this.idleEntity.getRNG().nextFloat() < 0.02F);
  }
  
  public boolean continueExecuting() {
    return (this.idleTime >= 0);
  }
  
  public void startExecuting() {
    double d0 = 6.283185307179586D * this.idleEntity.getRNG().nextDouble();
    this.lookX = Math.cos(d0);
    this.lookZ = Math.sin(d0);
    this.idleTime = 20 + this.idleEntity.getRNG().nextInt(20);
  }
  
  public void updateTask() {
    this.idleTime--;
    this.idleEntity.getLookHelper().setLookPosition(this.idleEntity.posX + this.lookX, this.idleEntity.posY + this.idleEntity.getEyeHeight(), this.idleEntity.posZ + this.lookZ, 10.0F, this.idleEntity.getVerticalFaceSpeed());
  }
}
