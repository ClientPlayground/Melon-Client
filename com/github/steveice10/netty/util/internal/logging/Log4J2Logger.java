package com.github.steveice10.netty.util.internal.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

class Log4J2Logger extends ExtendedLoggerWrapper implements InternalLogger {
  private static final long serialVersionUID = 5485418394879791397L;
  
  private static final String EXCEPTION_MESSAGE = "Unexpected exception:";
  
  Log4J2Logger(Logger logger) {
    super((ExtendedLogger)logger, logger.getName(), logger.getMessageFactory());
  }
  
  public String name() {
    return getName();
  }
  
  public void trace(Throwable t) {
    log(Level.TRACE, "Unexpected exception:", t);
  }
  
  public void debug(Throwable t) {
    log(Level.DEBUG, "Unexpected exception:", t);
  }
  
  public void info(Throwable t) {
    log(Level.INFO, "Unexpected exception:", t);
  }
  
  public void warn(Throwable t) {
    log(Level.WARN, "Unexpected exception:", t);
  }
  
  public void error(Throwable t) {
    log(Level.ERROR, "Unexpected exception:", t);
  }
  
  public boolean isEnabled(InternalLogLevel level) {
    return isEnabled(toLevel(level));
  }
  
  public void log(InternalLogLevel level, String msg) {
    log(toLevel(level), msg);
  }
  
  public void log(InternalLogLevel level, String format, Object arg) {
    log(toLevel(level), format, arg);
  }
  
  public void log(InternalLogLevel level, String format, Object argA, Object argB) {
    log(toLevel(level), format, argA, argB);
  }
  
  public void log(InternalLogLevel level, String format, Object... arguments) {
    log(toLevel(level), format, arguments);
  }
  
  public void log(InternalLogLevel level, String msg, Throwable t) {
    log(toLevel(level), msg, t);
  }
  
  public void log(InternalLogLevel level, Throwable t) {
    log(toLevel(level), "Unexpected exception:", t);
  }
  
  protected Level toLevel(InternalLogLevel level) {
    switch (level) {
      case INFO:
        return Level.INFO;
      case DEBUG:
        return Level.DEBUG;
      case WARN:
        return Level.WARN;
      case ERROR:
        return Level.ERROR;
      case TRACE:
        return Level.TRACE;
    } 
    throw new Error();
  }
}
