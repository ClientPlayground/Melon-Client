package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.ReferenceCounted;

public class TextWebSocketFrame extends WebSocketFrame {
  public TextWebSocketFrame() {
    super(Unpooled.buffer(0));
  }
  
  public TextWebSocketFrame(String text) {
    super(fromText(text));
  }
  
  public TextWebSocketFrame(ByteBuf binaryData) {
    super(binaryData);
  }
  
  public TextWebSocketFrame(boolean finalFragment, int rsv, String text) {
    super(finalFragment, rsv, fromText(text));
  }
  
  private static ByteBuf fromText(String text) {
    if (text == null || text.isEmpty())
      return Unpooled.EMPTY_BUFFER; 
    return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
  }
  
  public TextWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(finalFragment, rsv, binaryData);
  }
  
  public String text() {
    return content().toString(CharsetUtil.UTF_8);
  }
  
  public TextWebSocketFrame copy() {
    return (TextWebSocketFrame)super.copy();
  }
  
  public TextWebSocketFrame duplicate() {
    return (TextWebSocketFrame)super.duplicate();
  }
  
  public TextWebSocketFrame retainedDuplicate() {
    return (TextWebSocketFrame)super.retainedDuplicate();
  }
  
  public TextWebSocketFrame replace(ByteBuf content) {
    return new TextWebSocketFrame(isFinalFragment(), rsv(), content);
  }
  
  public TextWebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public TextWebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public TextWebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public TextWebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
