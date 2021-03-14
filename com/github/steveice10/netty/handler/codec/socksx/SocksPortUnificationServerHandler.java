package com.github.steveice10.netty.handler.codec.socksx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4ServerDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4ServerEncoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.List;

public class SocksPortUnificationServerHandler extends ByteToMessageDecoder {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(SocksPortUnificationServerHandler.class);
  
  private final Socks5ServerEncoder socks5encoder;
  
  public SocksPortUnificationServerHandler() {
    this(Socks5ServerEncoder.DEFAULT);
  }
  
  public SocksPortUnificationServerHandler(Socks5ServerEncoder socks5encoder) {
    if (socks5encoder == null)
      throw new NullPointerException("socks5encoder"); 
    this.socks5encoder = socks5encoder;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    int readerIndex = in.readerIndex();
    if (in.writerIndex() == readerIndex)
      return; 
    ChannelPipeline p = ctx.pipeline();
    byte versionVal = in.getByte(readerIndex);
    SocksVersion version = SocksVersion.valueOf(versionVal);
    switch (version) {
      case SOCKS4a:
        logKnownVersion(ctx, version);
        p.addAfter(ctx.name(), null, (ChannelHandler)Socks4ServerEncoder.INSTANCE);
        p.addAfter(ctx.name(), null, (ChannelHandler)new Socks4ServerDecoder());
        break;
      case SOCKS5:
        logKnownVersion(ctx, version);
        p.addAfter(ctx.name(), null, (ChannelHandler)this.socks5encoder);
        p.addAfter(ctx.name(), null, (ChannelHandler)new Socks5InitialRequestDecoder());
        break;
      default:
        logUnknownVersion(ctx, versionVal);
        in.skipBytes(in.readableBytes());
        ctx.close();
        return;
    } 
    p.remove((ChannelHandler)this);
  }
  
  private static void logKnownVersion(ChannelHandlerContext ctx, SocksVersion version) {
    logger.debug("{} Protocol version: {}({})", ctx.channel(), version);
  }
  
  private static void logUnknownVersion(ChannelHandlerContext ctx, byte versionVal) {
    if (logger.isDebugEnabled())
      logger.debug("{} Unknown protocol version: {}", ctx.channel(), Integer.valueOf(versionVal & 0xFF)); 
  }
}
