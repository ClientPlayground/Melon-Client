package com.github.steveice10.netty.bootstrap;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.ServerChannel;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerBootstrap.class);
  
  private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap<ChannelOption<?>, Object>();
  
  private final Map<AttributeKey<?>, Object> childAttrs = new LinkedHashMap<AttributeKey<?>, Object>();
  
  private final ServerBootstrapConfig config = new ServerBootstrapConfig(this);
  
  private volatile EventLoopGroup childGroup;
  
  private volatile ChannelHandler childHandler;
  
  private ServerBootstrap(ServerBootstrap bootstrap) {
    super(bootstrap);
    this.childGroup = bootstrap.childGroup;
    this.childHandler = bootstrap.childHandler;
    synchronized (bootstrap.childOptions) {
      this.childOptions.putAll(bootstrap.childOptions);
    } 
    synchronized (bootstrap.childAttrs) {
      this.childAttrs.putAll(bootstrap.childAttrs);
    } 
  }
  
  public ServerBootstrap group(EventLoopGroup group) {
    return group(group, group);
  }
  
  public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
    super.group(parentGroup);
    if (childGroup == null)
      throw new NullPointerException("childGroup"); 
    if (this.childGroup != null)
      throw new IllegalStateException("childGroup set already"); 
    this.childGroup = childGroup;
    return this;
  }
  
  public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value) {
    if (childOption == null)
      throw new NullPointerException("childOption"); 
    if (value == null) {
      synchronized (this.childOptions) {
        this.childOptions.remove(childOption);
      } 
    } else {
      synchronized (this.childOptions) {
        this.childOptions.put(childOption, value);
      } 
    } 
    return this;
  }
  
  public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value) {
    if (childKey == null)
      throw new NullPointerException("childKey"); 
    if (value == null) {
      this.childAttrs.remove(childKey);
    } else {
      this.childAttrs.put(childKey, value);
    } 
    return this;
  }
  
  public ServerBootstrap childHandler(ChannelHandler childHandler) {
    if (childHandler == null)
      throw new NullPointerException("childHandler"); 
    this.childHandler = childHandler;
    return this;
  }
  
  void init(Channel channel) throws Exception {
    final Map.Entry[] currentChildOptions, currentChildAttrs;
    Map<ChannelOption<?>, Object> options = options0();
    synchronized (options) {
      setChannelOptions(channel, options, logger);
    } 
    Map<AttributeKey<?>, Object> attrs = attrs0();
    synchronized (attrs) {
      for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
        AttributeKey<Object> key = (AttributeKey<Object>)e.getKey();
        channel.attr(key).set(e.getValue());
      } 
    } 
    ChannelPipeline p = channel.pipeline();
    final EventLoopGroup currentChildGroup = this.childGroup;
    final ChannelHandler currentChildHandler = this.childHandler;
    synchronized (this.childOptions) {
      arrayOfEntry1 = (Map.Entry[])this.childOptions.entrySet().toArray((Object[])newOptionArray(this.childOptions.size()));
    } 
    synchronized (this.childAttrs) {
      arrayOfEntry2 = (Map.Entry[])this.childAttrs.entrySet().toArray((Object[])newAttrArray(this.childAttrs.size()));
    } 
    p.addLast(new ChannelHandler[] { (ChannelHandler)new ChannelInitializer<Channel>() {
            public void initChannel(final Channel ch) throws Exception {
              final ChannelPipeline pipeline = ch.pipeline();
              ChannelHandler handler = ServerBootstrap.this.config.handler();
              if (handler != null)
                pipeline.addLast(new ChannelHandler[] { handler }); 
              ch.eventLoop().execute(new Runnable() {
                    public void run() {
                      pipeline.addLast(new ChannelHandler[] { (ChannelHandler)new ServerBootstrap.ServerBootstrapAcceptor(this.val$ch, this.this$1.val$currentChildGroup, this.this$1.val$currentChildHandler, (Map.Entry<ChannelOption<?>, Object>[])this.this$1.val$currentChildOptions, (Map.Entry<AttributeKey<?>, Object>[])this.this$1.val$currentChildAttrs) });
                    }
                  });
            }
          } });
  }
  
  public ServerBootstrap validate() {
    super.validate();
    if (this.childHandler == null)
      throw new IllegalStateException("childHandler not set"); 
    if (this.childGroup == null) {
      logger.warn("childGroup is not set. Using parentGroup instead.");
      this.childGroup = this.config.group();
    } 
    return this;
  }
  
  private static Map.Entry<AttributeKey<?>, Object>[] newAttrArray(int size) {
    return (Map.Entry<AttributeKey<?>, Object>[])new Map.Entry[size];
  }
  
  private static Map.Entry<ChannelOption<?>, Object>[] newOptionArray(int size) {
    return (Map.Entry<ChannelOption<?>, Object>[])new Map.Entry[size];
  }
  
  private static class ServerBootstrapAcceptor extends ChannelInboundHandlerAdapter {
    private final EventLoopGroup childGroup;
    
    private final ChannelHandler childHandler;
    
    private final Map.Entry<ChannelOption<?>, Object>[] childOptions;
    
    private final Map.Entry<AttributeKey<?>, Object>[] childAttrs;
    
    private final Runnable enableAutoReadTask;
    
    ServerBootstrapAcceptor(final Channel channel, EventLoopGroup childGroup, ChannelHandler childHandler, Map.Entry<ChannelOption<?>, Object>[] childOptions, Map.Entry<AttributeKey<?>, Object>[] childAttrs) {
      this.childGroup = childGroup;
      this.childHandler = childHandler;
      this.childOptions = childOptions;
      this.childAttrs = childAttrs;
      this.enableAutoReadTask = new Runnable() {
          public void run() {
            channel.config().setAutoRead(true);
          }
        };
    }
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      final Channel child = (Channel)msg;
      child.pipeline().addLast(new ChannelHandler[] { this.childHandler });
      AbstractBootstrap.setChannelOptions(child, this.childOptions, ServerBootstrap.logger);
      for (Map.Entry<AttributeKey<?>, Object> e : this.childAttrs)
        child.attr(e.getKey()).set(e.getValue()); 
      try {
        this.childGroup.register(child).addListener((GenericFutureListener)new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess())
                  ServerBootstrap.ServerBootstrapAcceptor.forceClose(child, future.cause()); 
              }
            });
      } catch (Throwable t) {
        forceClose(child, t);
      } 
    }
    
    private static void forceClose(Channel child, Throwable t) {
      child.unsafe().closeForcibly();
      ServerBootstrap.logger.warn("Failed to register an accepted channel: {}", child, t);
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      ChannelConfig config = ctx.channel().config();
      if (config.isAutoRead()) {
        config.setAutoRead(false);
        ctx.channel().eventLoop().schedule(this.enableAutoReadTask, 1L, TimeUnit.SECONDS);
      } 
      ctx.fireExceptionCaught(cause);
    }
  }
  
  public ServerBootstrap clone() {
    return new ServerBootstrap(this);
  }
  
  @Deprecated
  public EventLoopGroup childGroup() {
    return this.childGroup;
  }
  
  final ChannelHandler childHandler() {
    return this.childHandler;
  }
  
  final Map<ChannelOption<?>, Object> childOptions() {
    return copiedMap(this.childOptions);
  }
  
  final Map<AttributeKey<?>, Object> childAttrs() {
    return copiedMap(this.childAttrs);
  }
  
  public final ServerBootstrapConfig config() {
    return this.config;
  }
  
  public ServerBootstrap() {}
}
