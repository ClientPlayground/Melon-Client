package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import net.minecraft.util.Timer;

public class WrappedTimer extends Timer {
  protected final Timer wrapped;
  
  public WrappedTimer(Timer wrapped) {
    super(0.0F);
    this.wrapped = wrapped;
    copy(wrapped, this);
  }
  
  public void updateTimer() {
    copy(this, this.wrapped);
    this.wrapped.updateTimer();
    copy(this.wrapped, this);
  }
  
  protected void copy(Timer from, Timer to) {
    to.ticksPerSecond = from.ticksPerSecond;
    to.lastHRTime = from.lastHRTime;
    to.elapsedTicks = from.elapsedTicks;
    to.renderPartialTicks = from.renderPartialTicks;
    to.timerSpeed = from.timerSpeed;
    to.elapsedPartialTicks = from.elapsedPartialTicks;
    to.lastSyncSysClock = from.lastSyncSysClock;
    to.lastSyncHRClock = from.lastSyncHRClock;
    to.counter = from.counter;
    to.timeSyncAdjustment = from.timeSyncAdjustment;
  }
}
