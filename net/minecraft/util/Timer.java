package net.minecraft.util;

import net.minecraft.client.Minecraft;

public class Timer {
  public float ticksPerSecond;
  
  public double lastHRTime;
  
  public int elapsedTicks;
  
  public float renderPartialTicks;
  
  public float timerSpeed = 1.0F;
  
  public float elapsedPartialTicks;
  
  public long lastSyncSysClock;
  
  public long lastSyncHRClock;
  
  public long counter;
  
  public double timeSyncAdjustment = 1.0D;
  
  public Timer(float tps) {
    this.ticksPerSecond = tps;
    this.lastSyncSysClock = Minecraft.getSystemTime();
    this.lastSyncHRClock = System.nanoTime() / 1000000L;
  }
  
  public void updateTimer() {
    long i = Minecraft.getSystemTime();
    long j = i - this.lastSyncSysClock;
    long k = System.nanoTime() / 1000000L;
    double d0 = k / 1000.0D;
    if (j <= 1000L && j >= 0L) {
      this.counter += j;
      if (this.counter > 1000L) {
        long l = k - this.lastSyncHRClock;
        double d1 = this.counter / l;
        this.timeSyncAdjustment += (d1 - this.timeSyncAdjustment) * 0.20000000298023224D;
        this.lastSyncHRClock = k;
        this.counter = 0L;
      } 
      if (this.counter < 0L)
        this.lastSyncHRClock = k; 
    } else {
      this.lastHRTime = d0;
    } 
    this.lastSyncSysClock = i;
    double d2 = (d0 - this.lastHRTime) * this.timeSyncAdjustment;
    this.lastHRTime = d0;
    d2 = MathHelper.clamp_double(d2, 0.0D, 1.0D);
    this.elapsedPartialTicks = (float)(this.elapsedPartialTicks + d2 * this.timerSpeed * this.ticksPerSecond);
    this.elapsedTicks = (int)this.elapsedPartialTicks;
    this.elapsedPartialTicks -= this.elapsedTicks;
    if (this.elapsedTicks > 10)
      this.elapsedTicks = 10; 
    this.renderPartialTicks = this.elapsedPartialTicks;
  }
}
