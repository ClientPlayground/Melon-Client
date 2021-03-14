package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.util.ByteProcessor;

final class Utf8Validator implements ByteProcessor {
  private static final int UTF8_ACCEPT = 0;
  
  private static final int UTF8_REJECT = 12;
  
  private static final byte[] TYPES = new byte[] { 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
      1, 1, 1, 1, 9, 9, 9, 9, 9, 9, 
      9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
      7, 7, 8, 8, 2, 2, 2, 2, 2, 2, 
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
      2, 2, 2, 2, 10, 3, 3, 3, 3, 3, 
      3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 
      11, 6, 6, 6, 5, 8, 8, 8, 8, 8, 
      8, 8, 8, 8, 8, 8 };
  
  private static final byte[] STATES = new byte[] { 
      0, 12, 24, 36, 60, 96, 84, 12, 12, 12, 
      48, 72, 12, 12, 12, 12, 12, 12, 12, 12, 
      12, 12, 12, 12, 12, 0, 12, 12, 12, 12, 
      12, 0, 12, 0, 12, 12, 12, 24, 12, 12, 
      12, 12, 12, 24, 12, 24, 12, 12, 12, 12, 
      12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 
      12, 24, 12, 12, 12, 12, 12, 12, 12, 24, 
      12, 12, 12, 12, 12, 12, 12, 12, 12, 36, 
      12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 
      12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 
      12, 12, 12, 12, 12, 12, 12, 12 };
  
  private int state = 0;
  
  private int codep;
  
  private boolean checking;
  
  public void check(ByteBuf buffer) {
    this.checking = true;
    buffer.forEachByte(this);
  }
  
  public void finish() {
    this.checking = false;
    this.codep = 0;
    if (this.state != 0) {
      this.state = 0;
      throw new CorruptedFrameException("bytes are not UTF-8");
    } 
  }
  
  public boolean process(byte b) throws Exception {
    byte type = TYPES[b & 0xFF];
    this.codep = (this.state != 0) ? (b & 0x3F | this.codep << 6) : (255 >> type & b);
    this.state = STATES[this.state + type];
    if (this.state == 12) {
      this.checking = false;
      throw new CorruptedFrameException("bytes are not UTF-8");
    } 
    return true;
  }
  
  public boolean isChecking() {
    return this.checking;
  }
}
