package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import lombok.NonNull;

public final class RemovePath implements Change {
  private final int path;
  
  private volatile Path oldPath;
  
  private boolean applied;
  
  @NonNull
  public static RemovePath create(Path path) {
    return new RemovePath(path.getTimeline().getPaths().indexOf(path));
  }
  
  RemovePath(int path) {
    this.path = path;
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    this.oldPath = timeline.getPaths().remove(this.path);
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    timeline.getPaths().add(this.path, this.oldPath);
    this.applied = false;
  }
}
