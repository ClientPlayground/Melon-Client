package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;

public abstract class ZlibEncoder extends MessageToByteEncoder<ByteBuf> {
  protected ZlibEncoder() {
    super(false);
  }
  
  public abstract boolean isClosed();
  
  public abstract ChannelFuture close();
  
  public abstract ChannelFuture close(ChannelPromise paramChannelPromise);
}
