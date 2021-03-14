package com.github.steveice10.netty.util.internal.logging;

public abstract class InternalLoggerFactory {
  private static volatile InternalLoggerFactory defaultFactory;
  
  private static InternalLoggerFactory newDefaultFactory(String name) {
    InternalLoggerFactory f;
    try {
      f = new Slf4JLoggerFactory(true);
      f.newInstance(name).debug("Using SLF4J as the default logging framework");
    } catch (Throwable t1) {
      try {
        f = Log4JLoggerFactory.INSTANCE;
        f.newInstance(name).debug("Using Log4J as the default logging framework");
      } catch (Throwable t2) {
        f = JdkLoggerFactory.INSTANCE;
        f.newInstance(name).debug("Using java.util.logging as the default logging framework");
      } 
    } 
    return f;
  }
  
  public static InternalLoggerFactory getDefaultFactory() {
    if (defaultFactory == null)
      defaultFactory = newDefaultFactory(InternalLoggerFactory.class.getName()); 
    return defaultFactory;
  }
  
  public static void setDefaultFactory(InternalLoggerFactory defaultFactory) {
    if (defaultFactory == null)
      throw new NullPointerException("defaultFactory"); 
    InternalLoggerFactory.defaultFactory = defaultFactory;
  }
  
  public static InternalLogger getInstance(Class<?> clazz) {
    return getInstance(clazz.getName());
  }
  
  public static InternalLogger getInstance(String name) {
    return getDefaultFactory().newInstance(name);
  }
  
  protected abstract InternalLogger newInstance(String paramString);
}
