package com.github.steveice10.netty.handler.codec.smtp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;

public interface LastSmtpContent extends SmtpContent {
  public static final LastSmtpContent EMPTY_LAST_CONTENT = new LastSmtpContent() {
      public LastSmtpContent copy() {
        return this;
      }
      
      public LastSmtpContent duplicate() {
        return this;
      }
      
      public LastSmtpContent retainedDuplicate() {
        return this;
      }
      
      public LastSmtpContent replace(ByteBuf content) {
        return new DefaultLastSmtpContent(content);
      }
      
      public LastSmtpContent retain() {
        return this;
      }
      
      public LastSmtpContent retain(int increment) {
        return this;
      }
      
      public LastSmtpContent touch() {
        return this;
      }
      
      public LastSmtpContent touch(Object hint) {
        return this;
      }
      
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public int refCnt() {
        return 1;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
    };
  
  LastSmtpContent copy();
  
  LastSmtpContent duplicate();
  
  LastSmtpContent retainedDuplicate();
  
  LastSmtpContent replace(ByteBuf paramByteBuf);
  
  LastSmtpContent retain();
  
  LastSmtpContent retain(int paramInt);
  
  LastSmtpContent touch();
  
  LastSmtpContent touch(Object paramObject);
}
