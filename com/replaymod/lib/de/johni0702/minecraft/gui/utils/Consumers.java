package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

public class Consumers {
  public static <U> Consumer<U> from(final Runnable runnable) {
    return new Consumer<U>() {
        public void consume(U obj) {
          runnable.run();
        }
      };
  }
}
