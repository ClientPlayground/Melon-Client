package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.channel.ChannelInboundHandler;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.CombinedChannelDuplexHandler;

public class BinaryMemcacheServerCodec extends CombinedChannelDuplexHandler<BinaryMemcacheRequestDecoder, BinaryMemcacheResponseEncoder> {
  public BinaryMemcacheServerCodec() {
    this(8192);
  }
  
  public BinaryMemcacheServerCodec(int decodeChunkSize) {
    super((ChannelInboundHandler)new BinaryMemcacheRequestDecoder(decodeChunkSize), (ChannelOutboundHandler)new BinaryMemcacheResponseEncoder());
  }
}
