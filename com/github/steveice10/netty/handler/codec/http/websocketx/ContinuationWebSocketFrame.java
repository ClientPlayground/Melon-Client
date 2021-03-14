package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.ReferenceCounted;

public class ContinuationWebSocketFrame extends WebSocketFrame {
  public ContinuationWebSocketFrame() {
    this(Unpooled.buffer(0));
  }
  
  public ContinuationWebSocketFrame(ByteBuf binaryData) {
    super(binaryData);
  }
  
  public ContinuationWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(finalFragment, rsv, binaryData);
  }
  
  public ContinuationWebSocketFrame(boolean finalFragment, int rsv, String text) {
    this(finalFragment, rsv, fromText(text));
  }
  
  public String text() {
    return content().toString(CharsetUtil.UTF_8);
  }
  
  private static ByteBuf fromText(String text) {
    if (text == null || text.isEmpty())
      return Unpooled.EMPTY_BUFFER; 
    return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
  }
  
  public ContinuationWebSocketFrame copy() {
    return (ContinuationWebSocketFrame)super.copy();
  }
  
  public ContinuationWebSocketFrame duplicate() {
    return (ContinuationWebSocketFrame)super.duplicate();
  }
  
  public ContinuationWebSocketFrame retainedDuplicate() {
    return (ContinuationWebSocketFrame)super.retainedDuplicate();
  }
  
  public ContinuationWebSocketFrame replace(ByteBuf content) {
    return new ContinuationWebSocketFrame(isFinalFragment(), rsv(), content);
  }
  
  public ContinuationWebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public ContinuationWebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public ContinuationWebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public ContinuationWebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
