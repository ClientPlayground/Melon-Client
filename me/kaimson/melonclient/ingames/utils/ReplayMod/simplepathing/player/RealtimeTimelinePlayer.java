package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.player;

import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.replaystudio.pathing.path.Timeline;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.TimerTickEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;

public class RealtimeTimelinePlayer extends AbstractTimelinePlayer {
  private boolean firstFrame;
  
  private boolean secondFrame;
  
  private long startTime;
  
  public RealtimeTimelinePlayer(ReplayHandler replayHandler) {
    super(replayHandler);
  }
  
  public ListenableFuture<Void> start(Timeline timeline) {
    this.firstFrame = true;
    return super.start(timeline);
  }
  
  @TypeEvent
  public void onTick(TimerTickEvent e) {
    if (this.secondFrame) {
      this.secondFrame = false;
      this.startTime = System.currentTimeMillis();
    } 
    super.onTick(e);
    if (this.firstFrame) {
      this.firstFrame = false;
      this.secondFrame = true;
    } 
  }
  
  public long getTimePassed() {
    return this.startOffset + (this.firstFrame ? 0L : (System.currentTimeMillis() - this.startTime));
  }
}
