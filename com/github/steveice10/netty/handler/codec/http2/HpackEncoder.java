package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.MathUtil;
import java.util.Arrays;
import java.util.Map;

final class HpackEncoder {
  private final HeaderEntry[] headerFields;
  
  private final HeaderEntry head = new HeaderEntry(-1, (CharSequence)AsciiString.EMPTY_STRING, (CharSequence)AsciiString.EMPTY_STRING, 2147483647, null);
  
  private final HpackHuffmanEncoder hpackHuffmanEncoder = new HpackHuffmanEncoder();
  
  private final byte hashMask;
  
  private final boolean ignoreMaxHeaderListSize;
  
  private long size;
  
  private long maxHeaderTableSize;
  
  private long maxHeaderListSize;
  
  HpackEncoder() {
    this(false);
  }
  
  public HpackEncoder(boolean ignoreMaxHeaderListSize) {
    this(ignoreMaxHeaderListSize, 16);
  }
  
  public HpackEncoder(boolean ignoreMaxHeaderListSize, int arraySizeHint) {
    this.ignoreMaxHeaderListSize = ignoreMaxHeaderListSize;
    this.maxHeaderTableSize = 4096L;
    this.maxHeaderListSize = 4294967295L;
    this.headerFields = new HeaderEntry[MathUtil.findNextPositivePowerOfTwo(Math.max(2, Math.min(arraySizeHint, 128)))];
    this.hashMask = (byte)(this.headerFields.length - 1);
    this.head.before = this.head.after = this.head;
  }
  
  public void encodeHeaders(int streamId, ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector) throws Http2Exception {
    if (this.ignoreMaxHeaderListSize) {
      encodeHeadersIgnoreMaxHeaderListSize(out, headers, sensitivityDetector);
    } else {
      encodeHeadersEnforceMaxHeaderListSize(streamId, out, headers, sensitivityDetector);
    } 
  }
  
  private void encodeHeadersEnforceMaxHeaderListSize(int streamId, ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector) throws Http2Exception {
    long headerSize = 0L;
    for (Map.Entry<CharSequence, CharSequence> header : (Iterable<Map.Entry<CharSequence, CharSequence>>)headers) {
      CharSequence name = header.getKey();
      CharSequence value = header.getValue();
      headerSize += HpackHeaderField.sizeOf(name, value);
      if (headerSize > this.maxHeaderListSize)
        Http2CodecUtil.headerListSizeExceeded(streamId, this.maxHeaderListSize, false); 
    } 
    encodeHeadersIgnoreMaxHeaderListSize(out, headers, sensitivityDetector);
  }
  
  private void encodeHeadersIgnoreMaxHeaderListSize(ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector) throws Http2Exception {
    for (Map.Entry<CharSequence, CharSequence> header : (Iterable<Map.Entry<CharSequence, CharSequence>>)headers) {
      CharSequence name = header.getKey();
      CharSequence value = header.getValue();
      encodeHeader(out, name, value, sensitivityDetector.isSensitive(name, value), 
          HpackHeaderField.sizeOf(name, value));
    } 
  }
  
  private void encodeHeader(ByteBuf out, CharSequence name, CharSequence value, boolean sensitive, long headerSize) {
    if (sensitive) {
      int nameIndex = getNameIndex(name);
      encodeLiteral(out, name, value, HpackUtil.IndexType.NEVER, nameIndex);
      return;
    } 
    if (this.maxHeaderTableSize == 0L) {
      int staticTableIndex = HpackStaticTable.getIndex(name, value);
      if (staticTableIndex == -1) {
        int nameIndex = HpackStaticTable.getIndex(name);
        encodeLiteral(out, name, value, HpackUtil.IndexType.NONE, nameIndex);
      } else {
        encodeInteger(out, 128, 7, staticTableIndex);
      } 
      return;
    } 
    if (headerSize > this.maxHeaderTableSize) {
      int nameIndex = getNameIndex(name);
      encodeLiteral(out, name, value, HpackUtil.IndexType.NONE, nameIndex);
      return;
    } 
    HeaderEntry headerField = getEntry(name, value);
    if (headerField != null) {
      int index = getIndex(headerField.index) + HpackStaticTable.length;
      encodeInteger(out, 128, 7, index);
    } else {
      int staticTableIndex = HpackStaticTable.getIndex(name, value);
      if (staticTableIndex != -1) {
        encodeInteger(out, 128, 7, staticTableIndex);
      } else {
        ensureCapacity(headerSize);
        encodeLiteral(out, name, value, HpackUtil.IndexType.INCREMENTAL, getNameIndex(name));
        add(name, value, headerSize);
      } 
    } 
  }
  
