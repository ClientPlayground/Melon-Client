package com.replaymod.replaystudio.util;

public class Location {
  public static final Location NULL = new Location(0.0D, 0.0D, 0.0D);
  
  private final double x;
  
  private final double y;
  
  private final double z;
  
  private final float yaw;
  
  private final float pitch;
  
  public Location(DPosition position) {
    this(position, 0.0F, 0.0F);
  }
  
  public Location(DPosition position, float yaw, float pitch) {
    this(position.getX(), position.getY(), position.getZ(), yaw, pitch);
  }
  
  public Location(double x, double y, double z) {
    this(x, y, z, 0.0F, 0.0F);
  }
  
  public Location(double x, double y, double z, float yaw, float pitch) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }
  
  public DPosition getDPosition() {
    return new DPosition(this.x, this.y, this.z);
  }
  
  public double getX() {
    return this.x;
  }
  
  public double getY() {
    return this.y;
  }
  
  public double getZ() {
    return this.z;
  }
  
  public float getYaw() {
    return this.yaw;
  }
  
  public float getPitch() {
    return this.pitch;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Location))
      return false; 
    Location other = (Location)o;
    if (!other.canEqual(this))
      return false; 
    if (Double.compare(this.x, other.x) != 0)
      return false; 
    if (Double.compare(this.y, other.y) != 0)
      return false; 
    if (Double.compare(this.z, other.z) != 0)
      return false; 
    if (Float.compare(this.yaw, other.yaw) != 0)
      return false; 
    if (Float.compare(this.pitch, other.pitch) != 0)
      return false; 
    return true;
  }
  
  public int hashCode() {
    int result = 1;
    long x = Double.doubleToLongBits(this.x);
    result = result * 59 + (int)(x >>> 32L ^ x);
    long y = Double.doubleToLongBits(this.y);
    result = result * 59 + (int)(y >>> 32L ^ y);
    long z = Double.doubleToLongBits(this.z);
    result = result * 59 + (int)(z >>> 32L ^ z);
    result = result * 59 + Float.floatToIntBits(this.yaw);
    result = result * 59 + Float.floatToIntBits(this.pitch);
    return result;
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Location;
  }
  
  public String toString() {
    return "Location(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ")";
  }
}
