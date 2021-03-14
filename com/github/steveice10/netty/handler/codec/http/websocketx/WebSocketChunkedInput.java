package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.stream.ChunkedInput;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class WebSocketChunkedInput implements ChunkedInput<WebSocketFrame> {
  private final ChunkedInput<ByteBuf> input;
  
  private final int rsv;
  
  public WebSocketChunkedInput(ChunkedInput<ByteBuf> input) {
    this(input, 0);
  }
  
  public WebSocketChunkedInput(ChunkedInput<ByteBuf> input, int rsv) {
    this.input = (ChunkedInput<ByteBuf>)ObjectUtil.checkNotNull(input, "input");
    this.rsv = rsv;
  }
  
  public boolean isEndOfInput() throws Exception {
    return this.input.isEndOfInput();
  }
  
  public void close() throws Exception {
    this.input.close();
  }
  
  @Deprecated
  public WebSocketFrame readChunk(ChannelHandlerContext ctx) throws Exception {
    return readChunk(ctx.alloc());
  }
  
  public WebSocketFrame readChunk(ByteBufAllocator allocator) throws Exception {
    ByteBuf buf = (ByteBuf)this.input.readChunk(allocator);
    if (buf == null)
      return null; 
    return new ContinuationWebSocketFrame(this.input.isEndOfInput(), this.rsv, buf);
  }
  
  public long length() {
    return this.input.length();
  }
  
  public long progress() {
    return this.input.progress();
  }
}
