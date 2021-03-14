package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;

public interface Http2DataWriter {
  ChannelFuture writeData(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, ByteBuf paramByteBuf, int paramInt2, boolean paramBoolean, ChannelPromise paramChannelPromise);
}
