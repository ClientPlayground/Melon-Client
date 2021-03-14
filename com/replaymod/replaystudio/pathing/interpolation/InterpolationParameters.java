package com.replaymod.replaystudio.pathing.interpolation;

import java.beans.ConstructorProperties;

public class InterpolationParameters {
  private final double value;
  
  private final double velocity;
  
  private final double acceleration;
  
  @ConstructorProperties({"value", "velocity", "acceleration"})
  public InterpolationParameters(double value, double velocity, double acceleration) {
    this.value = value;
    this.velocity = velocity;
    this.acceleration = acceleration;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof InterpolationParameters))
      return false; 
    InterpolationParameters other = (InterpolationParameters)o;
    return !other.canEqual(this) ? false : ((Double.compare(getValue(), other.getValue()) != 0) ? false : ((Double.compare(getVelocity(), other.getVelocity()) != 0) ? false : (!(Double.compare(getAcceleration(), other.getAcceleration()) != 0))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof InterpolationParameters;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    long $value = Double.doubleToLongBits(getValue());
    result = result * 59 + (int)($value >>> 32L ^ $value);
    long $velocity = Double.doubleToLongBits(getVelocity());
    result = result * 59 + (int)($velocity >>> 32L ^ $velocity);
    long $acceleration = Double.doubleToLongBits(getAcceleration());
    return result * 59 + (int)($acceleration >>> 32L ^ $acceleration);
  }
  
  public String toString() {
    return "InterpolationParameters(value=" + getValue() + ", velocity=" + getVelocity() + ", acceleration=" + getAcceleration() + ")";
  }
  
  public double getValue() {
    return this.value;
  }
  
  public double getVelocity() {
    return this.velocity;
  }
  
  public double getAcceleration() {
    return this.acceleration;
  }
}
