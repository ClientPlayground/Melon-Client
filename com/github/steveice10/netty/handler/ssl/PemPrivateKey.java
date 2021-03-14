package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.security.PrivateKey;

public final class PemPrivateKey extends AbstractReferenceCounted implements PrivateKey, PemEncoded {
  private static final long serialVersionUID = 7978017465645018936L;
  
  private static final byte[] BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);
  
  private static final byte[] END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);
  
  private static final String PKCS8_FORMAT = "PKCS#8";
  
  private final ByteBuf content;
  
  static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect, PrivateKey key) {
    if (key instanceof PemEncoded)
      return ((PemEncoded)key).retain(); 
    ByteBuf encoded = Unpooled.wrappedBuffer(key.getEncoded());
    try {
      ByteBuf base64 = SslUtils.toBase64(allocator, encoded);
    } finally {
      SslUtils.zerooutAndRelease(encoded);
    } 
  }
  
  public static PemPrivateKey valueOf(byte[] key) {
    return valueOf(Unpooled.wrappedBuffer(key));
  }
  
  public static PemPrivateKey valueOf(ByteBuf key) {
    return new PemPrivateKey(key);
  }
  
  private PemPrivateKey(ByteBuf content) {
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
  }
  
  public boolean isSensitive() {
    return true;
  }
  
  public ByteBuf content() {
    int count = refCnt();
    if (count <= 0)
      throw new IllegalReferenceCountException(count); 
    return this.content;
  }
  
  public PemPrivateKey copy() {
    return replace(this.content.copy());
  }
  
  public PemPrivateKey duplicate() {
    return replace(this.content.duplicate());
  }
  
  public PemPrivateKey retainedDuplicate() {
    return replace(this.content.retainedDuplicate());
  }
  
  public PemPrivateKey replace(ByteBuf content) {
    return new PemPrivateKey(content);
  }
  
  public PemPrivateKey touch() {
    this.content.touch();
    return this;
  }
  
  public PemPrivateKey touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public PemPrivateKey retain() {
    return (PemPrivateKey)super.retain();
  }
  
  public PemPrivateKey retain(int increment) {
    return (PemPrivateKey)super.retain(increment);
  }
  
  protected void deallocate() {
    SslUtils.zerooutAndRelease(this.content);
  }
  
  public byte[] getEncoded() {
    throw new UnsupportedOperationException();
  }
  
  public String getAlgorithm() {
    throw new UnsupportedOperationException();
  }
  
  public String getFormat() {
    return "PKCS#8";
  }
  
  public void destroy() {
    release(refCnt());
  }
  
  public boolean isDestroyed() {
    return (refCnt() == 0);
  }
}
