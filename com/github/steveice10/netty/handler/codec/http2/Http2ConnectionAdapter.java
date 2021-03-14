package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;

public class Http2ConnectionAdapter implements Http2Connection.Listener {
  public void onStreamAdded(Http2Stream stream) {}
  
  public void onStreamActive(Http2Stream stream) {}
  
  public void onStreamHalfClosed(Http2Stream stream) {}
  
  public void onStreamClosed(Http2Stream stream) {}
  
  public void onStreamRemoved(Http2Stream stream) {}
  
  public void onGoAwaySent(int lastStreamId, long errorCode, ByteBuf debugData) {}
  
  public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {}
}
