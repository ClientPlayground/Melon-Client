package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.player;

import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.TimerTickEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.WrappedTimer;
import net.minecraft.util.Timer;

public class ReplayTimer extends WrappedTimer {
  private final Timer state = new Timer(0.0F);
  
  public ReplayTimer(Timer wrapped) {
    super(wrapped);
  }
  
  public void updateTimer() {
    copy((Timer)this, this.state);
    this.wrapped.updateTimer();
    copy(this.state, (Timer)this);
    EventHandler.call((Event)new TimerTickEvent());
  }
  
  public Timer getWrapped() {
    return this.wrapped;
  }
}
