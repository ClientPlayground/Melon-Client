package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import lombok.NonNull;

public final class RemoveKeyframe implements Change {
  private final int path;
  
  private final int index;
  
  private volatile Keyframe removedKeyframe;
  
  private volatile Interpolator removedInterpolator;
  
  private boolean applied;
  
  @NonNull
  public static RemoveKeyframe create(@NonNull Path path, @NonNull Keyframe keyframe) {
    if (path == null)
      throw new NullPointerException("path"); 
    if (keyframe == null)
      throw new NullPointerException("keyframe"); 
    return new RemoveKeyframe(path.getTimeline().getPaths().indexOf(path), 
        Iterables.indexOf(path.getKeyframes(), Predicates.equalTo(keyframe)));
  }
  
  RemoveKeyframe(int path, int index) {
    this.path = path;
    this.index = index;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    Path path = timeline.getPaths().get(this.path);
    if (!path.getSegments().isEmpty())
      if (this.index == path.getSegments().size()) {
        this.removedInterpolator = ((PathSegment)Iterables.get(path.getSegments(), this.index - 1)).getInterpolator();
      } else {
        this.removedInterpolator = ((PathSegment)Iterables.get(path.getSegments(), this.index)).getInterpolator();
      }  
    path.remove(this.removedKeyframe = (Keyframe)Iterables.get(path.getKeyframes(), this.index), true);
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    Path path = timeline.getPaths().get(this.path);
    path.insert(this.removedKeyframe);
    if (this.removedInterpolator != null)
      if (this.index == path.getSegments().size()) {
        ((PathSegment)Iterables.get(path.getSegments(), this.index - 1)).setInterpolator(this.removedInterpolator);
      } else {
        ((PathSegment)Iterables.get(path.getSegments(), this.index)).setInterpolator(this.removedInterpolator);
      }  
    this.applied = false;
  }
}
