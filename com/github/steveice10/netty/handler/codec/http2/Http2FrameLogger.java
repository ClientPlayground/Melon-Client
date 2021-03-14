package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.logging.LogLevel;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogLevel;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

public class Http2FrameLogger extends ChannelHandlerAdapter {
  private static final int BUFFER_LENGTH_THRESHOLD = 64;
  
  private final InternalLogger logger;
  
  private final InternalLogLevel level;
  
  public enum Direction {
    INBOUND, OUTBOUND;
  }
  
  public Http2FrameLogger(LogLevel level) {
    this(level.toInternalLevel(), InternalLoggerFactory.getInstance(Http2FrameLogger.class));
  }
  
  public Http2FrameLogger(LogLevel level, String name) {
    this(level.toInternalLevel(), InternalLoggerFactory.getInstance(name));
  }
  
  public Http2FrameLogger(LogLevel level, Class<?> clazz) {
    this(level.toInternalLevel(), InternalLoggerFactory.getInstance(clazz));
  }
  
  private Http2FrameLogger(InternalLogLevel level, InternalLogger logger) {
    this.level = (InternalLogLevel)ObjectUtil.checkNotNull(level, "level");
    this.logger = (InternalLogger)ObjectUtil.checkNotNull(logger, "logger");
  }
  
  public void logData(Direction direction, ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endStream) {
    this.logger.log(this.level, "{} {} DATA: streamId={} padding={} endStream={} length={} bytes={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), Integer.valueOf(padding), Boolean.valueOf(endStream), Integer.valueOf(data.readableBytes()), toString(data) });
  }
  
  public void logHeaders(Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) {
    this.logger.log(this.level, "{} {} HEADERS: streamId={} headers={} padding={} endStream={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), headers, Integer.valueOf(padding), Boolean.valueOf(endStream) });
  }
  
  public void logHeaders(Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) {
    this.logger.log(this.level, "{} {} HEADERS: streamId={} headers={} streamDependency={} weight={} exclusive={} padding={} endStream={}", new Object[] { ctx
          .channel(), direction
          .name(), Integer.valueOf(streamId), headers, Integer.valueOf(streamDependency), Short.valueOf(weight), Boolean.valueOf(exclusive), Integer.valueOf(padding), Boolean.valueOf(endStream) });
  }
  
  public void logPriority(Direction direction, ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) {
    this.logger.log(this.level, "{} {} PRIORITY: streamId={} streamDependency={} weight={} exclusive={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), Integer.valueOf(streamDependency), Short.valueOf(weight), Boolean.valueOf(exclusive) });
  }
  
  public void logRstStream(Direction direction, ChannelHandlerContext ctx, int streamId, long errorCode) {
    this.logger.log(this.level, "{} {} RST_STREAM: streamId={} errorCode={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), Long.valueOf(errorCode) });
  }
  
  public void logSettingsAck(Direction direction, ChannelHandlerContext ctx) {
    this.logger.log(this.level, "{} {} SETTINGS: ack=true", ctx.channel(), direction.name());
  }
  
  public void logSettings(Direction direction, ChannelHandlerContext ctx, Http2Settings settings) {
    this.logger.log(this.level, "{} {} SETTINGS: ack=false settings={}", new Object[] { ctx.channel(), direction.name(), settings });
  }
  
  public void logPing(Direction direction, ChannelHandlerContext ctx, long data) {
    this.logger.log(this.level, "{} {} PING: ack=false bytes={}", new Object[] { ctx.channel(), direction
          .name(), Long.valueOf(data) });
  }
  
  public void logPingAck(Direction direction, ChannelHandlerContext ctx, long data) {
    this.logger.log(this.level, "{} {} PING: ack=true bytes={}", new Object[] { ctx.channel(), direction
          .name(), Long.valueOf(data) });
  }
  
  public void logPushPromise(Direction direction, ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) {
    this.logger.log(this.level, "{} {} PUSH_PROMISE: streamId={} promisedStreamId={} headers={} padding={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), Integer.valueOf(promisedStreamId), headers, Integer.valueOf(padding) });
  }
  
  public void logGoAway(Direction direction, ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
    this.logger.log(this.level, "{} {} GO_AWAY: lastStreamId={} errorCode={} length={} bytes={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(lastStreamId), Long.valueOf(errorCode), Integer.valueOf(debugData.readableBytes()), toString(debugData) });
  }
  
  public void logWindowsUpdate(Direction direction, ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
    this.logger.log(this.level, "{} {} WINDOW_UPDATE: streamId={} windowSizeIncrement={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(streamId), Integer.valueOf(windowSizeIncrement) });
  }
  
  public void logUnknownFrame(Direction direction, ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf data) {
    this.logger.log(this.level, "{} {} UNKNOWN: frameType={} streamId={} flags={} length={} bytes={}", new Object[] { ctx.channel(), direction
          .name(), Integer.valueOf(frameType & 0xFF), Integer.valueOf(streamId), Short.valueOf(flags.value()), Integer.valueOf(data.readableBytes()), toString(data) });
  }
  
  private String toString(ByteBuf buf) {
    if (!this.logger.isEnabled(this.level))
      return ""; 
    if (this.level == InternalLogLevel.TRACE || buf.readableBytes() <= 64)
      return ByteBufUtil.hexDump(buf); 
    int length = Math.min(buf.readableBytes(), 64);
    return ByteBufUtil.hexDump(buf, buf.readerIndex(), length) + "...";
  }
}
