package me.kaimson.melonclient.ingames.utils.ReplayMod.Events;

import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;

public class ReplayEvent extends Event {
  private final State state;
  
  private final ReplayHandler replayHandler;
  
  public State getState() {
    return this.state;
  }
  
  public ReplayHandler getReplayHandler() {
    return this.replayHandler;
  }
  
  public ReplayEvent(State state, ReplayHandler replayHandler) {
    this.state = state;
    this.replayHandler = replayHandler;
  }
  
  public enum State {
    OPENED, CLOSING, CLOSED;
  }
}
