package com.github.steveice10.netty.util.internal.logging;

import org.apache.logging.log4j.LogManager;

public final class Log4J2LoggerFactory extends InternalLoggerFactory {
  public static final InternalLoggerFactory INSTANCE = new Log4J2LoggerFactory();
  
  public InternalLogger newInstance(String name) {
    return new Log4J2Logger(LogManager.getLogger(name));
  }
}
