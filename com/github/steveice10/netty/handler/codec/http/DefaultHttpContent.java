package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultHttpContent extends DefaultHttpObject implements HttpContent {
  private final ByteBuf content;
  
  public DefaultHttpContent(ByteBuf content) {
    if (content == null)
      throw new NullPointerException("content"); 
    this.content = content;
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public HttpContent copy() {
    return replace(this.content.copy());
  }
  
  public HttpContent duplicate() {
    return replace(this.content.duplicate());
  }
  
  public HttpContent retainedDuplicate() {
    return replace(this.content.retainedDuplicate());
  }
  
  public HttpContent replace(ByteBuf content) {
    return new DefaultHttpContent(content);
  }
  
  public int refCnt() {
    return this.content.refCnt();
  }
  
  public HttpContent retain() {
    this.content.retain();
    return this;
  }
  
  public HttpContent retain(int increment) {
    this.content.retain(increment);
    return this;
  }
  
  public HttpContent touch() {
    this.content.touch();
    return this;
  }
  
  public HttpContent touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public boolean release() {
    return this.content.release();
  }
  
  public boolean release(int decrement) {
    return this.content.release(decrement);
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(data: " + 
      content() + ", decoderResult: " + decoderResult() + ')';
  }
}
