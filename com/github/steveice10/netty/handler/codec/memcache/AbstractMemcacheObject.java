package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.AbstractReferenceCounted;

public abstract class AbstractMemcacheObject extends AbstractReferenceCounted implements MemcacheObject {
  private DecoderResult decoderResult = DecoderResult.SUCCESS;
  
  public DecoderResult decoderResult() {
    return this.decoderResult;
  }
  
  public void setDecoderResult(DecoderResult result) {
    if (result == null)
      throw new NullPointerException("DecoderResult should not be null."); 
    this.decoderResult = result;
  }
}
