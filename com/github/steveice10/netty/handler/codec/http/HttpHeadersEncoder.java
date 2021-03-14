package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.CharsetUtil;

final class HttpHeadersEncoder {
  private static final int COLON_AND_SPACE_SHORT = 14880;
  
  static void encoderHeader(CharSequence name, CharSequence value, ByteBuf buf) {
    int nameLen = name.length();
    int valueLen = value.length();
    int entryLen = nameLen + valueLen + 4;
    buf.ensureWritable(entryLen);
    int offset = buf.writerIndex();
    writeAscii(buf, offset, name);
    offset += nameLen;
    ByteBufUtil.setShortBE(buf, offset, 14880);
    offset += 2;
    writeAscii(buf, offset, value);
    offset += valueLen;
    ByteBufUtil.setShortBE(buf, offset, 3338);
    offset += 2;
    buf.writerIndex(offset);
  }
  
  private static void writeAscii(ByteBuf buf, int offset, CharSequence value) {
    if (value instanceof AsciiString) {
      ByteBufUtil.copy((AsciiString)value, 0, buf, offset, value.length());
    } else {
      buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
    } 
  }
}
