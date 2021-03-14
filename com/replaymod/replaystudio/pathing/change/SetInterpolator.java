package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import lombok.NonNull;

public final class SetInterpolator implements Change {
  private final PathSegment segment;
  
  private final Interpolator interpolator;
  
  private Interpolator oldInterpolator;
  
  private boolean applied;
  
  @NonNull
  public static SetInterpolator create(PathSegment segment, Interpolator interpolator) {
    return new SetInterpolator(segment, interpolator);
  }
  
  SetInterpolator(PathSegment segment, Interpolator interpolator) {
    this.segment = segment;
    this.interpolator = interpolator;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    this.oldInterpolator = this.segment.getInterpolator();
    this.segment.setInterpolator(this.interpolator);
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    this.segment.setInterpolator(this.oldInterpolator);
    this.applied = false;
  }
}
