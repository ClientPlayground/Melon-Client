package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.MessageAggregator;

public class StompSubframeAggregator extends MessageAggregator<StompSubframe, StompHeadersSubframe, StompContentSubframe, StompFrame> {
  public StompSubframeAggregator(int maxContentLength) {
    super(maxContentLength);
  }
  
  protected boolean isStartMessage(StompSubframe msg) throws Exception {
    return msg instanceof StompHeadersSubframe;
  }
  
  protected boolean isContentMessage(StompSubframe msg) throws Exception {
    return msg instanceof StompContentSubframe;
  }
  
  protected boolean isLastContentMessage(StompContentSubframe msg) throws Exception {
    return msg instanceof LastStompContentSubframe;
  }
  
  protected boolean isAggregated(StompSubframe msg) throws Exception {
    return msg instanceof StompFrame;
  }
  
  protected boolean isContentLengthInvalid(StompHeadersSubframe start, int maxContentLength) {
    return ((int)Math.min(2147483647L, start.headers().getLong(StompHeaders.CONTENT_LENGTH, -1L)) > maxContentLength);
  }
  
  protected Object newContinueResponse(StompHeadersSubframe start, int maxContentLength, ChannelPipeline pipeline) {
    return null;
  }
  
  protected boolean closeAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean ignoreContentAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected StompFrame beginAggregation(StompHeadersSubframe start, ByteBuf content) throws Exception {
    StompFrame ret = new DefaultStompFrame(start.command(), content);
    ret.headers().set(start.headers());
    return ret;
  }
}
