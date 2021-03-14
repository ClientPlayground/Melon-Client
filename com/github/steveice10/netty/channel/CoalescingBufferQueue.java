package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class CoalescingBufferQueue extends AbstractCoalescingBufferQueue {
  private final Channel channel;
  
  public CoalescingBufferQueue(Channel channel) {
    this(channel, 4);
  }
  
  public CoalescingBufferQueue(Channel channel, int initSize) {
    this(channel, initSize, false);
  }
  
  public CoalescingBufferQueue(Channel channel, int initSize, boolean updateWritability) {
    super(updateWritability ? channel : null, initSize);
    this.channel = (Channel)ObjectUtil.checkNotNull(channel, "channel");
  }
  
  public ByteBuf remove(int bytes, ChannelPromise aggregatePromise) {
    return remove(this.channel.alloc(), bytes, aggregatePromise);
  }
  
  public void releaseAndFailAll(Throwable cause) {
    releaseAndFailAll(this.channel, cause);
  }
  
  protected ByteBuf compose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
    if (cumulation instanceof CompositeByteBuf) {
      CompositeByteBuf composite = (CompositeByteBuf)cumulation;
      composite.addComponent(true, next);
      return (ByteBuf)composite;
    } 
    return composeIntoComposite(alloc, cumulation, next);
  }
  
  protected ByteBuf removeEmptyValue() {
    return Unpooled.EMPTY_BUFFER;
  }
}
