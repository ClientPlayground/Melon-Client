package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

interface PemEncoded extends ByteBufHolder {
  boolean isSensitive();
  
  PemEncoded copy();
  
  PemEncoded duplicate();
  
  PemEncoded retainedDuplicate();
  
  PemEncoded replace(ByteBuf paramByteBuf);
  
  PemEncoded retain();
  
  PemEncoded retain(int paramInt);
  
  PemEncoded touch();
  
  PemEncoded touch(Object paramObject);
}
