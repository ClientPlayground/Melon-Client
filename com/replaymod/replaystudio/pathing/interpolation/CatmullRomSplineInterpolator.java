package com.replaymod.replaystudio.pathing.interpolation;

import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CatmullRomSplineInterpolator extends AbstractInterpolator {
  private final double alpha;
  
  private Map<PropertyPart<?>, PolynomialSplineInterpolator.Polynomial[]> cubicPolynomials;
  
  private Map<Property<?>, Set<Keyframe>> framesToProperty;
  
  @ConstructorProperties({"alpha"})
  public CatmullRomSplineInterpolator(double alpha) {
    this.cubicPolynomials = (Map)new HashMap<>();
    this.framesToProperty = new HashMap<>();
    this.alpha = alpha;
  }
  
  public double getAlpha() {
    return this.alpha;
  }
  
  private void addToMap(Property<?> property, Keyframe keyframe) {
    Set<Keyframe> set = this.framesToProperty.get(property);
    if (set == null)
      this.framesToProperty.put(property, set = new LinkedHashSet<>()); 
    set.add(keyframe);
  }
  
  protected Map<PropertyPart, InterpolationParameters> bakeInterpolation(Map<PropertyPart, InterpolationParameters> parameters) {
    this.framesToProperty.clear();
    for (PathSegment segment : getSegments()) {
      for (Property property : getKeyframeProperties()) {
        if (segment.getStartKeyframe().getValue(property).isPresent())
          addToMap(property, segment.getStartKeyframe()); 
        if (segment.getEndKeyframe().getValue(property).isPresent())
          addToMap(property, segment.getEndKeyframe()); 
      } 
    } 
    calcPolynomials();
    Map<PropertyPart, InterpolationParameters> lastParameters = new HashMap<>();
    for (Property<?> property : getKeyframeProperties()) {
      for (PropertyPart<?> part : (Iterable<PropertyPart<?>>)property.getParts()) {
        PolynomialSplineInterpolator.Polynomial[] polynomials = this.cubicPolynomials.get(part);
        PolynomialSplineInterpolator.Polynomial last = polynomials[polynomials.length - 1];
        double value = last.eval(1.0D);
        double velocity = last.derivative().eval(1.0D);
        double acceleration = last.derivative().derivative().eval(1.0D);
        lastParameters.put(part, new InterpolationParameters(value, velocity, acceleration));
      } 
    } 
    return lastParameters;
  }
  
  protected void calcPolynomials() {
    for (Map.Entry<Property<?>, Set<Keyframe>> e : this.framesToProperty.entrySet()) {
      Property<?> property = e.getKey();
      Set<Keyframe> keyframes = e.getValue();
      for (PropertyPart<?> part : (Iterable<PropertyPart<?>>)property.getParts()) {
        if (!part.isInterpolatable())
          continue; 
        List<Double> values = new ArrayList<>();
        if (Double.isNaN(part.getUpperBound())) {
          for (Keyframe k : keyframes)
            values.add(Double.valueOf(getValueAsDouble(k, part))); 
        } else {
          double bound = part.getUpperBound();
          double halfBound = bound / 2.0D;
          Iterator<Keyframe> it = keyframes.iterator();
          Double lastValue = null;
          Integer offset = null;
          while (it.hasNext()) {
            Keyframe keyframe = it.next();
            double value = mod(getValueAsDouble(keyframe, part), bound);
            if (lastValue == null) {
              lastValue = Double.valueOf(value);
              offset = Integer.valueOf((int)Math.floor(value / bound));
            } 
            if (Math.abs(value - lastValue.doubleValue()) > halfBound)
              if (lastValue.doubleValue() < halfBound) {
                Integer integer1 = offset, integer2 = offset = Integer.valueOf(offset.intValue() - 1);
              } else {
                Integer integer1 = offset, integer2 = offset = Integer.valueOf(offset.intValue() + 1);
              }  
            values.add(Double.valueOf(value + offset.intValue() * bound));
            lastValue = Double.valueOf(value);
          } 
        } 
        PolynomialSplineInterpolator.Polynomial[] polynomials = new PolynomialSplineInterpolator.Polynomial[values.size() - 1];
        for (int i = 0; i < values.size() - 1; i++) {
          double p0, p3, p1 = ((Double)values.get(i)).doubleValue();
          double p2 = ((Double)values.get(i + 1)).doubleValue();
          if (i > 0) {
            p0 = ((Double)values.get(i - 1)).doubleValue();
          } else {
            p0 = p1;
          } 
          if (i < keyframes.size() - 2) {
            p3 = ((Double)values.get(i + 2)).doubleValue();
          } else {
            p3 = p2;
          } 
          double t0 = this.alpha * (p2 - p0);
          double t1 = this.alpha * (p3 - p1);
          double[] c = { 2.0D * p1 - 2.0D * p2 + t0 + t1, -3.0D * p1 + 3.0D * p2 - 2.0D * t0 - t1, t0, p1 };
          polynomials[i] = new PolynomialSplineInterpolator.Polynomial(c);
        } 
        this.cubicPolynomials.put(part, polynomials);
      } 
    } 
  }
  
  private double mod(double val, double m) {
    double off = Math.floor(val / m);
    return val - off * m;
  }
  
  private <T> double getValueAsDouble(Keyframe keyframe, PropertyPart<T> part) {
    return part.toDouble(keyframe.getValue(part.getProperty()).get());
  }
  
  public <T> Optional<T> getValue(Property<T> property, long time) {
    Set<Keyframe> kfSet = this.framesToProperty.get(property);
    if (kfSet == null)
      return Optional.empty(); 
    T valueBefore = null;
    long timeBefore = -1L, timeAfter = -1L;
    int index = 0;
    int i = 0;
    for (Keyframe keyframe : kfSet) {
      if (keyframe.getTime() == time)
        return keyframe.getValue(property); 
      if (keyframe.getTime() < time) {
        index = i;
        timeBefore = keyframe.getTime();
        valueBefore = keyframe.getValue(property).get();
      } else if (keyframe.getTime() > time) {
        timeAfter = keyframe.getTime();
        break;
      } 
      i++;
    } 
    if (timeBefore == -1L || timeAfter == -1L)
      return Optional.empty(); 
    double fraction = (time - timeBefore) / (timeAfter - timeBefore);
    T interpolated = valueBefore;
    for (PropertyPart<T> part : (Iterable<PropertyPart<T>>)property.getParts()) {
      if (!part.isInterpolatable())
        continue; 
      PolynomialSplineInterpolator.Polynomial[] polynomials = this.cubicPolynomials.get(part);
      interpolated = (T)part.fromDouble(interpolated, polynomials[index].eval(fraction));
    } 
    return Optional.of(interpolated);
  }
}
