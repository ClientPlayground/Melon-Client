package com.replaymod.replaystudio.pathing.path;

import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import lombok.NonNull;

public interface PathSegment {
  @NonNull
  Keyframe getStartKeyframe();
  
  @NonNull
  Keyframe getEndKeyframe();
  
  @NonNull
  Interpolator getInterpolator();
  
  void setInterpolator(Interpolator paramInterpolator);
}
