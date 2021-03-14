package com.replaymod.replaystudio.pathing.change;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.pathing.path.Timeline;
import lombok.NonNull;

public final class AddPath implements Change {
  private boolean applied;
  
  @NonNull
  public static AddPath create() {
    return new AddPath();
  }
  
  public void apply(Timeline timeline) {
    Preconditions.checkState(!this.applied, "Already applied!");
    timeline.createPath();
    this.applied = true;
  }
  
  public void undo(Timeline timeline) {
    Preconditions.checkState(this.applied, "Not yet applied!");
    timeline.getPaths().remove(timeline.getPaths().size() - 1);
    this.applied = false;
  }
}
