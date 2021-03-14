package com.replaymod.replaystudio.pathing.interpolation;

import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;

public interface Interpolator {
  void registerProperty(Property paramProperty);
  
  void unregisterProperty(Property paramProperty);
  
  @NonNull
  Collection<Property> getKeyframeProperties();
  
  void addSegment(PathSegment paramPathSegment);
  
  void removeSegment(PathSegment paramPathSegment);
  
  @NonNull
  List<PathSegment> getSegments();
  
  @NonNull
  Map<PropertyPart, InterpolationParameters> bake(Map<PropertyPart, InterpolationParameters> paramMap);
  
  boolean isDirty();
  
  <T> Optional<T> getValue(Property<T> paramProperty, long paramLong);
}
