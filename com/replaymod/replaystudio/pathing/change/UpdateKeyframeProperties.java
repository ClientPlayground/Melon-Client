package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyGroup;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;

public final class UpdateKeyframeProperties implements Change {
  private final int path;
  
  private final int index;
  
  private final Map<String, Optional<Object>> newValues;
  
  private final Map<String, Optional<Object>> oldValues;
  
  private boolean applied;
  
  private static String toId(Property property) {
    assert property != null;
    PropertyGroup group = property.getGroup();
    return ((group != null) ? (group.getId() + ":") : "") + property.getId();
  }
  
  public static class Builder {
    private final int path;
    
    private final int keyframe;
    
    private final Map<String, Optional<Object>> updates = new HashMap<>();
    
    private Builder(int path, int keyframe) {
      this.path = path;
      this.keyframe = keyframe;
    }
    
    public <T> Builder setValue(Property<T> property, T value) {
      this.updates.put(UpdateKeyframeProperties.toId(property), Optional.of(value));
      return this;
    }
    
    public Builder removeProperty(Property property) {
      this.updates.put(UpdateKeyframeProperties.toId(property), Optional.empty());
      return this;
    }
    
    public UpdateKeyframeProperties done() {
      return new UpdateKeyframeProperties(this.path, this.keyframe, this.updates);
    }
  }
  
  @NonNull
  public static Builder create(@NonNull Path path, @NonNull Keyframe keyframe) {
    if (path == null)
      throw new NullPointerException("path"); 
    if (keyframe == null)
      throw new NullPointerException("keyframe"); 
    return new Builder(path.getTimeline().getPaths().indexOf(path), 
        Iterables.indexOf(path.getKeyframes(), Predicates.equalTo(keyframe)));
  }
  
  UpdateKeyframeProperties(int path, int index, Map<String, Optional<Object>> newValues) {
    this.oldValues = new HashMap<>();
    this.path = path;
    this.index = index;
    this.newValues = newValues;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    Path path = timeline.getPaths().get(this.path);
    Keyframe keyframe = (Keyframe)Iterables.get(path.getKeyframes(), this.index);
    for (Map.Entry<String, Optional<Object>> entry : this.newValues.entrySet()) {
      Property property = timeline.getProperty(entry.getKey());
      if (property == null)
        throw new IllegalStateException("Property " + (String)entry.getKey() + " unknown."); 
      Optional<Object> newValue = entry.getValue();
      this.oldValues.put(entry.getKey(), keyframe.getValue(property));
      if (newValue.isPresent()) {
        keyframe.setValue(property, newValue.get());
        continue;
      } 
      keyframe.removeProperty(property);
    } 
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    Path path = timeline.getPaths().get(this.path);
    Keyframe keyframe = (Keyframe)Iterables.get(path.getKeyframes(), this.index);
    for (Map.Entry<String, Optional<Object>> entry : this.oldValues.entrySet()) {
      Property property = timeline.getProperty(entry.getKey());
      if (property == null)
        throw new IllegalStateException("Property " + (String)entry.getKey() + " unknown."); 
      Optional<Object> oldValue = entry.getValue();
      this.newValues.put(entry.getKey(), keyframe.getValue(property));
      if (oldValue.isPresent()) {
        keyframe.setValue(property, oldValue.get());
        continue;
      } 
      keyframe.removeProperty(property);
    } 
    this.applied = false;
  }
}
