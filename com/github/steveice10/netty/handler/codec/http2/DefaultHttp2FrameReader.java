package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.internal.PlatformDependent;

public class DefaultHttp2FrameReader implements Http2FrameReader, Http2FrameSizePolicy, Http2FrameReader.Configuration {
  private final Http2HeadersDecoder headersDecoder;
  
  private boolean readingHeaders = true;
  
  private boolean readError;
  
  private byte frameType;
  
  private int streamId;
  
  private Http2Flags flags;
  
  private int payloadLength;
  
  private HeadersContinuation headersContinuation;
  
  private int maxFrameSize;
  
  public DefaultHttp2FrameReader() {
    this(true);
  }
  
  public DefaultHttp2FrameReader(boolean validateHeaders) {
    this(new DefaultHttp2HeadersDecoder(validateHeaders));
  }
  
  public DefaultHttp2FrameReader(Http2HeadersDecoder headersDecoder) {
    this.headersDecoder = headersDecoder;
    this.maxFrameSize = 16384;
  }
  
  public Http2HeadersDecoder.Configuration headersConfiguration() {
    return this.headersDecoder.configuration();
  }
  
  public Http2FrameReader.Configuration configuration() {
    return this;
  }
  
  public Http2FrameSizePolicy frameSizePolicy() {
    return this;
  }
  
  public void maxFrameSize(int max) throws Http2Exception {
    if (!Http2CodecUtil.isMaxFrameSizeValid(max))
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid MAX_FRAME_SIZE specified in sent settings: %d", new Object[] { Integer.valueOf(max) }); 
    this.maxFrameSize = max;
  }
  
  public int maxFrameSize() {
    return this.maxFrameSize;
  }
  
  public void close() {
    closeHeadersContinuation();
  }
  
  private void closeHeadersContinuation() {
    if (this.headersContinuation != null) {
      this.headersContinuation.close();
      this.headersContinuation = null;
    } 
  }
  
  public void readFrame(ChannelHandlerContext ctx, ByteBuf input, Http2FrameListener listener) throws Http2Exception {
    if (this.readError) {
      input.skipBytes(input.readableBytes());
      return;
    } 
    try {
      do {
        if (this.readingHeaders) {
          processHeaderState(input);
          if (this.readingHeaders)
            return; 
        } 
        processPayloadState(ctx, input, listener);
        if (!this.readingHeaders)
          return; 
      } while (input.isReadable());
    } catch (Http2Exception e) {
      this.readError = !Http2Exception.isStreamError(e);
      throw e;
    } catch (RuntimeException e) {
      this.readError = true;
      throw e;
    } catch (Throwable cause) {
      this.readError = true;
      PlatformDependent.throwException(cause);
    } 
  }
  
