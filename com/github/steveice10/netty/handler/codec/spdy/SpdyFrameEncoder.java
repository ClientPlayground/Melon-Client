package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import java.nio.ByteOrder;
import java.util.Set;

public class SpdyFrameEncoder {
  private final int version;
  
  public SpdyFrameEncoder(SpdyVersion spdyVersion) {
    if (spdyVersion == null)
      throw new NullPointerException("spdyVersion"); 
    this.version = spdyVersion.getVersion();
  }
  
  private void writeControlFrameHeader(ByteBuf buffer, int type, byte flags, int length) {
    buffer.writeShort(this.version | 0x8000);
    buffer.writeShort(type);
    buffer.writeByte(flags);
    buffer.writeMedium(length);
  }
  
  public ByteBuf encodeDataFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf data) {
    byte flags = last ? 1 : 0;
    int length = data.readableBytes();
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    frame.writeInt(streamId & Integer.MAX_VALUE);
    frame.writeByte(flags);
    frame.writeMedium(length);
    frame.writeBytes(data, data.readerIndex(), length);
    return frame;
  }
  
  public ByteBuf encodeSynStreamFrame(ByteBufAllocator allocator, int streamId, int associatedToStreamId, byte priority, boolean last, boolean unidirectional, ByteBuf headerBlock) {
    int headerBlockLength = headerBlock.readableBytes();
    byte flags = last ? 1 : 0;
    if (unidirectional)
      flags = (byte)(flags | 0x2); 
    int length = 10 + headerBlockLength;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 1, flags, length);
    frame.writeInt(streamId);
    frame.writeInt(associatedToStreamId);
    frame.writeShort((priority & 0xFF) << 13);
    frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
    return frame;
  }
  
  public ByteBuf encodeSynReplyFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf headerBlock) {
    int headerBlockLength = headerBlock.readableBytes();
    byte flags = last ? 1 : 0;
    int length = 4 + headerBlockLength;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 2, flags, length);
    frame.writeInt(streamId);
    frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
    return frame;
  }
  
  public ByteBuf encodeRstStreamFrame(ByteBufAllocator allocator, int streamId, int statusCode) {
    byte flags = 0;
    int length = 8;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 3, flags, length);
    frame.writeInt(streamId);
    frame.writeInt(statusCode);
    return frame;
  }
  
  public ByteBuf encodeSettingsFrame(ByteBufAllocator allocator, SpdySettingsFrame spdySettingsFrame) {
    Set<Integer> ids = spdySettingsFrame.ids();
    int numSettings = ids.size();
    byte flags = spdySettingsFrame.clearPreviouslyPersistedSettings() ? 1 : 0;
    int length = 4 + 8 * numSettings;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 4, flags, length);
    frame.writeInt(numSettings);
    for (Integer id : ids) {
      flags = 0;
      if (spdySettingsFrame.isPersistValue(id.intValue()))
        flags = (byte)(flags | 0x1); 
      if (spdySettingsFrame.isPersisted(id.intValue()))
        flags = (byte)(flags | 0x2); 
      frame.writeByte(flags);
      frame.writeMedium(id.intValue());
      frame.writeInt(spdySettingsFrame.getValue(id.intValue()));
    } 
    return frame;
  }
  
  public ByteBuf encodePingFrame(ByteBufAllocator allocator, int id) {
    byte flags = 0;
    int length = 4;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 6, flags, length);
    frame.writeInt(id);
    return frame;
  }
  
  public ByteBuf encodeGoAwayFrame(ByteBufAllocator allocator, int lastGoodStreamId, int statusCode) {
    byte flags = 0;
    int length = 8;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 7, flags, length);
    frame.writeInt(lastGoodStreamId);
    frame.writeInt(statusCode);
    return frame;
  }
  
  public ByteBuf encodeHeadersFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf headerBlock) {
    int headerBlockLength = headerBlock.readableBytes();
    byte flags = last ? 1 : 0;
    int length = 4 + headerBlockLength;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 8, flags, length);
    frame.writeInt(streamId);
    frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
    return frame;
  }
  
  public ByteBuf encodeWindowUpdateFrame(ByteBufAllocator allocator, int streamId, int deltaWindowSize) {
    byte flags = 0;
    int length = 8;
    ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
    writeControlFrameHeader(frame, 9, flags, length);
    frame.writeInt(streamId);
    frame.writeInt(deltaWindowSize);
    return frame;
  }
}
