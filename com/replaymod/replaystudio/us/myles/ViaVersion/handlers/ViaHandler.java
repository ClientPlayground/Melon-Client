package com.replaymod.replaystudio.us.myles.ViaVersion.handlers;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;

public interface ViaHandler {
  void transform(ByteBuf paramByteBuf) throws Exception;
  
  void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable) throws Exception;
}
