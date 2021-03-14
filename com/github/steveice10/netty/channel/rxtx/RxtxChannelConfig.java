package com.github.steveice10.netty.channel.rxtx;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

@Deprecated
public interface RxtxChannelConfig extends ChannelConfig {
  RxtxChannelConfig setBaudrate(int paramInt);
  
  RxtxChannelConfig setStopbits(Stopbits paramStopbits);
  
  RxtxChannelConfig setDatabits(Databits paramDatabits);
  
  RxtxChannelConfig setParitybit(Paritybit paramParitybit);
  
  int getBaudrate();
  
  Stopbits getStopbits();
  
  Databits getDatabits();
  
  Paritybit getParitybit();
  
  boolean isDtr();
  
  RxtxChannelConfig setDtr(boolean paramBoolean);
  
  boolean isRts();
  
  RxtxChannelConfig setRts(boolean paramBoolean);
  
  int getWaitTimeMillis();
  
  RxtxChannelConfig setWaitTimeMillis(int paramInt);
  
  RxtxChannelConfig setReadTimeout(int paramInt);
  
  int getReadTimeout();
  
  RxtxChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  RxtxChannelConfig setMaxMessagesPerRead(int paramInt);
  
  RxtxChannelConfig setWriteSpinCount(int paramInt);
  
  RxtxChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  RxtxChannelConfig setAutoRead(boolean paramBoolean);
  
  RxtxChannelConfig setAutoClose(boolean paramBoolean);
  
  RxtxChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  RxtxChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  RxtxChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  public enum Stopbits {
    STOPBITS_1(1),
    STOPBITS_2(2),
    STOPBITS_1_5(3);
    
    private final int value;
    
    Stopbits(int value) {
      this.value = value;
    }
    
    public int value() {
      return this.value;
    }
  }
  
  public enum Databits {
    DATABITS_5(5),
    DATABITS_6(6),
    DATABITS_7(7),
    DATABITS_8(8);
    
    private final int value;
    
    Databits(int value) {
      this.value = value;
    }
    
    public int value() {
      return this.value;
    }
  }
  
  public enum Paritybit {
    NONE(0),
    ODD(1),
    EVEN(2),
    MARK(3),
    SPACE(4);
    
    private final int value;
    
    Paritybit(int value) {
      this.value = value;
    }
    
    public int value() {
      return this.value;
    }
  }
}
