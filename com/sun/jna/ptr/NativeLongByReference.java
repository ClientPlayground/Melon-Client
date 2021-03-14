package com.sun.jna.ptr;

import com.sun.jna.NativeLong;

public class NativeLongByReference extends ByReference {
  public NativeLongByReference() {
    this(new NativeLong(0L));
  }
  
  public NativeLongByReference(NativeLong value) {
    super(NativeLong.SIZE);
    setValue(value);
  }
  
  public void setValue(NativeLong value) {
    getPointer().setNativeLong(0L, value);
  }
  
  public NativeLong getValue() {
    return getPointer().getNativeLong(0L);
  }
}
