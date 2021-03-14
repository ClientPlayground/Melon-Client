package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.ReferenceCounted;

public interface LastMemcacheContent extends MemcacheContent {
  public static final LastMemcacheContent EMPTY_LAST_CONTENT = new LastMemcacheContent() {
      public LastMemcacheContent copy() {
        return EMPTY_LAST_CONTENT;
      }
      
      public LastMemcacheContent duplicate() {
        return this;
      }
      
      public LastMemcacheContent retainedDuplicate() {
        return this;
      }
      
      public LastMemcacheContent replace(ByteBuf content) {
        return new DefaultLastMemcacheContent(content);
      }
      
      public LastMemcacheContent retain(int increment) {
        return this;
      }
      
      public LastMemcacheContent retain() {
        return this;
      }
      
      public LastMemcacheContent touch() {
        return this;
      }
      
      public LastMemcacheContent touch(Object hint) {
        return this;
      }
      
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public DecoderResult decoderResult() {
        return DecoderResult.SUCCESS;
      }
      
      public void setDecoderResult(DecoderResult result) {
        throw new UnsupportedOperationException("read only");
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
    };
  
  LastMemcacheContent copy();
  
  LastMemcacheContent duplicate();
  
  LastMemcacheContent retainedDuplicate();
  
  LastMemcacheContent replace(ByteBuf paramByteBuf);
  
  LastMemcacheContent retain(int paramInt);
  
  LastMemcacheContent retain();
  
  LastMemcacheContent touch();
  
  LastMemcacheContent touch(Object paramObject);
}
