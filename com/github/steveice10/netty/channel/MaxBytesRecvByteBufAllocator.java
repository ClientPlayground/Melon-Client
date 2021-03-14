package com.github.steveice10.netty.channel;

import java.util.Map;

public interface MaxBytesRecvByteBufAllocator extends RecvByteBufAllocator {
  int maxBytesPerRead();
  
  MaxBytesRecvByteBufAllocator maxBytesPerRead(int paramInt);
  
  int maxBytesPerIndividualRead();
  
  MaxBytesRecvByteBufAllocator maxBytesPerIndividualRead(int paramInt);
  
  Map.Entry<Integer, Integer> maxBytesPerReadPair();
  
  MaxBytesRecvByteBufAllocator maxBytesPerReadPair(int paramInt1, int paramInt2);
}
