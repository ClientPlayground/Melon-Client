package com.github.steveice10.netty.handler.codec.protobuf;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import java.util.List;

@Sharable
public class ProtobufDecoder extends MessageToMessageDecoder<ByteBuf> {
  private static final boolean HAS_PARSER;
  
  private final MessageLite prototype;
  
  private final ExtensionRegistryLite extensionRegistry;
  
  static {
    boolean hasParser = false;
    try {
      MessageLite.class.getDeclaredMethod("getParserForType", new Class[0]);
      hasParser = true;
    } catch (Throwable throwable) {}
    HAS_PARSER = hasParser;
  }
  
  public ProtobufDecoder(MessageLite prototype) {
    this(prototype, (ExtensionRegistry)null);
  }
  
  public ProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
    this(prototype, (ExtensionRegistryLite)extensionRegistry);
  }
  
  public ProtobufDecoder(MessageLite prototype, ExtensionRegistryLite extensionRegistry) {
    if (prototype == null)
      throw new NullPointerException("prototype"); 
    this.prototype = prototype.getDefaultInstanceForType();
    this.extensionRegistry = extensionRegistry;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    byte[] array;
    int offset, length = msg.readableBytes();
    if (msg.hasArray()) {
      array = msg.array();
      offset = msg.arrayOffset() + msg.readerIndex();
    } else {
      array = new byte[length];
      msg.getBytes(msg.readerIndex(), array, 0, length);
      offset = 0;
    } 
    if (this.extensionRegistry == null) {
      if (HAS_PARSER) {
        out.add(this.prototype.getParserForType().parseFrom(array, offset, length));
      } else {
        out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length).build());
      } 
    } else if (HAS_PARSER) {
      out.add(this.prototype.getParserForType().parseFrom(array, offset, length, this.extensionRegistry));
    } else {
      out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length, this.extensionRegistry)
          .build());
    } 
  }
}
