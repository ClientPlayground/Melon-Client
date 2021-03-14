package net.optifine;

import java.util.Comparator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

public class ChunkPosComparator implements Comparator<ChunkCoordIntPair> {
  private int chunkPosX;
  
  private int chunkPosZ;
  
  private double yawRad;
  
  private double pitchNorm;
  
  public ChunkPosComparator(int chunkPosX, int chunkPosZ, double yawRad, double pitchRad) {
    this.chunkPosX = chunkPosX;
    this.chunkPosZ = chunkPosZ;
    this.yawRad = yawRad;
    this.pitchNorm = 1.0D - MathHelper.clamp_double(Math.abs(pitchRad) / 1.5707963267948966D, 0.0D, 1.0D);
  }
  
  public int compare(ChunkCoordIntPair cp1, ChunkCoordIntPair cp2) {
    int i = getDistSq(cp1);
    int j = getDistSq(cp2);
    return i - j;
  }
  
  private int getDistSq(ChunkCoordIntPair cp) {
    int i = cp.chunkXPos - this.chunkPosX;
    int j = cp.chunkZPos - this.chunkPosZ;
    int k = i * i + j * j;
    double d0 = MathHelper.atan2(j, i);
    double d1 = Math.abs(d0 - this.yawRad);
    if (d1 > Math.PI)
      d1 = 6.283185307179586D - d1; 
    k = (int)(k * 1000.0D * this.pitchNorm * d1 * d1);
    return k;
  }
}
