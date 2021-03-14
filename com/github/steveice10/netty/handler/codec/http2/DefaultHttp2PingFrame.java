package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultHttp2PingFrame implements Http2PingFrame {
  private final long content;
  
  private final boolean ack;
  
  public DefaultHttp2PingFrame(long content) {
    this(content, false);
  }
  
  DefaultHttp2PingFrame(long content, boolean ack) {
    this.content = content;
    this.ack = ack;
  }
  
  public boolean ack() {
    return this.ack;
  }
  
  public String name() {
    return "PING";
  }
  
  public long content() {
    return this.content;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Http2PingFrame))
      return false; 
    Http2PingFrame other = (Http2PingFrame)o;
    return (this.ack == other.ack() && this.content == other.content());
  }
  
  public int hashCode() {
    int hash = super.hashCode();
    hash = hash * 31 + (this.ack ? 1 : 0);
    return hash;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(content=" + this.content + ", ack=" + this.ack + ')';
  }
}
