package com.replaymod.replaystudio.util;

import java.util.Objects;

public class IPosition {
  public static final IPosition NULL = new IPosition(0, 0, 0);
  
  private final int x;
  
  private final int y;
  
  private final int z;
  
  public IPosition(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public int getX() {
    return this.x;
  }
  
  public int getY() {
    return this.y;
  }
  
  public int getZ() {
    return this.z;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    IPosition iPosition = (IPosition)o;
    return (this.x == iPosition.x && this.y == iPosition.y && this.z == iPosition.z);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.z) });
  }
  
  public String toString() {
    return "IPosition(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
  }
}
