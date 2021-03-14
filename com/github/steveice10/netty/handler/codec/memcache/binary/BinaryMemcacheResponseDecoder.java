package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;

public class BinaryMemcacheResponseDecoder extends AbstractBinaryMemcacheDecoder<BinaryMemcacheResponse> {
  public BinaryMemcacheResponseDecoder() {
    this(8192);
  }
  
  public BinaryMemcacheResponseDecoder(int chunkSize) {
    super(chunkSize);
  }
  
  protected BinaryMemcacheResponse decodeHeader(ByteBuf in) {
    DefaultBinaryMemcacheResponse header = new DefaultBinaryMemcacheResponse();
    header.setMagic(in.readByte());
    header.setOpcode(in.readByte());
    header.setKeyLength(in.readShort());
    header.setExtrasLength(in.readByte());
    header.setDataType(in.readByte());
    header.setStatus(in.readShort());
    header.setTotalBodyLength(in.readInt());
    header.setOpaque(in.readInt());
    header.setCas(in.readLong());
    return header;
  }
  
  protected BinaryMemcacheResponse buildInvalidMessage() {
    return new DefaultBinaryMemcacheResponse(Unpooled.EMPTY_BUFFER, Unpooled.EMPTY_BUFFER);
  }
}
