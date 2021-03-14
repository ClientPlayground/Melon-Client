package com.github.steveice10.netty.handler.codec.http.websocketx.extensions;

import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WebSocketClientExtensionHandler extends ChannelDuplexHandler {
  private final List<WebSocketClientExtensionHandshaker> extensionHandshakers;
  
  public WebSocketClientExtensionHandler(WebSocketClientExtensionHandshaker... extensionHandshakers) {
    if (extensionHandshakers == null)
      throw new NullPointerException("extensionHandshakers"); 
    if (extensionHandshakers.length == 0)
      throw new IllegalArgumentException("extensionHandshakers must contains at least one handshaker"); 
    this.extensionHandshakers = Arrays.asList(extensionHandshakers);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof HttpRequest && WebSocketExtensionUtil.isWebsocketUpgrade(((HttpRequest)msg).headers())) {
      HttpRequest request = (HttpRequest)msg;
      String headerValue = request.headers().getAsString((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
      for (WebSocketClientExtensionHandshaker extensionHandshaker : this.extensionHandshakers) {
        WebSocketExtensionData extensionData = extensionHandshaker.newRequestData();
        headerValue = WebSocketExtensionUtil.appendExtension(headerValue, extensionData
            .name(), extensionData.parameters());
      } 
      request.headers().set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS, headerValue);
    } 
    super.write(ctx, msg, promise);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpResponse) {
      HttpResponse response = (HttpResponse)msg;
      if (WebSocketExtensionUtil.isWebsocketUpgrade(response.headers())) {
        String extensionsHeader = response.headers().getAsString((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
        if (extensionsHeader != null) {
          List<WebSocketExtensionData> extensions = WebSocketExtensionUtil.extractExtensions(extensionsHeader);
          List<WebSocketClientExtension> validExtensions = new ArrayList<WebSocketClientExtension>(extensions.size());
          int rsv = 0;
          for (WebSocketExtensionData extensionData : extensions) {
            Iterator<WebSocketClientExtensionHandshaker> extensionHandshakersIterator = this.extensionHandshakers.iterator();
            WebSocketClientExtension validExtension = null;
            while (validExtension == null && extensionHandshakersIterator.hasNext()) {
              WebSocketClientExtensionHandshaker extensionHandshaker = extensionHandshakersIterator.next();
              validExtension = extensionHandshaker.handshakeExtension(extensionData);
            } 
            if (validExtension != null && (validExtension.rsv() & rsv) == 0) {
              rsv |= validExtension.rsv();
              validExtensions.add(validExtension);
              continue;
            } 
            throw new CodecException("invalid WebSocket Extension handshake for \"" + extensionsHeader + '"');
          } 
          for (WebSocketClientExtension validExtension : validExtensions) {
            WebSocketExtensionDecoder decoder = validExtension.newExtensionDecoder();
            WebSocketExtensionEncoder encoder = validExtension.newExtensionEncoder();
            ctx.pipeline().addAfter(ctx.name(), decoder.getClass().getName(), (ChannelHandler)decoder);
            ctx.pipeline().addAfter(ctx.name(), encoder.getClass().getName(), (ChannelHandler)encoder);
          } 
        } 
        ctx.pipeline().remove(ctx.name());
      } 
    } 
    super.channelRead(ctx, msg);
  }
}
