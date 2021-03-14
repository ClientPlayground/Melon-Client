package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.ReferenceCounted;

public interface LastHttpContent extends HttpContent {
  public static final LastHttpContent EMPTY_LAST_CONTENT = new LastHttpContent() {
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public LastHttpContent copy() {
        return EMPTY_LAST_CONTENT;
      }
      
      public LastHttpContent duplicate() {
        return this;
      }
      
      public LastHttpContent replace(ByteBuf content) {
        return new DefaultLastHttpContent(content);
      }
      
      public LastHttpContent retainedDuplicate() {
        return this;
      }
      
      public HttpHeaders trailingHeaders() {
        return EmptyHttpHeaders.INSTANCE;
      }
      
      public DecoderResult decoderResult() {
        return DecoderResult.SUCCESS;
      }
      
      @Deprecated
      public DecoderResult getDecoderResult() {
        return decoderResult();
      }
      
      public void setDecoderResult(DecoderResult result) {
        throw new UnsupportedOperationException("read only");
      }
      
      public int refCnt() {
        return 1;
      }
      
      public LastHttpContent retain() {
        return this;
      }
      
      public LastHttpContent retain(int increment) {
        return this;
      }
      
      public LastHttpContent touch() {
        return this;
      }
      
      public LastHttpContent touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
      
      public String toString() {
        return "EmptyLastHttpContent";
      }
    };
  
  HttpHeaders trailingHeaders();
  
  LastHttpContent copy();
  
  LastHttpContent duplicate();
  
  LastHttpContent retainedDuplicate();
  
  LastHttpContent replace(ByteBuf paramByteBuf);
  
  LastHttpContent retain(int paramInt);
  
  LastHttpContent retain();
  
  LastHttpContent touch();
  
  LastHttpContent touch(Object paramObject);
}
