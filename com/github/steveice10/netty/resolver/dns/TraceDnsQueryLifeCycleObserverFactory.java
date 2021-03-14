package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogLevel;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

final class TraceDnsQueryLifeCycleObserverFactory implements DnsQueryLifecycleObserverFactory {
  private static final InternalLogger DEFAULT_LOGGER = InternalLoggerFactory.getInstance(TraceDnsQueryLifeCycleObserverFactory.class);
  
  private static final InternalLogLevel DEFAULT_LEVEL = InternalLogLevel.DEBUG;
  
  private final InternalLogger logger;
  
  private final InternalLogLevel level;
  
  TraceDnsQueryLifeCycleObserverFactory() {
    this(DEFAULT_LOGGER, DEFAULT_LEVEL);
  }
  
  TraceDnsQueryLifeCycleObserverFactory(InternalLogger logger, InternalLogLevel level) {
    this.logger = (InternalLogger)ObjectUtil.checkNotNull(logger, "logger");
    this.level = (InternalLogLevel)ObjectUtil.checkNotNull(level, "level");
  }
  
  public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
    return new TraceDnsQueryLifecycleObserver(question, this.logger, this.level);
  }
}
