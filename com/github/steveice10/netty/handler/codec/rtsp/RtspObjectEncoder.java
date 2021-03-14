package com.github.steveice10.netty.handler.codec.rtsp;

import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpObjectEncoder;

@Sharable
@Deprecated
public abstract class RtspObjectEncoder<H extends HttpMessage> extends HttpObjectEncoder<H> {
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return msg instanceof com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
  }
}
