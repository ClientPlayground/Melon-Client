package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Event<T> {
  private T invoker;
  
  private Function<List<T>, T> invokerFactory;
  
  public static <T> Event<T> create(Function<List<T>, T> invokerFactory) {
    return new Event<>(invokerFactory);
  }
  
  private List<T> listeners = new ArrayList<>();
  
  private Event(Function<List<T>, T> invokerFactory) {
    this.invokerFactory = invokerFactory;
    update();
  }
  
  void register(T listener) {
    this.listeners.add(listener);
    update();
  }
  
  void unregister(T listener) {
    this.listeners.remove(listener);
    update();
  }
  
  private void update() {
    this.invoker = this.invokerFactory.apply(new ArrayList<>(this.listeners));
  }
  
  public T invoker() {
    return this.invoker;
  }
}
