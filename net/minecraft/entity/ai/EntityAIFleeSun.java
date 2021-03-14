package net.minecraft.entity.ai;

import java.util.Random;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityAIFleeSun extends EntityAIBase {
  private EntityCreature theCreature;
  
  private double shelterX;
  
  private double shelterY;
  
  private double shelterZ;
  
  private double movementSpeed;
  
  private World theWorld;
  
  public EntityAIFleeSun(EntityCreature theCreatureIn, double movementSpeedIn) {
    this.theCreature = theCreatureIn;
    this.movementSpeed = movementSpeedIn;
    this.theWorld = theCreatureIn.worldObj;
    setMutexBits(1);
  }
  
  public boolean shouldExecute() {
    if (!this.theWorld.isDaytime())
      return false; 
    if (!this.theCreature.isBurning())
      return false; 
    if (!this.theWorld.canSeeSky(new BlockPos(this.theCreature.posX, (this.theCreature.getEntityBoundingBox()).minY, this.theCreature.posZ)))
      return false; 
    Vec3 vec3 = findPossibleShelter();
    if (vec3 == null)
      return false; 
    this.shelterX = vec3.xCoord;
    this.shelterY = vec3.yCoord;
    this.shelterZ = vec3.zCoord;
    return true;
  }
  
  public boolean continueExecuting() {
    return !this.theCreature.getNavigator().noPath();
  }
  
  public void startExecuting() {
    this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
  }
  
  private Vec3 findPossibleShelter() {
    Random random = this.theCreature.getRNG();
    BlockPos blockpos = new BlockPos(this.theCreature.posX, (this.theCreature.getEntityBoundingBox()).minY, this.theCreature.posZ);
    for (int i = 0; i < 10; i++) {
      BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
      if (!this.theWorld.canSeeSky(blockpos1) && this.theCreature.getBlockPathWeight(blockpos1) < 0.0F)
        return new Vec3(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()); 
    } 
    return null;
  }
}
