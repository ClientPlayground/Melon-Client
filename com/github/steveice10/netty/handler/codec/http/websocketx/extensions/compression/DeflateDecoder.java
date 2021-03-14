package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
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
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import java.util.List;

abstract class DeflateDecoder extends WebSocketExtensionDecoder {
  static final byte[] FRAME_TAIL = new byte[] { 0, 0, -1, -1 };
  
  private final boolean noContext;
  
  private EmbeddedChannel decoder;
  
  public DeflateDecoder(boolean noContext) {
    this.noContext = noContext;
  }
  
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
    ContinuationWebSocketFrame continuationWebSocketFrame;
    if (this.decoder == null) {
      if (!(msg instanceof TextWebSocketFrame) && !(msg instanceof BinaryWebSocketFrame))
        throw new CodecException("unexpected initial frame type: " + msg.getClass().getName()); 
      this.decoder = new EmbeddedChannel(new ChannelHandler[] { (ChannelHandler)ZlibCodecFactory.newZlibDecoder(ZlibWrapper.NONE) });
    } 
    boolean readable = msg.content().isReadable();
    this.decoder.writeInbound(new Object[] { msg.content().retain() });
    if (appendFrameTail(msg))
      this.decoder.writeInbound(new Object[] { Unpooled.wrappedBuffer(FRAME_TAIL) }); 
    CompositeByteBuf compositeUncompressedContent = ctx.alloc().compositeBuffer();
    while (true) {
      ByteBuf partUncompressedContent = (ByteBuf)this.decoder.readInbound();
      if (partUncompressedContent == null)
        break; 
      if (!partUncompressedContent.isReadable()) {
        partUncompressedContent.release();
        continue;
      } 
      compositeUncompressedContent.addComponent(true, partUncompressedContent);
    } 
    if (readable && compositeUncompressedContent.numComponents() <= 0) {
      compositeUncompressedContent.release();
      throw new CodecException("cannot read uncompressed buffer");
    } 
    if (msg.isFinalFragment() && this.noContext)
      cleanup(); 
    if (msg instanceof TextWebSocketFrame) {
      TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg.isFinalFragment(), newRsv(msg), (ByteBuf)compositeUncompressedContent);
    } else if (msg instanceof BinaryWebSocketFrame) {
      BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(msg.isFinalFragment(), newRsv(msg), (ByteBuf)compositeUncompressedContent);
    } else if (msg instanceof ContinuationWebSocketFrame) {
      continuationWebSocketFrame = new ContinuationWebSocketFrame(msg.isFinalFragment(), newRsv(msg), (ByteBuf)compositeUncompressedContent);
    } else {
      throw new CodecException("unexpected frame type: " + msg.getClass().getName());
    } 
    out.add(continuationWebSocketFrame);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    cleanup();
    super.handlerRemoved(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    cleanup();
    super.channelInactive(ctx);
  }
  
  private void cleanup() {
    if (this.decoder != null) {
      if (this.decoder.finish())
        while (true) {
          ByteBuf buf = (ByteBuf)this.decoder.readOutbound();
          if (buf == null)
            break; 
          buf.release();
        }  
      this.decoder = null;
    } 
  }
  
  protected abstract boolean appendFrameTail(WebSocketFrame paramWebSocketFrame);
  
  protected abstract int newRsv(WebSocketFrame paramWebSocketFrame);
}
