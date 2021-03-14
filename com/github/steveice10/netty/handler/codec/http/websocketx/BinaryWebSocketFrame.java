package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;

public class BinaryWebSocketFrame extends WebSocketFrame {
  public BinaryWebSocketFrame() {
    super(Unpooled.buffer(0));
  }
  
  public BinaryWebSocketFrame(ByteBuf binaryData) {
    super(binaryData);
  }
  
  public BinaryWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(finalFragment, rsv, binaryData);
  }
  
  public BinaryWebSocketFrame copy() {
    return (BinaryWebSocketFrame)super.copy();
  }
  
  public BinaryWebSocketFrame duplicate() {
    return (BinaryWebSocketFrame)super.duplicate();
  }
  
  public BinaryWebSocketFrame retainedDuplicate() {
    return (BinaryWebSocketFrame)super.retainedDuplicate();
  }
  
  public BinaryWebSocketFrame replace(ByteBuf content) {
    return new BinaryWebSocketFrame(isFinalFragment(), rsv(), content);
  }
  
  public BinaryWebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public BinaryWebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public BinaryWebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public BinaryWebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
