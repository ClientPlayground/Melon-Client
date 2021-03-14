package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.player;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import java.util.Iterator;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.TimerTickEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public abstract class AbstractTimelinePlayer {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final ReplayHandler replayHandler;
  
  private Timeline timeline;
  
  protected long startOffset;
  
  private long lastTime;
  
  private long lastTimestamp;
  
  private ListenableFuture<Void> future;
  
  private SettableFuture<Void> settableFuture;
  
  public AbstractTimelinePlayer(ReplayHandler replayHandler) {
    this.replayHandler = replayHandler;
  }
  
  public ListenableFuture<Void> start(Timeline timeline, long from) {
    this.startOffset = from;
    return start(timeline);
  }
  
  public ListenableFuture<Void> start(Timeline timeline) {
    this.timeline = timeline;
    Iterator<Keyframe> iter = Iterables.concat(Iterables.transform(timeline.getPaths(), new Function<Path, Iterable<Keyframe>>() {
            public Iterable<Keyframe> apply(Path input) {
              assert input != null;
              return input.getKeyframes();
            }
          })).iterator();
    if (!iter.hasNext()) {
      this.lastTimestamp = 0L;
    } else {
      this
        
        .lastTimestamp = ((Keyframe)(new Ordering<Keyframe>() {
          public int compare(Keyframe left, Keyframe right) {
            assert left != null;
            assert right != null;
            return Longs.compare(left.getTime(), right.getTime());
          }
        }).max(iter)).getTime();
    } 
    this.replayHandler.getReplaySender().setSyncModeAndWait();
    EventHandler.register(this);
    this.lastTime = 0L;
    Minecraft mcA = this.mc;
    ReplayTimer timer = new ReplayTimer(mcA.timer);
    mcA.timer = (Timer)timer;
    timer.timerSpeed = 1.0F;
    timer.elapsedPartialTicks = (timer.elapsedTicks = 0);
    return this.future = (ListenableFuture<Void>)(this.settableFuture = SettableFuture.create());
  }
  
  public ListenableFuture<Void> getFuture() {
    return this.future;
  }
  
  public boolean isActive() {
    return (this.future != null && !this.future.isDone());
  }
  
  @TypeEvent
  public void onTick(TimerTickEvent e) {
    if (this.future.isDone()) {
      this.mc.timer = ((ReplayTimer)this.mc.timer).getWrapped();
      this.replayHandler.getReplaySender().setReplaySpeed(0.0D);
      this.replayHandler.getReplaySender().setAsyncMode(true);
      EventHandler.unregister(this);
      return;
    } 
    long time = getTimePassed();
    if (time > this.lastTimestamp)
      time = this.lastTimestamp; 
    this.timeline.applyToGame(time, this.replayHandler);
    long replayTime = this.replayHandler.getReplaySender().currentTimeStamp();
    if (this.lastTime == 0L)
      this.lastTime = replayTime; 
    float timeInTicks = (float)replayTime / 50.0F;
    float previousTimeInTicks = (float)this.lastTime / 50.0F;
    float passedTicks = timeInTicks - previousTimeInTicks;
    Timer timer = this.mc.timer;
    timer.elapsedPartialTicks += passedTicks;
    timer.elapsedTicks = (int)timer.elapsedPartialTicks;
    timer.elapsedPartialTicks -= timer.elapsedTicks;
    timer.renderPartialTicks = timer.elapsedPartialTicks;
    this.lastTime = replayTime;
    if (time >= this.lastTimestamp)
      this.settableFuture.set(null); 
  }
  
  public abstract long getTimePassed();
}
