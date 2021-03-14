package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DefaultFullHttpRequest extends DefaultHttpRequest implements FullHttpRequest {
  private final ByteBuf content;
  
  private final HttpHeaders trailingHeader;
  
  private int hash;
  
  public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
    this(httpVersion, method, uri, Unpooled.buffer(0));
  }
  
  public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content) {
    this(httpVersion, method, uri, content, true);
  }
  
  public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
    this(httpVersion, method, uri, Unpooled.buffer(0), validateHeaders);
  }
  
  public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content, boolean validateHeaders) {
    super(httpVersion, method, uri, validateHeaders);
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
    this.trailingHeader = new DefaultHttpHeaders(validateHeaders);
  }
  
  public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeader) {
    super(httpVersion, method, uri, headers);
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
    this.trailingHeader = (HttpHeaders)ObjectUtil.checkNotNull(trailingHeader, "trailingHeader");
  }
  
  public HttpHeaders trailingHeaders() {
    return this.trailingHeader;
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public int refCnt() {
    return this.content.refCnt();
  }
  
  public FullHttpRequest retain() {
    this.content.retain();
    return this;
  }
  
  public FullHttpRequest retain(int increment) {
    this.content.retain(increment);
    return this;
  }
  
  public FullHttpRequest touch() {
    this.content.touch();
    return this;
  }
  
  public FullHttpRequest touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public boolean release() {
    return this.content.release();
  }
  
  public boolean release(int decrement) {
    return this.content.release(decrement);
  }
  
  public FullHttpRequest setProtocolVersion(HttpVersion version) {
    super.setProtocolVersion(version);
    return this;
  }
  
  public FullHttpRequest setMethod(HttpMethod method) {
    super.setMethod(method);
    return this;
  }
  
  public FullHttpRequest setUri(String uri) {
    super.setUri(uri);
    return this;
  }
  
  public FullHttpRequest copy() {
    return replace(content().copy());
  }
  
  public FullHttpRequest duplicate() {
    return replace(content().duplicate());
  }
  
  public FullHttpRequest retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public FullHttpRequest replace(ByteBuf content) {
    FullHttpRequest request = new DefaultFullHttpRequest(protocolVersion(), method(), uri(), content, headers().copy(), trailingHeaders().copy());
    request.setDecoderResult(decoderResult());
    return request;
  }
  
  public int hashCode() {
    int hash = this.hash;
    if (hash == 0) {
      if (content().refCnt() != 0) {
        try {
          hash = 31 + content().hashCode();
        } catch (IllegalReferenceCountException ignored) {
          hash = 31;
        } 
      } else {
        hash = 31;
      } 
      hash = 31 * hash + trailingHeaders().hashCode();
      hash = 31 * hash + super.hashCode();
      this.hash = hash;
    } 
    return hash;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof DefaultFullHttpRequest))
      return false; 
    DefaultFullHttpRequest other = (DefaultFullHttpRequest)o;
    return (super.equals(other) && 
      content().equals(other.content()) && 
      trailingHeaders().equals(other.trailingHeaders()));
  }
  
  public String toString() {
    return HttpMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
  }
}
