package com.replaymod.replaystudio.pathing.path;

import com.replaymod.replaystudio.pathing.property.Property;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;

public interface Keyframe {
  long getTime();
  
  @NonNull
  <T> Optional<T> getValue(Property<T> paramProperty);
  
  <T> void setValue(Property<T> paramProperty, T paramT);
  
  void removeProperty(Property paramProperty);
  
  Set<Property> getProperties();
}
