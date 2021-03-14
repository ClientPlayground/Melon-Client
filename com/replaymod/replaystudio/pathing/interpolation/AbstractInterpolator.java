package com.replaymod.replaystudio.pathing.interpolation;

import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractInterpolator implements Interpolator {
  private List<PathSegment> segments = new LinkedList<>();
  
  private boolean dirty;
  
  private final Set<Property> properties = new HashSet<>();
  
  public Collection<Property> getKeyframeProperties() {
    return Collections.unmodifiableCollection(this.properties);
  }
  
  public void registerProperty(Property property) {
    if (this.properties.add(property))
      this.dirty = true; 
  }
  
  public void unregisterProperty(Property property) {
    if (this.properties.remove(property))
      this.dirty = true; 
  }
  
  public void addSegment(PathSegment segment) {
    this.segments.add(segment);
    this.dirty = true;
  }
  
  public void removeSegment(PathSegment segment) {
    this.segments.remove(segment);
    this.dirty = true;
  }
  
  public List<PathSegment> getSegments() {
    return Collections.unmodifiableList(this.segments);
  }
  
  public Map<PropertyPart, InterpolationParameters> bake(Map<PropertyPart, InterpolationParameters> parameters) {
    if (this.segments.isEmpty())
      throw new IllegalStateException("No segments have been added yet."); 
    Collections.sort(this.segments, new Comparator<PathSegment>() {
          public int compare(PathSegment s1, PathSegment s2) {
            return Long.compare(s1.getStartKeyframe().getTime(), s2.getStartKeyframe().getTime());
          }
        });
    Iterator<PathSegment> iter = this.segments.iterator();
    PathSegment last = iter.next();
    while (iter.hasNext()) {
      if (last.getEndKeyframe() != (last = iter.next()).getStartKeyframe())
        throw new IllegalStateException("Segments are not continuous."); 
    } 
    return bakeInterpolation(parameters);
  }
  
  protected abstract Map<PropertyPart, InterpolationParameters> bakeInterpolation(Map<PropertyPart, InterpolationParameters> paramMap);
  
  public boolean isDirty() {
    return this.dirty;
  }
}
