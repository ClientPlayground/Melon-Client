package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

public class EulerAngle {
  private float x;
  
  private float y;
  
  private float z;
  
  public void setX(float x) {
    this.x = x;
  }
  
  public void setY(float y) {
    this.y = y;
  }
  
  public void setZ(float z) {
    this.z = z;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof EulerAngle))
      return false; 
    EulerAngle other = (EulerAngle)o;
    return !other.canEqual(this) ? false : ((Float.compare(getX(), other.getX()) != 0) ? false : ((Float.compare(getY(), other.getY()) != 0) ? false : (!(Float.compare(getZ(), other.getZ()) != 0))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof EulerAngle;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + Float.floatToIntBits(getX());
    result = result * 59 + Float.floatToIntBits(getY());
    return result * 59 + Float.floatToIntBits(getZ());
  }
  
  public String toString() {
    return "EulerAngle(x=" + getX() + ", y=" + getY() + ", z=" + getZ() + ")";
  }
  
  public EulerAngle(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public float getX() {
    return this.x;
  }
  
  public float getY() {
    return this.y;
  }
  
  public float getZ() {
    return this.z;
  }
}
