package com.github.steveice10.netty.util.internal;

import java.util.concurrent.atomic.LongAdder;

final class LongAdderCounter extends LongAdder implements LongCounter {
  public long value() {
    return longValue();
  }
}
