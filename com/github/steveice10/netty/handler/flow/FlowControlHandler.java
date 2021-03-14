package com.github.steveice10.netty.handler.flow;

import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;

public class FlowControlHandler extends ChannelDuplexHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(FlowControlHandler.class);
  
  private final boolean releaseMessages;
  
  private RecyclableArrayDeque queue;
  
  private ChannelConfig config;
  
  private boolean shouldConsume;
  
  public FlowControlHandler() {
    this(true);
  }
  
  public FlowControlHandler(boolean releaseMessages) {
    this.releaseMessages = releaseMessages;
  }
  
  boolean isQueueEmpty() {
    return this.queue.isEmpty();
  }
  
  private void destroy() {
    if (this.queue != null) {
      if (!this.queue.isEmpty()) {
        logger.trace("Non-empty queue: {}", this.queue);
        if (this.releaseMessages) {
          Object msg;
          while ((msg = this.queue.poll()) != null)
            ReferenceCountUtil.safeRelease(msg); 
        } 
      } 
      this.queue.recycle();
      this.queue = null;
    } 
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.config = ctx.channel().config();
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    destroy();
    ctx.fireChannelInactive();
  }
  
  public void read(ChannelHandlerContext ctx) throws Exception {
    if (dequeue(ctx, 1) == 0) {
      this.shouldConsume = true;
      ctx.read();
    } 
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (this.queue == null)
      this.queue = RecyclableArrayDeque.newInstance(); 
    this.queue.offer(msg);
    int minConsume = this.shouldConsume ? 1 : 0;
    this.shouldConsume = false;
    dequeue(ctx, minConsume);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {}
  
  private int dequeue(ChannelHandlerContext ctx, int minConsume) {
    if (this.queue != null) {
      int consumed = 0;
      while (consumed < minConsume || this.config.isAutoRead()) {
        Object msg = this.queue.poll();
        if (msg == null)
          break; 
        consumed++;
        ctx.fireChannelRead(msg);
      } 
      if (this.queue.isEmpty() && consumed > 0)
        ctx.fireChannelReadComplete(); 
      return consumed;
    } 
    return 0;
  }
  
  private static final class RecyclableArrayDeque extends ArrayDeque<Object> {
    private static final long serialVersionUID = 0L;
    
    private static final int DEFAULT_NUM_ELEMENTS = 2;
    
    private static final Recycler<RecyclableArrayDeque> RECYCLER = new Recycler<RecyclableArrayDeque>() {
        protected FlowControlHandler.RecyclableArrayDeque newObject(Recycler.Handle<FlowControlHandler.RecyclableArrayDeque> handle) {
          return new FlowControlHandler.RecyclableArrayDeque(2, handle);
        }
      };
    
    private final Recycler.Handle<RecyclableArrayDeque> handle;
    
    public static RecyclableArrayDeque newInstance() {
      return (RecyclableArrayDeque)RECYCLER.get();
    }
    
    private RecyclableArrayDeque(int numElements, Recycler.Handle<RecyclableArrayDeque> handle) {
      super(numElements);
      this.handle = handle;
    }
    
    public void recycle() {
      clear();
      this.handle.recycle(this);
    }
  }
}
