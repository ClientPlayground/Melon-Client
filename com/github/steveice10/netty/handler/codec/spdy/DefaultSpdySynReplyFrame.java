package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSpdySynReplyFrame extends DefaultSpdyHeadersFrame implements SpdySynReplyFrame {
  public DefaultSpdySynReplyFrame(int streamId) {
    super(streamId);
  }
  
  public DefaultSpdySynReplyFrame(int streamId, boolean validateHeaders) {
    super(streamId, validateHeaders);
  }
  
  public SpdySynReplyFrame setStreamId(int streamId) {
    super.setStreamId(streamId);
    return this;
  }
  
  public SpdySynReplyFrame setLast(boolean last) {
    super.setLast(last);
    return this;
  }
  
  public SpdySynReplyFrame setInvalid() {
    super.setInvalid();
    return this;
  }
  
  public String toString() {
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append("(last: ").append(isLast()).append(')').append(StringUtil.NEWLINE).append("--> Stream-ID = ").append(streamId()).append(StringUtil.NEWLINE).append("--> Headers:").append(StringUtil.NEWLINE);
    appendHeaders(buf);
    buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    return buf.toString();
  }
}
