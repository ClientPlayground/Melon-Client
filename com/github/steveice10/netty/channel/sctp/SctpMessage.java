package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.sun.nio.sctp.MessageInfo;

public final class SctpMessage extends DefaultByteBufHolder {
  private final int streamIdentifier;
  
  private final int protocolIdentifier;
  
  private final boolean unordered;
  
  private final MessageInfo msgInfo;
  
  public SctpMessage(int protocolIdentifier, int streamIdentifier, ByteBuf payloadBuffer) {
    this(protocolIdentifier, streamIdentifier, false, payloadBuffer);
  }
  
  public SctpMessage(int protocolIdentifier, int streamIdentifier, boolean unordered, ByteBuf payloadBuffer) {
    super(payloadBuffer);
    this.protocolIdentifier = protocolIdentifier;
    this.streamIdentifier = streamIdentifier;
    this.unordered = unordered;
    this.msgInfo = null;
  }
  
  public SctpMessage(MessageInfo msgInfo, ByteBuf payloadBuffer) {
    super(payloadBuffer);
    if (msgInfo == null)
      throw new NullPointerException("msgInfo"); 
    this.msgInfo = msgInfo;
    this.streamIdentifier = msgInfo.streamNumber();
    this.protocolIdentifier = msgInfo.payloadProtocolID();
    this.unordered = msgInfo.isUnordered();
  }
  
  public int streamIdentifier() {
    return this.streamIdentifier;
  }
  
  public int protocolIdentifier() {
    return this.protocolIdentifier;
  }
  
  public boolean isUnordered() {
    return this.unordered;
  }
  
  public MessageInfo messageInfo() {
    return this.msgInfo;
  }
  
  public boolean isComplete() {
    if (this.msgInfo != null)
      return this.msgInfo.isComplete(); 
    return true;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    SctpMessage sctpFrame = (SctpMessage)o;
    if (this.protocolIdentifier != sctpFrame.protocolIdentifier)
      return false; 
    if (this.streamIdentifier != sctpFrame.streamIdentifier)
      return false; 
    if (this.unordered != sctpFrame.unordered)
      return false; 
    return content().equals(sctpFrame.content());
  }
  
  public int hashCode() {
    int result = this.streamIdentifier;
    result = 31 * result + this.protocolIdentifier;
    result = 31 * result + (this.unordered ? 1231 : 1237);
    result = 31 * result + content().hashCode();
    return result;
  }
  
  public SctpMessage copy() {
    return (SctpMessage)super.copy();
  }
  
  public SctpMessage duplicate() {
    return (SctpMessage)super.duplicate();
  }
  
  public SctpMessage retainedDuplicate() {
    return (SctpMessage)super.retainedDuplicate();
  }
  
  public SctpMessage replace(ByteBuf content) {
    if (this.msgInfo == null)
      return new SctpMessage(this.protocolIdentifier, this.streamIdentifier, this.unordered, content); 
    return new SctpMessage(this.msgInfo, content);
  }
  
  public SctpMessage retain() {
    super.retain();
    return this;
  }
  
  public SctpMessage retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public SctpMessage touch() {
    super.touch();
    return this;
  }
  
  public SctpMessage touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public String toString() {
    return "SctpFrame{streamIdentifier=" + this.streamIdentifier + ", protocolIdentifier=" + this.protocolIdentifier + ", unordered=" + this.unordered + ", data=" + 
      
      contentToString() + '}';
  }
}
