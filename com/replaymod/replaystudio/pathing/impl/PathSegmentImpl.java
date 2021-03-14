package com.replaymod.replaystudio.pathing.impl;

import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import java.beans.ConstructorProperties;

public class PathSegmentImpl implements PathSegment {
  private final Keyframe startKeyframe;
  
  private final Keyframe endKeyframe;
  
  private Interpolator interpolator;
  
  @ConstructorProperties({"startKeyframe", "endKeyframe"})
  public PathSegmentImpl(Keyframe startKeyframe, Keyframe endKeyframe) {
    this.startKeyframe = startKeyframe;
    this.endKeyframe = endKeyframe;
  }
  
  public Keyframe getStartKeyframe() {
    return this.startKeyframe;
  }
  
  public Keyframe getEndKeyframe() {
    return this.endKeyframe;
  }
  
  public Interpolator getInterpolator() {
    return this.interpolator;
  }
  
  public PathSegmentImpl(Keyframe startKeyframe, Keyframe endKeyframe, Interpolator interpolator) {
    this.startKeyframe = startKeyframe;
    this.endKeyframe = endKeyframe;
    setInterpolator(interpolator);
  }
  
  public void setInterpolator(Interpolator interpolator) {
    if (this.interpolator != null)
      this.interpolator.removeSegment(this); 
    this.interpolator = interpolator;
    if (this.interpolator != null)
      this.interpolator.addSegment(this); 
  }
}
