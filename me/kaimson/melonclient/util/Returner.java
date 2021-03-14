package me.kaimson.melonclient.util;

public class Returner {
  private boolean cancelled = false;
  
  public void cancel() {
    this.cancelled = true;
  }
  
  public boolean isCancelled() {
    return this.cancelled;
  }
}
