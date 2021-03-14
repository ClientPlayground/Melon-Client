package com.github.steveice10.netty.util.internal.logging;

public interface InternalLogger {
  String name();
  
  boolean isTraceEnabled();
  
  void trace(String paramString);
  
  void trace(String paramString, Object paramObject);
  
  void trace(String paramString, Object paramObject1, Object paramObject2);
  
  void trace(String paramString, Object... paramVarArgs);
  
  void trace(String paramString, Throwable paramThrowable);
  
  void trace(Throwable paramThrowable);
  
  boolean isDebugEnabled();
  
  void debug(String paramString);
  
  void debug(String paramString, Object paramObject);
  
  void debug(String paramString, Object paramObject1, Object paramObject2);
  
  void debug(String paramString, Object... paramVarArgs);
  
  void debug(String paramString, Throwable paramThrowable);
  
  void debug(Throwable paramThrowable);
  
  boolean isInfoEnabled();
  
  void info(String paramString);
  
  void info(String paramString, Object paramObject);
  
  void info(String paramString, Object paramObject1, Object paramObject2);
  
  void info(String paramString, Object... paramVarArgs);
  
  void info(String paramString, Throwable paramThrowable);
  
  void info(Throwable paramThrowable);
  
  boolean isWarnEnabled();
  
  void warn(String paramString);
  
  void warn(String paramString, Object paramObject);
  
  void warn(String paramString, Object... paramVarArgs);
  
  void warn(String paramString, Object paramObject1, Object paramObject2);
  
  void warn(String paramString, Throwable paramThrowable);
  
  void warn(Throwable paramThrowable);
  
  boolean isErrorEnabled();
  
  void error(String paramString);
  
  void error(String paramString, Object paramObject);
  
  void error(String paramString, Object paramObject1, Object paramObject2);
  
  void error(String paramString, Object... paramVarArgs);
  
  void error(String paramString, Throwable paramThrowable);
  
  void error(Throwable paramThrowable);
  
  boolean isEnabled(InternalLogLevel paramInternalLogLevel);
  
  void log(InternalLogLevel paramInternalLogLevel, String paramString);
  
  void log(InternalLogLevel paramInternalLogLevel, String paramString, Object paramObject);
  
  void log(InternalLogLevel paramInternalLogLevel, String paramString, Object paramObject1, Object paramObject2);
  
  void log(InternalLogLevel paramInternalLogLevel, String paramString, Object... paramVarArgs);
  
  void log(InternalLogLevel paramInternalLogLevel, String paramString, Throwable paramThrowable);
  
  void log(InternalLogLevel paramInternalLogLevel, Throwable paramThrowable);
}
