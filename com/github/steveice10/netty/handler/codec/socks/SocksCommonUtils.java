package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

final class SocksCommonUtils {
  public static final SocksRequest UNKNOWN_SOCKS_REQUEST = new UnknownSocksRequest();
  
  public static final SocksResponse UNKNOWN_SOCKS_RESPONSE = new UnknownSocksResponse();
  
  private static final char ipv6hextetSeparator = ':';
  
  public static String ipv6toStr(byte[] src) {
    assert src.length == 16;
    StringBuilder sb = new StringBuilder(39);
    ipv6toStr(sb, src, 0, 8);
    return sb.toString();
  }
  
  private static void ipv6toStr(StringBuilder sb, byte[] src, int fromHextet, int toHextet) {
    toHextet--;
    int i;
    for (i = fromHextet; i < toHextet; i++) {
      appendHextet(sb, src, i);
      sb.append(':');
    } 
    appendHextet(sb, src, i);
  }
  
  private static void appendHextet(StringBuilder sb, byte[] src, int i) {
    StringUtil.toHexString(sb, src, i << 1, 2);
  }
  
  static String readUsAscii(ByteBuf buffer, int length) {
    String s = buffer.toString(buffer.readerIndex(), length, CharsetUtil.US_ASCII);
    buffer.skipBytes(length);
    return s;
  }
}
