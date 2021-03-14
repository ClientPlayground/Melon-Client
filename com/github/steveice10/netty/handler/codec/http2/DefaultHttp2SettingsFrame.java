package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultHttp2SettingsFrame implements Http2SettingsFrame {
  private final Http2Settings settings;
  
  public DefaultHttp2SettingsFrame(Http2Settings settings) {
    this.settings = (Http2Settings)ObjectUtil.checkNotNull(settings, "settings");
  }
  
  public Http2Settings settings() {
    return this.settings;
  }
  
  public String name() {
    return "SETTINGS";
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(settings=" + this.settings + ')';
  }
}
