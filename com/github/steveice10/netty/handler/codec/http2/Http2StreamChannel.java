package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.Channel;

public interface Http2StreamChannel extends Channel {
  Http2FrameStream stream();
}
