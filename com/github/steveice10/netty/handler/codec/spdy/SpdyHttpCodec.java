package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.channel.ChannelInboundHandler;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.CombinedChannelDuplexHandler;

public final class SpdyHttpCodec extends CombinedChannelDuplexHandler<SpdyHttpDecoder, SpdyHttpEncoder> {
  public SpdyHttpCodec(SpdyVersion version, int maxContentLength) {
    super((ChannelInboundHandler)new SpdyHttpDecoder(version, maxContentLength), (ChannelOutboundHandler)new SpdyHttpEncoder(version));
  }
  
  public SpdyHttpCodec(SpdyVersion version, int maxContentLength, boolean validateHttpHeaders) {
    super((ChannelInboundHandler)new SpdyHttpDecoder(version, maxContentLength, validateHttpHeaders), (ChannelOutboundHandler)new SpdyHttpEncoder(version));
  }
}
