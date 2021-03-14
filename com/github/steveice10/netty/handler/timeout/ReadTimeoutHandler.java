package com.github.steveice10.netty.handler.timeout;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.util.concurrent.TimeUnit;

public class ReadTimeoutHandler extends IdleStateHandler {
  private boolean closed;
  
  public ReadTimeoutHandler(int timeoutSeconds) {
    this(timeoutSeconds, TimeUnit.SECONDS);
  }
  
  public ReadTimeoutHandler(long timeout, TimeUnit unit) {
    super(timeout, 0L, 0L, unit);
  }
  
  protected final void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
    assert evt.state() == IdleState.READER_IDLE;
    readTimedOut(ctx);
  }
  
  protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
    if (!this.closed) {
      ctx.fireExceptionCaught((Throwable)ReadTimeoutException.INSTANCE);
      ctx.close();
      this.closed = true;
    } 
  }
}
