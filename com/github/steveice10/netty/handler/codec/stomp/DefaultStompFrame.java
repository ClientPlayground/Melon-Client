package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultStompFrame extends DefaultStompHeadersSubframe implements StompFrame {
  private final ByteBuf content;
  
  public DefaultStompFrame(StompCommand command) {
    this(command, Unpooled.buffer(0));
  }
  
  public DefaultStompFrame(StompCommand command, ByteBuf content) {
    this(command, content, (DefaultStompHeaders)null);
  }
  
  DefaultStompFrame(StompCommand command, ByteBuf content, DefaultStompHeaders headers) {
    super(command, headers);
    if (content == null)
      throw new NullPointerException("content"); 
    this.content = content;
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public StompFrame copy() {
    return replace(this.content.copy());
  }
  
  public StompFrame duplicate() {
    return replace(this.content.duplicate());
  }
  
  public StompFrame retainedDuplicate() {
    return replace(this.content.retainedDuplicate());
  }
  
  public StompFrame replace(ByteBuf content) {
    return new DefaultStompFrame(this.command, content, this.headers.copy());
  }
  
  public int refCnt() {
    return this.content.refCnt();
  }
  
  public StompFrame retain() {
    this.content.retain();
    return this;
  }
  
  public StompFrame retain(int increment) {
    this.content.retain(increment);
    return this;
  }
  
  public StompFrame touch() {
    this.content.touch();
    return this;
  }
  
  public StompFrame touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public boolean release() {
    return this.content.release();
  }
  
  public boolean release(int decrement) {
    return this.content.release(decrement);
  }
  
  public String toString() {
    return "DefaultStompFrame{command=" + this.command + ", headers=" + this.headers + ", content=" + this.content
      
      .toString(CharsetUtil.UTF_8) + '}';
  }
}
