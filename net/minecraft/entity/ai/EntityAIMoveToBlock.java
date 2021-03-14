package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class EntityAIMoveToBlock extends EntityAIBase {
  private final EntityCreature theEntity;
  
  private final double movementSpeed;
  
  protected int runDelay;
  
  private int timeoutCounter;
  
  private int field_179490_f;
  
  protected BlockPos destinationBlock = BlockPos.ORIGIN;
  
  private boolean isAboveDestination;
  
  private int searchLength;
  
  public EntityAIMoveToBlock(EntityCreature creature, double speedIn, int length) {
    this.theEntity = creature;
    this.movementSpeed = speedIn;
    this.searchLength = length;
    setMutexBits(5);
  }
  
  public boolean shouldExecute() {
    if (this.runDelay > 0) {
      this.runDelay--;
      return false;
    } 
    this.runDelay = 200 + this.theEntity.getRNG().nextInt(200);
    return searchForDestination();
  }
  
  public boolean continueExecuting() {
    return (this.timeoutCounter >= -this.field_179490_f && this.timeoutCounter <= 1200 && shouldMoveTo(this.theEntity.worldObj, this.destinationBlock));
  }
  
  public void startExecuting() {
    this.theEntity.getNavigator().tryMoveToXYZ(this.destinationBlock.getX() + 0.5D, (this.destinationBlock.getY() + 1), this.destinationBlock.getZ() + 0.5D, this.movementSpeed);
    this.timeoutCounter = 0;
    this.field_179490_f = this.theEntity.getRNG().nextInt(this.theEntity.getRNG().nextInt(1200) + 1200) + 1200;
  }
  
  public void resetTask() {}
  
  public void updateTask() {
    if (this.theEntity.getDistanceSqToCenter(this.destinationBlock.up()) > 1.0D) {
      this.isAboveDestination = false;
      this.timeoutCounter++;
      if (this.timeoutCounter % 40 == 0)
        this.theEntity.getNavigator().tryMoveToXYZ(this.destinationBlock.getX() + 0.5D, (this.destinationBlock.getY() + 1), this.destinationBlock.getZ() + 0.5D, this.movementSpeed); 
    } else {
      this.isAboveDestination = true;
      this.timeoutCounter--;
    } 
  }
  
  protected boolean getIsAboveDestination() {
    return this.isAboveDestination;
  }
  
  private boolean searchForDestination() {
    int i = this.searchLength;
    int j = 1;
    BlockPos blockpos = new BlockPos((Entity)this.theEntity);
    int k;
    for (k = 0; k <= 1; k = (k > 0) ? -k : (1 - k)) {
      for (int l = 0; l < i; l++) {
        int i1;
        for (i1 = 0; i1 <= l; i1 = (i1 > 0) ? -i1 : (1 - i1)) {
          int j1;
          for (j1 = (i1 < l && i1 > -l) ? l : 0; j1 <= l; j1 = (j1 > 0) ? -j1 : (1 - j1)) {
            BlockPos blockpos1 = blockpos.add(i1, k - 1, j1);
            if (this.theEntity.isWithinHomeDistanceFromPosition(blockpos1) && shouldMoveTo(this.theEntity.worldObj, blockpos1)) {
              this.destinationBlock = blockpos1;
              return true;
            } 
          } 
        } 
      } 
    } 
    return false;
  }
  
  protected abstract boolean shouldMoveTo(World paramWorld, BlockPos paramBlockPos);
}
