package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;

class PemValue extends AbstractReferenceCounted implements PemEncoded {
  private final ByteBuf content;
  
  private final boolean sensitive;
  
  public PemValue(ByteBuf content, boolean sensitive) {
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
    this.sensitive = sensitive;
  }
  
  public boolean isSensitive() {
    return this.sensitive;
  }
  
  public ByteBuf content() {
    int count = refCnt();
    if (count <= 0)
      throw new IllegalReferenceCountException(count); 
    return this.content;
  }
  
  public PemValue copy() {
    return replace(this.content.copy());
  }
  
  public PemValue duplicate() {
    return replace(this.content.duplicate());
  }
  
  public PemValue retainedDuplicate() {
    return replace(this.content.retainedDuplicate());
  }
  
  public PemValue replace(ByteBuf content) {
    return new PemValue(content, this.sensitive);
  }
  
  public PemValue touch() {
    return (PemValue)super.touch();
  }
  
  public PemValue touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public PemValue retain() {
    return (PemValue)super.retain();
  }
  
  public PemValue retain(int increment) {
    return (PemValue)super.retain(increment);
  }
  
  protected void deallocate() {
    if (this.sensitive)
      SslUtils.zeroout(this.content); 
    this.content.release();
  }
}
