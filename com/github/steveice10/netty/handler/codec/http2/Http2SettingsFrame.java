package com.github.steveice10.netty.handler.codec.http2;

public interface Http2SettingsFrame extends Http2Frame {
  Http2Settings settings();
  
  String name();
}
