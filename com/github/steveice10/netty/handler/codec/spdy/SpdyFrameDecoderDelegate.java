package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface SpdyFrameDecoderDelegate {
  void readDataFrame(int paramInt, boolean paramBoolean, ByteBuf paramByteBuf);
  
  void readSynStreamFrame(int paramInt1, int paramInt2, byte paramByte, boolean paramBoolean1, boolean paramBoolean2);
  
  void readSynReplyFrame(int paramInt, boolean paramBoolean);
  
  void readRstStreamFrame(int paramInt1, int paramInt2);
  
  void readSettingsFrame(boolean paramBoolean);
  
  void readSetting(int paramInt1, int paramInt2, boolean paramBoolean1, boolean paramBoolean2);
  
  void readSettingsEnd();
  
  void readPingFrame(int paramInt);
  
  void readGoAwayFrame(int paramInt1, int paramInt2);
  
  void readHeadersFrame(int paramInt, boolean paramBoolean);
  
  void readWindowUpdateFrame(int paramInt1, int paramInt2);
  
  void readHeaderBlock(ByteBuf paramByteBuf);
  
  void readHeaderBlockEnd();
  
  void readFrameError(String paramString);
}
