package com.github.steveice10.netty.channel;

public interface MaxMessagesRecvByteBufAllocator extends RecvByteBufAllocator {
  int maxMessagesPerRead();
  
  MaxMessagesRecvByteBufAllocator maxMessagesPerRead(int paramInt);
}
