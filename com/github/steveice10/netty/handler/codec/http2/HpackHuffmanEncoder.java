package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;

final class HpackHuffmanEncoder {
  private final int[] codes;
  
  private final byte[] lengths;
  
  private final EncodedLengthProcessor encodedLengthProcessor = new EncodedLengthProcessor();
  
  private final EncodeProcessor encodeProcessor = new EncodeProcessor();
  
  HpackHuffmanEncoder() {
    this(HpackUtil.HUFFMAN_CODES, HpackUtil.HUFFMAN_CODE_LENGTHS);
  }
  
  private HpackHuffmanEncoder(int[] codes, byte[] lengths) {
    this.codes = codes;
    this.lengths = lengths;
  }
  
  public void encode(ByteBuf out, CharSequence data) {
    ObjectUtil.checkNotNull(out, "out");
    if (data instanceof AsciiString) {
      AsciiString string = (AsciiString)data;
      try {
        this.encodeProcessor.out = out;
        string.forEachByte(this.encodeProcessor);
      } catch (Exception e) {
        PlatformDependent.throwException(e);
      } finally {
        this.encodeProcessor.end();
      } 
    } else {
      encodeSlowPath(out, data);
    } 
  }
  
  private void encodeSlowPath(ByteBuf out, CharSequence data) {
    long current = 0L;
    int n = 0;
    for (int i = 0; i < data.length(); i++) {
      int b = data.charAt(i) & 0xFF;
      int code = this.codes[b];
      int nbits = this.lengths[b];
      current <<= nbits;
      current |= code;
      n += nbits;
      while (n >= 8) {
        n -= 8;
        out.writeByte((int)(current >> n));
      } 
    } 
    if (n > 0) {
      current <<= 8 - n;
      current |= (255 >>> n);
      out.writeByte((int)current);
    } 
  }
  
  int getEncodedLength(CharSequence data) {
    if (data instanceof AsciiString) {
      AsciiString string = (AsciiString)data;
      try {
        this.encodedLengthProcessor.reset();
        string.forEachByte(this.encodedLengthProcessor);
        return this.encodedLengthProcessor.length();
      } catch (Exception e) {
        PlatformDependent.throwException(e);
        return -1;
      } 
    } 
    return getEncodedLengthSlowPath(data);
  }
  
  private int getEncodedLengthSlowPath(CharSequence data) {
    long len = 0L;
    for (int i = 0; i < data.length(); i++)
      len += this.lengths[data.charAt(i) & 0xFF]; 
    return (int)(len + 7L >> 3L);
  }
  
  private final class EncodeProcessor implements ByteProcessor {
    ByteBuf out;
    
    private long current;
    
    private int n;
    
    private EncodeProcessor() {}
    
    public boolean process(byte value) {
      int b = value & 0xFF;
      int nbits = HpackHuffmanEncoder.this.lengths[b];
      this.current <<= nbits;
      this.current |= HpackHuffmanEncoder.this.codes[b];
      this.n += nbits;
      while (this.n >= 8) {
        this.n -= 8;
        this.out.writeByte((int)(this.current >> this.n));
      } 
      return true;
    }
    
    void end() {
      try {
        if (this.n > 0) {
          this.current <<= 8 - this.n;
          this.current |= (255 >>> this.n);
          this.out.writeByte((int)this.current);
        } 
      } finally {
        this.out = null;
        this.current = 0L;
        this.n = 0;
      } 
    }
  }
  
  private final class EncodedLengthProcessor implements ByteProcessor {
    private long len;
    
    private EncodedLengthProcessor() {}
    
    public boolean process(byte value) {
      this.len += HpackHuffmanEncoder.this.lengths[value & 0xFF];
      return true;
    }
    
    void reset() {
      this.len = 0L;
    }
    
    int length() {
      return (int)(this.len + 7L >> 3L);
    }
  }
}
