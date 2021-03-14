package com.github.steveice10.opennbt.tag.builtin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends Tag {
  private float value;
  
  public FloatTag(String name) {
    this(name, 0.0F);
  }
  
  public FloatTag(String name, float value) {
    super(name);
    this.value = value;
  }
  
  public Float getValue() {
    return Float.valueOf(this.value);
  }
  
  public void setValue(float value) {
    this.value = value;
  }
  
  public void read(DataInput in) throws IOException {
    this.value = in.readFloat();
  }
  
  public void write(DataOutput out) throws IOException {
    out.writeFloat(this.value);
  }
  
  public FloatTag clone() {
    return new FloatTag(getName(), getValue().floatValue());
  }
}
