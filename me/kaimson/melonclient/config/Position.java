package me.kaimson.melonclient.config;

import org.apache.commons.lang3.mutable.MutableFloat;

public class Position {
  private MutableFloat x;
  
  private MutableFloat y;
  
  public Position(float x, float y) {
    this.x = new MutableFloat(x);
    this.y = new MutableFloat(y);
  }
  
  public void setX(float x) {
    this.x.setValue(x);
  }
  
  public float getX() {
    return this.x.getValue().floatValue();
  }
  
  public void setY(float y) {
    this.y.setValue(y);
  }
  
  public float getY() {
    return this.y.getValue().floatValue();
  }
}
