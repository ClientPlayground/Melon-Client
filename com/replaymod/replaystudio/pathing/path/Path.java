package com.replaymod.replaystudio.pathing.path;

import com.replaymod.replaystudio.pathing.property.Property;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;

public interface Path {
  Timeline getTimeline();
  
  @NonNull
  Collection<Keyframe> getKeyframes();
  
  @NonNull
  Collection<PathSegment> getSegments();
  
  void update();
  
  void updateAll();
  
  <T> Optional<T> getValue(Property<T> paramProperty, long paramLong);
  
  Keyframe insert(long paramLong);
  
  Keyframe getKeyframe(long paramLong);
  
  void insert(Keyframe paramKeyframe);
  
  void remove(Keyframe paramKeyframe, boolean paramBoolean);
  
  void setActive(boolean paramBoolean);
  
  boolean isActive();
}
