package net.minecraft.world.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class NodeProcessor {
  protected IBlockAccess blockaccess;
  
  protected IntHashMap<PathPoint> pointMap = new IntHashMap();
  
  protected int entitySizeX;
  
  protected int entitySizeY;
  
  protected int entitySizeZ;
  
  public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
    this.blockaccess = iblockaccessIn;
    this.pointMap.clearMap();
    this.entitySizeX = MathHelper.floor_float(entityIn.width + 1.0F);
    this.entitySizeY = MathHelper.floor_float(entityIn.height + 1.0F);
    this.entitySizeZ = MathHelper.floor_float(entityIn.width + 1.0F);
  }
  
  public void postProcess() {}
  
  protected PathPoint openPoint(int x, int y, int z) {
    int i = PathPoint.makeHash(x, y, z);
    PathPoint pathpoint = (PathPoint)this.pointMap.lookup(i);
    if (pathpoint == null) {
      pathpoint = new PathPoint(x, y, z);
      this.pointMap.addKey(i, pathpoint);
    } 
    return pathpoint;
  }
  
  public abstract PathPoint getPathPointTo(Entity paramEntity);
  
  public abstract PathPoint getPathPointToCoords(Entity paramEntity, double paramDouble1, double paramDouble2, double paramDouble3);
  
  public abstract int findPathOptions(PathPoint[] paramArrayOfPathPoint, Entity paramEntity, PathPoint paramPathPoint1, PathPoint paramPathPoint2, float paramFloat);
}
