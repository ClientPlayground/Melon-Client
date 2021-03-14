package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class ResourceLeakDetectorFactory {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetectorFactory.class);
  
  private static volatile ResourceLeakDetectorFactory factoryInstance = new DefaultResourceLeakDetectorFactory();
  
  public static ResourceLeakDetectorFactory instance() {
    return factoryInstance;
  }
  
  public static void setResourceLeakDetectorFactory(ResourceLeakDetectorFactory factory) {
    factoryInstance = (ResourceLeakDetectorFactory)ObjectUtil.checkNotNull(factory, "factory");
  }
  
  public final <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource) {
    return newResourceLeakDetector(resource, 128);
  }
  
  public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval) {
    return newResourceLeakDetector(resource, 128, Long.MAX_VALUE);
  }
  
  @Deprecated
  public abstract <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> paramClass, int paramInt, long paramLong);
  
  private static final class DefaultResourceLeakDetectorFactory extends ResourceLeakDetectorFactory {
    private final Constructor<?> obsoleteCustomClassConstructor;
    
    private final Constructor<?> customClassConstructor;
    
    DefaultResourceLeakDetectorFactory() {
      String customLeakDetector;
      try {
        customLeakDetector = AccessController.<String>doPrivileged(new PrivilegedAction<String>() {
              public String run() {
                return SystemPropertyUtil.get("com.github.steveice10.netty.customResourceLeakDetector");
              }
            });
      } catch (Throwable cause) {
        ResourceLeakDetectorFactory.logger.error("Could not access System property: io.netty.customResourceLeakDetector", cause);
        customLeakDetector = null;
      } 
      if (customLeakDetector == null) {
        this.obsoleteCustomClassConstructor = this.customClassConstructor = null;
      } else {
        this.obsoleteCustomClassConstructor = obsoleteCustomClassConstructor(customLeakDetector);
        this.customClassConstructor = customClassConstructor(customLeakDetector);
      } 
    }
    
    private static Constructor<?> obsoleteCustomClassConstructor(String customLeakDetector) {
      try {
        Class<?> detectorClass = Class.forName(customLeakDetector, true, 
            PlatformDependent.getSystemClassLoader());
        if (ResourceLeakDetector.class.isAssignableFrom(detectorClass))
          return detectorClass.getConstructor(new Class[] { Class.class, int.class, long.class }); 
        ResourceLeakDetectorFactory.logger.error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
      } catch (Throwable t) {
        ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector class provided: {}", customLeakDetector, t);
      } 
      return null;
    }
    
    private static Constructor<?> customClassConstructor(String customLeakDetector) {
      try {
        Class<?> detectorClass = Class.forName(customLeakDetector, true, 
            PlatformDependent.getSystemClassLoader());
        if (ResourceLeakDetector.class.isAssignableFrom(detectorClass))
          return detectorClass.getConstructor(new Class[] { Class.class, int.class }); 
        ResourceLeakDetectorFactory.logger.error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
      } catch (Throwable t) {
        ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector class provided: {}", customLeakDetector, t);
      } 
      return null;
    }
    
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval, long maxActive) {
      if (this.obsoleteCustomClassConstructor != null)
        try {
          ResourceLeakDetector<T> leakDetector = (ResourceLeakDetector<T>)this.obsoleteCustomClassConstructor.newInstance(new Object[] { resource, Integer.valueOf(samplingInterval), Long.valueOf(maxActive) });
          ResourceLeakDetectorFactory.logger.debug("Loaded custom ResourceLeakDetector: {}", this.obsoleteCustomClassConstructor
              .getDeclaringClass().getName());
          return leakDetector;
        } catch (Throwable t) {
          ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector provided: {} with the given resource: {}", new Object[] { this.obsoleteCustomClassConstructor
                
                .getDeclaringClass().getName(), resource, t });
        }  
      ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector<T>(resource, samplingInterval, maxActive);
      ResourceLeakDetectorFactory.logger.debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
      return resourceLeakDetector;
    }
    
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval) {
      if (this.customClassConstructor != null)
        try {
          ResourceLeakDetector<T> leakDetector = (ResourceLeakDetector<T>)this.customClassConstructor.newInstance(new Object[] { resource, Integer.valueOf(samplingInterval) });
          ResourceLeakDetectorFactory.logger.debug("Loaded custom ResourceLeakDetector: {}", this.customClassConstructor
              .getDeclaringClass().getName());
          return leakDetector;
        } catch (Throwable t) {
          ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector provided: {} with the given resource: {}", new Object[] { this.customClassConstructor
                
                .getDeclaringClass().getName(), resource, t });
        }  
      ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector<T>(resource, samplingInterval);
      ResourceLeakDetectorFactory.logger.debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
      return resourceLeakDetector;
    }
  }
}
