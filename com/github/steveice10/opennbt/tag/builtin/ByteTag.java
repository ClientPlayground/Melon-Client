package com.github.steveice10.opennbt.tag.builtin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends Tag {
  private byte value;
  
  public ByteTag(String name) {
    this(name, (byte)0);
  }
  
  public ByteTag(String name, byte value) {
    super(name);
    this.value = value;
  }
  
  public Byte getValue() {
    return Byte.valueOf(this.value);
  }
  
  public void setValue(byte value) {
    this.value = value;
  }
  
  public void read(DataInput in) throws IOException {
    this.value = in.readByte();
  }
  
  public void write(DataOutput out) throws IOException {
    out.writeByte(this.value);
  }
  
  public ByteTag clone() {
    return new ByteTag(getName(), getValue().byteValue());
  }
}
