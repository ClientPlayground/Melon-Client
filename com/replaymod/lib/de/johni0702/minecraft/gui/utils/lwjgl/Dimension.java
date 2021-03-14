package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

import java.io.Serializable;

public final class Dimension implements Serializable, ReadableDimension, WritableDimension {
  static final long serialVersionUID = 1L;
  
  private int width;
  
  private int height;
  
  public Dimension() {}
  
  public Dimension(int w, int h) {
    this.width = w;
    this.height = h;
  }
  
  public Dimension(ReadableDimension d) {
    setSize(d);
  }
  
  public void setSize(int w, int h) {
    this.width = w;
    this.height = h;
  }
  
  public void setSize(ReadableDimension d) {
    this.width = d.getWidth();
    this.height = d.getHeight();
  }
  
  public void getSize(WritableDimension dest) {
    dest.setSize(this);
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof ReadableDimension) {
      ReadableDimension d = (ReadableDimension)obj;
      return (this.width == d.getWidth() && this.height == d.getHeight());
    } 
    return false;
  }
  
  public int hashCode() {
    int sum = this.width + this.height;
    return sum * (sum + 1) / 2 + this.width;
  }
  
  public String toString() {
    return getClass().getName() + "[width=" + this.width + ",height=" + this.height + "]";
  }
  
  public int getHeight() {
    return this.height;
  }
  
  public void setHeight(int height) {
    this.height = height;
  }
  
  public int getWidth() {
    return this.width;
  }
  
  public void setWidth(int width) {
    this.width = width;
  }
}
