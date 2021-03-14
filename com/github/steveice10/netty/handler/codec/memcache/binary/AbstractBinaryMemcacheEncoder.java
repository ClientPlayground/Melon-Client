package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.memcache.AbstractMemcacheObjectEncoder;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;

public abstract class AbstractBinaryMemcacheEncoder<M extends BinaryMemcacheMessage> extends AbstractMemcacheObjectEncoder<M> {
  private static final int MINIMUM_HEADER_SIZE = 24;
  
  protected ByteBuf encodeMessage(ChannelHandlerContext ctx, M msg) {
    ByteBuf buf = ctx.alloc().buffer(24 + msg.extrasLength() + msg
        .keyLength());
    encodeHeader(buf, msg);
    encodeExtras(buf, msg.extras());
    encodeKey(buf, msg.key());
    return buf;
  }
  
  private static void encodeExtras(ByteBuf buf, ByteBuf extras) {
    if (extras == null || !extras.isReadable())
      return; 
    buf.writeBytes(extras);
  }
  
  private static void encodeKey(ByteBuf buf, ByteBuf key) {
    if (key == null || !key.isReadable())
      return; 
    buf.writeBytes(key);
  }
  
  protected abstract void encodeHeader(ByteBuf paramByteBuf, M paramM);
}
