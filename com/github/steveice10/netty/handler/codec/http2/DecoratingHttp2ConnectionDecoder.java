package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

public class DecoratingHttp2ConnectionDecoder implements Http2ConnectionDecoder {
  private final Http2ConnectionDecoder delegate;
  
  public DecoratingHttp2ConnectionDecoder(Http2ConnectionDecoder delegate) {
    this.delegate = (Http2ConnectionDecoder)ObjectUtil.checkNotNull(delegate, "delegate");
  }
  
  public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
    this.delegate.lifecycleManager(lifecycleManager);
  }
  
  public Http2Connection connection() {
    return this.delegate.connection();
  }
  
  public Http2LocalFlowController flowController() {
    return this.delegate.flowController();
  }
  
  public void frameListener(Http2FrameListener listener) {
    this.delegate.frameListener(listener);
  }
  
  public Http2FrameListener frameListener() {
    return this.delegate.frameListener();
  }
  
  public void decodeFrame(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Http2Exception {
    this.delegate.decodeFrame(ctx, in, out);
  }
  
  public Http2Settings localSettings() {
    return this.delegate.localSettings();
  }
  
  public boolean prefaceReceived() {
    return this.delegate.prefaceReceived();
  }
  
  public void close() {
    this.delegate.close();
  }
}
