package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.compression.ZlibCodecFactory;
import com.github.steveice10.netty.handler.codec.compression.ZlibWrapper;
import com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import com.github.steveice10.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.github.steveice10.netty.handler.codec.http.websocketx.WebSocketFrame;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import java.util.List;

abstract class DeflateEncoder extends WebSocketExtensionEncoder {
  private final int compressionLevel;
  
  private final int windowSize;
  
  private final boolean noContext;
  
  private EmbeddedChannel encoder;
  
  public DeflateEncoder(int compressionLevel, int windowSize, boolean noContext) {
    this.compressionLevel = compressionLevel;
    this.windowSize = windowSize;
    this.noContext = noContext;
  }
  
  protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
    CompositeByteBuf compositeByteBuf1;
    ContinuationWebSocketFrame continuationWebSocketFrame;
    if (this.encoder == null)
      this.encoder = new EmbeddedChannel(new ChannelHandler[] { (ChannelHandler)ZlibCodecFactory.newZlibEncoder(ZlibWrapper.NONE, this.compressionLevel, this.windowSize, 8) }); 
    this.encoder.writeOutbound(new Object[] { msg.content().retain() });
    CompositeByteBuf fullCompressedContent = ctx.alloc().compositeBuffer();
    while (true) {
      ByteBuf partCompressedContent = (ByteBuf)this.encoder.readOutbound();
      if (partCompressedContent == null)
        break; 
      if (!partCompressedContent.isReadable()) {
        partCompressedContent.release();
        continue;
      } 
      fullCompressedContent.addComponent(true, partCompressedContent);
    } 
    if (fullCompressedContent.numComponents() <= 0) {
      fullCompressedContent.release();
      throw new CodecException("cannot read compressed buffer");
    } 
    if (msg.isFinalFragment() && this.noContext)
      cleanup(); 
    if (removeFrameTail(msg)) {
      int realLength = fullCompressedContent.readableBytes() - PerMessageDeflateDecoder.FRAME_TAIL.length;
      ByteBuf compressedContent = fullCompressedContent.slice(0, realLength);
    } else {
      compositeByteBuf1 = fullCompressedContent;
    } 
    if (msg instanceof TextWebSocketFrame) {
      TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg.isFinalFragment(), rsv(msg), (ByteBuf)compositeByteBuf1);
    } else if (msg instanceof BinaryWebSocketFrame) {
      BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(msg.isFinalFragment(), rsv(msg), (ByteBuf)compositeByteBuf1);
    } else if (msg instanceof ContinuationWebSocketFrame) {
      continuationWebSocketFrame = new ContinuationWebSocketFrame(msg.isFinalFragment(), rsv(msg), (ByteBuf)compositeByteBuf1);
    } else {
      throw new CodecException("unexpected frame type: " + msg.getClass().getName());
    } 
    out.add(continuationWebSocketFrame);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    cleanup();
    super.handlerRemoved(ctx);
  }
  
  private void cleanup() {
    if (this.encoder != null) {
      if (this.encoder.finish())
        while (true) {
          ByteBuf buf = (ByteBuf)this.encoder.readOutbound();
          if (buf == null)
            break; 
          buf.release();
        }  
      this.encoder = null;
    } 
  }
  
  protected abstract int rsv(WebSocketFrame paramWebSocketFrame);
  
  protected abstract boolean removeFrameTail(WebSocketFrame paramWebSocketFrame);
}
