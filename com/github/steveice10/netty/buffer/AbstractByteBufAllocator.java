package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class AbstractByteBufAllocator implements ByteBufAllocator {
  static final int DEFAULT_INITIAL_CAPACITY = 256;
  
  static final int DEFAULT_MAX_CAPACITY = 2147483647;
  
  static final int DEFAULT_MAX_COMPONENTS = 16;
  
  static final int CALCULATE_THRESHOLD = 4194304;
  
  private final boolean directByDefault;
  
  private final ByteBuf emptyBuf;
  
  static {
    ResourceLeakDetector.addExclusions(AbstractByteBufAllocator.class, new String[] { "toLeakAwareBuffer" });
  }
  
  protected static ByteBuf toLeakAwareBuffer(ByteBuf buf) {
    ResourceLeakTracker<ByteBuf> leak;
    switch (ResourceLeakDetector.getLevel()) {
      case SIMPLE:
        leak = AbstractByteBuf.leakDetector.track(buf);
        if (leak != null)
          buf = new SimpleLeakAwareByteBuf(buf, leak); 
        break;
      case ADVANCED:
      case PARANOID:
        leak = AbstractByteBuf.leakDetector.track(buf);
        if (leak != null)
          buf = new AdvancedLeakAwareByteBuf(buf, leak); 
        break;
    } 
    return buf;
  }
  
  protected static CompositeByteBuf toLeakAwareBuffer(CompositeByteBuf buf) {
    ResourceLeakTracker<ByteBuf> leak;
    switch (ResourceLeakDetector.getLevel()) {
      case SIMPLE:
        leak = AbstractByteBuf.leakDetector.track(buf);
        if (leak != null)
          buf = new SimpleLeakAwareCompositeByteBuf(buf, leak); 
        break;
      case ADVANCED:
      case PARANOID:
        leak = AbstractByteBuf.leakDetector.track(buf);
        if (leak != null)
          buf = new AdvancedLeakAwareCompositeByteBuf(buf, leak); 
        break;
    } 
    return buf;
  }
  
  protected AbstractByteBufAllocator() {
    this(false);
  }
  
  protected AbstractByteBufAllocator(boolean preferDirect) {
    this.directByDefault = (preferDirect && PlatformDependent.hasUnsafe());
    this.emptyBuf = new EmptyByteBuf(this);
  }
  
  public ByteBuf buffer() {
    if (this.directByDefault)
      return directBuffer(); 
    return heapBuffer();
  }
  
  public ByteBuf buffer(int initialCapacity) {
    if (this.directByDefault)
      return directBuffer(initialCapacity); 
    return heapBuffer(initialCapacity);
  }
  
  public ByteBuf buffer(int initialCapacity, int maxCapacity) {
    if (this.directByDefault)
      return directBuffer(initialCapacity, maxCapacity); 
    return heapBuffer(initialCapacity, maxCapacity);
  }
  
  public ByteBuf ioBuffer() {
    if (PlatformDependent.hasUnsafe())
      return directBuffer(256); 
    return heapBuffer(256);
  }
  
  public ByteBuf ioBuffer(int initialCapacity) {
    if (PlatformDependent.hasUnsafe())
      return directBuffer(initialCapacity); 
    return heapBuffer(initialCapacity);
  }
  
  public ByteBuf ioBuffer(int initialCapacity, int maxCapacity) {
    if (PlatformDependent.hasUnsafe())
      return directBuffer(initialCapacity, maxCapacity); 
    return heapBuffer(initialCapacity, maxCapacity);
  }
  
  public ByteBuf heapBuffer() {
    return heapBuffer(256, 2147483647);
  }
  
  public ByteBuf heapBuffer(int initialCapacity) {
    return heapBuffer(initialCapacity, 2147483647);
  }
  
  public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
    if (initialCapacity == 0 && maxCapacity == 0)
      return this.emptyBuf; 
    validate(initialCapacity, maxCapacity);
    return newHeapBuffer(initialCapacity, maxCapacity);
  }
  
  public ByteBuf directBuffer() {
    return directBuffer(256, 2147483647);
  }
  
  public ByteBuf directBuffer(int initialCapacity) {
    return directBuffer(initialCapacity, 2147483647);
  }
  
  public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
    if (initialCapacity == 0 && maxCapacity == 0)
      return this.emptyBuf; 
    validate(initialCapacity, maxCapacity);
    return newDirectBuffer(initialCapacity, maxCapacity);
  }
  
  public CompositeByteBuf compositeBuffer() {
    if (this.directByDefault)
      return compositeDirectBuffer(); 
    return compositeHeapBuffer();
  }
  
  public CompositeByteBuf compositeBuffer(int maxNumComponents) {
    if (this.directByDefault)
      return compositeDirectBuffer(maxNumComponents); 
    return compositeHeapBuffer(maxNumComponents);
  }
  
  public CompositeByteBuf compositeHeapBuffer() {
    return compositeHeapBuffer(16);
  }
  
  public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
    return toLeakAwareBuffer(new CompositeByteBuf(this, false, maxNumComponents));
  }
  
  public CompositeByteBuf compositeDirectBuffer() {
    return compositeDirectBuffer(16);
  }
  
  public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
    return toLeakAwareBuffer(new CompositeByteBuf(this, true, maxNumComponents));
  }
  
  private static void validate(int initialCapacity, int maxCapacity) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("initialCapacity: " + initialCapacity + " (expected: 0+)"); 
    if (initialCapacity > maxCapacity)
      throw new IllegalArgumentException(String.format("initialCapacity: %d (expected: not greater than maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) })); 
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(directByDefault: " + this.directByDefault + ')';
  }
  
  public int calculateNewCapacity(int minNewCapacity, int maxCapacity) {
    if (minNewCapacity < 0)
      throw new IllegalArgumentException("minNewCapacity: " + minNewCapacity + " (expected: 0+)"); 
    if (minNewCapacity > maxCapacity)
      throw new IllegalArgumentException(String.format("minNewCapacity: %d (expected: not greater than maxCapacity(%d)", new Object[] { Integer.valueOf(minNewCapacity), Integer.valueOf(maxCapacity) })); 
    int threshold = 4194304;
    if (minNewCapacity == 4194304)
      return 4194304; 
    if (minNewCapacity > 4194304) {
      int i = minNewCapacity / 4194304 * 4194304;
      if (i > maxCapacity - 4194304) {
        i = maxCapacity;
      } else {
        i += 4194304;
      } 
      return i;
    } 
    int newCapacity = 64;
    while (newCapacity < minNewCapacity)
      newCapacity <<= 1; 
    return Math.min(newCapacity, maxCapacity);
  }
  
  protected abstract ByteBuf newHeapBuffer(int paramInt1, int paramInt2);
  
  protected abstract ByteBuf newDirectBuffer(int paramInt1, int paramInt2);
}
