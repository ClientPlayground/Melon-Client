package com.replaymod.replaystudio.pathing.property;

import org.apache.commons.lang3.tuple.Triple;

public class PropertyParts {
  public static class ForInteger extends AbstractPropertyPart<Integer> {
    public ForInteger(Property<Integer> property, boolean interpolatable) {
      super(property, interpolatable);
    }
    
    public ForInteger(Property<Integer> property, boolean interpolatable, int upperBound) {
      super(property, interpolatable, upperBound);
    }
    
    public double toDouble(Integer value) {
      return value.intValue();
    }
    
    public Integer fromDouble(Integer value, double d) {
      return Integer.valueOf((int)Math.round(d));
    }
  }
  
  public static class ForDoubleTriple extends AbstractPropertyPart<Triple<Double, Double, Double>> {
    private final PropertyParts.TripleElement element;
    
    public ForDoubleTriple(Property<Triple<Double, Double, Double>> property, boolean interpolatable, PropertyParts.TripleElement element) {
      super(property, interpolatable);
      this.element = element;
    }
    
    public ForDoubleTriple(Property<Triple<Double, Double, Double>> property, boolean interpolatable, double upperBound, PropertyParts.TripleElement element) {
      super(property, interpolatable, upperBound);
      this.element = element;
    }
    
    public double toDouble(Triple<Double, Double, Double> value) {
      switch (this.element) {
        case LEFT:
          return ((Double)value.getLeft()).doubleValue();
        case MIDDLE:
          return ((Double)value.getMiddle()).doubleValue();
        case RIGHT:
          return ((Double)value.getRight()).doubleValue();
      } 
      throw new AssertionError(this.element);
    }
    
    public Triple<Double, Double, Double> fromDouble(Triple<Double, Double, Double> value, double d) {
      switch (this.element) {
        case LEFT:
          return Triple.of(Double.valueOf(d), value.getMiddle(), value.getRight());
        case MIDDLE:
          return Triple.of(value.getLeft(), Double.valueOf(d), value.getRight());
        case RIGHT:
          return Triple.of(value.getLeft(), value.getMiddle(), Double.valueOf(d));
      } 
      throw new AssertionError(this.element);
    }
  }
  
  public static class ForFloatTriple extends AbstractPropertyPart<Triple<Float, Float, Float>> {
    private final PropertyParts.TripleElement element;
    
    public ForFloatTriple(Property<Triple<Float, Float, Float>> property, boolean interpolatable, PropertyParts.TripleElement element) {
      super(property, interpolatable);
      this.element = element;
    }
    
    public ForFloatTriple(Property<Triple<Float, Float, Float>> property, boolean interpolatable, float upperBound, PropertyParts.TripleElement element) {
      super(property, interpolatable, upperBound);
      this.element = element;
    }
    
    public double toDouble(Triple<Float, Float, Float> value) {
      switch (this.element) {
        case LEFT:
          return ((Float)value.getLeft()).floatValue();
        case MIDDLE:
          return ((Float)value.getMiddle()).floatValue();
        case RIGHT:
          return ((Float)value.getRight()).floatValue();
      } 
      throw new AssertionError(this.element);
    }
    
    public Triple<Float, Float, Float> fromDouble(Triple<Float, Float, Float> value, double d) {
      switch (this.element) {
        case LEFT:
          return Triple.of(Float.valueOf((float)d), value.getMiddle(), value.getRight());
        case MIDDLE:
          return Triple.of(value.getLeft(), Float.valueOf((float)d), value.getRight());
        case RIGHT:
          return Triple.of(value.getLeft(), value.getMiddle(), Float.valueOf((float)d));
      } 
      throw new AssertionError(this.element);
    }
  }
  
  public enum TripleElement {
    LEFT, MIDDLE, RIGHT;
  }
}
