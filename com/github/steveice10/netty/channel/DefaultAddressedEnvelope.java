package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.SocketAddress;

public class DefaultAddressedEnvelope<M, A extends SocketAddress> implements AddressedEnvelope<M, A> {
  private final M message;
  
  private final A sender;
  
  private final A recipient;
  
  public DefaultAddressedEnvelope(M message, A recipient, A sender) {
    if (message == null)
      throw new NullPointerException("message"); 
    if (recipient == null && sender == null)
      throw new NullPointerException("recipient and sender"); 
    this.message = message;
    this.sender = sender;
    this.recipient = recipient;
  }
  
  public DefaultAddressedEnvelope(M message, A recipient) {
    this(message, recipient, null);
  }
  
  public M content() {
    return this.message;
  }
  
  public A sender() {
    return this.sender;
  }
  
  public A recipient() {
    return this.recipient;
  }
  
  public int refCnt() {
    if (this.message instanceof ReferenceCounted)
      return ((ReferenceCounted)this.message).refCnt(); 
    return 1;
  }
  
  public AddressedEnvelope<M, A> retain() {
    ReferenceCountUtil.retain(this.message);
    return this;
  }
  
  public AddressedEnvelope<M, A> retain(int increment) {
    ReferenceCountUtil.retain(this.message, increment);
    return this;
  }
  
  public boolean release() {
    return ReferenceCountUtil.release(this.message);
  }
  
  public boolean release(int decrement) {
    return ReferenceCountUtil.release(this.message, decrement);
  }
  
  public AddressedEnvelope<M, A> touch() {
    ReferenceCountUtil.touch(this.message);
    return this;
  }
  
  public AddressedEnvelope<M, A> touch(Object hint) {
    ReferenceCountUtil.touch(this.message, hint);
    return this;
  }
  
  public String toString() {
    if (this.sender != null)
      return StringUtil.simpleClassName(this) + '(' + this.sender + " => " + this.recipient + ", " + this.message + ')'; 
    return StringUtil.simpleClassName(this) + "(=> " + this.recipient + ", " + this.message + ')';
  }
}
