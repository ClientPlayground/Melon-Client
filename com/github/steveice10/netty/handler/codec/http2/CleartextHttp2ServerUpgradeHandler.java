package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.http.HttpServerCodec;
import com.github.steveice10.netty.handler.codec.http.HttpServerUpgradeHandler;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

public final class CleartextHttp2ServerUpgradeHandler extends ChannelHandlerAdapter {
  private static final ByteBuf CONNECTION_PREFACE = Unpooled.unreleasableBuffer(Http2CodecUtil.connectionPrefaceBuf());
  
  private final HttpServerCodec httpServerCodec;
  
  private final HttpServerUpgradeHandler httpServerUpgradeHandler;
  
  private final ChannelHandler http2ServerHandler;
  
  public CleartextHttp2ServerUpgradeHandler(HttpServerCodec httpServerCodec, HttpServerUpgradeHandler httpServerUpgradeHandler, ChannelHandler http2ServerHandler) {
    this.httpServerCodec = (HttpServerCodec)ObjectUtil.checkNotNull(httpServerCodec, "httpServerCodec");
    this.httpServerUpgradeHandler = (HttpServerUpgradeHandler)ObjectUtil.checkNotNull(httpServerUpgradeHandler, "httpServerUpgradeHandler");
    this.http2ServerHandler = (ChannelHandler)ObjectUtil.checkNotNull(http2ServerHandler, "http2ServerHandler");
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    ctx.pipeline()
      .addBefore(ctx.name(), null, (ChannelHandler)new PriorKnowledgeHandler())
      .addBefore(ctx.name(), null, (ChannelHandler)this.httpServerCodec)
      .replace((ChannelHandler)this, null, (ChannelHandler)this.httpServerUpgradeHandler);
  }
  
  private final class PriorKnowledgeHandler extends ByteToMessageDecoder {
    private PriorKnowledgeHandler() {}
    
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      int prefaceLength = CleartextHttp2ServerUpgradeHandler.CONNECTION_PREFACE.readableBytes();
      int bytesRead = Math.min(in.readableBytes(), prefaceLength);
      if (!ByteBufUtil.equals(CleartextHttp2ServerUpgradeHandler.CONNECTION_PREFACE, CleartextHttp2ServerUpgradeHandler.CONNECTION_PREFACE.readerIndex(), in, in
          .readerIndex(), bytesRead)) {
        ctx.pipeline().remove((ChannelHandler)this);
      } else if (bytesRead == prefaceLength) {
        ctx.pipeline()
          .remove((ChannelHandler)CleartextHttp2ServerUpgradeHandler.this.httpServerCodec)
          .remove((ChannelHandler)CleartextHttp2ServerUpgradeHandler.this.httpServerUpgradeHandler);
        ctx.pipeline().addAfter(ctx.name(), null, CleartextHttp2ServerUpgradeHandler.this.http2ServerHandler);
        ctx.pipeline().remove((ChannelHandler)this);
        ctx.fireUserEventTriggered(CleartextHttp2ServerUpgradeHandler.PriorKnowledgeUpgradeEvent.INSTANCE);
      } 
    }
  }
  
  public static final class PriorKnowledgeUpgradeEvent {
    private static final PriorKnowledgeUpgradeEvent INSTANCE = new PriorKnowledgeUpgradeEvent();
  }
}
