package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

public class EventRegistration<T> {
  private final Event<T> event;
  
  private final T listener;
  
  private boolean registered;
  
  public static <T> EventRegistration<T> create(Event<T> event, T callback) {
    return new EventRegistration<>(event, callback);
  }
  
  public static <T> EventRegistration<T> register(Event<T> event, T callback) {
    EventRegistration<T> registration = new EventRegistration<>(event, callback);
    registration.register();
    return registration;
  }
  
  private EventRegistration(Event<T> event, T listener) {
    this.event = event;
    this.listener = listener;
  }
  
  public void register() {
    if (this.registered)
      throw new IllegalStateException(); 
    this.event.register(this.listener);
    this.registered = true;
  }
  
  public void unregister() {
    if (!this.registered)
      throw new IllegalStateException(); 
    this.event.unregister(this.listener);
    this.registered = false;
  }
}
