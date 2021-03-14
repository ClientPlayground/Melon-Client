package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.base64.Base64;
import com.github.steveice10.netty.handler.codec.base64.Base64Dialect;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpClientUpgradeHandler;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.collection.CharObjectMap;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Http2ClientUpgradeCodec implements HttpClientUpgradeHandler.UpgradeCodec {
  private static final List<CharSequence> UPGRADE_HEADERS = Collections.singletonList(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER);
  
  private final String handlerName;
  
  private final Http2ConnectionHandler connectionHandler;
  
  private final ChannelHandler upgradeToHandler;
  
  public Http2ClientUpgradeCodec(Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
    this((String)null, frameCodec, upgradeToHandler);
  }
  
  public Http2ClientUpgradeCodec(String handlerName, Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
    this(handlerName, frameCodec, upgradeToHandler);
  }
  
  public Http2ClientUpgradeCodec(Http2ConnectionHandler connectionHandler) {
    this((String)null, connectionHandler);
  }
  
  public Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler) {
    this(handlerName, connectionHandler, (ChannelHandler)connectionHandler);
  }
  
  private Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler, ChannelHandler upgradeToHandler) {
    this.handlerName = handlerName;
    this.connectionHandler = (Http2ConnectionHandler)ObjectUtil.checkNotNull(connectionHandler, "connectionHandler");
    this.upgradeToHandler = (ChannelHandler)ObjectUtil.checkNotNull(upgradeToHandler, "upgradeToHandler");
  }
  
  public CharSequence protocol() {
    return Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME;
  }
  
  public Collection<CharSequence> setUpgradeHeaders(ChannelHandlerContext ctx, HttpRequest upgradeRequest) {
    CharSequence settingsValue = getSettingsHeaderValue(ctx);
    upgradeRequest.headers().set(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER, settingsValue);
    return UPGRADE_HEADERS;
  }
  
  public void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse) throws Exception {
    ctx.pipeline().addAfter(ctx.name(), this.handlerName, this.upgradeToHandler);
    this.connectionHandler.onHttpClientUpgrade();
  }
  
  private CharSequence getSettingsHeaderValue(ChannelHandlerContext ctx) {
    ByteBuf buf = null;
    ByteBuf encodedBuf = null;
    try {
      Http2Settings settings = this.connectionHandler.decoder().localSettings();
      int payloadLength = 6 * settings.size();
      buf = ctx.alloc().buffer(payloadLength);
      for (CharObjectMap.PrimitiveEntry<Long> entry : (Iterable<CharObjectMap.PrimitiveEntry<Long>>)settings.entries()) {
        buf.writeChar(entry.key());
        buf.writeInt(((Long)entry.value()).intValue());
      } 
      encodedBuf = Base64.encode(buf, Base64Dialect.URL_SAFE);
      return encodedBuf.toString(CharsetUtil.UTF_8);
    } finally {
      ReferenceCountUtil.release(buf);
      ReferenceCountUtil.release(encodedBuf);
    } 
  }
}
