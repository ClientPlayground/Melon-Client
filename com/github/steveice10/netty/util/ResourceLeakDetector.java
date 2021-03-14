package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ResourceLeakDetector<T> {
  private static final String PROP_LEVEL_OLD = "com.github.steveice10.netty.leakDetectionLevel";
  
  private static final String PROP_LEVEL = "com.github.steveice10.netty.leakDetection.level";
  
  static {
    boolean disabled;
  }
  
  private static final Level DEFAULT_LEVEL = Level.SIMPLE;
  
  private static final String PROP_TARGET_RECORDS = "com.github.steveice10.netty.leakDetection.targetRecords";
  
  private static final int DEFAULT_TARGET_RECORDS = 4;
  
  private static final int TARGET_RECORDS;
  
  private static Level level;
  
  public enum Level {
    DISABLED, SIMPLE, ADVANCED, PARANOID;
    
    static Level parseLevel(String levelStr) {
      String trimmedLevelStr = levelStr.trim();
      for (Level l : values()) {
        if (trimmedLevelStr.equalsIgnoreCase(l.name()) || trimmedLevelStr.equals(String.valueOf(l.ordinal())))
          return l; 
      } 
      return ResourceLeakDetector.DEFAULT_LEVEL;
    }
  }
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetector.class);
  
  static final int DEFAULT_SAMPLING_INTERVAL = 128;
  
  static {
    if (SystemPropertyUtil.get("com.github.steveice10.netty.noResourceLeakDetection") != null) {
      disabled = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.noResourceLeakDetection", false);
      logger.debug("-Dio.netty.noResourceLeakDetection: {}", Boolean.valueOf(disabled));
      logger.warn("-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.", "com.github.steveice10.netty.leakDetection.level", DEFAULT_LEVEL
          
          .name().toLowerCase());
    } else {
      disabled = false;
    } 
    Level defaultLevel = disabled ? Level.DISABLED : DEFAULT_LEVEL;
    String levelStr = SystemPropertyUtil.get("com.github.steveice10.netty.leakDetectionLevel", defaultLevel.name());
    levelStr = SystemPropertyUtil.get("com.github.steveice10.netty.leakDetection.level", levelStr);
    Level level = Level.parseLevel(levelStr);
    TARGET_RECORDS = SystemPropertyUtil.getInt("com.github.steveice10.netty.leakDetection.targetRecords", 4);
    ResourceLeakDetector.level = level;
    if (logger.isDebugEnabled()) {
      logger.debug("-D{}: {}", "com.github.steveice10.netty.leakDetection.level", level.name().toLowerCase());
      logger.debug("-D{}: {}", "com.github.steveice10.netty.leakDetection.targetRecords", Integer.valueOf(TARGET_RECORDS));
    } 
    excludedMethods = (AtomicReference)new AtomicReference<String>(EmptyArrays.EMPTY_STRINGS);
  }
  
  @Deprecated
  public static void setEnabled(boolean enabled) {
    setLevel(enabled ? Level.SIMPLE : Level.DISABLED);
  }
  
  public static boolean isEnabled() {
    return (getLevel().ordinal() > Level.DISABLED.ordinal());
  }
  
  public static void setLevel(Level level) {
    if (level == null)
      throw new NullPointerException("level"); 
    ResourceLeakDetector.level = level;
  }
  
  public static Level getLevel() {
    return level;
  }
  
  private final ConcurrentMap<DefaultResourceLeak<?>, LeakEntry> allLeaks = PlatformDependent.newConcurrentHashMap();
  
  private final ReferenceQueue<Object> refQueue = new ReferenceQueue();
  
  private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent.newConcurrentHashMap();
  
  private final String resourceType;
  
  private final int samplingInterval;
  
  private static final AtomicReference<String[]> excludedMethods;
  
  @Deprecated
  public ResourceLeakDetector(Class<?> resourceType) {
    this(StringUtil.simpleClassName(resourceType));
  }
  
  @Deprecated
  public ResourceLeakDetector(String resourceType) {
    this(resourceType, 128, Long.MAX_VALUE);
  }
  
  @Deprecated
  public ResourceLeakDetector(Class<?> resourceType, int samplingInterval, long maxActive) {
    this(resourceType, samplingInterval);
  }
  
  public ResourceLeakDetector(Class<?> resourceType, int samplingInterval) {
    this(StringUtil.simpleClassName(resourceType), samplingInterval, Long.MAX_VALUE);
  }
  
  @Deprecated
  public ResourceLeakDetector(String resourceType, int samplingInterval, long maxActive) {
    if (resourceType == null)
      throw new NullPointerException("resourceType"); 
    this.resourceType = resourceType;
    this.samplingInterval = samplingInterval;
  }
  
  @Deprecated
  public final ResourceLeak open(T obj) {
    return track0(obj);
  }
  
  public final ResourceLeakTracker<T> track(T obj) {
    return track0(obj);
  }
  
  private DefaultResourceLeak track0(T obj) {
    Level level = ResourceLeakDetector.level;
    if (level == Level.DISABLED)
      return null; 
    if (level.ordinal() < Level.PARANOID.ordinal()) {
      if (PlatformDependent.threadLocalRandom().nextInt(this.samplingInterval) == 0) {
        reportLeak();
        return new DefaultResourceLeak(obj, this.refQueue, this.allLeaks);
      } 
      return null;
    } 
    reportLeak();
    return new DefaultResourceLeak(obj, this.refQueue, this.allLeaks);
  }
  
  private void clearRefQueue() {
    while (true) {
      DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
      if (ref == null)
        break; 
      ref.dispose();
    } 
  }
  
  private void reportLeak() {
    if (!logger.isErrorEnabled()) {
      clearRefQueue();
      return;
    } 
    while (true) {
      DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
      if (ref == null)
        break; 
      if (!ref.dispose())
        continue; 
      String records = ref.toString();
      if (this.reportedLeaks.putIfAbsent(records, Boolean.TRUE) == null) {
        if (records.isEmpty()) {
          reportUntracedLeak(this.resourceType);
          continue;
        } 
        reportTracedLeak(this.resourceType, records);
      } 
    } 
  }
  
  protected void reportTracedLeak(String resourceType, String records) {
    logger.error("LEAK: {}.release() was not called before it's garbage-collected. See http://netty.io/wiki/reference-counted-objects.html for more information.{}", resourceType, records);
  }
  
  protected void reportUntracedLeak(String resourceType) {
    logger.error("LEAK: {}.release() was not called before it's garbage-collected. Enable advanced leak reporting to find out where the leak occurred. To enable advanced leak reporting, specify the JVM option '-D{}={}' or call {}.setLevel() See http://netty.io/wiki/reference-counted-objects.html for more information.", new Object[] { resourceType, "com.github.steveice10.netty.leakDetection.level", Level.ADVANCED.name().toLowerCase(), StringUtil.simpleClassName(this) });
  }
  
  @Deprecated
  protected void reportInstancesLeak(String resourceType) {}
  
  private static final class DefaultResourceLeak<T> extends WeakReference<Object> implements ResourceLeakTracker<T>, ResourceLeak {
    private static final AtomicReferenceFieldUpdater<DefaultResourceLeak<?>, ResourceLeakDetector.Record> headUpdater = AtomicReferenceFieldUpdater.newUpdater((Class)DefaultResourceLeak.class, ResourceLeakDetector.Record.class, "head");
    
    private static final AtomicIntegerFieldUpdater<DefaultResourceLeak<?>> droppedRecordsUpdater = AtomicIntegerFieldUpdater.newUpdater((Class)DefaultResourceLeak.class, "droppedRecords");
    
    private volatile ResourceLeakDetector.Record head;
    
    private volatile int droppedRecords;
    
    private final ConcurrentMap<DefaultResourceLeak<?>, ResourceLeakDetector.LeakEntry> allLeaks;
    
    private final int trackedHash;
    
    DefaultResourceLeak(Object referent, ReferenceQueue<Object> refQueue, ConcurrentMap<DefaultResourceLeak<?>, ResourceLeakDetector.LeakEntry> allLeaks) {
      super(referent, refQueue);
      assert referent != null;
      this.trackedHash = System.identityHashCode(referent);
      allLeaks.put(this, ResourceLeakDetector.LeakEntry.INSTANCE);
      headUpdater.set(this, new ResourceLeakDetector.Record(ResourceLeakDetector.Record.BOTTOM));
      this.allLeaks = allLeaks;
    }
    
    public void record() {
      record0(null);
    }
    
    public void record(Object hint) {
      record0(hint);
    }
    
    private void record0(Object hint) {
      if (ResourceLeakDetector.TARGET_RECORDS > 0)
        while (true) {
          boolean dropped;
          ResourceLeakDetector.Record oldHead, prevHead;
          if ((prevHead = oldHead = headUpdater.get(this)) == null)
            return; 
          int numElements = oldHead.pos + 1;
          if (numElements >= ResourceLeakDetector.TARGET_RECORDS) {
            int backOffFactor = Math.min(numElements - ResourceLeakDetector.TARGET_RECORDS, 30);
            if (dropped = (PlatformDependent.threadLocalRandom().nextInt(1 << backOffFactor) != 0))
              prevHead = oldHead.next; 
          } else {
            dropped = false;
          } 
          ResourceLeakDetector.Record newHead = (hint != null) ? new ResourceLeakDetector.Record(prevHead, hint) : new ResourceLeakDetector.Record(prevHead);
          if (headUpdater.compareAndSet(this, oldHead, newHead)) {
            if (dropped)
              droppedRecordsUpdater.incrementAndGet(this); 
            break;
          } 
        }  
    }
    
    boolean dispose() {
      clear();
      return this.allLeaks.remove(this, ResourceLeakDetector.LeakEntry.INSTANCE);
    }
    
    public boolean close() {
      if (this.allLeaks.remove(this, ResourceLeakDetector.LeakEntry.INSTANCE)) {
        clear();
        headUpdater.set(this, null);
        return true;
      } 
      return false;
    }
    
    public boolean close(T trackedObject) {
      assert this.trackedHash == System.identityHashCode(trackedObject);
      return (close() && trackedObject != null);
    }
    
    public String toString() {
      ResourceLeakDetector.Record oldHead = headUpdater.getAndSet(this, null);
      if (oldHead == null)
        return ""; 
      int dropped = droppedRecordsUpdater.get(this);
      int duped = 0;
      int present = oldHead.pos + 1;
      StringBuilder buf = (new StringBuilder(present * 2048)).append(StringUtil.NEWLINE);
      buf.append("Recent access records: ").append(StringUtil.NEWLINE);
      int i = 1;
      Set<String> seen = new HashSet<String>(present);
      for (; oldHead != ResourceLeakDetector.Record.BOTTOM; oldHead = oldHead.next) {
        String s = oldHead.toString();
        if (seen.add(s)) {
          if (oldHead.next == ResourceLeakDetector.Record.BOTTOM) {
            buf.append("Created at:").append(StringUtil.NEWLINE).append(s);
          } else {
            buf.append('#').append(i++).append(':').append(StringUtil.NEWLINE).append(s);
          } 
        } else {
          duped++;
        } 
      } 
      if (duped > 0)
        buf.append(": ").append(dropped).append(" leak records were discarded because they were duplicates").append(StringUtil.NEWLINE); 
      if (dropped > 0)
        buf.append(": ").append(dropped).append(" leak records were discarded because the leak record count is targeted to ").append(ResourceLeakDetector.TARGET_RECORDS).append(". Use system property ").append("com.github.steveice10.netty.leakDetection.targetRecords").append(" to increase the limit.").append(StringUtil.NEWLINE); 
      buf.setLength(buf.length() - StringUtil.NEWLINE.length());
      return buf.toString();
    }
  }
  
  public static void addExclusions(Class clz, String... methodNames) {
    String[] oldMethods, newMethods;
    Set<String> nameSet = new HashSet<String>(Arrays.asList(methodNames));
    for (Method method : clz.getDeclaredMethods()) {
      if (nameSet.remove(method.getName()) && nameSet.isEmpty())
        break; 
    } 
    if (!nameSet.isEmpty())
      throw new IllegalArgumentException("Can't find '" + nameSet + "' in " + clz.getName()); 
    do {
      oldMethods = excludedMethods.get();
      newMethods = Arrays.<String>copyOf(oldMethods, oldMethods.length + 2 * methodNames.length);
      for (int i = 0; i < methodNames.length; i++) {
        newMethods[oldMethods.length + i * 2] = clz.getName();
        newMethods[oldMethods.length + i * 2 + 1] = methodNames[i];
      } 
    } while (!excludedMethods.compareAndSet(oldMethods, newMethods));
  }
  
  private static final class Record extends Throwable {
    private static final long serialVersionUID = 6065153674892850720L;
    
    private static final Record BOTTOM = new Record();
    
    private final String hintString;
    
    private final Record next;
    
    private final int pos;
    
    Record(Record next, Object hint) {
      this.hintString = (hint instanceof ResourceLeakHint) ? ((ResourceLeakHint)hint).toHintString() : hint.toString();
      this.next = next;
      next.pos++;
    }
    
    Record(Record next) {
      this.hintString = null;
      this.next = next;
      next.pos++;
    }
    
    private Record() {
      this.hintString = null;
      this.next = null;
      this.pos = -1;
    }
    
    public String toString() {
      StringBuilder buf = new StringBuilder(2048);
      if (this.hintString != null)
        buf.append("\tHint: ").append(this.hintString).append(StringUtil.NEWLINE); 
      StackTraceElement[] array = getStackTrace();
      for (int i = 3; i < array.length; i++) {
        StackTraceElement element = array[i];
        String[] exclusions = ResourceLeakDetector.excludedMethods.get();
        int k = 0;
        while (true) {
          if (k < exclusions.length) {
            if (exclusions[k].equals(element.getClassName()) && exclusions[k + 1]
              .equals(element.getMethodName()))
              break; 
            k += 2;
            continue;
          } 
          buf.append('\t');
          buf.append(element.toString());
          buf.append(StringUtil.NEWLINE);
          break;
        } 
      } 
      return buf.toString();
    }
  }
  
  private static final class LeakEntry {
    static final LeakEntry INSTANCE = new LeakEntry();
    
    private static final int HASH = System.identityHashCode(INSTANCE);
    
    public int hashCode() {
      return HASH;
    }
    
    public boolean equals(Object obj) {
      return (obj == this);
    }
  }
}
