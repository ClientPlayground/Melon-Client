package com.github.steveice10.netty.util.internal.logging;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.io.ObjectStreamException;
import java.io.Serializable;

public abstract class AbstractInternalLogger implements InternalLogger, Serializable {
  private static final long serialVersionUID = -6382972526573193470L;
  
  private static final String EXCEPTION_MESSAGE = "Unexpected exception:";
  
  private final String name;
  
  protected AbstractInternalLogger(String name) {
    if (name == null)
      throw new NullPointerException("name"); 
    this.name = name;
  }
  
  public String name() {
    return this.name;
  }
  
  public boolean isEnabled(InternalLogLevel level) {
    switch (level) {
      case TRACE:
        return isTraceEnabled();
      case DEBUG:
        return isDebugEnabled();
      case INFO:
        return isInfoEnabled();
      case WARN:
        return isWarnEnabled();
      case ERROR:
        return isErrorEnabled();
    } 
    throw new Error();
  }
  
  public void trace(Throwable t) {
    trace("Unexpected exception:", t);
  }
  
  public void debug(Throwable t) {
    debug("Unexpected exception:", t);
  }
  
  public void info(Throwable t) {
    info("Unexpected exception:", t);
  }
  
  public void warn(Throwable t) {
    warn("Unexpected exception:", t);
  }
  
  public void error(Throwable t) {
    error("Unexpected exception:", t);
  }
  
  public void log(InternalLogLevel level, String msg, Throwable cause) {
    switch (level) {
      case TRACE:
        trace(msg, cause);
        return;
      case DEBUG:
        debug(msg, cause);
        return;
      case INFO:
        info(msg, cause);
        return;
      case WARN:
        warn(msg, cause);
        return;
      case ERROR:
        error(msg, cause);
        return;
    } 
    throw new Error();
  }
  
  public void log(InternalLogLevel level, Throwable cause) {
    switch (level) {
      case TRACE:
        trace(cause);
        return;
      case DEBUG:
        debug(cause);
        return;
      case INFO:
        info(cause);
        return;
      case WARN:
        warn(cause);
        return;
      case ERROR:
        error(cause);
        return;
    } 
    throw new Error();
  }
  
  public void log(InternalLogLevel level, String msg) {
    switch (level) {
      case TRACE:
        trace(msg);
        return;
      case DEBUG:
        debug(msg);
        return;
      case INFO:
        info(msg);
        return;
      case WARN:
        warn(msg);
        return;
      case ERROR:
        error(msg);
        return;
    } 
    throw new Error();
  }
  
  public void log(InternalLogLevel level, String format, Object arg) {
    switch (level) {
      case TRACE:
        trace(format, arg);
        return;
      case DEBUG:
        debug(format, arg);
        return;
      case INFO:
        info(format, arg);
        return;
      case WARN:
        warn(format, arg);
        return;
      case ERROR:
        error(format, arg);
        return;
    } 
    throw new Error();
  }
  
  public void log(InternalLogLevel level, String format, Object argA, Object argB) {
    switch (level) {
      case TRACE:
        trace(format, argA, argB);
        return;
      case DEBUG:
        debug(format, argA, argB);
        return;
      case INFO:
        info(format, argA, argB);
        return;
      case WARN:
        warn(format, argA, argB);
        return;
      case ERROR:
        error(format, argA, argB);
        return;
    } 
    throw new Error();
  }
  
  public void log(InternalLogLevel level, String format, Object... arguments) {
    switch (level) {
      case TRACE:
        trace(format, arguments);
        return;
      case DEBUG:
        debug(format, arguments);
        return;
      case INFO:
        info(format, arguments);
        return;
      case WARN:
        warn(format, arguments);
        return;
      case ERROR:
        error(format, arguments);
        return;
    } 
    throw new Error();
  }
  
  protected Object readResolve() throws ObjectStreamException {
    return InternalLoggerFactory.getInstance(name());
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '(' + name() + ')';
  }
}
