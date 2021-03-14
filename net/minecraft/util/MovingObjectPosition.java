package net.minecraft.util;

import net.minecraft.entity.Entity;

public class MovingObjectPosition {
  private BlockPos blockPos;
  
  public MovingObjectType typeOfHit;
  
  public EnumFacing sideHit;
  
  public Vec3 hitVec;
  
  public Entity entityHit;
  
  public MovingObjectPosition(Vec3 hitVecIn, EnumFacing facing, BlockPos blockPosIn) {
    this(MovingObjectType.BLOCK, hitVecIn, facing, blockPosIn);
  }
  
  public MovingObjectPosition(Vec3 p_i45552_1_, EnumFacing facing) {
    this(MovingObjectType.BLOCK, p_i45552_1_, facing, BlockPos.ORIGIN);
  }
  
  public MovingObjectPosition(Entity entityIn) {
    this(entityIn, new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ));
  }
  
  public MovingObjectPosition(MovingObjectType typeOfHitIn, Vec3 hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
    this.typeOfHit = typeOfHitIn;
    this.blockPos = blockPosIn;
    this.sideHit = sideHitIn;
    this.hitVec = new Vec3(hitVecIn.xCoord, hitVecIn.yCoord, hitVecIn.zCoord);
  }
  
  public MovingObjectPosition(Entity entityHitIn, Vec3 hitVecIn) {
    this.typeOfHit = MovingObjectType.ENTITY;
    this.entityHit = entityHitIn;
    this.hitVec = hitVecIn;
  }
  
  public BlockPos getBlockPos() {
    return this.blockPos;
  }
  
  public String toString() {
    return "HitResult{type=" + this.typeOfHit + ", blockpos=" + this.blockPos + ", f=" + this.sideHit + ", pos=" + this.hitVec + ", entity=" + this.entityHit + '}';
  }
  
  public enum MovingObjectType {
    MISS, BLOCK, ENTITY;
  }
}
