package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.memcache.AbstractMemcacheObjectDecoder;
import com.github.steveice10.netty.handler.codec.memcache.DefaultLastMemcacheContent;
import com.github.steveice10.netty.handler.codec.memcache.DefaultMemcacheContent;
import com.github.steveice10.netty.handler.codec.memcache.LastMemcacheContent;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheContent;
import java.util.List;

public abstract class AbstractBinaryMemcacheDecoder<M extends BinaryMemcacheMessage> extends AbstractMemcacheObjectDecoder {
  public static final int DEFAULT_MAX_CHUNK_SIZE = 8192;
  
  private final int chunkSize;
  
  private M currentMessage;
  
  private int alreadyReadChunkSize;
  
  private State state = State.READ_HEADER;
  
  protected AbstractBinaryMemcacheDecoder() {
    this(8192);
  }
  
  protected AbstractBinaryMemcacheDecoder(int chunkSize) {
    if (chunkSize < 0)
      throw new IllegalArgumentException("chunkSize must be a positive integer: " + chunkSize); 
    this.chunkSize = chunkSize;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    switch (this.state) {
      case READ_HEADER:
        try {
          if (in.readableBytes() < 24)
            return; 
          resetDecoder();
          this.currentMessage = decodeHeader(in);
          this.state = State.READ_EXTRAS;
        } catch (Exception e) {
          resetDecoder();
          out.add(invalidMessage(e));
          return;
        } 
      case READ_EXTRAS:
        try {
          byte extrasLength = this.currentMessage.extrasLength();
          if (extrasLength > 0) {
            if (in.readableBytes() < extrasLength)
              return; 
            this.currentMessage.setExtras(in.readRetainedSlice(extrasLength));
          } 
          this.state = State.READ_KEY;
        } catch (Exception e) {
          resetDecoder();
          out.add(invalidMessage(e));
          return;
        } 
      case READ_KEY:
        try {
          short keyLength = this.currentMessage.keyLength();
          if (keyLength > 0) {
            if (in.readableBytes() < keyLength)
              return; 
            this.currentMessage.setKey(in.readRetainedSlice(keyLength));
          } 
          out.add(this.currentMessage.retain());
          this.state = State.READ_CONTENT;
        } catch (Exception e) {
          resetDecoder();
          out.add(invalidMessage(e));
          return;
        } 
      case READ_CONTENT:
        try {
          int valueLength = this.currentMessage.totalBodyLength() - this.currentMessage.keyLength() - this.currentMessage.extrasLength();
          int toRead = in.readableBytes();
          if (valueLength > 0) {
            DefaultMemcacheContent defaultMemcacheContent;
            if (toRead == 0)
              return; 
            if (toRead > this.chunkSize)
              toRead = this.chunkSize; 
            int remainingLength = valueLength - this.alreadyReadChunkSize;
            if (toRead > remainingLength)
              toRead = remainingLength; 
            ByteBuf chunkBuffer = in.readRetainedSlice(toRead);
            if ((this.alreadyReadChunkSize += toRead) >= valueLength) {
              DefaultLastMemcacheContent defaultLastMemcacheContent = new DefaultLastMemcacheContent(chunkBuffer);
            } else {
              defaultMemcacheContent = new DefaultMemcacheContent(chunkBuffer);
            } 
            out.add(defaultMemcacheContent);
            if (this.alreadyReadChunkSize < valueLength)
              return; 
          } else {
            out.add(LastMemcacheContent.EMPTY_LAST_CONTENT);
          } 
          resetDecoder();
          this.state = State.READ_HEADER;
          return;
        } catch (Exception e) {
          resetDecoder();
          out.add(invalidChunk(e));
          return;
        } 
      case BAD_MESSAGE:
        in.skipBytes(actualReadableBytes());
        return;
    } 
    throw new Error("Unknown state reached: " + this.state);
  }
  
  private M invalidMessage(Exception cause) {
    this.state = State.BAD_MESSAGE;
    M message = buildInvalidMessage();
    message.setDecoderResult(DecoderResult.failure(cause));
    return message;
  }
  
  private MemcacheContent invalidChunk(Exception cause) {
    this.state = State.BAD_MESSAGE;
    DefaultLastMemcacheContent defaultLastMemcacheContent = new DefaultLastMemcacheContent(Unpooled.EMPTY_BUFFER);
    defaultLastMemcacheContent.setDecoderResult(DecoderResult.failure(cause));
    return (MemcacheContent)defaultLastMemcacheContent;
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    resetDecoder();
  }
  
  protected void resetDecoder() {
    if (this.currentMessage != null) {
      this.currentMessage.release();
      this.currentMessage = null;
    } 
    this.alreadyReadChunkSize = 0;
  }
  
  protected abstract M decodeHeader(ByteBuf paramByteBuf);
  
  protected abstract M buildInvalidMessage();
  
  enum State {
    READ_HEADER, READ_EXTRAS, READ_KEY, READ_CONTENT, BAD_MESSAGE;
  }
}