  public void setMaxHeaderTableSize(ByteBuf out, long maxHeaderTableSize) throws Http2Exception {
    if (maxHeaderTableSize < 0L || maxHeaderTableSize > 4294967295L)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header Table Size must be >= %d and <= %d but was %d", new Object[] { Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(maxHeaderTableSize) }); 
    if (this.maxHeaderTableSize == maxHeaderTableSize)
      return; 
    this.maxHeaderTableSize = maxHeaderTableSize;
    ensureCapacity(0L);
    encodeInteger(out, 32, 5, maxHeaderTableSize);
  }
  
  public long getMaxHeaderTableSize() {
    return this.maxHeaderTableSize;
  }
  
  public void setMaxHeaderListSize(long maxHeaderListSize) throws Http2Exception {
    if (maxHeaderListSize < 0L || maxHeaderListSize > 4294967295L)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header List Size must be >= %d and <= %d but was %d", new Object[] { Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(maxHeaderListSize) }); 
    this.maxHeaderListSize = maxHeaderListSize;
  }
  
  public long getMaxHeaderListSize() {
    return this.maxHeaderListSize;
  }
  
  private static void encodeInteger(ByteBuf out, int mask, int n, int i) {
    encodeInteger(out, mask, n, i);
  }
  
  private static void encodeInteger(ByteBuf out, int mask, int n, long i) {
    assert n >= 0 && n <= 8 : "N: " + n;
    int nbits = 255 >>> 8 - n;
    if (i < nbits) {
      out.writeByte((int)(mask | i));
    } else {
      out.writeByte(mask | nbits);
      long length = i - nbits;
      for (; (length & 0xFFFFFFFFFFFFFF80L) != 0L; length >>>= 7L)
        out.writeByte((int)(length & 0x7FL | 0x80L)); 
      out.writeByte((int)length);
    } 
  }
  
  private void encodeStringLiteral(ByteBuf out, CharSequence string) {
    int huffmanLength = this.hpackHuffmanEncoder.getEncodedLength(string);
    if (huffmanLength < string.length()) {
      encodeInteger(out, 128, 7, huffmanLength);
      this.hpackHuffmanEncoder.encode(out, string);
    } else {
      encodeInteger(out, 0, 7, string.length());
      if (string instanceof AsciiString) {
        AsciiString asciiString = (AsciiString)string;
        out.writeBytes(asciiString.array(), asciiString.arrayOffset(), asciiString.length());
      } else {
        out.writeCharSequence(string, CharsetUtil.ISO_8859_1);
      } 
    } 
  }
  
  private void encodeLiteral(ByteBuf out, CharSequence name, CharSequence value, HpackUtil.IndexType indexType, int nameIndex) {
    boolean nameIndexValid = (nameIndex != -1);
    switch (indexType) {
      case INCREMENTAL:
        encodeInteger(out, 64, 6, nameIndexValid ? nameIndex : 0);
        break;
      case NONE:
        encodeInteger(out, 0, 4, nameIndexValid ? nameIndex : 0);
        break;
      case NEVER:
        encodeInteger(out, 16, 4, nameIndexValid ? nameIndex : 0);
        break;
      default:
        throw new Error("should not reach here");
    } 
    if (!nameIndexValid)
      encodeStringLiteral(out, name); 
    encodeStringLiteral(out, value);
  }
  
