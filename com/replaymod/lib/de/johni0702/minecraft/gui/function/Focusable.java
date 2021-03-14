package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;

public interface Focusable<T extends Focusable<T>> {
  boolean isFocused();
  
  T setFocused(boolean paramBoolean);
  
  T onFocusChange(Consumer<Boolean> paramConsumer);
  
  Focusable getNext();
  
  T setNext(Focusable paramFocusable);
  
  Focusable getPrevious();
  
  T setPrevious(Focusable paramFocusable);
}
