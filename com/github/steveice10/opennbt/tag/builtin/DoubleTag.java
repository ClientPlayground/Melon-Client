package com.github.steveice10.opennbt.tag.builtin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends Tag {
  private double value;
  
  public DoubleTag(String name) {
    this(name, 0.0D);
  }
  
  public DoubleTag(String name, double value) {
    super(name);
    this.value = value;
  }
  
  public Double getValue() {
    return Double.valueOf(this.value);
  }
  
  public void setValue(double value) {
    this.value = value;
  }
  
  public void read(DataInput in) throws IOException {
    this.value = in.readDouble();
  }
  
  public void write(DataOutput out) throws IOException {
    out.writeDouble(this.value);
  }
  
  public DoubleTag clone() {
    return new DoubleTag(getName(), getValue().doubleValue());
  }
}
