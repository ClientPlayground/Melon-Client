package me.kaimson.melonclient.Events;

public class Cancellable extends Event {
  private boolean cancelled = false;
  
  public Cancellable() {
    this.cancelled = false;
  }
  
  public void cancel() {
    this.cancelled = true;
  }
  
  public boolean isCancelled() {
    return this.cancelled;
  }
}
