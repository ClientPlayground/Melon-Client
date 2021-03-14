package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.handler.codec.TooLongFrameException;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteOrder;
import java.util.List;

public class WebSocket08FrameDecoder extends ByteToMessageDecoder implements WebSocketFrameDecoder {
  enum State {
    READING_FIRST, READING_SECOND, READING_SIZE, MASKING_KEY, PAYLOAD, CORRUPT;
  }
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocket08FrameDecoder.class);
  
  private static final byte OPCODE_CONT = 0;
  
  private static final byte OPCODE_TEXT = 1;
  
  private static final byte OPCODE_BINARY = 2;
  
  private static final byte OPCODE_CLOSE = 8;
  
  private static final byte OPCODE_PING = 9;
  
  private static final byte OPCODE_PONG = 10;
  
  private final long maxFramePayloadLength;
  
  private final boolean allowExtensions;
  
  private final boolean expectMaskedFrames;
  
  private final boolean allowMaskMismatch;
  
  private int fragmentedFramesCount;
  
  private boolean frameFinalFlag;
  
  private boolean frameMasked;
  
  private int frameRsv;
  
  private int frameOpcode;
  
  private long framePayloadLength;
  
  private byte[] maskingKey;
  
  private int framePayloadLen1;
  
  private boolean receivedClosingHandshake;
  
  private State state = State.READING_FIRST;
  
  public WebSocket08FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength) {
    this(expectMaskedFrames, allowExtensions, maxFramePayloadLength, false);
  }
  
  public WebSocket08FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength, boolean allowMaskMismatch) {
    this.expectMaskedFrames = expectMaskedFrames;
    this.allowMaskMismatch = allowMaskMismatch;
    this.allowExtensions = allowExtensions;
    this.maxFramePayloadLength = maxFramePayloadLength;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    byte b;
    ByteBuf payloadBuffer;
    if (this.receivedClosingHandshake) {
      in.skipBytes(actualReadableBytes());
      return;
    } 
    switch (this.state) {
      case READING_FIRST:
        if (!in.isReadable())
          return; 
        this.framePayloadLength = 0L;
        b = in.readByte();
        this.frameFinalFlag = ((b & 0x80) != 0);
        this.frameRsv = (b & 0x70) >> 4;
        this.frameOpcode = b & 0xF;
        if (logger.isDebugEnabled())
          logger.debug("Decoding WebSocket Frame opCode={}", Integer.valueOf(this.frameOpcode)); 
        this.state = State.READING_SECOND;
      case READING_SECOND:
        if (!in.isReadable())
          return; 
        b = in.readByte();
        this.frameMasked = ((b & 0x80) != 0);
        this.framePayloadLen1 = b & Byte.MAX_VALUE;
        if (this.frameRsv != 0 && !this.allowExtensions) {
          protocolViolation(ctx, "RSV != 0 and no extension negotiated, RSV:" + this.frameRsv);
          return;
        } 
        if (!this.allowMaskMismatch && this.expectMaskedFrames != this.frameMasked) {
          protocolViolation(ctx, "received a frame that is not masked as expected");
          return;
        } 
        if (this.frameOpcode > 7) {
          if (!this.frameFinalFlag) {
            protocolViolation(ctx, "fragmented control frame");
            return;
          } 
          if (this.framePayloadLen1 > 125) {
            protocolViolation(ctx, "control frame with payload length > 125 octets");
            return;
          } 
          if (this.frameOpcode != 8 && this.frameOpcode != 9 && this.frameOpcode != 10) {
            protocolViolation(ctx, "control frame using reserved opcode " + this.frameOpcode);
            return;
          } 
          if (this.frameOpcode == 8 && this.framePayloadLen1 == 1) {
            protocolViolation(ctx, "received close control frame with payload len 1");
            return;
          } 
        } else {
          if (this.frameOpcode != 0 && this.frameOpcode != 1 && this.frameOpcode != 2) {
            protocolViolation(ctx, "data frame using reserved opcode " + this.frameOpcode);
            return;
          } 
          if (this.fragmentedFramesCount == 0 && this.frameOpcode == 0) {
            protocolViolation(ctx, "received continuation data frame outside fragmented message");
            return;
          } 
          if (this.fragmentedFramesCount != 0 && this.frameOpcode != 0 && this.frameOpcode != 9) {
            protocolViolation(ctx, "received non-continuation data frame while inside fragmented message");
            return;
          } 
        } 
        this.state = State.READING_SIZE;
      case READING_SIZE:
        if (this.framePayloadLen1 == 126) {
          if (in.readableBytes() < 2)
            return; 
          this.framePayloadLength = in.readUnsignedShort();
          if (this.framePayloadLength < 126L) {
            protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
            return;
          } 
        } else if (this.framePayloadLen1 == 127) {
          if (in.readableBytes() < 8)
            return; 
          this.framePayloadLength = in.readLong();
          if (this.framePayloadLength < 65536L) {
            protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
            return;
          } 
        } else {
          this.framePayloadLength = this.framePayloadLen1;
        } 
        if (this.framePayloadLength > this.maxFramePayloadLength) {
          protocolViolation(ctx, "Max frame length of " + this.maxFramePayloadLength + " has been exceeded.");
          return;
        } 
        if (logger.isDebugEnabled())
          logger.debug("Decoding WebSocket Frame length={}", Long.valueOf(this.framePayloadLength)); 
        this.state = State.MASKING_KEY;
      case MASKING_KEY:
        if (this.frameMasked) {
          if (in.readableBytes() < 4)
            return; 
          if (this.maskingKey == null)
            this.maskingKey = new byte[4]; 
          in.readBytes(this.maskingKey);
        } 
        this.state = State.PAYLOAD;
      case PAYLOAD:
        if (in.readableBytes() < this.framePayloadLength)
          return; 
        payloadBuffer = null;
        try {
          payloadBuffer = ByteBufUtil.readBytes(ctx.alloc(), in, toFrameLength(this.framePayloadLength));
          this.state = State.READING_FIRST;
          if (this.frameMasked)
            unmask(payloadBuffer); 
          if (this.frameOpcode == 9) {
            out.add(new PingWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          if (this.frameOpcode == 10) {
            out.add(new PongWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          if (this.frameOpcode == 8) {
            this.receivedClosingHandshake = true;
            checkCloseFrameBody(ctx, payloadBuffer);
            out.add(new CloseWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          if (this.frameFinalFlag) {
            if (this.frameOpcode != 9)
              this.fragmentedFramesCount = 0; 
          } else {
            this.fragmentedFramesCount++;
          } 
          if (this.frameOpcode == 1) {
            out.add(new TextWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          if (this.frameOpcode == 2) {
            out.add(new BinaryWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          if (this.frameOpcode == 0) {
            out.add(new ContinuationWebSocketFrame(this.frameFinalFlag, this.frameRsv, payloadBuffer));
            payloadBuffer = null;
            return;
          } 
          throw new UnsupportedOperationException("Cannot decode web socket frame with opcode: " + this.frameOpcode);
        } finally {
          if (payloadBuffer != null)
            payloadBuffer.release(); 
        } 
      case CORRUPT:
        if (in.isReadable())
          in.readByte(); 
        return;
    } 
    throw new Error("Shouldn't reach here.");
  }
  
  private void unmask(ByteBuf frame) {
    int i = frame.readerIndex();
    int end = frame.writerIndex();
    ByteOrder order = frame.order();
    int intMask = (this.maskingKey[0] & 0xFF) << 24 | (this.maskingKey[1] & 0xFF) << 16 | (this.maskingKey[2] & 0xFF) << 8 | this.maskingKey[3] & 0xFF;
    if (order == ByteOrder.LITTLE_ENDIAN)
      intMask = Integer.reverseBytes(intMask); 
    for (; i + 3 < end; i += 4) {
      int unmasked = frame.getInt(i) ^ intMask;
      frame.setInt(i, unmasked);
    } 
    for (; i < end; i++)
      frame.setByte(i, frame.getByte(i) ^ this.maskingKey[i % 4]); 
  }
  
  private void protocolViolation(ChannelHandlerContext ctx, String reason) {
    protocolViolation(ctx, new CorruptedFrameException(reason));
  }
  
  private void protocolViolation(ChannelHandlerContext ctx, CorruptedFrameException ex) {
    this.state = State.CORRUPT;
    if (ctx.channel().isActive()) {
      Object closeMessage;
      if (this.receivedClosingHandshake) {
        closeMessage = Unpooled.EMPTY_BUFFER;
      } else {
        closeMessage = new CloseWebSocketFrame(1002, null);
      } 
      ctx.writeAndFlush(closeMessage).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
    } 
    throw ex;
  }
  
  private static int toFrameLength(long l) {
    if (l > 2147483647L)
      throw new TooLongFrameException("Length:" + l); 
    return (int)l;
  }
  
  protected void checkCloseFrameBody(ChannelHandlerContext ctx, ByteBuf buffer) {
    if (buffer == null || !buffer.isReadable())
      return; 
    if (buffer.readableBytes() == 1)
      protocolViolation(ctx, "Invalid close frame body"); 
    int idx = buffer.readerIndex();
    buffer.readerIndex(0);
    int statusCode = buffer.readShort();
    if ((statusCode >= 0 && statusCode <= 999) || (statusCode >= 1004 && statusCode <= 1006) || (statusCode >= 1012 && statusCode <= 2999))
      protocolViolation(ctx, "Invalid close frame getStatus code: " + statusCode); 
    if (buffer.isReadable())
      try {
        (new Utf8Validator()).check(buffer);
      } catch (CorruptedFrameException ex) {
        protocolViolation(ctx, ex);
      }  
    buffer.readerIndex(idx);
  }
}
