package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;

public class PingWebSocketFrame extends WebSocketFrame {
  public PingWebSocketFrame() {
    super(true, 0, Unpooled.buffer(0));
  }
  
  public PingWebSocketFrame(ByteBuf binaryData) {
    super(binaryData);
  }
  
  public PingWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(finalFragment, rsv, binaryData);
  }
  
  public PingWebSocketFrame copy() {
    return (PingWebSocketFrame)super.copy();
  }
  
  public PingWebSocketFrame duplicate() {
    return (PingWebSocketFrame)super.duplicate();
  }
  
  public PingWebSocketFrame retainedDuplicate() {
    return (PingWebSocketFrame)super.retainedDuplicate();
  }
  
  public PingWebSocketFrame replace(ByteBuf content) {
    return new PingWebSocketFrame(isFinalFragment(), rsv(), content);
  }
  
  public PingWebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public PingWebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public PingWebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public PingWebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
