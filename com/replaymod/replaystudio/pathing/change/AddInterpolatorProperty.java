package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import lombok.NonNull;

public final class AddInterpolatorProperty implements Change {
  private final Interpolator interpolator;
  
  private final Property property;
  
  private boolean applied;
  
  @NonNull
  public static AddInterpolatorProperty create(Interpolator interpolator, Property property) {
    return new AddInterpolatorProperty(interpolator, property);
  }
  
  AddInterpolatorProperty(Interpolator interpolator, Property property) {
    this.interpolator = interpolator;
    this.property = property;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    this.interpolator.registerProperty(this.property);
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    this.interpolator.unregisterProperty(this.property);
    this.applied = false;
  }
}
