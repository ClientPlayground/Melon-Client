package com.sun.jna.ptr;

public class DoubleByReference extends ByReference {
  public DoubleByReference() {
    this(0.0D);
  }
  
  public DoubleByReference(double value) {
    super(8);
    setValue(value);
  }
  
  public void setValue(double value) {
    getPointer().setDouble(0L, value);
  }
  
  public double getValue() {
    return getPointer().getDouble(0L);
  }
}
