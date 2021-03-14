package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;

public class KeyInputEvent extends Event {
  private int key;
  
  public KeyInputEvent(int key) {
    this.key = key;
  }
  
  public int getKey() {
    return this.key;
  }
}
