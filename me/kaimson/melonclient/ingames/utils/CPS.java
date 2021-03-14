package me.kaimson.melonclient.ingames.utils;

import com.google.common.collect.Lists;
import java.util.Queue;
import org.lwjgl.input.Mouse;

public class CPS {
  public final Counter leftCounter = new Counter();
  
  public final Counter rightCounter = new Counter();
  
  private boolean LwasDown = false;
  
  private boolean RwasDown = false;
  
  public void onTick() {
    cps();
  }
  
  private void cps() {
    Mouse.poll();
    boolean downNow = Mouse.isButtonDown(0);
    if (downNow != this.LwasDown && downNow)
      this.leftCounter.onClick(); 
    this.LwasDown = downNow;
    downNow = Mouse.isButtonDown(1);
    if (downNow != this.RwasDown && downNow)
      this.rightCounter.onClick(); 
    this.RwasDown = downNow;
  }
  
  public static class Counter {
    private final Queue<Long> clicks = Lists.newLinkedList();
    
    public Counter onClick() {
      this.clicks.add(Long.valueOf(System.currentTimeMillis() + 1000L));
      return this;
    }
    
    public int getCPS() {
      long time = System.currentTimeMillis();
      while (!this.clicks.isEmpty() && ((Long)this.clicks.peek()).longValue() < time)
        this.clicks.remove(); 
      return this.clicks.size();
    }
  }
}
