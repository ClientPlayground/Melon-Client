package com.replaymod.lib.org.apache.commons.lang3.mutable;

public interface Mutable<T> {
  T getValue();
  
  void setValue(T paramT);
}
