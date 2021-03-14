package com.replaymod.replaystudio.data;

public final class Marker {
  private String name;
  
  private int time;
  
  private double x;
  
  private double y;
  
  private double z;
  
  private float yaw;
  
  private float pitch;
  
  private float roll;
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public int getTime() {
    return this.time;
  }
  
  public void setTime(int time) {
    this.time = time;
  }
  
  public double getX() {
    return this.x;
  }
  
  public void setX(double x) {
    this.x = x;
  }
  
  public double getY() {
    return this.y;
  }
  
  public void setY(double y) {
    this.y = y;
  }
  
  public double getZ() {
    return this.z;
  }
  
  public void setZ(double z) {
    this.z = z;
  }
  
  public float getYaw() {
    return this.yaw;
  }
  
  public void setYaw(float yaw) {
    this.yaw = yaw;
  }
  
  public float getPitch() {
    return this.pitch;
  }
  
  public void setPitch(float pitch) {
    this.pitch = pitch;
  }
  
  public float getRoll() {
    return this.roll;
  }
  
  public void setRoll(float roll) {
    this.roll = roll;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof Marker))
      return false; 
    Marker marker = (Marker)o;
    if (this.time != marker.time)
      return false; 
    if (Double.compare(marker.x, this.x) != 0)
      return false; 
    if (Double.compare(marker.y, this.y) != 0)
      return false; 
    if (Double.compare(marker.z, this.z) != 0)
      return false; 
    if (Float.compare(marker.yaw, this.yaw) != 0)
      return false; 
    if (Float.compare(marker.pitch, this.pitch) != 0)
      return false; 
    if (Float.compare(marker.roll, this.roll) != 0)
      return false; 
    if ((this.name != null) ? !this.name.equals(marker.name) : (marker.name != null))
      return false; 
  }
  
  public int hashCode() {
    int result = (this.name != null) ? this.name.hashCode() : 0;
    result = 31 * result + this.time;
    long temp = Double.doubleToLongBits(this.x);
    result = 31 * result + (int)(temp ^ temp >>> 32L);
    temp = Double.doubleToLongBits(this.y);
    result = 31 * result + (int)(temp ^ temp >>> 32L);
    temp = Double.doubleToLongBits(this.z);
    result = 31 * result + (int)(temp ^ temp >>> 32L);
    result = 31 * result + ((this.yaw != 0.0F) ? Float.floatToIntBits(this.yaw) : 0);
    result = 31 * result + ((this.pitch != 0.0F) ? Float.floatToIntBits(this.pitch) : 0);
    result = 31 * result + ((this.roll != 0.0F) ? Float.floatToIntBits(this.roll) : 0);
    return result;
  }
  
  public String toString() {
    return "Marker{name='" + this.name + '\'' + ", time=" + this.time + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ", roll=" + this.roll + '}';
  }
}
