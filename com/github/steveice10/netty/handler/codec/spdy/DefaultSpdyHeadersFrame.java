package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Map;

public class DefaultSpdyHeadersFrame extends DefaultSpdyStreamFrame implements SpdyHeadersFrame {
  private boolean invalid;
  
  private boolean truncated;
  
  private final SpdyHeaders headers;
  
  public DefaultSpdyHeadersFrame(int streamId) {
    this(streamId, true);
  }
  
  public DefaultSpdyHeadersFrame(int streamId, boolean validate) {
    super(streamId);
    this.headers = new DefaultSpdyHeaders(validate);
  }
  
  public SpdyHeadersFrame setStreamId(int streamId) {
    super.setStreamId(streamId);
    return this;
  }
  
  public SpdyHeadersFrame setLast(boolean last) {
    super.setLast(last);
    return this;
  }
  
  public boolean isInvalid() {
    return this.invalid;
  }
  
  public SpdyHeadersFrame setInvalid() {
    this.invalid = true;
    return this;
  }
  
  public boolean isTruncated() {
    return this.truncated;
  }
  
  public SpdyHeadersFrame setTruncated() {
    this.truncated = true;
    return this;
  }
  
  public SpdyHeaders headers() {
    return this.headers;
  }
  
  public String toString() {
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append("(last: ").append(isLast()).append(')').append(StringUtil.NEWLINE).append("--> Stream-ID = ").append(streamId()).append(StringUtil.NEWLINE).append("--> Headers:").append(StringUtil.NEWLINE);
    appendHeaders(buf);
    buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    return buf.toString();
  }
  
  protected void appendHeaders(StringBuilder buf) {
    for (Map.Entry<CharSequence, CharSequence> e : (Iterable<Map.Entry<CharSequence, CharSequence>>)headers()) {
      buf.append("    ");
      buf.append(e.getKey());
      buf.append(": ");
      buf.append(e.getValue());
      buf.append(StringUtil.NEWLINE);
    } 
  }
}
