package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class WebSocketFrame extends DefaultByteBufHolder {
  private final boolean finalFragment;
  
  private final int rsv;
  
  protected WebSocketFrame(ByteBuf binaryData) {
    this(true, 0, binaryData);
  }
  
  protected WebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
    super(binaryData);
    this.finalFragment = finalFragment;
    this.rsv = rsv;
  }
  
  public boolean isFinalFragment() {
    return this.finalFragment;
  }
  
  public int rsv() {
    return this.rsv;
  }
  
  public WebSocketFrame copy() {
    return (WebSocketFrame)super.copy();
  }
  
  public WebSocketFrame duplicate() {
    return (WebSocketFrame)super.duplicate();
  }
  
  public WebSocketFrame retainedDuplicate() {
    return (WebSocketFrame)super.retainedDuplicate();
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(data: " + contentToString() + ')';
  }
  
  public WebSocketFrame retain() {
    super.retain();
    return this;
  }
  
  public WebSocketFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public WebSocketFrame touch() {
    super.touch();
    return this;
  }
  
  public WebSocketFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public abstract WebSocketFrame replace(ByteBuf paramByteBuf);
}
