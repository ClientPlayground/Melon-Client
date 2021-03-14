package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.DecoderResult;

public class DefaultHttpObject implements HttpObject {
  private static final int HASH_CODE_PRIME = 31;
  
  private DecoderResult decoderResult = DecoderResult.SUCCESS;
  
  public DecoderResult decoderResult() {
    return this.decoderResult;
  }
  
  @Deprecated
  public DecoderResult getDecoderResult() {
    return decoderResult();
  }
  
  public void setDecoderResult(DecoderResult decoderResult) {
    if (decoderResult == null)
      throw new NullPointerException("decoderResult"); 
    this.decoderResult = decoderResult;
  }
  
  public int hashCode() {
    int result = 1;
    result = 31 * result + this.decoderResult.hashCode();
    return result;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof DefaultHttpObject))
      return false; 
    DefaultHttpObject other = (DefaultHttpObject)o;
    return decoderResult().equals(other.decoderResult());
  }
}
