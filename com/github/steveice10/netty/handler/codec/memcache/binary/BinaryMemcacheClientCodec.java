package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandler;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.CombinedChannelDuplexHandler;
import com.github.steveice10.netty.handler.codec.PrematureChannelClosureException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class BinaryMemcacheClientCodec extends CombinedChannelDuplexHandler<BinaryMemcacheResponseDecoder, BinaryMemcacheRequestEncoder> {
  private final boolean failOnMissingResponse;
  
  private final AtomicLong requestResponseCounter = new AtomicLong();
  
  public BinaryMemcacheClientCodec() {
    this(8192);
  }
  
  public BinaryMemcacheClientCodec(int decodeChunkSize) {
    this(decodeChunkSize, false);
  }
  
  public BinaryMemcacheClientCodec(int decodeChunkSize, boolean failOnMissingResponse) {
    this.failOnMissingResponse = failOnMissingResponse;
    init((ChannelInboundHandler)new Decoder(decodeChunkSize), (ChannelOutboundHandler)new Encoder());
  }
  
  private final class Encoder extends BinaryMemcacheRequestEncoder {
    private Encoder() {}
    
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
      super.encode(ctx, msg, out);
      if (BinaryMemcacheClientCodec.this.failOnMissingResponse && msg instanceof com.github.steveice10.netty.handler.codec.memcache.LastMemcacheContent)
        BinaryMemcacheClientCodec.this.requestResponseCounter.incrementAndGet(); 
    }
  }
  
  private final class Decoder extends BinaryMemcacheResponseDecoder {
    Decoder(int chunkSize) {
      super(chunkSize);
    }
    
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      int oldSize = out.size();
      super.decode(ctx, in, out);
      if (BinaryMemcacheClientCodec.this.failOnMissingResponse) {
        int size = out.size();
        for (int i = oldSize; i < size; i++) {
          Object msg = out.get(i);
          if (msg instanceof com.github.steveice10.netty.handler.codec.memcache.LastMemcacheContent)
            BinaryMemcacheClientCodec.this.requestResponseCounter.decrementAndGet(); 
        } 
      } 
    }
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      if (BinaryMemcacheClientCodec.this.failOnMissingResponse) {
        long missingResponses = BinaryMemcacheClientCodec.this.requestResponseCounter.get();
        if (missingResponses > 0L)
          ctx.fireExceptionCaught((Throwable)new PrematureChannelClosureException("channel gone inactive with " + missingResponses + " missing response(s)")); 
      } 
    }
  }
}
