package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

public class Vector {
  private int blockX;
  
  private int blockY;
  
  private int blockZ;
  
  public void setBlockX(int blockX) {
    this.blockX = blockX;
  }
  
  public void setBlockY(int blockY) {
    this.blockY = blockY;
  }
  
  public void setBlockZ(int blockZ) {
    this.blockZ = blockZ;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Vector))
      return false; 
    Vector other = (Vector)o;
    return !other.canEqual(this) ? false : ((getBlockX() != other.getBlockX()) ? false : ((getBlockY() != other.getBlockY()) ? false : (!(getBlockZ() != other.getBlockZ()))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Vector;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getBlockX();
    result = result * 59 + getBlockY();
    return result * 59 + getBlockZ();
  }
  
  public String toString() {
    return "Vector(blockX=" + getBlockX() + ", blockY=" + getBlockY() + ", blockZ=" + getBlockZ() + ")";
  }
  
  public Vector(int blockX, int blockY, int blockZ) {
    this.blockX = blockX;
    this.blockY = blockY;
    this.blockZ = blockZ;
  }
  
  public int getBlockX() {
    return this.blockX;
  }
  
  public int getBlockY() {
    return this.blockY;
  }
  
  public int getBlockZ() {
    return this.blockZ;
  }
}
