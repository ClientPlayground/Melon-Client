package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.dns.AbstractDnsOptPseudoRrRecord;
import com.github.steveice10.netty.handler.codec.dns.DatagramDnsQuery;
import com.github.steveice10.netty.handler.codec.dns.DnsQuery;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsResponse;
import com.github.steveice10.netty.handler.codec.dns.DnsSection;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.ScheduledFuture;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

final class DnsQueryContext {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsQueryContext.class);
  
  private final DnsNameResolver parent;
  
  private final Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise;
  
  private final int id;
  
  private final DnsQuestion question;
  
  private final DnsRecord[] additionals;
  
  private final DnsRecord optResource;
  
  private final InetSocketAddress nameServerAddr;
  
  private final boolean recursionDesired;
  
  private volatile ScheduledFuture<?> timeoutFuture;
  
  DnsQueryContext(DnsNameResolver parent, InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise) {
    this.parent = (DnsNameResolver)ObjectUtil.checkNotNull(parent, "parent");
    this.nameServerAddr = (InetSocketAddress)ObjectUtil.checkNotNull(nameServerAddr, "nameServerAddr");
    this.question = (DnsQuestion)ObjectUtil.checkNotNull(question, "question");
    this.additionals = (DnsRecord[])ObjectUtil.checkNotNull(additionals, "additionals");
    this.promise = (Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>>)ObjectUtil.checkNotNull(promise, "promise");
    this.recursionDesired = parent.isRecursionDesired();
    this.id = parent.queryContextManager.add(this);
    if (parent.isOptResourceEnabled()) {
      this.optResource = (DnsRecord)new AbstractDnsOptPseudoRrRecord(parent.maxPayloadSize(), 0, 0) {
        
        };
    } else {
      this.optResource = null;
    } 
  }
  
  InetSocketAddress nameServerAddr() {
    return this.nameServerAddr;
  }
  
  DnsQuestion question() {
    return this.question;
  }
  
  void query(ChannelPromise writePromise) {
    DnsQuestion question = question();
    InetSocketAddress nameServerAddr = nameServerAddr();
    DatagramDnsQuery query = new DatagramDnsQuery(null, nameServerAddr, this.id);
    query.setRecursionDesired(this.recursionDesired);
    query.addRecord(DnsSection.QUESTION, (DnsRecord)question);
    for (DnsRecord record : this.additionals)
      query.addRecord(DnsSection.ADDITIONAL, record); 
    if (this.optResource != null)
      query.addRecord(DnsSection.ADDITIONAL, this.optResource); 
    if (logger.isDebugEnabled())
      logger.debug("{} WRITE: [{}: {}], {}", new Object[] { this.parent.ch, Integer.valueOf(this.id), nameServerAddr, question }); 
    sendQuery((DnsQuery)query, writePromise);
  }
  
  private void sendQuery(final DnsQuery query, final ChannelPromise writePromise) {
    if (this.parent.channelFuture.isDone()) {
      writeQuery(query, writePromise);
    } else {
      this.parent.channelFuture.addListener(new GenericFutureListener<Future<? super Channel>>() {
            public void operationComplete(Future<? super Channel> future) throws Exception {
              if (future.isSuccess()) {
                DnsQueryContext.this.writeQuery(query, writePromise);
              } else {
                Throwable cause = future.cause();
                DnsQueryContext.this.promise.tryFailure(cause);
                writePromise.setFailure(cause);
              } 
            }
          });
    } 
  }
  
  private void writeQuery(DnsQuery query, ChannelPromise writePromise) {
    final ChannelFuture writeFuture = this.parent.ch.writeAndFlush(query, writePromise);
    if (writeFuture.isDone()) {
      onQueryWriteCompletion(writeFuture);
    } else {
      writeFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
              DnsQueryContext.this.onQueryWriteCompletion(writeFuture);
            }
          });
    } 
  }
  
  private void onQueryWriteCompletion(ChannelFuture writeFuture) {
    if (!writeFuture.isSuccess()) {
      setFailure("failed to send a query", writeFuture.cause());
      return;
    } 
    final long queryTimeoutMillis = this.parent.queryTimeoutMillis();
    if (queryTimeoutMillis > 0L)
      this.timeoutFuture = this.parent.ch.eventLoop().schedule(new Runnable() {
            public void run() {
              if (DnsQueryContext.this.promise.isDone())
                return; 
              DnsQueryContext.this.setFailure("query timed out after " + queryTimeoutMillis + " milliseconds", null);
            }
          }queryTimeoutMillis, TimeUnit.MILLISECONDS); 
  }
  
  void finish(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
    DnsResponse res = (DnsResponse)envelope.content();
    if (res.count(DnsSection.QUESTION) != 1) {
      logger.warn("Received a DNS response with invalid number of questions: {}", envelope);
      return;
    } 
    if (!question().equals(res.recordAt(DnsSection.QUESTION))) {
      logger.warn("Received a mismatching DNS response: {}", envelope);
      return;
    } 
    setSuccess(envelope);
  }
  
  private void setSuccess(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
    this.parent.queryContextManager.remove(nameServerAddr(), this.id);
    ScheduledFuture<?> timeoutFuture = this.timeoutFuture;
    if (timeoutFuture != null)
      timeoutFuture.cancel(false); 
    Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise = this.promise;
    if (promise.setUncancellable()) {
      AddressedEnvelope<DnsResponse, InetSocketAddress> castResponse = envelope.retain();
      if (!promise.trySuccess(castResponse))
        envelope.release(); 
    } 
  }
  
  private void setFailure(String message, Throwable cause) {
    DnsNameResolverException e;
    InetSocketAddress nameServerAddr = nameServerAddr();
    this.parent.queryContextManager.remove(nameServerAddr, this.id);
    StringBuilder buf = new StringBuilder(message.length() + 64);
    buf.append('[')
      .append(nameServerAddr)
      .append("] ")
      .append(message)
      .append(" (no stack trace available)");
    if (cause == null) {
      e = new DnsNameResolverTimeoutException(nameServerAddr, question(), buf.toString());
    } else {
      e = new DnsNameResolverException(nameServerAddr, question(), buf.toString(), cause);
    } 
    this.promise.tryFailure(e);
  }
}
