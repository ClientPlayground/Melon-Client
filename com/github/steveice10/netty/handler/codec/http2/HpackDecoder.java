package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;

final class HpackDecoder {
  private static final Http2Exception DECODE_ULE_128_DECOMPRESSION_EXCEPTION = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - decompression failure", new Object[0]), HpackDecoder.class, "decodeULE128(..)");
  
  private static final Http2Exception DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - long overflow", new Object[0]), HpackDecoder.class, "decodeULE128(..)");
  
  private static final Http2Exception DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - int overflow", new Object[0]), HpackDecoder.class, "decodeULE128ToInt(..)");
  
  private static final Http2Exception DECODE_ILLEGAL_INDEX_VALUE = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", new Object[0]), HpackDecoder.class, "decode(..)");
  
  private static final Http2Exception INDEX_HEADER_ILLEGAL_INDEX_VALUE = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", new Object[0]), HpackDecoder.class, "indexHeader(..)");
  
  private static final Http2Exception READ_NAME_ILLEGAL_INDEX_VALUE = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", new Object[0]), HpackDecoder.class, "readName(..)");
  
  private static final Http2Exception INVALID_MAX_DYNAMIC_TABLE_SIZE = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - invalid max dynamic table size", new Object[0]), HpackDecoder.class, "setDynamicTableSize(..)");
  
