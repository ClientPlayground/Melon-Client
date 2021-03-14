package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import lombok.NonNull;

public final class AddKeyframe implements Change {
  private final int path;
  
  private final long time;
  
  private int index;
  
  private boolean applied;
  
  @NonNull
  public static AddKeyframe create(Path path, long time) {
    return new AddKeyframe(path.getTimeline().getPaths().indexOf(path), time);
  }
  
  AddKeyframe(int path, long time) {
    this.path = path;
    this.time = time;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    Path path = timeline.getPaths().get(this.path);
    Keyframe keyframe = path.insert(this.time);
    this.index = Iterables.indexOf(path.getKeyframes(), Predicates.equalTo(keyframe));
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    Path path = timeline.getPaths().get(this.path);
    path.remove((Keyframe)Iterables.get(path.getKeyframes(), this.index), true);
    this.applied = false;
  }
}
