package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import java.net.URI;
import java.util.List;

public class WebSocketClientProtocolHandler extends WebSocketProtocolHandler {
  private final WebSocketClientHandshaker handshaker;
  
  private final boolean handleCloseFrames;
  
  public WebSocketClientHandshaker handshaker() {
    return this.handshaker;
  }
  
  public enum ClientHandshakeStateEvent {
    HANDSHAKE_ISSUED, HANDSHAKE_COMPLETE;
  }
  
  public WebSocketClientProtocolHandler(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength, boolean handleCloseFrames, boolean performMasking, boolean allowMaskMismatch) {
    this(WebSocketClientHandshakerFactory.newHandshaker(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, performMasking, allowMaskMismatch), handleCloseFrames);
  }
  
  public WebSocketClientProtocolHandler(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength, boolean handleCloseFrames) {
    this(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, handleCloseFrames, true, false);
  }
  
  public WebSocketClientProtocolHandler(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength) {
    this(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, true);
  }
  
  public WebSocketClientProtocolHandler(WebSocketClientHandshaker handshaker, boolean handleCloseFrames) {
    this.handshaker = handshaker;
    this.handleCloseFrames = handleCloseFrames;
  }
  
  public WebSocketClientProtocolHandler(WebSocketClientHandshaker handshaker) {
    this(handshaker, true);
  }
  
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
    if (this.handleCloseFrames && frame instanceof CloseWebSocketFrame) {
      ctx.close();
      return;
    } 
    super.decode(ctx, frame, out);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) {
    ChannelPipeline cp = ctx.pipeline();
    if (cp.get(WebSocketClientProtocolHandshakeHandler.class) == null)
      ctx.pipeline().addBefore(ctx.name(), WebSocketClientProtocolHandshakeHandler.class.getName(), (ChannelHandler)new WebSocketClientProtocolHandshakeHandler(this.handshaker)); 
    if (cp.get(Utf8FrameValidator.class) == null)
      ctx.pipeline().addBefore(ctx.name(), Utf8FrameValidator.class.getName(), (ChannelHandler)new Utf8FrameValidator()); 
  }
}
