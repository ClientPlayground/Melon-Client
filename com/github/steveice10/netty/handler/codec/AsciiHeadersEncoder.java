package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.CharsetUtil;
import java.util.Map;

public final class AsciiHeadersEncoder {
  private final ByteBuf buf;
  
  private final SeparatorType separatorType;
  
  private final NewlineType newlineType;
  
  public enum SeparatorType {
    COLON, COLON_SPACE;
  }
  
  public enum NewlineType {
    LF, CRLF;
  }
  
  public AsciiHeadersEncoder(ByteBuf buf) {
    this(buf, SeparatorType.COLON_SPACE, NewlineType.CRLF);
  }
  
  public AsciiHeadersEncoder(ByteBuf buf, SeparatorType separatorType, NewlineType newlineType) {
    if (buf == null)
      throw new NullPointerException("buf"); 
    if (separatorType == null)
      throw new NullPointerException("separatorType"); 
    if (newlineType == null)
      throw new NullPointerException("newlineType"); 
    this.buf = buf;
    this.separatorType = separatorType;
    this.newlineType = newlineType;
  }
  
  public void encode(Map.Entry<CharSequence, CharSequence> entry) {
    CharSequence name = entry.getKey();
    CharSequence value = entry.getValue();
    ByteBuf buf = this.buf;
    int nameLen = name.length();
    int valueLen = value.length();
    int entryLen = nameLen + valueLen + 4;
    int offset = buf.writerIndex();
    buf.ensureWritable(entryLen);
    writeAscii(buf, offset, name);
    offset += nameLen;
    switch (this.separatorType) {
      case LF:
        buf.setByte(offset++, 58);
        break;
      case CRLF:
        buf.setByte(offset++, 58);
        buf.setByte(offset++, 32);
        break;
      default:
        throw new Error();
    } 
    writeAscii(buf, offset, value);
    offset += valueLen;
    switch (this.newlineType) {
      case LF:
        buf.setByte(offset++, 10);
        break;
      case CRLF:
        buf.setByte(offset++, 13);
        buf.setByte(offset++, 10);
        break;
      default:
        throw new Error();
    } 
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
