package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface HttpContent extends HttpObject, ByteBufHolder {
  HttpContent copy();
  
  HttpContent duplicate();
  
  HttpContent retainedDuplicate();
  
  HttpContent replace(ByteBuf paramByteBuf);
  
  HttpContent retain();
  
  HttpContent retain(int paramInt);
  
  HttpContent touch();
  
  HttpContent touch(Object paramObject);
}
