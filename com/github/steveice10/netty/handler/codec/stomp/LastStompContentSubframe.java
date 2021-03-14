package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.ReferenceCounted;

public interface LastStompContentSubframe extends StompContentSubframe {
  public static final LastStompContentSubframe EMPTY_LAST_CONTENT = new LastStompContentSubframe() {
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public LastStompContentSubframe copy() {
        return EMPTY_LAST_CONTENT;
      }
      
      public LastStompContentSubframe duplicate() {
        return this;
      }
      
      public LastStompContentSubframe retainedDuplicate() {
        return this;
      }
      
      public LastStompContentSubframe replace(ByteBuf content) {
        return new DefaultLastStompContentSubframe(content);
      }
      
      public LastStompContentSubframe retain() {
        return this;
      }
      
      public LastStompContentSubframe retain(int increment) {
        return this;
      }
      
      public LastStompContentSubframe touch() {
        return this;
      }
      
      public LastStompContentSubframe touch(Object hint) {
        return this;
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
      
      public DecoderResult decoderResult() {
        return DecoderResult.SUCCESS;
      }
      
      public void setDecoderResult(DecoderResult result) {
        throw new UnsupportedOperationException("read only");
      }
    };
  
  LastStompContentSubframe copy();
  
  LastStompContentSubframe duplicate();
  
  LastStompContentSubframe retainedDuplicate();
  
  LastStompContentSubframe replace(ByteBuf paramByteBuf);
  
  LastStompContentSubframe retain();
  
  LastStompContentSubframe retain(int paramInt);
  
  LastStompContentSubframe touch();
  
  LastStompContentSubframe touch(Object paramObject);
}
