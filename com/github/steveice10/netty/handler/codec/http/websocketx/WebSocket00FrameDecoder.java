package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.TooLongFrameException;
import java.util.List;

public class WebSocket00FrameDecoder extends ReplayingDecoder<Void> implements WebSocketFrameDecoder {
  static final int DEFAULT_MAX_FRAME_SIZE = 16384;
  
  private final long maxFrameSize;
  
  private boolean receivedClosingHandshake;
  
  public WebSocket00FrameDecoder() {
    this(16384);
  }
  
  public WebSocket00FrameDecoder(int maxFrameSize) {
    this.maxFrameSize = maxFrameSize;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    WebSocketFrame frame;
    if (this.receivedClosingHandshake) {
      in.skipBytes(actualReadableBytes());
      return;
    } 
    byte type = in.readByte();
    if ((type & 0x80) == 128) {
      frame = decodeBinaryFrame(ctx, type, in);
    } else {
      frame = decodeTextFrame(ctx, in);
    } 
    if (frame != null)
      out.add(frame); 
  }
  
  private WebSocketFrame decodeBinaryFrame(ChannelHandlerContext ctx, byte type, ByteBuf buffer) {
    byte b;
    long frameSize = 0L;
    int lengthFieldSize = 0;
    do {
      b = buffer.readByte();
      frameSize <<= 7L;
      frameSize |= (b & Byte.MAX_VALUE);
      if (frameSize > this.maxFrameSize)
        throw new TooLongFrameException(); 
      lengthFieldSize++;
      if (lengthFieldSize > 8)
        throw new TooLongFrameException(); 
    } while ((b & 0x80) == 128);
    if (type == -1 && frameSize == 0L) {
      this.receivedClosingHandshake = true;
      return new CloseWebSocketFrame();
    } 
    ByteBuf payload = ByteBufUtil.readBytes(ctx.alloc(), buffer, (int)frameSize);
    return new BinaryWebSocketFrame(payload);
  }
  
  private WebSocketFrame decodeTextFrame(ChannelHandlerContext ctx, ByteBuf buffer) {
    int ridx = buffer.readerIndex();
    int rbytes = actualReadableBytes();
    int delimPos = buffer.indexOf(ridx, ridx + rbytes, (byte)-1);
    if (delimPos == -1) {
      if (rbytes > this.maxFrameSize)
        throw new TooLongFrameException(); 
      return null;
    } 
    int frameSize = delimPos - ridx;
    if (frameSize > this.maxFrameSize)
      throw new TooLongFrameException(); 
    ByteBuf binaryData = ByteBufUtil.readBytes(ctx.alloc(), buffer, frameSize);
    buffer.skipBytes(1);
    int ffDelimPos = binaryData.indexOf(binaryData.readerIndex(), binaryData.writerIndex(), (byte)-1);
    if (ffDelimPos >= 0) {
      binaryData.release();
      throw new IllegalArgumentException("a text frame should not contain 0xFF.");
    } 
    return new TextWebSocketFrame(binaryData);
  }
}
