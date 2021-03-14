package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;

final class HpackHuffmanDecoder {
  private static final Http2Exception EOS_DECODED = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - EOS Decoded", new Object[0]), HpackHuffmanDecoder.class, "decode(..)");
  
  private static final Http2Exception INVALID_PADDING = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - Invalid Padding", new Object[0]), HpackHuffmanDecoder.class, "decode(..)");
  
  private static final Node ROOT = buildTree(HpackUtil.HUFFMAN_CODES, HpackUtil.HUFFMAN_CODE_LENGTHS);
  
  private final DecoderProcessor processor;
  
  HpackHuffmanDecoder(int initialCapacity) {
    this.processor = new DecoderProcessor(initialCapacity);
  }
  
  public AsciiString decode(ByteBuf buf, int length) throws Http2Exception {
    this.processor.reset();
    buf.forEachByte(buf.readerIndex(), length, this.processor);
    buf.skipBytes(length);
    return this.processor.end();
  }
  
  private static final class Node {
    private final int symbol;
    
    private final int bits;
    
    private final Node[] children;
    
    Node() {
      this.symbol = 0;
      this.bits = 8;
      this.children = new Node[256];
    }
    
    Node(int symbol, int bits) {
      assert bits > 0 && bits <= 8;
      this.symbol = symbol;
      this.bits = bits;
      this.children = null;
    }
    
    private boolean isTerminal() {
      return (this.children == null);
    }
  }
  
  private static Node buildTree(int[] codes, byte[] lengths) {
    Node root = new Node();
    for (int i = 0; i < codes.length; i++)
      insert(root, i, codes[i], lengths[i]); 
    return root;
  }
  
  private static void insert(Node root, int symbol, int code, byte length) {
    Node current = root;
    while (length > 8) {
      if (current.isTerminal())
        throw new IllegalStateException("invalid Huffman code: prefix not unique"); 
      length = (byte)(length - 8);
      int j = code >>> length & 0xFF;
      if (current.children[j] == null)
        current.children[j] = new Node(); 
      current = current.children[j];
    } 
    Node terminal = new Node(symbol, length);
    int shift = 8 - length;
    int start = code << shift & 0xFF;
    int end = 1 << shift;
    for (int i = start; i < start + end; i++)
      current.children[i] = terminal; 
  }
  
  private static final class DecoderProcessor implements ByteProcessor {
    private final int initialCapacity;
    
    private byte[] bytes;
    
    private int index;
    
    private HpackHuffmanDecoder.Node node;
    
    private int current;
    
    private int currentBits;
    
    private int symbolBits;
    
    DecoderProcessor(int initialCapacity) {
      this.initialCapacity = ObjectUtil.checkPositive(initialCapacity, "initialCapacity");
    }
    
    void reset() {
      this.node = HpackHuffmanDecoder.ROOT;
      this.current = 0;
      this.currentBits = 0;
      this.symbolBits = 0;
      this.bytes = new byte[this.initialCapacity];
      this.index = 0;
    }
    
    public boolean process(byte value) throws Http2Exception {
      this.current = this.current << 8 | value & 0xFF;
      this.currentBits += 8;
      this.symbolBits += 8;
      while (true) {
        this.node = this.node.children[this.current >>> this.currentBits - 8 & 0xFF];
        this.currentBits -= this.node.bits;
        if (this.node.isTerminal()) {
          if (this.node.symbol == 256)
            throw HpackHuffmanDecoder.EOS_DECODED; 
          append(this.node.symbol);
          this.node = HpackHuffmanDecoder.ROOT;
          this.symbolBits = this.currentBits;
        } 
        if (this.currentBits < 8)
          return true; 
      } 
    }
    
    AsciiString end() throws Http2Exception {
      while (this.currentBits > 0) {
        this.node = this.node.children[this.current << 8 - this.currentBits & 0xFF];
        if (this.node.isTerminal() && this.node.bits <= this.currentBits) {
          if (this.node.symbol == 256)
            throw HpackHuffmanDecoder.EOS_DECODED; 
          this.currentBits -= this.node.bits;
          append(this.node.symbol);
          this.node = HpackHuffmanDecoder.ROOT;
          this.symbolBits = this.currentBits;
        } 
      } 
      int mask = (1 << this.symbolBits) - 1;
      if (this.symbolBits > 7 || (this.current & mask) != mask)
        throw HpackHuffmanDecoder.INVALID_PADDING; 
      return new AsciiString(this.bytes, 0, this.index, false);
    }
    
    private void append(int i) {
      if (this.bytes.length == this.index) {
        int newLength = (this.bytes.length >= 1024) ? (this.bytes.length + this.initialCapacity) : (this.bytes.length << 1);
        byte[] newBytes = new byte[newLength];
        System.arraycopy(this.bytes, 0, newBytes, 0, this.bytes.length);
        this.bytes = newBytes;
      } 
      this.bytes[this.index++] = (byte)i;
    }
  }
}