  private static final Http2Exception MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED = (Http2Exception)ThrowableUtil.unknownStackTrace(
      Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "HPACK - max dynamic table size change required", new Object[0]), HpackDecoder.class, "decode(..)");
  
  private static final byte READ_HEADER_REPRESENTATION = 0;
  
  private static final byte READ_MAX_DYNAMIC_TABLE_SIZE = 1;
  
  private static final byte READ_INDEXED_HEADER = 2;
  
  private static final byte READ_INDEXED_HEADER_NAME = 3;
  
  private static final byte READ_LITERAL_HEADER_NAME_LENGTH_PREFIX = 4;
  
  private static final byte READ_LITERAL_HEADER_NAME_LENGTH = 5;
  
  private static final byte READ_LITERAL_HEADER_NAME = 6;
  
  private static final byte READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX = 7;
  
  private static final byte READ_LITERAL_HEADER_VALUE_LENGTH = 8;
  
  private static final byte READ_LITERAL_HEADER_VALUE = 9;
  
  private final HpackDynamicTable hpackDynamicTable;
  
  private final HpackHuffmanDecoder hpackHuffmanDecoder;
  
  private long maxHeaderListSizeGoAway;
  
  private long maxHeaderListSize;
  
  private long maxDynamicTableSize;
  
  private long encoderMaxDynamicTableSize;
  
  private boolean maxDynamicTableSizeChangeRequired;
  
  HpackDecoder(long maxHeaderListSize, int initialHuffmanDecodeCapacity) {
    this(maxHeaderListSize, initialHuffmanDecodeCapacity, 4096);
  }
  
  HpackDecoder(long maxHeaderListSize, int initialHuffmanDecodeCapacity, int maxHeaderTableSize) {
    this.maxHeaderListSize = ObjectUtil.checkPositive(maxHeaderListSize, "maxHeaderListSize");
    this.maxHeaderListSizeGoAway = Http2CodecUtil.calculateMaxHeaderListSizeGoAway(maxHeaderListSize);
    this.maxDynamicTableSize = this.encoderMaxDynamicTableSize = maxHeaderTableSize;
    this.maxDynamicTableSizeChangeRequired = false;
    this.hpackDynamicTable = new HpackDynamicTable(maxHeaderTableSize);
    this.hpackHuffmanDecoder = new HpackHuffmanDecoder(initialHuffmanDecodeCapacity);
  }
  
  public void decode(int streamId, ByteBuf in, Http2Headers headers, boolean validateHeaders) throws Http2Exception {
    int index = 0;
    long headersLength = 0L;
    int nameLength = 0;
    int valueLength = 0;
    byte state = 0;
    boolean huffmanEncoded = false;
    CharSequence name = null;
    HeaderType headerType = null;
    HpackUtil.IndexType indexType = HpackUtil.IndexType.NONE;
    while (in.isReadable()) {
      byte b;
      HpackHeaderField indexedHeader;
      CharSequence value;
      switch (state) {
        case 0:
          b = in.readByte();
          if (this.maxDynamicTableSizeChangeRequired && (b & 0xE0) != 32)
            throw MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED; 
          if (b < 0) {
            index = b & Byte.MAX_VALUE;
            switch (index) {
              case 0:
                throw DECODE_ILLEGAL_INDEX_VALUE;
              case 127:
                state = 2;
                continue;
            } 
            HpackHeaderField hpackHeaderField = getIndexedHeader(index);
            headerType = validate(hpackHeaderField.name, headerType, validateHeaders);
            headersLength = addHeader(headers, hpackHeaderField.name, hpackHeaderField.value, headersLength);
            continue;
          } 
          if ((b & 0x40) == 64) {
            indexType = HpackUtil.IndexType.INCREMENTAL;
            index = b & 0x3F;
            switch (index) {
              case 0:
                state = 4;
                continue;
              case 63:
                state = 3;
                continue;
            } 
            name = readName(index);
            headerType = validate(name, headerType, validateHeaders);
            nameLength = name.length();
            state = 7;
            continue;
          } 
          if ((b & 0x20) == 32) {
            index = b & 0x1F;
            if (index == 31) {
              state = 1;
              continue;
            } 
            setDynamicTableSize(index);
            state = 0;
            continue;
          } 
          indexType = ((b & 0x10) == 16) ? HpackUtil.IndexType.NEVER : HpackUtil.IndexType.NONE;
          index = b & 0xF;
          switch (index) {
            case 0:
              state = 4;
              continue;
            case 15:
              state = 3;
              continue;
          } 
          name = readName(index);
          headerType = validate(name, headerType, validateHeaders);
          nameLength = name.length();
          state = 7;
          continue;
        case 1:
          setDynamicTableSize(decodeULE128(in, index));
          state = 0;
          continue;
        case 2:
          indexedHeader = getIndexedHeader(decodeULE128(in, index));
          headerType = validate(indexedHeader.name, headerType, validateHeaders);
          headersLength = addHeader(headers, indexedHeader.name, indexedHeader.value, headersLength);
          state = 0;
          continue;
        case 3:
          name = readName(decodeULE128(in, index));
          headerType = validate(name, headerType, validateHeaders);
          nameLength = name.length();
          state = 7;
          continue;
        case 4:
          b = in.readByte();
          huffmanEncoded = ((b & 0x80) == 128);
          index = b & Byte.MAX_VALUE;
          if (index == 127) {
            state = 5;
            continue;
          } 
          if (index > this.maxHeaderListSizeGoAway - headersLength)
            Http2CodecUtil.headerListSizeExceeded(this.maxHeaderListSizeGoAway); 
          nameLength = index;
          state = 6;
          continue;
        case 5:
          nameLength = decodeULE128(in, index);
          if (nameLength > this.maxHeaderListSizeGoAway - headersLength)
            Http2CodecUtil.headerListSizeExceeded(this.maxHeaderListSizeGoAway); 
          state = 6;
          continue;
        case 6:
          if (in.readableBytes() < nameLength)
            throw notEnoughDataException(in); 
          name = readStringLiteral(in, nameLength, huffmanEncoded);
          headerType = validate(name, headerType, validateHeaders);
          state = 7;
          continue;
        case 7:
          b = in.readByte();
          huffmanEncoded = ((b & 0x80) == 128);
          index = b & Byte.MAX_VALUE;
          switch (index) {
            case 127:
              state = 8;
              continue;
            case 0:
              headerType = validate(name, headerType, validateHeaders);
              headersLength = insertHeader(headers, name, (CharSequence)AsciiString.EMPTY_STRING, indexType, headersLength);
              state = 0;
              continue;
          } 
          if (index + nameLength > this.maxHeaderListSizeGoAway - headersLength)
            Http2CodecUtil.headerListSizeExceeded(this.maxHeaderListSizeGoAway); 
          valueLength = index;
          state = 9;
          continue;
        case 8:
          valueLength = decodeULE128(in, index);
          if (valueLength + nameLength > this.maxHeaderListSizeGoAway - headersLength)
            Http2CodecUtil.headerListSizeExceeded(this.maxHeaderListSizeGoAway); 
          state = 9;
          continue;
        case 9:
          if (in.readableBytes() < valueLength)
            throw notEnoughDataException(in); 
          value = readStringLiteral(in, valueLength, huffmanEncoded);
          headerType = validate(name, headerType, validateHeaders);
          headersLength = insertHeader(headers, name, value, indexType, headersLength);
          state = 0;
          continue;
      } 
      throw new Error("should not reach here state: " + state);
    } 
    if (headersLength > this.maxHeaderListSize)
      Http2CodecUtil.headerListSizeExceeded(streamId, this.maxHeaderListSize, true); 
    if (state != 0)
      throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "Incomplete header block fragment.", new Object[0]); 
  }
  
  public void setMaxHeaderTableSize(long maxHeaderTableSize) throws Http2Exception {
    if (maxHeaderTableSize < 0L || maxHeaderTableSize > 4294967295L)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header Table Size must be >= %d and <= %d but was %d", new Object[] { Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(maxHeaderTableSize) }); 
    this.maxDynamicTableSize = maxHeaderTableSize;
    if (this.maxDynamicTableSize < this.encoderMaxDynamicTableSize) {
      this.maxDynamicTableSizeChangeRequired = true;
      this.hpackDynamicTable.setCapacity(this.maxDynamicTableSize);
    } 
  }
  
  public void setMaxHeaderListSize(long maxHeaderListSize, long maxHeaderListSizeGoAway) throws Http2Exception {
    if (maxHeaderListSizeGoAway < maxHeaderListSize || maxHeaderListSizeGoAway < 0L)
      throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Header List Size GO_AWAY %d must be positive and >= %d", new Object[] { Long.valueOf(maxHeaderListSizeGoAway), Long.valueOf(maxHeaderListSize) }); 
    if (maxHeaderListSize < 0L || maxHeaderListSize > 4294967295L)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header List Size must be >= %d and <= %d but was %d", new Object[] { Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(maxHeaderListSize) }); 
    this.maxHeaderListSize = maxHeaderListSize;
    this.maxHeaderListSizeGoAway = maxHeaderListSizeGoAway;
  }
  
  public long getMaxHeaderListSize() {
    return this.maxHeaderListSize;
  }
  
  public long getMaxHeaderListSizeGoAway() {
    return this.maxHeaderListSizeGoAway;
  }
  
  public long getMaxHeaderTableSize() {
    return this.hpackDynamicTable.capacity();
  }
  
  int length() {
    return this.hpackDynamicTable.length();
  }
  
  long size() {
    return this.hpackDynamicTable.size();
  }
  
  HpackHeaderField getHeaderField(int index) {
    return this.hpackDynamicTable.getEntry(index + 1);
  }
  
  private void setDynamicTableSize(long dynamicTableSize) throws Http2Exception {
    if (dynamicTableSize > this.maxDynamicTableSize)
      throw INVALID_MAX_DYNAMIC_TABLE_SIZE; 
    this.encoderMaxDynamicTableSize = dynamicTableSize;
    this.maxDynamicTableSizeChangeRequired = false;
    this.hpackDynamicTable.setCapacity(dynamicTableSize);
  }
  
  private HeaderType validate(CharSequence name, HeaderType previousHeaderType, boolean validateHeaders) throws Http2Exception {
    if (!validateHeaders)
      return null; 
    if (Http2Headers.PseudoHeaderName.hasPseudoHeaderFormat(name)) {
      if (previousHeaderType == HeaderType.REGULAR_HEADER)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Pseudo-header field '%s' found after regular header.", new Object[] { name }); 
      Http2Headers.PseudoHeaderName pseudoHeader = Http2Headers.PseudoHeaderName.getPseudoHeader(name);
      if (pseudoHeader == null)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 pseudo-header '%s' encountered.", new Object[] { name }); 
      HeaderType currentHeaderType = pseudoHeader.isRequestOnly() ? HeaderType.REQUEST_PSEUDO_HEADER : HeaderType.RESPONSE_PSEUDO_HEADER;
      if (previousHeaderType != null && currentHeaderType != previousHeaderType)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Mix of request and response pseudo-headers.", new Object[0]); 
      return currentHeaderType;
    } 
    return HeaderType.REGULAR_HEADER;
  }
  
  private CharSequence readName(int index) throws Http2Exception {
    if (index <= HpackStaticTable.length) {
      HpackHeaderField hpackHeaderField = HpackStaticTable.getEntry(index);
      return hpackHeaderField.name;
    } 
    if (index - HpackStaticTable.length <= this.hpackDynamicTable.length()) {
      HpackHeaderField hpackHeaderField = this.hpackDynamicTable.getEntry(index - HpackStaticTable.length);
      return hpackHeaderField.name;
    } 
    throw READ_NAME_ILLEGAL_INDEX_VALUE;
  }
  
  private HpackHeaderField getIndexedHeader(int index) throws Http2Exception {
    if (index <= HpackStaticTable.length)
      return HpackStaticTable.getEntry(index); 
    if (index - HpackStaticTable.length <= this.hpackDynamicTable.length())
      return this.hpackDynamicTable.getEntry(index - HpackStaticTable.length); 
    throw INDEX_HEADER_ILLEGAL_INDEX_VALUE;
  }
  
  private long insertHeader(Http2Headers headers, CharSequence name, CharSequence value, HpackUtil.IndexType indexType, long headerSize) throws Http2Exception {
    headerSize = addHeader(headers, name, value, headerSize);
    switch (indexType) {
      case NONE:
      case NEVER:
        return headerSize;
      case INCREMENTAL:
        this.hpackDynamicTable.add(new HpackHeaderField(name, value));
    } 
    throw new Error("should not reach here");
  }
  
  private long addHeader(Http2Headers headers, CharSequence name, CharSequence value, long headersLength) throws Http2Exception {
    headersLength += HpackHeaderField.sizeOf(name, value);
    if (headersLength > this.maxHeaderListSizeGoAway)
      Http2CodecUtil.headerListSizeExceeded(this.maxHeaderListSizeGoAway); 
    headers.add(name, value);
    return headersLength;
  }
  
  private CharSequence readStringLiteral(ByteBuf in, int length, boolean huffmanEncoded) throws Http2Exception {
    if (huffmanEncoded)
      return (CharSequence)this.hpackHuffmanDecoder.decode(in, length); 
    byte[] buf = new byte[length];
    in.readBytes(buf);
    return (CharSequence)new AsciiString(buf, false);
  }
  
  private static IllegalArgumentException notEnoughDataException(ByteBuf in) {
    return new IllegalArgumentException("decode only works with an entire header block! " + in);
  }
  
  static int decodeULE128(ByteBuf in, int result) throws Http2Exception {
    int readerIndex = in.readerIndex();
    long v = decodeULE128(in, result);
    if (v > 2147483647L) {
      in.readerIndex(readerIndex);
      throw DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION;
    } 
    return (int)v;
  }
  
  static long decodeULE128(ByteBuf in, long result) throws Http2Exception {
    assert result <= 127L && result >= 0L;
    boolean resultStartedAtZero = (result == 0L);
    int writerIndex = in.writerIndex();
    for (int readerIndex = in.readerIndex(), shift = 0; readerIndex < writerIndex; readerIndex++, shift += 7) {
      byte b = in.getByte(readerIndex);
      if (shift == 56 && ((b & 0x80) != 0 || (b == Byte.MAX_VALUE && !resultStartedAtZero)))
        throw DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION; 
      if ((b & 0x80) == 0) {
        in.readerIndex(readerIndex + 1);
        return result + ((b & 0x7FL) << shift);
      } 
      result += (b & 0x7FL) << shift;
    } 
    throw DECODE_ULE_128_DECOMPRESSION_EXCEPTION;
  }
  
  private enum HeaderType {
    REGULAR_HEADER, REQUEST_PSEUDO_HEADER, RESPONSE_PSEUDO_HEADER;
  }
}
