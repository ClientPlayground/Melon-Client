package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveThroughVillage extends EntityAIBase {
  private EntityCreature theEntity;
  
  private double movementSpeed;
  
  private PathEntity entityPathNavigate;
  
  private VillageDoorInfo doorInfo;
  
  private boolean isNocturnal;
  
  private List<VillageDoorInfo> doorList = Lists.newArrayList();
  
  public EntityAIMoveThroughVillage(EntityCreature theEntityIn, double movementSpeedIn, boolean isNocturnalIn) {
    this.theEntity = theEntityIn;
    this.movementSpeed = movementSpeedIn;
    this.isNocturnal = isNocturnalIn;
    setMutexBits(1);
    if (!(theEntityIn.getNavigator() instanceof PathNavigateGround))
      throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal"); 
  }
  
  public boolean shouldExecute() {
    resizeDoorList();
    if (this.isNocturnal && this.theEntity.worldObj.isDaytime())
      return false; 
    Village village = this.theEntity.worldObj.getVillageCollection().getNearestVillage(new BlockPos((Entity)this.theEntity), 0);
    if (village == null)
      return false; 
    this.doorInfo = findNearestDoor(village);
    if (this.doorInfo == null)
      return false; 
    PathNavigateGround pathnavigateground = (PathNavigateGround)this.theEntity.getNavigator();
    boolean flag = pathnavigateground.getEnterDoors();
    pathnavigateground.setBreakDoors(false);
    this.entityPathNavigate = pathnavigateground.getPathToPos(this.doorInfo.getDoorBlockPos());
    pathnavigateground.setBreakDoors(flag);
    if (this.entityPathNavigate != null)
      return true; 
    Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 10, 7, new Vec3(this.doorInfo.getDoorBlockPos().getX(), this.doorInfo.getDoorBlockPos().getY(), this.doorInfo.getDoorBlockPos().getZ()));
    if (vec3 == null)
      return false; 
    pathnavigateground.setBreakDoors(false);
    this.entityPathNavigate = this.theEntity.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    pathnavigateground.setBreakDoors(flag);
    return (this.entityPathNavigate != null);
  }
  
  public boolean continueExecuting() {
    if (this.theEntity.getNavigator().noPath())
      return false; 
    float f = this.theEntity.width + 4.0F;
    return (this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) > (f * f));
  }
  
  public void startExecuting() {
    this.theEntity.getNavigator().setPath(this.entityPathNavigate, this.movementSpeed);
  }
  
  public void resetTask() {
    if (this.theEntity.getNavigator().noPath() || this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) < 16.0D)
      this.doorList.add(this.doorInfo); 
  }
  
  private VillageDoorInfo findNearestDoor(Village villageIn) {
    VillageDoorInfo villagedoorinfo = null;
    int i = Integer.MAX_VALUE;
    for (VillageDoorInfo villagedoorinfo1 : villageIn.getVillageDoorInfoList()) {
      int j = villagedoorinfo1.getDistanceSquared(MathHelper.floor_double(this.theEntity.posX), MathHelper.floor_double(this.theEntity.posY), MathHelper.floor_double(this.theEntity.posZ));
      if (j < i && !doesDoorListContain(villagedoorinfo1)) {
        villagedoorinfo = villagedoorinfo1;
        i = j;
      } 
    } 
    return villagedoorinfo;
  }
  
  private boolean doesDoorListContain(VillageDoorInfo doorInfoIn) {
    for (VillageDoorInfo villagedoorinfo : this.doorList) {
      if (doorInfoIn.getDoorBlockPos().equals(villagedoorinfo.getDoorBlockPos()))
        return true; 
    } 
    return false;
  }
  
  private void resizeDoorList() {
    if (this.doorList.size() > 15)
      this.doorList.remove(0); 
  }
}
