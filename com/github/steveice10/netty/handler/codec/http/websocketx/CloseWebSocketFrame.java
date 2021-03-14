package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.ReferenceCounted;

public class CloseWebSocketFrame extends WebSocketFrame {
  public CloseWebSocketFrame() {
    super(Unpooled.buffer(0));
  }
  
  public CloseWebSocketFrame(int statusCode, String reasonText) {
    this(true, 0, statusCode, reasonText);
  }
  
  public CloseWebSocketFrame(boolean finalFragment, int rsv) {
    this(finalFragment, rsv, Unpooled.buffer(0));
  }
  
  public CloseWebSocketFrame(boolean finalFragment, int rsv, int statusCode, String reasonText) {
    super(finalFragment, rsv, newBinaryData(statusCode, reasonText));
  }
  
  private static ByteBuf newBinaryData(int statusCode, String reasonText) {
    if (reasonText == null)
      reasonText = ""; 
    ByteBuf binaryData = Unpooled.buffer(2 + reasonText.length());
    binaryData.writeShort(statusCode);
    if (!reasonText.isEmpty())
      binaryData.writeCharSequence(reasonText, CharsetUtil.UTF_8); 
    binaryData.readerIndex(0);
    return binaryData;
  }
  
  public CloseWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(finalFragment, rsv, binaryData);
  }
  
  public int statusCode() {
    ByteBuf binaryData = content();
    if (binaryData == null || binaryData.capacity() == 0)
      return -1; 
    binaryData.readerIndex(0);
    int statusCode = binaryData.readShort();
    binaryData.readerIndex(0);
    return statusCode;
  }
  
  public String reasonText() {
    ByteBuf binaryData = content();
    if (binaryData == null || binaryData.capacity() <= 2)
      return ""; 
    binaryData.readerIndex(2);
    String reasonText = binaryData.toString(CharsetUtil.UTF_8);
    binaryData.readerIndex(0);
    return reasonText;
  }
  
  public CloseWebSocketFrame copy() {
    return (CloseWebSocketFrame)super.copy();
  }
  
  public CloseWebSocketFrame duplicate() {
    return (CloseWebSocketFrame)super.duplicate();
  }
  
  public CloseWebSocketFrame retainedDuplicate() {
    return (CloseWebSocketFrame)super.retainedDuplicate();
  }
  
  public CloseWebSocketFrame replace(ByteBuf content) {
    return new CloseWebSocketFrame(isFinalFragment(), rsv(), content);
  }
  
  public CloseWebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public CloseWebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public CloseWebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public CloseWebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
