package com.github.steveice10.netty.handler.codec.haproxy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.ProtocolDetectionResult;
import com.github.steveice10.netty.util.CharsetUtil;
import java.util.List;

public class HAProxyMessageDecoder extends ByteToMessageDecoder {
  private static final int V1_MAX_LENGTH = 108;
  
  private static final int V2_MAX_LENGTH = 65551;
  
  private static final int V2_MIN_LENGTH = 232;
  
  private static final int V2_MAX_TLV = 65319;
  
  private static final int DELIMITER_LENGTH = 2;
  
  private static final byte[] BINARY_PREFIX = new byte[] { 
      13, 10, 13, 10, 0, 13, 10, 81, 85, 73, 
      84, 10 };
  
  private static final byte[] TEXT_PREFIX = new byte[] { 80, 82, 79, 88, 89 };
  
  private static final int BINARY_PREFIX_LENGTH = BINARY_PREFIX.length;
  
  private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V1 = ProtocolDetectionResult.detected(HAProxyProtocolVersion.V1);
  
  private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V2 = ProtocolDetectionResult.detected(HAProxyProtocolVersion.V2);
  
  private boolean discarding;
  
  private int discardedBytes;
  
  private boolean finished;
  
  private int version = -1;
  
  private final int v2MaxHeaderSize;
  
  public HAProxyMessageDecoder() {
    this.v2MaxHeaderSize = 65551;
  }
  
  public HAProxyMessageDecoder(int maxTlvSize) {
    if (maxTlvSize < 1) {
      this.v2MaxHeaderSize = 232;
    } else if (maxTlvSize > 65319) {
      this.v2MaxHeaderSize = 65551;
    } else {
      int calcMax = maxTlvSize + 232;
      if (calcMax > 65551) {
        this.v2MaxHeaderSize = 65551;
      } else {
        this.v2MaxHeaderSize = calcMax;
      } 
    } 
  }
  
  private static int findVersion(ByteBuf buffer) {
    int n = buffer.readableBytes();
    if (n < 13)
      return -1; 
    int idx = buffer.readerIndex();
    return match(BINARY_PREFIX, buffer, idx) ? buffer.getByte(idx + BINARY_PREFIX_LENGTH) : 1;
  }
  
  private static int findEndOfHeader(ByteBuf buffer) {
    int n = buffer.readableBytes();
    if (n < 16)
      return -1; 
    int offset = buffer.readerIndex() + 14;
    int totalHeaderBytes = 16 + buffer.getUnsignedShort(offset);
    if (n >= totalHeaderBytes)
      return totalHeaderBytes; 
    return -1;
  }
  
  private static int findEndOfLine(ByteBuf buffer) {
    int n = buffer.writerIndex();
    for (int i = buffer.readerIndex(); i < n; i++) {
      byte b = buffer.getByte(i);
      if (b == 13 && i < n - 1 && buffer.getByte(i + 1) == 10)
        return i; 
    } 
    return -1;
  }
  
  public boolean isSingleDecode() {
    return true;
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    super.channelRead(ctx, msg);
    if (this.finished)
      ctx.pipeline().remove((ChannelHandler)this); 
  }
  
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    ByteBuf decoded;
    if (this.version == -1 && (
      this.version = findVersion(in)) == -1)
      return; 
    if (this.version == 1) {
      decoded = decodeLine(ctx, in);
    } else {
      decoded = decodeStruct(ctx, in);
    } 
    if (decoded != null) {
      this.finished = true;
      try {
        if (this.version == 1) {
          out.add(HAProxyMessage.decodeHeader(decoded.toString(CharsetUtil.US_ASCII)));
        } else {
          out.add(HAProxyMessage.decodeHeader(decoded));
        } 
      } catch (HAProxyProtocolException e) {
        fail(ctx, null, (Exception)e);
      } 
    } 
  }
  
  private ByteBuf decodeStruct(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    int eoh = findEndOfHeader(buffer);
    if (!this.discarding) {
      if (eoh >= 0) {
        int i = eoh - buffer.readerIndex();
        if (i > this.v2MaxHeaderSize) {
          buffer.readerIndex(eoh);
          failOverLimit(ctx, i);
          return null;
        } 
        return buffer.readSlice(i);
      } 
      int length = buffer.readableBytes();
      if (length > this.v2MaxHeaderSize) {
        this.discardedBytes = length;
        buffer.skipBytes(length);
        this.discarding = true;
        failOverLimit(ctx, "over " + this.discardedBytes);
      } 
      return null;
    } 
    if (eoh >= 0) {
      buffer.readerIndex(eoh);
      this.discardedBytes = 0;
      this.discarding = false;
    } else {
      this.discardedBytes = buffer.readableBytes();
      buffer.skipBytes(this.discardedBytes);
    } 
    return null;
  }
  
  private ByteBuf decodeLine(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    int eol = findEndOfLine(buffer);
    if (!this.discarding) {
      if (eol >= 0) {
        int i = eol - buffer.readerIndex();
        if (i > 108) {
          buffer.readerIndex(eol + 2);
          failOverLimit(ctx, i);
          return null;
        } 
        ByteBuf frame = buffer.readSlice(i);
        buffer.skipBytes(2);
        return frame;
      } 
      int length = buffer.readableBytes();
      if (length > 108) {
        this.discardedBytes = length;
        buffer.skipBytes(length);
        this.discarding = true;
        failOverLimit(ctx, "over " + this.discardedBytes);
      } 
      return null;
    } 
    if (eol >= 0) {
      int delimLength = (buffer.getByte(eol) == 13) ? 2 : 1;
      buffer.readerIndex(eol + delimLength);
      this.discardedBytes = 0;
      this.discarding = false;
    } else {
      this.discardedBytes = buffer.readableBytes();
      buffer.skipBytes(this.discardedBytes);
    } 
    return null;
  }
  
  private void failOverLimit(ChannelHandlerContext ctx, int length) {
    failOverLimit(ctx, String.valueOf(length));
  }
  
  private void failOverLimit(ChannelHandlerContext ctx, String length) {
    int maxLength = (this.version == 1) ? 108 : this.v2MaxHeaderSize;
    fail(ctx, "header length (" + length + ") exceeds the allowed maximum (" + maxLength + ')', null);
  }
  
  private void fail(ChannelHandlerContext ctx, String errMsg, Exception e) {
    HAProxyProtocolException ppex;
    this.finished = true;
    ctx.close();
    if (errMsg != null && e != null) {
      ppex = new HAProxyProtocolException(errMsg, e);
    } else if (errMsg != null) {
      ppex = new HAProxyProtocolException(errMsg);
    } else if (e != null) {
      ppex = new HAProxyProtocolException(e);
    } else {
      ppex = new HAProxyProtocolException();
    } 
    throw ppex;
  }
  
  public static ProtocolDetectionResult<HAProxyProtocolVersion> detectProtocol(ByteBuf buffer) {
    if (buffer.readableBytes() < 12)
      return ProtocolDetectionResult.needsMoreData(); 
    int idx = buffer.readerIndex();
    if (match(BINARY_PREFIX, buffer, idx))
      return DETECTION_RESULT_V2; 
    if (match(TEXT_PREFIX, buffer, idx))
      return DETECTION_RESULT_V1; 
    return ProtocolDetectionResult.invalid();
  }
  
  private static boolean match(byte[] prefix, ByteBuf buffer, int idx) {
    for (int i = 0; i < prefix.length; i++) {
      byte b = buffer.getByte(idx + i);
      if (b != prefix[i])
        return false; 
    } 
    return true;
  }
}
