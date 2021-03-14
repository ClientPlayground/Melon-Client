package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;

public class BinaryMemcacheRequestEncoder extends AbstractBinaryMemcacheEncoder<BinaryMemcacheRequest> {
  protected void encodeHeader(ByteBuf buf, BinaryMemcacheRequest msg) {
    buf.writeByte(msg.magic());
    buf.writeByte(msg.opcode());
    buf.writeShort(msg.keyLength());
    buf.writeByte(msg.extrasLength());
    buf.writeByte(msg.dataType());
    buf.writeShort(msg.reserved());
    buf.writeInt(msg.totalBodyLength());
    buf.writeInt(msg.opaque());
    buf.writeLong(msg.cas());
  }
}
