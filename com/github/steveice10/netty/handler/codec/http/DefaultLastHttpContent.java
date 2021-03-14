package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.DefaultHeaders;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Map;

public class DefaultLastHttpContent extends DefaultHttpContent implements LastHttpContent {
  private final HttpHeaders trailingHeaders;
  
  private final boolean validateHeaders;
  
  public DefaultLastHttpContent() {
    this(Unpooled.buffer(0));
  }
  
  public DefaultLastHttpContent(ByteBuf content) {
    this(content, true);
  }
  
  public DefaultLastHttpContent(ByteBuf content, boolean validateHeaders) {
    super(content);
    this.trailingHeaders = new TrailingHttpHeaders(validateHeaders);
    this.validateHeaders = validateHeaders;
  }
  
  public LastHttpContent copy() {
    return replace(content().copy());
  }
  
  public LastHttpContent duplicate() {
    return replace(content().duplicate());
  }
  
  public LastHttpContent retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public LastHttpContent replace(ByteBuf content) {
    DefaultLastHttpContent dup = new DefaultLastHttpContent(content, this.validateHeaders);
    dup.trailingHeaders().set(trailingHeaders());
    return dup;
  }
  
  public LastHttpContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public LastHttpContent retain() {
    super.retain();
    return this;
  }
  
  public LastHttpContent touch() {
    super.touch();
    return this;
  }
  
  public LastHttpContent touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public HttpHeaders trailingHeaders() {
    return this.trailingHeaders;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(super.toString());
    buf.append(StringUtil.NEWLINE);
    appendHeaders(buf);
    buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    return buf.toString();
  }
  
  private void appendHeaders(StringBuilder buf) {
    for (Map.Entry<String, String> e : (Iterable<Map.Entry<String, String>>)trailingHeaders()) {
      buf.append(e.getKey());
      buf.append(": ");
      buf.append(e.getValue());
      buf.append(StringUtil.NEWLINE);
    } 
  }
  
  private static final class TrailingHttpHeaders extends DefaultHttpHeaders {
    private static final DefaultHeaders.NameValidator<CharSequence> TrailerNameValidator = new DefaultHeaders.NameValidator<CharSequence>() {
        public void validateName(CharSequence name) {
          DefaultHttpHeaders.HttpNameValidator.validateName(name);
          if (HttpHeaderNames.CONTENT_LENGTH.contentEqualsIgnoreCase(name) || HttpHeaderNames.TRANSFER_ENCODING
            .contentEqualsIgnoreCase(name) || HttpHeaderNames.TRAILER
            .contentEqualsIgnoreCase(name))
            throw new IllegalArgumentException("prohibited trailing header: " + name); 
        }
      };
    
    TrailingHttpHeaders(boolean validate) {
      super(validate, validate ? TrailerNameValidator : DefaultHeaders.NameValidator.NOT_NULL);
    }
  }
}
