package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class RedisArrayAggregator extends MessageToMessageDecoder<RedisMessage> {
  private final Deque<AggregateState> depths = new ArrayDeque<AggregateState>(4);
  
  protected void decode(ChannelHandlerContext ctx, RedisMessage msg, List<Object> out) throws Exception {
    if (msg instanceof ArrayHeaderRedisMessage) {
      msg = decodeRedisArrayHeader((ArrayHeaderRedisMessage)msg);
      if (msg == null)
        return; 
    } else {
      ReferenceCountUtil.retain(msg);
    } 
    while (!this.depths.isEmpty()) {
      AggregateState current = this.depths.peek();
      current.children.add(msg);
      if (current.children.size() == current.length) {
        msg = new ArrayRedisMessage(current.children);
        this.depths.pop();
        continue;
      } 
      return;
    } 
    out.add(msg);
  }
  
  private RedisMessage decodeRedisArrayHeader(ArrayHeaderRedisMessage header) {
    if (header.isNull())
      return ArrayRedisMessage.NULL_INSTANCE; 
    if (header.length() == 0L)
      return ArrayRedisMessage.EMPTY_INSTANCE; 
    if (header.length() > 0L) {
      if (header.length() > 2147483647L)
        throw new CodecException("this codec doesn't support longer length than 2147483647"); 
      this.depths.push(new AggregateState((int)header.length()));
      return null;
    } 
    throw new CodecException("bad length: " + header.length());
  }
  
  private static final class AggregateState {
    private final int length;
    
    private final List<RedisMessage> children;
    
    AggregateState(int length) {
      this.length = length;
      this.children = new ArrayList<RedisMessage>(length);
    }
  }
}
