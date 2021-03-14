package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

public interface QueueProgressIndicators {
  long currentProducerIndex();
  
  long currentConsumerIndex();
}
