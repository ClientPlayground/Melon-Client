package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;

final class RedisCodecUtil {
  static byte[] longToAsciiBytes(long value) {
    return Long.toString(value).getBytes(CharsetUtil.US_ASCII);
  }
  
  static short makeShort(char first, char second) {
    return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)(second << 8 | first) : (short)(first << 8 | second);
  }
  
  static byte[] shortToBytes(short value) {
    byte[] bytes = new byte[2];
    if (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER) {
      bytes[1] = (byte)(value >> 8 & 0xFF);
      bytes[0] = (byte)(value & 0xFF);
    } else {
      bytes[0] = (byte)(value >> 8 & 0xFF);
      bytes[1] = (byte)(value & 0xFF);
    } 
    return bytes;
  }
}
