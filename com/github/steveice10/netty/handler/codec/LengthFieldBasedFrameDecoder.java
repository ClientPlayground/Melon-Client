package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.nio.ByteOrder;
import java.util.List;

public class LengthFieldBasedFrameDecoder extends ByteToMessageDecoder {
  private final ByteOrder byteOrder;
  
  private final int maxFrameLength;
  
  private final int lengthFieldOffset;
  
  private final int lengthFieldLength;
  
  private final int lengthFieldEndOffset;
  
  private final int lengthAdjustment;
  
  private final int initialBytesToStrip;
  
  private final boolean failFast;
  
  private boolean discardingTooLongFrame;
  
  private long tooLongFrameLength;
  
  private long bytesToDiscard;
  
  public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
    this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
  }
  
  public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
    this(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true);
  }
  
  public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
    this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
  }
  
  public LengthFieldBasedFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
    if (byteOrder == null)
      throw new NullPointerException("byteOrder"); 
    if (maxFrameLength <= 0)
      throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength); 
    if (lengthFieldOffset < 0)
      throw new IllegalArgumentException("lengthFieldOffset must be a non-negative integer: " + lengthFieldOffset); 
    if (initialBytesToStrip < 0)
      throw new IllegalArgumentException("initialBytesToStrip must be a non-negative integer: " + initialBytesToStrip); 
    if (lengthFieldOffset > maxFrameLength - lengthFieldLength)
      throw new IllegalArgumentException("maxFrameLength (" + maxFrameLength + ") must be equal to or greater than lengthFieldOffset (" + lengthFieldOffset + ") + lengthFieldLength (" + lengthFieldLength + ")."); 
    this.byteOrder = byteOrder;
    this.maxFrameLength = maxFrameLength;
    this.lengthFieldOffset = lengthFieldOffset;
    this.lengthFieldLength = lengthFieldLength;
    this.lengthAdjustment = lengthAdjustment;
    this.lengthFieldEndOffset = lengthFieldOffset + lengthFieldLength;
    this.initialBytesToStrip = initialBytesToStrip;
    this.failFast = failFast;
  }
  
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object decoded = decode(ctx, in);
    if (decoded != null)
      out.add(decoded); 
  }
  
  private void discardingTooLongFrame(ByteBuf in) {
    long bytesToDiscard = this.bytesToDiscard;
    int localBytesToDiscard = (int)Math.min(bytesToDiscard, in.readableBytes());
    in.skipBytes(localBytesToDiscard);
    bytesToDiscard -= localBytesToDiscard;
    this.bytesToDiscard = bytesToDiscard;
    failIfNecessary(false);
  }
  
  private static void failOnNegativeLengthField(ByteBuf in, long frameLength, int lengthFieldEndOffset) {
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
  }
  
  private static void failOnFrameLengthLessThanLengthFieldEndOffset(ByteBuf in, long frameLength, int lengthFieldEndOffset) {
    in.skipBytes(lengthFieldEndOffset);
    throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less than lengthFieldEndOffset: " + lengthFieldEndOffset);
  }
  
  private void exceededFrameLength(ByteBuf in, long frameLength) {
    long discard = frameLength - in.readableBytes();
    this.tooLongFrameLength = frameLength;
    if (discard < 0L) {
      in.skipBytes((int)frameLength);
    } else {
      this.discardingTooLongFrame = true;
      this.bytesToDiscard = discard;
      in.skipBytes(in.readableBytes());
    } 
    failIfNecessary(true);
  }
  
  private static void failOnFrameLengthLessThanInitialBytesToStrip(ByteBuf in, long frameLength, int initialBytesToStrip) {
    in.skipBytes((int)frameLength);
    throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less than initialBytesToStrip: " + initialBytesToStrip);
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    if (this.discardingTooLongFrame)
      discardingTooLongFrame(in); 
    if (in.readableBytes() < this.lengthFieldEndOffset)
      return null; 
    int actualLengthFieldOffset = in.readerIndex() + this.lengthFieldOffset;
    long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, this.lengthFieldLength, this.byteOrder);
    if (frameLength < 0L)
      failOnNegativeLengthField(in, frameLength, this.lengthFieldEndOffset); 
    frameLength += (this.lengthAdjustment + this.lengthFieldEndOffset);
    if (frameLength < this.lengthFieldEndOffset)
      failOnFrameLengthLessThanLengthFieldEndOffset(in, frameLength, this.lengthFieldEndOffset); 
    if (frameLength > this.maxFrameLength) {
      exceededFrameLength(in, frameLength);
      return null;
    } 
    int frameLengthInt = (int)frameLength;
    if (in.readableBytes() < frameLengthInt)
      return null; 
    if (this.initialBytesToStrip > frameLengthInt)
      failOnFrameLengthLessThanInitialBytesToStrip(in, frameLength, this.initialBytesToStrip); 
    in.skipBytes(this.initialBytesToStrip);
    int readerIndex = in.readerIndex();
    int actualFrameLength = frameLengthInt - this.initialBytesToStrip;
    ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
    in.readerIndex(readerIndex + actualFrameLength);
    return frame;
  }
  
  protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
    long frameLength;
    buf = buf.order(order);
    switch (length) {
      case 1:
        frameLength = buf.getUnsignedByte(offset);
        return frameLength;
      case 2:
        frameLength = buf.getUnsignedShort(offset);
        return frameLength;
      case 3:
        frameLength = buf.getUnsignedMedium(offset);
        return frameLength;
      case 4:
        frameLength = buf.getUnsignedInt(offset);
        return frameLength;
      case 8:
        frameLength = buf.getLong(offset);
        return frameLength;
    } 
    throw new DecoderException("unsupported lengthFieldLength: " + this.lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
  }
  
  private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
    if (this.bytesToDiscard == 0L) {
      long tooLongFrameLength = this.tooLongFrameLength;
      this.tooLongFrameLength = 0L;
      this.discardingTooLongFrame = false;
      if (!this.failFast || firstDetectionOfTooLongFrame)
        fail(tooLongFrameLength); 
    } else if (this.failFast && firstDetectionOfTooLongFrame) {
      fail(this.tooLongFrameLength);
    } 
  }
  
  protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
    return buffer.retainedSlice(index, length);
  }
  
  private void fail(long frameLength) {
    if (frameLength > 0L)
      throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded"); 
    throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + " - discarding");
  }
}
