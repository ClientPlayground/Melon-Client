package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultStompContentSubframe extends DefaultByteBufHolder implements StompContentSubframe {
  private DecoderResult decoderResult = DecoderResult.SUCCESS;
  
  public DefaultStompContentSubframe(ByteBuf content) {
    super(content);
  }
  
  public StompContentSubframe copy() {
    return (StompContentSubframe)super.copy();
  }
  
  public StompContentSubframe duplicate() {
    return (StompContentSubframe)super.duplicate();
  }
  
  public StompContentSubframe retainedDuplicate() {
    return (StompContentSubframe)super.retainedDuplicate();
  }
  
  public StompContentSubframe replace(ByteBuf content) {
    return new DefaultStompContentSubframe(content);
  }
  
  public StompContentSubframe retain() {
    super.retain();
    return this;
  }
  
  public StompContentSubframe retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public StompContentSubframe touch() {
    super.touch();
    return this;
  }
  
  public StompContentSubframe touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public DecoderResult decoderResult() {
    return this.decoderResult;
  }
  
  public void setDecoderResult(DecoderResult decoderResult) {
    this.decoderResult = decoderResult;
  }
  
  public String toString() {
    return "DefaultStompContent{decoderResult=" + this.decoderResult + '}';
  }
}
