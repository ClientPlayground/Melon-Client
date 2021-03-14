package me.kaimson.melonclient.config;

public enum AnchorPoint {
  TOP_LEFT(0),
  TOP_CENTER(1),
  TOP_RIGHT(2),
  BOTTOM_LEFT(3),
  BOTTOM_CENTER(4),
  BOTTOM_RIGHT(5),
  CENTER_LEFT(6),
  CENTER(7),
  CENTER_RIGHT(8);
  
  private final int id;
  
  public int getId() {
    return this.id;
  }
  
  AnchorPoint(int id) {
    this.id = id;
  }
  
  public static AnchorPoint fromId(int id) {
    for (AnchorPoint ap : values()) {
      if (ap.getId() == id)
        return ap; 
    } 
    return null;
  }
  
  public int getX(int maxX) {
    switch (this) {
      case TOP_RIGHT:
      case BOTTOM_RIGHT:
      case CENTER_RIGHT:
        x = maxX;
        return x;
      case BOTTOM_CENTER:
      case CENTER:
      case TOP_CENTER:
        x = maxX / 2;
        return x;
    } 
    int x = 0;
    return x;
  }
  
  public int getY(int maxY) {
    switch (this) {
      case BOTTOM_RIGHT:
      case BOTTOM_CENTER:
      case BOTTOM_LEFT:
        y = maxY;
        return y;
      case CENTER_RIGHT:
      case CENTER:
      case CENTER_LEFT:
        y = maxY / 2;
        return y;
    } 
    int y = 0;
    return y;
  }
}
