package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.util.internal.PlatformDependent;

final class KQueueEventArray {
  private static final int KQUEUE_EVENT_SIZE = Native.sizeofKEvent();
  
  private static final int KQUEUE_IDENT_OFFSET = Native.offsetofKEventIdent();
  
  private static final int KQUEUE_FILTER_OFFSET = Native.offsetofKEventFilter();
  
  private static final int KQUEUE_FFLAGS_OFFSET = Native.offsetofKEventFFlags();
  
  private static final int KQUEUE_FLAGS_OFFSET = Native.offsetofKEventFlags();
  
  private static final int KQUEUE_DATA_OFFSET = Native.offsetofKeventData();
  
  private long memoryAddress;
  
  private int size;
  
  private int capacity;
  
  KQueueEventArray(int capacity) {
    if (capacity < 1)
      throw new IllegalArgumentException("capacity must be >= 1 but was " + capacity); 
    this.memoryAddress = PlatformDependent.allocateMemory((capacity * KQUEUE_EVENT_SIZE));
    this.capacity = capacity;
  }
  
  long memoryAddress() {
    return this.memoryAddress;
  }
  
  int capacity() {
    return this.capacity;
  }
  
  int size() {
    return this.size;
  }
  
  void clear() {
    this.size = 0;
  }
  
  void evSet(AbstractKQueueChannel ch, short filter, short flags, int fflags) {
    checkSize();
    evSet(getKEventOffset(this.size++), ch, ch.socket.intValue(), filter, flags, fflags);
  }
  
  private void checkSize() {
    if (this.size == this.capacity)
      realloc(true); 
  }
  
  void realloc(boolean throwIfFail) {
    int newLength = (this.capacity <= 65536) ? (this.capacity << 1) : (this.capacity + this.capacity >> 1);
    long newMemoryAddress = PlatformDependent.reallocateMemory(this.memoryAddress, (newLength * KQUEUE_EVENT_SIZE));
    if (newMemoryAddress != 0L) {
      this.memoryAddress = newMemoryAddress;
      this.capacity = newLength;
      return;
    } 
    if (throwIfFail)
      throw new OutOfMemoryError("unable to allocate " + newLength + " new bytes! Existing capacity is: " + this.capacity); 
  }
  
  void free() {
    PlatformDependent.freeMemory(this.memoryAddress);
    this.memoryAddress = (this.size = this.capacity = 0);
  }
  
  long getKEventOffset(int index) {
    return this.memoryAddress + (index * KQUEUE_EVENT_SIZE);
  }
  
  short flags(int index) {
    return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FLAGS_OFFSET);
  }
  
  short filter(int index) {
    return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FILTER_OFFSET);
  }
  
  short fflags(int index) {
    return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FFLAGS_OFFSET);
  }
  
  int fd(int index) {
    return PlatformDependent.getInt(getKEventOffset(index) + KQUEUE_IDENT_OFFSET);
  }
  
  long data(int index) {
    return PlatformDependent.getLong(getKEventOffset(index) + KQUEUE_DATA_OFFSET);
  }
  
  AbstractKQueueChannel channel(int index) {
    return getChannel(getKEventOffset(index));
  }
  
  private static native void evSet(long paramLong, AbstractKQueueChannel paramAbstractKQueueChannel, int paramInt1, short paramShort1, short paramShort2, int paramInt2);
  
  private static native AbstractKQueueChannel getChannel(long paramLong);
  
  static native void deleteGlobalRefs(long paramLong1, long paramLong2);
}
