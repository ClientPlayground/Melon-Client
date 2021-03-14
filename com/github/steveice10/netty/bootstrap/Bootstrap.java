package com.github.steveice10.netty.bootstrap;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.resolver.AddressResolver;
import com.github.steveice10.netty.resolver.AddressResolverGroup;
import com.github.steveice10.netty.resolver.DefaultAddressResolverGroup;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

public class Bootstrap extends AbstractBootstrap<Bootstrap, Channel> {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Bootstrap.class);
  
  private static final AddressResolverGroup<?> DEFAULT_RESOLVER = (AddressResolverGroup<?>)DefaultAddressResolverGroup.INSTANCE;
  
  private final BootstrapConfig config = new BootstrapConfig(this);
  
  private volatile AddressResolverGroup<SocketAddress> resolver = (AddressResolverGroup)DEFAULT_RESOLVER;
  
  private volatile SocketAddress remoteAddress;
  
  private Bootstrap(Bootstrap bootstrap) {
    super(bootstrap);
    this.resolver = bootstrap.resolver;
    this.remoteAddress = bootstrap.remoteAddress;
  }
  
  public Bootstrap resolver(AddressResolverGroup<?> resolver) {
    this.resolver = (resolver == null) ? (AddressResolverGroup)DEFAULT_RESOLVER : (AddressResolverGroup)resolver;
    return this;
  }
  
  public Bootstrap remoteAddress(SocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }
  
  public Bootstrap remoteAddress(String inetHost, int inetPort) {
    this.remoteAddress = InetSocketAddress.createUnresolved(inetHost, inetPort);
    return this;
  }
  
  public Bootstrap remoteAddress(InetAddress inetHost, int inetPort) {
    this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
    return this;
  }
  
  public ChannelFuture connect() {
    validate();
    SocketAddress remoteAddress = this.remoteAddress;
    if (remoteAddress == null)
      throw new IllegalStateException("remoteAddress not set"); 
    return doResolveAndConnect(remoteAddress, this.config.localAddress());
  }
  
  public ChannelFuture connect(String inetHost, int inetPort) {
    return connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
  }
  
  public ChannelFuture connect(InetAddress inetHost, int inetPort) {
    return connect(new InetSocketAddress(inetHost, inetPort));
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress) {
    if (remoteAddress == null)
      throw new NullPointerException("remoteAddress"); 
    validate();
    return doResolveAndConnect(remoteAddress, this.config.localAddress());
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
    if (remoteAddress == null)
      throw new NullPointerException("remoteAddress"); 
    validate();
    return doResolveAndConnect(remoteAddress, localAddress);
  }
  
  private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
    ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.isDone()) {
      if (!regFuture.isSuccess())
        return regFuture; 
      return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
    } 
    final AbstractBootstrap.PendingRegistrationPromise promise = new AbstractBootstrap.PendingRegistrationPromise(channel);
    regFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            Throwable cause = future.cause();
            if (cause != null) {
              promise.setFailure(cause);
            } else {
              promise.registered();
              Bootstrap.this.doResolveAndConnect0(channel, remoteAddress, localAddress, (ChannelPromise)promise);
            } 
          }
        });
    return (ChannelFuture)promise;
  }
  
  private ChannelFuture doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
    try {
      EventLoop eventLoop = channel.eventLoop();
      AddressResolver<SocketAddress> resolver = this.resolver.getResolver((EventExecutor)eventLoop);
      if (!resolver.isSupported(remoteAddress) || resolver.isResolved(remoteAddress)) {
        doConnect(remoteAddress, localAddress, promise);
        return (ChannelFuture)promise;
      } 
      Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);
      if (resolveFuture.isDone()) {
        Throwable resolveFailureCause = resolveFuture.cause();
        if (resolveFailureCause != null) {
          channel.close();
          promise.setFailure(resolveFailureCause);
        } else {
          doConnect((SocketAddress)resolveFuture.getNow(), localAddress, promise);
        } 
        return (ChannelFuture)promise;
      } 
      resolveFuture.addListener((GenericFutureListener)new FutureListener<SocketAddress>() {
            public void operationComplete(Future<SocketAddress> future) throws Exception {
              if (future.cause() != null) {
                channel.close();
                promise.setFailure(future.cause());
              } else {
                Bootstrap.doConnect((SocketAddress)future.getNow(), localAddress, promise);
              } 
            }
          });
    } catch (Throwable cause) {
      promise.tryFailure(cause);
    } 
    return (ChannelFuture)promise;
  }
  
  private static void doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise connectPromise) {
    final Channel channel = connectPromise.channel();
    channel.eventLoop().execute(new Runnable() {
          public void run() {
            if (localAddress == null) {
              channel.connect(remoteAddress, connectPromise);
            } else {
              channel.connect(remoteAddress, localAddress, connectPromise);
            } 
            connectPromise.addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
          }
        });
  }
  
  void init(Channel channel) throws Exception {
    ChannelPipeline p = channel.pipeline();
    p.addLast(new ChannelHandler[] { this.config.handler() });
    Map<ChannelOption<?>, Object> options = options0();
    synchronized (options) {
      setChannelOptions(channel, options, logger);
    } 
    Map<AttributeKey<?>, Object> attrs = attrs0();
    synchronized (attrs) {
      for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet())
        channel.attr(e.getKey()).set(e.getValue()); 
    } 
  }
  
  public Bootstrap validate() {
    super.validate();
    if (this.config.handler() == null)
      throw new IllegalStateException("handler not set"); 
    return this;
  }
  
  public Bootstrap clone() {
    return new Bootstrap(this);
  }
  
  public Bootstrap clone(EventLoopGroup group) {
    Bootstrap bs = new Bootstrap(this);
    bs.group = group;
    return bs;
  }
  
  public final BootstrapConfig config() {
    return this.config;
  }
  
  final SocketAddress remoteAddress() {
    return this.remoteAddress;
  }
  
  final AddressResolverGroup<?> resolver() {
    return this.resolver;
  }
  
  public Bootstrap() {}
}
