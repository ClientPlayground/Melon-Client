package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;

public class BinaryMemcacheResponseEncoder extends AbstractBinaryMemcacheEncoder<BinaryMemcacheResponse> {
  protected void encodeHeader(ByteBuf buf, BinaryMemcacheResponse msg) {
    buf.writeByte(msg.magic());
    buf.writeByte(msg.opcode());
    buf.writeShort(msg.keyLength());
    buf.writeByte(msg.extrasLength());
    buf.writeByte(msg.dataType());
    buf.writeShort(msg.status());
    buf.writeInt(msg.totalBodyLength());
    buf.writeInt(msg.opaque());
    buf.writeLong(msg.cas());
  }
}
