package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.URI;

public class WebSocketClientHandshaker13 extends WebSocketClientHandshaker {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketClientHandshaker13.class);
  
  public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  
  private String expectedChallengeResponseString;
  
  private final boolean allowExtensions;
  
  private final boolean performMasking;
  
  private final boolean allowMaskMismatch;
  
  public WebSocketClientHandshaker13(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength) {
    this(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, true, false);
  }
  
  public WebSocketClientHandshaker13(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength, boolean performMasking, boolean allowMaskMismatch) {
    super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength);
    this.allowExtensions = allowExtensions;
    this.performMasking = performMasking;
    this.allowMaskMismatch = allowMaskMismatch;
  }
  
  protected FullHttpRequest newHandshakeRequest() {
    URI wsURL = uri();
    String path = rawPath(wsURL);
    byte[] nonce = WebSocketUtil.randomBytes(16);
    String key = WebSocketUtil.base64(nonce);
    String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
    this.expectedChallengeResponseString = WebSocketUtil.base64(sha1);
    if (logger.isDebugEnabled())
      logger.debug("WebSocket version 13 client handshake key: {}, expected response: {}", key, this.expectedChallengeResponseString); 
    DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
    HttpHeaders headers = defaultFullHttpRequest.headers();
    headers.add((CharSequence)HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET)
      .add((CharSequence)HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
      .add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY, key)
      .add((CharSequence)HttpHeaderNames.HOST, websocketHostValue(wsURL))
      .add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_ORIGIN, websocketOriginValue(wsURL));
    String expectedSubprotocol = expectedSubprotocol();
    if (expectedSubprotocol != null && !expectedSubprotocol.isEmpty())
      headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, expectedSubprotocol); 
    headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_VERSION, "13");
    if (this.customHeaders != null)
      headers.add(this.customHeaders); 
    return (FullHttpRequest)defaultFullHttpRequest;
  }
  
  protected void verify(FullHttpResponse response) {
    HttpResponseStatus status = HttpResponseStatus.SWITCHING_PROTOCOLS;
    HttpHeaders headers = response.headers();
    if (!response.status().equals(status))
      throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.status()); 
    CharSequence upgrade = headers.get((CharSequence)HttpHeaderNames.UPGRADE);
    if (!HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(upgrade))
      throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + upgrade); 
    if (!headers.containsValue((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.UPGRADE, true))
      throw new WebSocketHandshakeException("Invalid handshake response connection: " + headers
          .get(HttpHeaderNames.CONNECTION)); 
    CharSequence accept = headers.get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_ACCEPT);
    if (accept == null || !accept.equals(this.expectedChallengeResponseString))
      throw new WebSocketHandshakeException(String.format("Invalid challenge. Actual: %s. Expected: %s", new Object[] { accept, this.expectedChallengeResponseString })); 
  }
  
  protected WebSocketFrameDecoder newWebsocketDecoder() {
    return new WebSocket13FrameDecoder(false, this.allowExtensions, maxFramePayloadLength(), this.allowMaskMismatch);
  }
  
  protected WebSocketFrameEncoder newWebSocketEncoder() {
    return new WebSocket13FrameEncoder(this.performMasking);
  }
}
