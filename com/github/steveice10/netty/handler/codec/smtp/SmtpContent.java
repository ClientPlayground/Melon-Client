package com.github.steveice10.netty.handler.codec.smtp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface SmtpContent extends ByteBufHolder {
  SmtpContent copy();
  
  SmtpContent duplicate();
  
  SmtpContent retainedDuplicate();
  
  SmtpContent replace(ByteBuf paramByteBuf);
  
  SmtpContent retain();
  
  SmtpContent retain(int paramInt);
  
  SmtpContent touch();
  
  SmtpContent touch(Object paramObject);
}
