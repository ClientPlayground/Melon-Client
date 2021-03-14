package com.replaymod.replaystudio.pathing.interpolation;

import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LinearInterpolator extends AbstractInterpolator {
  private Map<Property, Set<Keyframe>> framesToProperty = new HashMap<>();
  
  private void addToMap(Property property, Keyframe keyframe) {
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
    Keyframe lastKeyframe = ((PathSegment)getSegments().get(getSegments().size() - 1)).getEndKeyframe();
    Map<PropertyPart, InterpolationParameters> lastParameters = new HashMap<>();
    for (Property<?> property : getKeyframeProperties()) {
      Optional optionalValue = lastKeyframe.getValue(property);
      if (optionalValue.isPresent()) {
        Object value = optionalValue.get();
        for (PropertyPart part : property.getParts())
          lastParameters.put(part, new InterpolationParameters(part.toDouble(value), 1.0D, 0.0D)); 
      } 
    } 
    return lastParameters;
  }
  
  public <T> Optional<T> getValue(Property<T> property, long time) {
    Set<Keyframe> kfSet = this.framesToProperty.get(property);
    if (kfSet == null)
      return Optional.empty(); 
    Keyframe kfBefore = null, kfAfter = null;
    for (Keyframe keyframe : kfSet) {
      if (keyframe.getTime() == time)
        return keyframe.getValue(property); 
      if (keyframe.getTime() < time) {
        kfBefore = keyframe;
        continue;
      } 
      if (keyframe.getTime() > time) {
        kfAfter = keyframe;
        break;
      } 
    } 
    if (kfBefore == null || kfAfter == null)
      return Optional.empty(); 
    T valueBefore = kfBefore.getValue(property).get();
    T valueAfter = kfAfter.getValue(property).get();
    double fraction = (time - kfBefore.getTime()) / (kfAfter.getTime() - kfBefore.getTime());
    T interpolated = valueBefore;
    for (PropertyPart<T> part : (Iterable<PropertyPart<T>>)property.getParts()) {
      if (part.isInterpolatable()) {
        double before = part.toDouble(valueBefore);
        double after = part.toDouble(valueAfter);
        double bound = part.getUpperBound();
        if (!Double.isNaN(bound)) {
          before = mod(before, bound);
          after = mod(after, bound);
          if ((((before < bound / 2.0D) ? 1 : 0) ^ ((after < bound / 2.0D) ? 1 : 0)) != 0)
            if (before < bound / 2.0D) {
              after -= bound;
            } else {
              after += bound;
            }  
        } 
        double value = (after - before) * fraction + before;
        if (!Double.isNaN(bound))
          value = mod(value, bound); 
        interpolated = (T)part.fromDouble(interpolated, value);
      } 
    } 
    return Optional.of(interpolated);
  }
  
  private double mod(double val, double m) {
    double off = Math.floor(val / m);
    return val - off * m;
  }
}
