package com.replaymod.replaystudio.pathing.property;

public interface PropertyPart<T> {
  Property<T> getProperty();
  
  boolean isInterpolatable();
  
  double getUpperBound();
  
  double toDouble(T paramT);
  
  T fromDouble(T paramT, double paramDouble);
}
