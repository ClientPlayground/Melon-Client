package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

public class RedisEncoder extends MessageToMessageEncoder<RedisMessage> {
  private final RedisMessagePool messagePool;
  
  public RedisEncoder() {
    this(FixedRedisMessagePool.INSTANCE);
  }
  
  public RedisEncoder(RedisMessagePool messagePool) {
    this.messagePool = (RedisMessagePool)ObjectUtil.checkNotNull(messagePool, "messagePool");
  }
  
  protected void encode(ChannelHandlerContext ctx, RedisMessage msg, List<Object> out) throws Exception {
    try {
      writeRedisMessage(ctx.alloc(), msg, out);
    } catch (CodecException e) {
      throw e;
    } catch (Exception e) {
      throw new CodecException(e);
    } 
  }
  
  private void writeRedisMessage(ByteBufAllocator allocator, RedisMessage msg, List<Object> out) {
    if (msg instanceof InlineCommandRedisMessage) {
      writeInlineCommandMessage(allocator, (InlineCommandRedisMessage)msg, out);
    } else if (msg instanceof SimpleStringRedisMessage) {
      writeSimpleStringMessage(allocator, (SimpleStringRedisMessage)msg, out);
    } else if (msg instanceof ErrorRedisMessage) {
      writeErrorMessage(allocator, (ErrorRedisMessage)msg, out);
    } else if (msg instanceof IntegerRedisMessage) {
      writeIntegerMessage(allocator, (IntegerRedisMessage)msg, out);
    } else if (msg instanceof FullBulkStringRedisMessage) {
      writeFullBulkStringMessage(allocator, (FullBulkStringRedisMessage)msg, out);
    } else if (msg instanceof BulkStringRedisContent) {
      writeBulkStringContent(allocator, (BulkStringRedisContent)msg, out);
    } else if (msg instanceof BulkStringHeaderRedisMessage) {
      writeBulkStringHeader(allocator, (BulkStringHeaderRedisMessage)msg, out);
    } else if (msg instanceof ArrayHeaderRedisMessage) {
      writeArrayHeader(allocator, (ArrayHeaderRedisMessage)msg, out);
    } else if (msg instanceof ArrayRedisMessage) {
      writeArrayMessage(allocator, (ArrayRedisMessage)msg, out);
    } else {
      throw new CodecException("unknown message type: " + msg);
    } 
  }
  
  private static void writeInlineCommandMessage(ByteBufAllocator allocator, InlineCommandRedisMessage msg, List<Object> out) {
    writeString(allocator, RedisMessageType.INLINE_COMMAND, msg.content(), out);
  }
  
  private static void writeSimpleStringMessage(ByteBufAllocator allocator, SimpleStringRedisMessage msg, List<Object> out) {
    writeString(allocator, RedisMessageType.SIMPLE_STRING, msg.content(), out);
  }
  
  private static void writeErrorMessage(ByteBufAllocator allocator, ErrorRedisMessage msg, List<Object> out) {
    writeString(allocator, RedisMessageType.ERROR, msg.content(), out);
  }
  
  private static void writeString(ByteBufAllocator allocator, RedisMessageType type, String content, List<Object> out) {
    ByteBuf buf = allocator.ioBuffer(type.length() + ByteBufUtil.utf8MaxBytes(content) + 2);
    type.writeTo(buf);
    ByteBufUtil.writeUtf8(buf, content);
    buf.writeShort(RedisConstants.EOL_SHORT);
    out.add(buf);
  }
  
  private void writeIntegerMessage(ByteBufAllocator allocator, IntegerRedisMessage msg, List<Object> out) {
    ByteBuf buf = allocator.ioBuffer(23);
    RedisMessageType.INTEGER.writeTo(buf);
    buf.writeBytes(numberToBytes(msg.value()));
    buf.writeShort(RedisConstants.EOL_SHORT);
    out.add(buf);
  }
  
  private void writeBulkStringHeader(ByteBufAllocator allocator, BulkStringHeaderRedisMessage msg, List<Object> out) {
    ByteBuf buf = allocator.ioBuffer(1 + (
        msg.isNull() ? 2 : 22));
    RedisMessageType.BULK_STRING.writeTo(buf);
    if (msg.isNull()) {
      buf.writeShort(RedisConstants.NULL_SHORT);
    } else {
      buf.writeBytes(numberToBytes(msg.bulkStringLength()));
      buf.writeShort(RedisConstants.EOL_SHORT);
    } 
    out.add(buf);
  }
  
  private static void writeBulkStringContent(ByteBufAllocator allocator, BulkStringRedisContent msg, List<Object> out) {
    out.add(msg.content().retain());
    if (msg instanceof LastBulkStringRedisContent)
      out.add(allocator.ioBuffer(2).writeShort(RedisConstants.EOL_SHORT)); 
  }
  
  private void writeFullBulkStringMessage(ByteBufAllocator allocator, FullBulkStringRedisMessage msg, List<Object> out) {
    if (msg.isNull()) {
      ByteBuf buf = allocator.ioBuffer(5);
      RedisMessageType.BULK_STRING.writeTo(buf);
      buf.writeShort(RedisConstants.NULL_SHORT);
      buf.writeShort(RedisConstants.EOL_SHORT);
      out.add(buf);
    } else {
      ByteBuf headerBuf = allocator.ioBuffer(23);
      RedisMessageType.BULK_STRING.writeTo(headerBuf);
      headerBuf.writeBytes(numberToBytes(msg.content().readableBytes()));
      headerBuf.writeShort(RedisConstants.EOL_SHORT);
      out.add(headerBuf);
      out.add(msg.content().retain());
      out.add(allocator.ioBuffer(2).writeShort(RedisConstants.EOL_SHORT));
    } 
  }
  
  private void writeArrayHeader(ByteBufAllocator allocator, ArrayHeaderRedisMessage msg, List<Object> out) {
    writeArrayHeader(allocator, msg.isNull(), msg.length(), out);
  }
  
  private void writeArrayMessage(ByteBufAllocator allocator, ArrayRedisMessage msg, List<Object> out) {
    if (msg.isNull()) {
      writeArrayHeader(allocator, msg.isNull(), -1L, out);
    } else {
      writeArrayHeader(allocator, msg.isNull(), msg.children().size(), out);
      for (RedisMessage child : msg.children())
        writeRedisMessage(allocator, child, out); 
    } 
  }
  
  private void writeArrayHeader(ByteBufAllocator allocator, boolean isNull, long length, List<Object> out) {
    if (isNull) {
      ByteBuf buf = allocator.ioBuffer(5);
      RedisMessageType.ARRAY_HEADER.writeTo(buf);
      buf.writeShort(RedisConstants.NULL_SHORT);
      buf.writeShort(RedisConstants.EOL_SHORT);
      out.add(buf);
    } else {
      ByteBuf buf = allocator.ioBuffer(23);
      RedisMessageType.ARRAY_HEADER.writeTo(buf);
      buf.writeBytes(numberToBytes(length));
      buf.writeShort(RedisConstants.EOL_SHORT);
      out.add(buf);
    } 
  }
  
  private byte[] numberToBytes(long value) {
    byte[] bytes = this.messagePool.getByteBufOfInteger(value);
    return (bytes != null) ? bytes : RedisCodecUtil.longToAsciiBytes(value);
  }
}
