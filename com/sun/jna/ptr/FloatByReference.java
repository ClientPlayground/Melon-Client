package com.sun.jna.ptr;

public class FloatByReference extends ByReference {
  public FloatByReference() {
    this(0.0F);
  }
  
  public FloatByReference(float value) {
    super(4);
    setValue(value);
  }
  
  public void setValue(float value) {
    getPointer().setFloat(0L, value);
  }
  
  public float getValue() {
    return getPointer().getFloat(0L);
  }
}
