package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.stream.ChunkedInput;

public class HttpChunkedInput implements ChunkedInput<HttpContent> {
  private final ChunkedInput<ByteBuf> input;
  
  private final LastHttpContent lastHttpContent;
  
  private boolean sentLastChunk;
  
  public HttpChunkedInput(ChunkedInput<ByteBuf> input) {
    this.input = input;
    this.lastHttpContent = LastHttpContent.EMPTY_LAST_CONTENT;
  }
  
  public HttpChunkedInput(ChunkedInput<ByteBuf> input, LastHttpContent lastHttpContent) {
    this.input = input;
    this.lastHttpContent = lastHttpContent;
  }
  
  public boolean isEndOfInput() throws Exception {
    if (this.input.isEndOfInput())
      return this.sentLastChunk; 
    return false;
  }
  
  public void close() throws Exception {
    this.input.close();
  }
  
  @Deprecated
  public HttpContent readChunk(ChannelHandlerContext ctx) throws Exception {
    return readChunk(ctx.alloc());
  }
  
  public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
    if (this.input.isEndOfInput()) {
      if (this.sentLastChunk)
        return null; 
      this.sentLastChunk = true;
      return this.lastHttpContent;
    } 
    ByteBuf buf = (ByteBuf)this.input.readChunk(allocator);
    if (buf == null)
      return null; 
    return new DefaultHttpContent(buf);
  }
  
  public long length() {
    return this.input.length();
  }
  
  public long progress() {
    return this.input.progress();
  }
}
