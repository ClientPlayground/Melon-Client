package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.MessageAggregator;

public class WebSocketFrameAggregator extends MessageAggregator<WebSocketFrame, WebSocketFrame, ContinuationWebSocketFrame, WebSocketFrame> {
  public WebSocketFrameAggregator(int maxContentLength) {
    super(maxContentLength);
  }
  
  protected boolean isStartMessage(WebSocketFrame msg) throws Exception {
    return (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame);
  }
  
  protected boolean isContentMessage(WebSocketFrame msg) throws Exception {
    return msg instanceof ContinuationWebSocketFrame;
  }
  
  protected boolean isLastContentMessage(ContinuationWebSocketFrame msg) throws Exception {
    return (isContentMessage(msg) && msg.isFinalFragment());
  }
  
  protected boolean isAggregated(WebSocketFrame msg) throws Exception {
    if (msg.isFinalFragment())
      return !isContentMessage(msg); 
    return (!isStartMessage(msg) && !isContentMessage(msg));
  }
  
  protected boolean isContentLengthInvalid(WebSocketFrame start, int maxContentLength) {
    return false;
  }
  
  protected Object newContinueResponse(WebSocketFrame start, int maxContentLength, ChannelPipeline pipeline) {
    return null;
  }
  
  protected boolean closeAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean ignoreContentAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected WebSocketFrame beginAggregation(WebSocketFrame start, ByteBuf content) throws Exception {
    if (start instanceof TextWebSocketFrame)
      return new TextWebSocketFrame(true, start.rsv(), content); 
    if (start instanceof BinaryWebSocketFrame)
      return new BinaryWebSocketFrame(true, start.rsv(), content); 
    throw new Error();
  }
}
