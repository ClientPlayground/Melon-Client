package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.MessageAggregator;

public abstract class AbstractMemcacheObjectAggregator<H extends MemcacheMessage> extends MessageAggregator<MemcacheObject, H, MemcacheContent, FullMemcacheMessage> {
  protected AbstractMemcacheObjectAggregator(int maxContentLength) {
    super(maxContentLength);
  }
  
  protected boolean isContentMessage(MemcacheObject msg) throws Exception {
    return msg instanceof MemcacheContent;
  }
  
  protected boolean isLastContentMessage(MemcacheContent msg) throws Exception {
    return msg instanceof LastMemcacheContent;
  }
  
  protected boolean isAggregated(MemcacheObject msg) throws Exception {
    return msg instanceof FullMemcacheMessage;
  }
  
  protected boolean isContentLengthInvalid(H start, int maxContentLength) {
    return false;
  }
  
  protected Object newContinueResponse(H start, int maxContentLength, ChannelPipeline pipeline) {
    return null;
  }
  
  protected boolean closeAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean ignoreContentAfterContinueResponse(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
}