  private void processHeaderState(ByteBuf in) throws Http2Exception {
    if (in.readableBytes() < 9)
      return; 
    this.payloadLength = in.readUnsignedMedium();
    if (this.payloadLength > this.maxFrameSize)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length: %d exceeds maximum: %d", new Object[] { Integer.valueOf(this.payloadLength), 
            Integer.valueOf(this.maxFrameSize) }); 
    this.frameType = in.readByte();
    this.flags = new Http2Flags(in.readUnsignedByte());
    this.streamId = Http2CodecUtil.readUnsignedInt(in);
    this.readingHeaders = false;
    switch (this.frameType) {
      case 0:
        verifyDataFrame();
        return;
      case 1:
        verifyHeadersFrame();
        return;
      case 2:
        verifyPriorityFrame();
        return;
      case 3:
        verifyRstStreamFrame();
        return;
      case 4:
        verifySettingsFrame();
        return;
      case 5:
        verifyPushPromiseFrame();
        return;
      case 6:
        verifyPingFrame();
        return;
      case 7:
        verifyGoAwayFrame();
        return;
      case 8:
        verifyWindowUpdateFrame();
        return;
      case 9:
        verifyContinuationFrame();
        return;
    } 
    verifyUnknownFrame();
  }
  
  private void processPayloadState(ChannelHandlerContext ctx, ByteBuf in, Http2FrameListener listener) throws Http2Exception {
    if (in.readableBytes() < this.payloadLength)
      return; 
    ByteBuf payload = in.readSlice(this.payloadLength);
    this.readingHeaders = true;
    switch (this.frameType) {
      case 0:
        readDataFrame(ctx, payload, listener);
        return;
      case 1:
        readHeadersFrame(ctx, payload, listener);
        return;
      case 2:
        readPriorityFrame(ctx, payload, listener);
        return;
      case 3:
        readRstStreamFrame(ctx, payload, listener);
        return;
      case 4:
        readSettingsFrame(ctx, payload, listener);
        return;
      case 5:
        readPushPromiseFrame(ctx, payload, listener);
        return;
      case 6:
        readPingFrame(ctx, payload.readLong(), listener);
        return;
      case 7:
        readGoAwayFrame(ctx, payload, listener);
        return;
      case 8:
        readWindowUpdateFrame(ctx, payload, listener);
        return;
      case 9:
        readContinuationFrame(payload, listener);
        return;
    } 
    readUnknownFrame(ctx, payload, listener);
  }
  
  private void verifyDataFrame() throws Http2Exception {
    verifyAssociatedWithAStream();
    verifyNotProcessingHeaders();
    verifyPayloadLength(this.payloadLength);
    if (this.payloadLength < this.flags.getPaddingPresenceFieldLength())
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyHeadersFrame() throws Http2Exception {
    verifyAssociatedWithAStream();
    verifyNotProcessingHeaders();
    verifyPayloadLength(this.payloadLength);
    int requiredLength = this.flags.getPaddingPresenceFieldLength() + this.flags.getNumPriorityBytes();
    if (this.payloadLength < requiredLength)
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length too small." + this.payloadLength, new Object[0]); 
  }
  
  private void verifyPriorityFrame() throws Http2Exception {
    verifyAssociatedWithAStream();
    verifyNotProcessingHeaders();
    if (this.payloadLength != 5)
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyRstStreamFrame() throws Http2Exception {
    verifyAssociatedWithAStream();
    verifyNotProcessingHeaders();
    if (this.payloadLength != 4)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifySettingsFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
    verifyPayloadLength(this.payloadLength);
    if (this.streamId != 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]); 
    if (this.flags.ack() && this.payloadLength > 0)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Ack settings frame must have an empty payload.", new Object[0]); 
    if (this.payloadLength % 6 > 0)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d invalid.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyPushPromiseFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
    verifyPayloadLength(this.payloadLength);
    int minLength = this.flags.getPaddingPresenceFieldLength() + 4;
    if (this.payloadLength < minLength)
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyPingFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
    if (this.streamId != 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]); 
    if (this.payloadLength != 8)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d incorrect size for ping.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyGoAwayFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
    verifyPayloadLength(this.payloadLength);
    if (this.streamId != 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]); 
    if (this.payloadLength < 8)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyWindowUpdateFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
    verifyStreamOrConnectionId(this.streamId, "Stream ID");
    if (this.payloadLength != 4)
      throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyContinuationFrame() throws Http2Exception {
    verifyAssociatedWithAStream();
    verifyPayloadLength(this.payloadLength);
    if (this.headersContinuation == null)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received %s frame but not currently processing headers.", new Object[] { Byte.valueOf(this.frameType) }); 
    if (this.streamId != this.headersContinuation.getStreamId())
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Continuation stream ID does not match pending headers. Expected %d, but received %d.", new Object[] { Integer.valueOf(this.headersContinuation.getStreamId()), Integer.valueOf(this.streamId) }); 
    if (this.payloadLength < this.flags.getPaddingPresenceFieldLength())
      throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small for padding.", new Object[] { Integer.valueOf(this.payloadLength) }); 
  }
  
  private void verifyUnknownFrame() throws Http2Exception {
    verifyNotProcessingHeaders();
  }
  
  private void readDataFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    int padding = readPadding(payload);
    verifyPadding(padding);
    int dataLength = lengthWithoutTrailingPadding(payload.readableBytes(), padding);
    ByteBuf data = payload.readSlice(dataLength);
    listener.onDataRead(ctx, this.streamId, data, padding, this.flags.endOfStream());
    payload.skipBytes(payload.readableBytes());
  }
  
  private void readHeadersFrame(final ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    final int headersStreamId = this.streamId;
    final Http2Flags headersFlags = this.flags;
    final int padding = readPadding(payload);
    verifyPadding(padding);
    if (this.flags.priorityPresent()) {
      long word1 = payload.readUnsignedInt();
      final boolean exclusive = ((word1 & 0x80000000L) != 0L);
      final int streamDependency = (int)(word1 & 0x7FFFFFFFL);
      if (streamDependency == this.streamId)
        throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "A stream cannot depend on itself.", new Object[0]); 
      final short weight = (short)(payload.readUnsignedByte() + 1);
      ByteBuf byteBuf = payload.readSlice(lengthWithoutTrailingPadding(payload.readableBytes(), padding));
      this.headersContinuation = new HeadersContinuation() {
          public int getStreamId() {
            return headersStreamId;
          }
          
          public void processFragment(boolean endOfHeaders, ByteBuf fragment, Http2FrameListener listener) throws Http2Exception {
            DefaultHttp2FrameReader.HeadersBlockBuilder hdrBlockBuilder = headersBlockBuilder();
            hdrBlockBuilder.addFragment(fragment, ctx.alloc(), endOfHeaders);
            if (endOfHeaders)
              listener.onHeadersRead(ctx, headersStreamId, hdrBlockBuilder.headers(), streamDependency, weight, exclusive, padding, headersFlags
                  .endOfStream()); 
          }
        };
      this.headersContinuation.processFragment(this.flags.endOfHeaders(), byteBuf, listener);
      resetHeadersContinuationIfEnd(this.flags.endOfHeaders());
      return;
    } 
    this.headersContinuation = new HeadersContinuation() {
        public int getStreamId() {
          return headersStreamId;
        }
        
        public void processFragment(boolean endOfHeaders, ByteBuf fragment, Http2FrameListener listener) throws Http2Exception {
          DefaultHttp2FrameReader.HeadersBlockBuilder hdrBlockBuilder = headersBlockBuilder();
          hdrBlockBuilder.addFragment(fragment, ctx.alloc(), endOfHeaders);
          if (endOfHeaders)
            listener.onHeadersRead(ctx, headersStreamId, hdrBlockBuilder.headers(), padding, headersFlags
                .endOfStream()); 
        }
      };
    ByteBuf fragment = payload.readSlice(lengthWithoutTrailingPadding(payload.readableBytes(), padding));
    this.headersContinuation.processFragment(this.flags.endOfHeaders(), fragment, listener);
    resetHeadersContinuationIfEnd(this.flags.endOfHeaders());
  }
  
  private void resetHeadersContinuationIfEnd(boolean endOfHeaders) {
    if (endOfHeaders)
      closeHeadersContinuation(); 
  }
  
  private void readPriorityFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    long word1 = payload.readUnsignedInt();
    boolean exclusive = ((word1 & 0x80000000L) != 0L);
    int streamDependency = (int)(word1 & 0x7FFFFFFFL);
    if (streamDependency == this.streamId)
      throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "A stream cannot depend on itself.", new Object[0]); 
    short weight = (short)(payload.readUnsignedByte() + 1);
    listener.onPriorityRead(ctx, this.streamId, streamDependency, weight, exclusive);
  }
  
  private void readRstStreamFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    long errorCode = payload.readUnsignedInt();
    listener.onRstStreamRead(ctx, this.streamId, errorCode);
  }
  
  private void readSettingsFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    if (this.flags.ack()) {
      listener.onSettingsAckRead(ctx);
    } else {
      int numSettings = this.payloadLength / 6;
      Http2Settings settings = new Http2Settings();
      for (int index = 0; index < numSettings; index++) {
        char id = (char)payload.readUnsignedShort();
        long value = payload.readUnsignedInt();
        try {
          settings.put(id, Long.valueOf(value));
        } catch (IllegalArgumentException e) {
          switch (id) {
            case '\005':
              throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, e, e.getMessage(), new Object[0]);
            case '\004':
              throw Http2Exception.connectionError(Http2Error.FLOW_CONTROL_ERROR, e, e.getMessage(), new Object[0]);
          } 
          throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, e, e.getMessage(), new Object[0]);
        } 
      } 
      listener.onSettingsRead(ctx, settings);
    } 
  }
  
  private void readPushPromiseFrame(final ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    final int pushPromiseStreamId = this.streamId;
    final int padding = readPadding(payload);
    verifyPadding(padding);
    final int promisedStreamId = Http2CodecUtil.readUnsignedInt(payload);
    this.headersContinuation = new HeadersContinuation() {
        public int getStreamId() {
          return pushPromiseStreamId;
        }
        
        public void processFragment(boolean endOfHeaders, ByteBuf fragment, Http2FrameListener listener) throws Http2Exception {
          headersBlockBuilder().addFragment(fragment, ctx.alloc(), endOfHeaders);
          if (endOfHeaders)
            listener.onPushPromiseRead(ctx, pushPromiseStreamId, promisedStreamId, 
                headersBlockBuilder().headers(), padding); 
        }
      };
    ByteBuf fragment = payload.readSlice(lengthWithoutTrailingPadding(payload.readableBytes(), padding));
    this.headersContinuation.processFragment(this.flags.endOfHeaders(), fragment, listener);
    resetHeadersContinuationIfEnd(this.flags.endOfHeaders());
  }
  
  private void readPingFrame(ChannelHandlerContext ctx, long data, Http2FrameListener listener) throws Http2Exception {
    if (this.flags.ack()) {
      listener.onPingAckRead(ctx, data);
    } else {
      listener.onPingRead(ctx, data);
    } 
  }
  
  private static void readGoAwayFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    int lastStreamId = Http2CodecUtil.readUnsignedInt(payload);
    long errorCode = payload.readUnsignedInt();
    ByteBuf debugData = payload.readSlice(payload.readableBytes());
    listener.onGoAwayRead(ctx, lastStreamId, errorCode, debugData);
  }
  
  private void readWindowUpdateFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    int windowSizeIncrement = Http2CodecUtil.readUnsignedInt(payload);
    if (windowSizeIncrement == 0)
      throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "Received WINDOW_UPDATE with delta 0 for stream: %d", new Object[] { Integer.valueOf(this.streamId) }); 
    listener.onWindowUpdateRead(ctx, this.streamId, windowSizeIncrement);
  }
  
  private void readContinuationFrame(ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    ByteBuf continuationFragment = payload.readSlice(payload.readableBytes());
    this.headersContinuation.processFragment(this.flags.endOfHeaders(), continuationFragment, listener);
    resetHeadersContinuationIfEnd(this.flags.endOfHeaders());
  }
  
  private void readUnknownFrame(ChannelHandlerContext ctx, ByteBuf payload, Http2FrameListener listener) throws Http2Exception {
    payload = payload.readSlice(payload.readableBytes());
    listener.onUnknownFrame(ctx, this.frameType, this.streamId, this.flags, payload);
  }
  
  private int readPadding(ByteBuf payload) {
    if (!this.flags.paddingPresent())
      return 0; 
    return payload.readUnsignedByte() + 1;
  }
  
  private void verifyPadding(int padding) throws Http2Exception {
    int len = lengthWithoutTrailingPadding(this.payloadLength, padding);
    if (len < 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Frame payload too small for padding.", new Object[0]); 
  }
  
  private static int lengthWithoutTrailingPadding(int readableBytes, int padding) {
    return (padding == 0) ? readableBytes : (readableBytes - padding - 1);
  }
  
  private abstract class HeadersContinuation {
    private final DefaultHttp2FrameReader.HeadersBlockBuilder builder = new DefaultHttp2FrameReader.HeadersBlockBuilder();
    
    final DefaultHttp2FrameReader.HeadersBlockBuilder headersBlockBuilder() {
      return this.builder;
    }
    
    final void close() {
      this.builder.close();
    }
    
    private HeadersContinuation() {}
    
    abstract int getStreamId();
    
    abstract void processFragment(boolean param1Boolean, ByteBuf param1ByteBuf, Http2FrameListener param1Http2FrameListener) throws Http2Exception;
  }
  
  protected class HeadersBlockBuilder {
    private ByteBuf headerBlock;
    
    private void headerSizeExceeded() throws Http2Exception {
      close();
      Http2CodecUtil.headerListSizeExceeded(DefaultHttp2FrameReader.this.headersDecoder.configuration().maxHeaderListSizeGoAway());
    }
    
    final void addFragment(ByteBuf fragment, ByteBufAllocator alloc, boolean endOfHeaders) throws Http2Exception {
      if (this.headerBlock == null) {
        if (fragment.readableBytes() > DefaultHttp2FrameReader.this.headersDecoder.configuration().maxHeaderListSizeGoAway())
          headerSizeExceeded(); 
        if (endOfHeaders) {
          this.headerBlock = fragment.retain();
        } else {
          this.headerBlock = alloc.buffer(fragment.readableBytes());
          this.headerBlock.writeBytes(fragment);
        } 
        return;
      } 
      if (DefaultHttp2FrameReader.this.headersDecoder.configuration().maxHeaderListSizeGoAway() - fragment.readableBytes() < this.headerBlock
        .readableBytes())
        headerSizeExceeded(); 
      if (this.headerBlock.isWritable(fragment.readableBytes())) {
        this.headerBlock.writeBytes(fragment);
      } else {
        ByteBuf buf = alloc.buffer(this.headerBlock.readableBytes() + fragment.readableBytes());
        buf.writeBytes(this.headerBlock);
        buf.writeBytes(fragment);
        this.headerBlock.release();
        this.headerBlock = buf;
      } 
    }
    
    Http2Headers headers() throws Http2Exception {
      try {
        return DefaultHttp2FrameReader.this.headersDecoder.decodeHeaders(DefaultHttp2FrameReader.this.streamId, this.headerBlock);
      } finally {
        close();
      } 
    }
    
    void close() {
      if (this.headerBlock != null) {
        this.headerBlock.release();
        this.headerBlock = null;
      } 
      DefaultHttp2FrameReader.this.headersContinuation = null;
    }
  }
  
  private void verifyNotProcessingHeaders() throws Http2Exception {
    if (this.headersContinuation != null)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received frame of type %s while processing headers on stream %d.", new Object[] { Byte.valueOf(this.frameType), Integer.valueOf(this.headersContinuation.getStreamId()) }); 
  }
  
  private void verifyPayloadLength(int payloadLength) throws Http2Exception {
    if (payloadLength > this.maxFrameSize)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Total payload length %d exceeds max frame length.", new Object[] { Integer.valueOf(payloadLength) }); 
  }
  
  private void verifyAssociatedWithAStream() throws Http2Exception {
    if (this.streamId == 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Frame of type %s must be associated with a stream.", new Object[] { Byte.valueOf(this.frameType) }); 
  }
  
  private static void verifyStreamOrConnectionId(int streamId, String argumentName) throws Http2Exception {
    if (streamId < 0)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "%s must be >= 0", new Object[] { argumentName }); 
  }
}