  private int getNameIndex(CharSequence name) {
    int index = HpackStaticTable.getIndex(name);
    if (index == -1) {
      index = getIndex(name);
      if (index >= 0)
        index += HpackStaticTable.length; 
    } 
    return index;
  }
  
  private void ensureCapacity(long headerSize) {
    while (this.maxHeaderTableSize - this.size < headerSize) {
      int index = length();
      if (index == 0)
        break; 
      remove();
    } 
  }
  
  int length() {
    return (this.size == 0L) ? 0 : (this.head.after.index - this.head.before.index + 1);
  }
  
  long size() {
    return this.size;
  }
  
  HpackHeaderField getHeaderField(int index) {
    HeaderEntry entry = this.head;
    while (index-- >= 0)
      entry = entry.before; 
    return entry;
  }
  
  private HeaderEntry getEntry(CharSequence name, CharSequence value) {
    if (length() == 0 || name == null || value == null)
      return null; 
    int h = AsciiString.hashCode(name);
    int i = index(h);
    for (HeaderEntry e = this.headerFields[i]; e != null; e = e.next) {
      if (e.hash == h && (HpackUtil.equalsConstantTime(name, e.name) & HpackUtil.equalsConstantTime(value, e.value)) != 0)
        return e; 
    } 
    return null;
  }
  
  private int getIndex(CharSequence name) {
    if (length() == 0 || name == null)
      return -1; 
    int h = AsciiString.hashCode(name);
    int i = index(h);
    for (HeaderEntry e = this.headerFields[i]; e != null; e = e.next) {
      if (e.hash == h && HpackUtil.equalsConstantTime(name, e.name) != 0)
        return getIndex(e.index); 
    } 
    return -1;
  }
  
  private int getIndex(int index) {
    return (index == -1) ? -1 : (index - this.head.before.index + 1);
  }
  
  private void add(CharSequence name, CharSequence value, long headerSize) {
    if (headerSize > this.maxHeaderTableSize) {
      clear();
      return;
    } 
    while (this.maxHeaderTableSize - this.size < headerSize)
      remove(); 
    int h = AsciiString.hashCode(name);
    int i = index(h);
    HeaderEntry old = this.headerFields[i];
    HeaderEntry e = new HeaderEntry(h, name, value, this.head.before.index - 1, old);
    this.headerFields[i] = e;
    e.addBefore(this.head);
    this.size += headerSize;
  }
  
  private HpackHeaderField remove() {
    if (this.size == 0L)
      return null; 
    HeaderEntry eldest = this.head.after;
    int h = eldest.hash;
    int i = index(h);
    HeaderEntry prev = this.headerFields[i];
    HeaderEntry e = prev;
    while (e != null) {
      HeaderEntry next = e.next;
      if (e == eldest) {
        if (prev == eldest) {
          this.headerFields[i] = next;
        } else {
          prev.next = next;
        } 
        eldest.remove();
        this.size -= eldest.size();
        return eldest;
      } 
      prev = e;
      e = next;
    } 
    return null;
  }
  
  private void clear() {
    Arrays.fill((Object[])this.headerFields, (Object)null);
    this.head.before = this.head.after = this.head;
    this.size = 0L;
  }
  
  private int index(int h) {
    return h & this.hashMask;
  }
  
  private static final class HeaderEntry extends HpackHeaderField {
    HeaderEntry before;
    
    HeaderEntry after;
    
    HeaderEntry next;
    
    int hash;
    
    int index;
    
    HeaderEntry(int hash, CharSequence name, CharSequence value, int index, HeaderEntry next) {
      super(name, value);
      this.index = index;
      this.hash = hash;
      this.next = next;
    }
    
    private void remove() {
      this.before.after = this.after;
      this.after.before = this.before;
      this.before = null;
      this.after = null;
      this.next = null;
    }
    
    private void addBefore(HeaderEntry existingEntry) {
      this.after = existingEntry;
      this.before = existingEntry.before;
      this.before.after = this;
      this.after.before = this;
    }
  }
}
