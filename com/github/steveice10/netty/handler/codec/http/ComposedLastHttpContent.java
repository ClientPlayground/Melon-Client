package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.ReferenceCounted;

final class ComposedLastHttpContent implements LastHttpContent {
  private final HttpHeaders trailingHeaders;
  
  private DecoderResult result;
  
  ComposedLastHttpContent(HttpHeaders trailingHeaders) {
    this.trailingHeaders = trailingHeaders;
  }
  
  public HttpHeaders trailingHeaders() {
    return this.trailingHeaders;
  }
  
  public LastHttpContent copy() {
    LastHttpContent content = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
    content.trailingHeaders().set(trailingHeaders());
    return content;
  }
  
  public LastHttpContent duplicate() {
    return copy();
  }
  
  public LastHttpContent retainedDuplicate() {
    return copy();
  }
  
  public LastHttpContent replace(ByteBuf content) {
    LastHttpContent dup = new DefaultLastHttpContent(content);
    dup.trailingHeaders().setAll(trailingHeaders());
    return dup;
  }
  
  public LastHttpContent retain(int increment) {
    return this;
  }
  
  public LastHttpContent retain() {
    return this;
  }
  
  public LastHttpContent touch() {
    return this;
  }
  
  public LastHttpContent touch(Object hint) {
    return this;
  }
  
  public ByteBuf content() {
    return Unpooled.EMPTY_BUFFER;
  }
  
  public DecoderResult decoderResult() {
    return this.result;
  }
  
  public DecoderResult getDecoderResult() {
    return decoderResult();
  }
  
  public void setDecoderResult(DecoderResult result) {
    this.result = result;
  }
  
  public int refCnt() {
    return 1;
  }
  
  public boolean release() {
    return false;
  }
  
  public boolean release(int decrement) {
    return false;
  }
}
