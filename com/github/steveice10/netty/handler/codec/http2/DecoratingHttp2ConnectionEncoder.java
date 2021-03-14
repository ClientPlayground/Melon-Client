package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DecoratingHttp2ConnectionEncoder extends DecoratingHttp2FrameWriter implements Http2ConnectionEncoder {
  private final Http2ConnectionEncoder delegate;
  
  public DecoratingHttp2ConnectionEncoder(Http2ConnectionEncoder delegate) {
    super(delegate);
    this.delegate = (Http2ConnectionEncoder)ObjectUtil.checkNotNull(delegate, "delegate");
  }
  
  public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
    this.delegate.lifecycleManager(lifecycleManager);
  }
  
  public Http2Connection connection() {
    return this.delegate.connection();
  }
  
  public Http2RemoteFlowController flowController() {
    return this.delegate.flowController();
  }
  
  public Http2FrameWriter frameWriter() {
    return this.delegate.frameWriter();
  }
  
  public Http2Settings pollSentSettings() {
    return this.delegate.pollSentSettings();
  }
  
  public void remoteSettings(Http2Settings settings) throws Http2Exception {
    this.delegate.remoteSettings(settings);
  }
}
