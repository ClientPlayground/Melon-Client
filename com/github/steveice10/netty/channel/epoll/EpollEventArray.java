package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.util.internal.PlatformDependent;

final class EpollEventArray {
  private static final int EPOLL_EVENT_SIZE = Native.sizeofEpollEvent();
  
  private static final int EPOLL_DATA_OFFSET = Native.offsetofEpollData();
  
  private long memoryAddress;
  
  private int length;
  
  EpollEventArray(int length) {
    if (length < 1)
      throw new IllegalArgumentException("length must be >= 1 but was " + length); 
    this.length = length;
    this.memoryAddress = allocate(length);
  }
  
  private static long allocate(int length) {
    return PlatformDependent.allocateMemory((length * EPOLL_EVENT_SIZE));
  }
  
  long memoryAddress() {
    return this.memoryAddress;
  }
  
  int length() {
    return this.length;
  }
  
  void increase() {
    this.length <<= 1;
    free();
    this.memoryAddress = allocate(this.length);
  }
  
  void free() {
    PlatformDependent.freeMemory(this.memoryAddress);
  }
  
  int events(int index) {
    return PlatformDependent.getInt(this.memoryAddress + (index * EPOLL_EVENT_SIZE));
  }
  
  int fd(int index) {
    return PlatformDependent.getInt(this.memoryAddress + (index * EPOLL_EVENT_SIZE) + EPOLL_DATA_OFFSET);
  }
}
