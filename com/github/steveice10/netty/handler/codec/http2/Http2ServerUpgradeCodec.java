package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.base64.Base64;
import com.github.steveice10.netty.handler.codec.base64.Base64Dialect;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpServerUpgradeHandler;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Http2ServerUpgradeCodec implements HttpServerUpgradeHandler.UpgradeCodec {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2ServerUpgradeCodec.class);
  
  private static final List<CharSequence> REQUIRED_UPGRADE_HEADERS = Collections.singletonList(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER);
  
  private static final ChannelHandler[] EMPTY_HANDLERS = new ChannelHandler[0];
  
  private final String handlerName;
  
  private final Http2ConnectionHandler connectionHandler;
  
  private final ChannelHandler[] handlers;
  
  private final Http2FrameReader frameReader;
  
  private Http2Settings settings;
  
  public Http2ServerUpgradeCodec(Http2ConnectionHandler connectionHandler) {
    this(null, connectionHandler, EMPTY_HANDLERS);
  }
  
  public Http2ServerUpgradeCodec(Http2MultiplexCodec http2Codec) {
    this(null, http2Codec, EMPTY_HANDLERS);
  }
  
  public Http2ServerUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler) {
    this(handlerName, connectionHandler, EMPTY_HANDLERS);
  }
  
  public Http2ServerUpgradeCodec(String handlerName, Http2MultiplexCodec http2Codec) {
    this(handlerName, http2Codec, EMPTY_HANDLERS);
  }
  
  public Http2ServerUpgradeCodec(Http2FrameCodec http2Codec, ChannelHandler... handlers) {
    this(null, http2Codec, handlers);
  }
  
  private Http2ServerUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler, ChannelHandler... handlers) {
    this.handlerName = handlerName;
    this.connectionHandler = connectionHandler;
    this.handlers = handlers;
    this.frameReader = new DefaultHttp2FrameReader();
  }
  
  public Collection<CharSequence> requiredUpgradeHeaders() {
    return REQUIRED_UPGRADE_HEADERS;
  }
  
  public boolean prepareUpgradeResponse(ChannelHandlerContext ctx, FullHttpRequest upgradeRequest, HttpHeaders headers) {
    try {
      List<String> upgradeHeaders = upgradeRequest.headers().getAll(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER);
      if (upgradeHeaders.isEmpty() || upgradeHeaders.size() > 1)
        throw new IllegalArgumentException("There must be 1 and only 1 " + Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER + " header."); 
      this.settings = decodeSettingsHeader(ctx, upgradeHeaders.get(0));
      return true;
    } catch (Throwable cause) {
      logger.info("Error during upgrade to HTTP/2", cause);
      return false;
    } 
  }
  
  public void upgradeTo(ChannelHandlerContext ctx, FullHttpRequest upgradeRequest) {
    try {
      ctx.pipeline().addAfter(ctx.name(), this.handlerName, (ChannelHandler)this.connectionHandler);
      this.connectionHandler.onHttpServerUpgrade(this.settings);
    } catch (Http2Exception e) {
      ctx.fireExceptionCaught(e);
      ctx.close();
      return;
    } 
    if (this.handlers != null) {
      String name = ctx.pipeline().context((ChannelHandler)this.connectionHandler).name();
      for (int i = this.handlers.length - 1; i >= 0; i--)
        ctx.pipeline().addAfter(name, null, this.handlers[i]); 
    } 
  }
  
  private Http2Settings decodeSettingsHeader(ChannelHandlerContext ctx, CharSequence settingsHeader) throws Http2Exception {
    ByteBuf header = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(settingsHeader), CharsetUtil.UTF_8);
    try {
      ByteBuf payload = Base64.decode(header, Base64Dialect.URL_SAFE);
      ByteBuf frame = createSettingsFrame(ctx, payload);
      return decodeSettings(ctx, frame);
    } finally {
      header.release();
    } 
  }
  
  private Http2Settings decodeSettings(ChannelHandlerContext ctx, ByteBuf frame) throws Http2Exception {
    try {
      final Http2Settings decodedSettings = new Http2Settings();
      this.frameReader.readFrame(ctx, frame, new Http2FrameAdapter() {
            public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
              decodedSettings.copyFrom(settings);
            }
          });
      return decodedSettings;
    } finally {
      frame.release();
    } 
  }
  
  private static ByteBuf createSettingsFrame(ChannelHandlerContext ctx, ByteBuf payload) {
    ByteBuf frame = ctx.alloc().buffer(9 + payload.readableBytes());
    Http2CodecUtil.writeFrameHeader(frame, payload.readableBytes(), (byte)4, new Http2Flags(), 0);
    frame.writeBytes(payload);
    payload.release();
    return frame;
  }
}
