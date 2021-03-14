package com.github.steveice10.netty.util.internal;

public final class ConstantTimeUtils {
  public static int equalsConstantTime(int x, int y) {
    int z = 0xFFFFFFFF ^ x ^ y;
    z &= z >> 16;
    z &= z >> 8;
    z &= z >> 4;
    z &= z >> 2;
    z &= z >> 1;
    return z & 0x1;
  }
  
  public static int equalsConstantTime(long x, long y) {
    long z = 0xFFFFFFFFFFFFFFFFL ^ x ^ y;
    z &= z >> 32L;
    z &= z >> 16L;
    z &= z >> 8L;
    z &= z >> 4L;
    z &= z >> 2L;
    z &= z >> 1L;
    return (int)(z & 0x1L);
  }
  
  public static int equalsConstantTime(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length) {
    int b = 0;
    int end = startPos1 + length;
    for (; startPos1 < end; startPos1++, startPos2++)
      b |= bytes1[startPos1] ^ bytes2[startPos2]; 
    return equalsConstantTime(b, 0);
  }
  
  public static int equalsConstantTime(CharSequence s1, CharSequence s2) {
    if (s1.length() != s2.length())
      return 0; 
    int c = 0;
    for (int i = 0; i < s1.length(); i++)
      c |= s1.charAt(i) ^ s2.charAt(i); 
    return equalsConstantTime(c, 0);
  }
}
