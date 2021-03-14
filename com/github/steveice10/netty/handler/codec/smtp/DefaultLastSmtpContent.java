package com.github.steveice10.netty.handler.codec.smtp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;

public final class DefaultLastSmtpContent extends DefaultSmtpContent implements LastSmtpContent {
  public DefaultLastSmtpContent(ByteBuf data) {
    super(data);
  }
  
  public LastSmtpContent copy() {
    return (LastSmtpContent)super.copy();
  }
  
  public LastSmtpContent duplicate() {
    return (LastSmtpContent)super.duplicate();
  }
  
  public LastSmtpContent retainedDuplicate() {
    return (LastSmtpContent)super.retainedDuplicate();
  }
  
  public LastSmtpContent replace(ByteBuf content) {
    return new DefaultLastSmtpContent(content);
  }
  
  public DefaultLastSmtpContent retain() {
    super.retain();
    return this;
  }
  
  public DefaultLastSmtpContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public DefaultLastSmtpContent touch() {
    super.touch();
    return this;
  }
  
  public DefaultLastSmtpContent touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
