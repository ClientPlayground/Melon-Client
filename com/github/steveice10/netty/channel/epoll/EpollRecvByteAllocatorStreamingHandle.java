package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.RecvByteBufAllocator;

final class EpollRecvByteAllocatorStreamingHandle extends EpollRecvByteAllocatorHandle {
  public EpollRecvByteAllocatorStreamingHandle(RecvByteBufAllocator.ExtendedHandle handle) {
    super(handle);
  }
  
  boolean maybeMoreDataToRead() {
    return (lastBytesRead() == attemptedBytesRead() || isReceivedRdHup());
  }
}
