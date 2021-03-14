package com.github.steveice10.netty.handler.traffic;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.Attribute;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Sharable
public class GlobalChannelTrafficShapingHandler extends AbstractTrafficShapingHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalChannelTrafficShapingHandler.class);
  
  final ConcurrentMap<Integer, PerChannel> channelQueues = PlatformDependent.newConcurrentHashMap();
  
  private final AtomicLong queuesSize = new AtomicLong();
  
  private final AtomicLong cumulativeWrittenBytes = new AtomicLong();
  
  private final AtomicLong cumulativeReadBytes = new AtomicLong();
  
  volatile long maxGlobalWriteSize = 419430400L;
  
  private volatile long writeChannelLimit;
  
  private volatile long readChannelLimit;
  
  private static final float DEFAULT_DEVIATION = 0.1F;
  
  private static final float MAX_DEVIATION = 0.4F;
  
  private static final float DEFAULT_SLOWDOWN = 0.4F;
  
  private static final float DEFAULT_ACCELERATION = -0.1F;
  
  private volatile float maxDeviation;
  
  private volatile float accelerationFactor;
  
  private volatile float slowDownFactor;
  
  private volatile boolean readDeviationActive;
  
  private volatile boolean writeDeviationActive;
  
  static final class PerChannel {
    ArrayDeque<GlobalChannelTrafficShapingHandler.ToSend> messagesQueue;
    
    TrafficCounter channelTrafficCounter;
    
    long queueSize;
    
    long lastWriteTimestamp;
    
    long lastReadTimestamp;
  }
  
  void createGlobalTrafficCounter(ScheduledExecutorService executor) {
    setMaxDeviation(0.1F, 0.4F, -0.1F);
    if (executor == null)
      throw new IllegalArgumentException("Executor must not be null"); 
    TrafficCounter tc = new GlobalChannelTrafficCounter(this, executor, "GlobalChannelTC", this.checkInterval);
    setTrafficCounter(tc);
    tc.start();
  }
  
  protected int userDefinedWritabilityIndex() {
    return 3;
  }
  
  public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval, long maxTime) {
    super(writeGlobalLimit, readGlobalLimit, checkInterval, maxTime);
    createGlobalTrafficCounter(executor);
    this.writeChannelLimit = writeChannelLimit;
    this.readChannelLimit = readChannelLimit;
  }
  
  public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval) {
    super(writeGlobalLimit, readGlobalLimit, checkInterval);
    this.writeChannelLimit = writeChannelLimit;
    this.readChannelLimit = readChannelLimit;
    createGlobalTrafficCounter(executor);
  }
  
  public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit) {
    super(writeGlobalLimit, readGlobalLimit);
    this.writeChannelLimit = writeChannelLimit;
    this.readChannelLimit = readChannelLimit;
    createGlobalTrafficCounter(executor);
  }
  
  public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long checkInterval) {
    super(checkInterval);
    createGlobalTrafficCounter(executor);
  }
  
  public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor) {
    createGlobalTrafficCounter(executor);
  }
  
  public float maxDeviation() {
    return this.maxDeviation;
  }
  
  public float accelerationFactor() {
    return this.accelerationFactor;
  }
  
  public float slowDownFactor() {
    return this.slowDownFactor;
  }
  
  public void setMaxDeviation(float maxDeviation, float slowDownFactor, float accelerationFactor) {
    if (maxDeviation > 0.4F)
      throw new IllegalArgumentException("maxDeviation must be <= 0.4"); 
    if (slowDownFactor < 0.0F)
      throw new IllegalArgumentException("slowDownFactor must be >= 0"); 
    if (accelerationFactor > 0.0F)
      throw new IllegalArgumentException("accelerationFactor must be <= 0"); 
    this.maxDeviation = maxDeviation;
    this.accelerationFactor = 1.0F + accelerationFactor;
    this.slowDownFactor = 1.0F + slowDownFactor;
  }
  
  private void computeDeviationCumulativeBytes() {
    long maxWrittenBytes = 0L;
    long maxReadBytes = 0L;
    long minWrittenBytes = Long.MAX_VALUE;
    long minReadBytes = Long.MAX_VALUE;
    for (PerChannel perChannel : this.channelQueues.values()) {
      long value = perChannel.channelTrafficCounter.cumulativeWrittenBytes();
      if (maxWrittenBytes < value)
        maxWrittenBytes = value; 
      if (minWrittenBytes > value)
        minWrittenBytes = value; 
      value = perChannel.channelTrafficCounter.cumulativeReadBytes();
      if (maxReadBytes < value)
        maxReadBytes = value; 
      if (minReadBytes > value)
        minReadBytes = value; 
    } 
    boolean multiple = (this.channelQueues.size() > 1);
    this.readDeviationActive = (multiple && minReadBytes < maxReadBytes / 2L);
    this.writeDeviationActive = (multiple && minWrittenBytes < maxWrittenBytes / 2L);
    this.cumulativeWrittenBytes.set(maxWrittenBytes);
    this.cumulativeReadBytes.set(maxReadBytes);
  }
  
  protected void doAccounting(TrafficCounter counter) {
    computeDeviationCumulativeBytes();
    super.doAccounting(counter);
  }
  
  private long computeBalancedWait(float maxLocal, float maxGlobal, long wait) {
    if (maxGlobal == 0.0F)
      return wait; 
    float ratio = maxLocal / maxGlobal;
    if (ratio > this.maxDeviation) {
      if (ratio < 1.0F - this.maxDeviation)
        return wait; 
      ratio = this.slowDownFactor;
      if (wait < 10L)
        wait = 10L; 
    } else {
      ratio = this.accelerationFactor;
    } 
    return (long)((float)wait * ratio);
  }
  
  public long getMaxGlobalWriteSize() {
    return this.maxGlobalWriteSize;
  }
  
  public void setMaxGlobalWriteSize(long maxGlobalWriteSize) {
    if (maxGlobalWriteSize <= 0L)
      throw new IllegalArgumentException("maxGlobalWriteSize must be positive"); 
    this.maxGlobalWriteSize = maxGlobalWriteSize;
  }
  
  public long queuesSize() {
    return this.queuesSize.get();
  }
  
  public void configureChannel(long newWriteLimit, long newReadLimit) {
    this.writeChannelLimit = newWriteLimit;
    this.readChannelLimit = newReadLimit;
    long now = TrafficCounter.milliSecondFromNano();
    for (PerChannel perChannel : this.channelQueues.values())
      perChannel.channelTrafficCounter.resetAccounting(now); 
  }
  
  public long getWriteChannelLimit() {
    return this.writeChannelLimit;
  }
  
  public void setWriteChannelLimit(long writeLimit) {
    this.writeChannelLimit = writeLimit;
    long now = TrafficCounter.milliSecondFromNano();
    for (PerChannel perChannel : this.channelQueues.values())
      perChannel.channelTrafficCounter.resetAccounting(now); 
  }
  
  public long getReadChannelLimit() {
    return this.readChannelLimit;
  }
  
  public void setReadChannelLimit(long readLimit) {
    this.readChannelLimit = readLimit;
    long now = TrafficCounter.milliSecondFromNano();
    for (PerChannel perChannel : this.channelQueues.values())
      perChannel.channelTrafficCounter.resetAccounting(now); 
  }
  
  public final void release() {
    this.trafficCounter.stop();
  }
  
  private PerChannel getOrSetPerChannel(ChannelHandlerContext ctx) {
    Channel channel = ctx.channel();
    Integer key = Integer.valueOf(channel.hashCode());
    PerChannel perChannel = this.channelQueues.get(key);
    if (perChannel == null) {
      perChannel = new PerChannel();
      perChannel.messagesQueue = new ArrayDeque<ToSend>();
      perChannel
        .channelTrafficCounter = new TrafficCounter(this, null, "ChannelTC" + ctx.channel().hashCode(), this.checkInterval);
      perChannel.queueSize = 0L;
      perChannel.lastReadTimestamp = TrafficCounter.milliSecondFromNano();
      perChannel.lastWriteTimestamp = perChannel.lastReadTimestamp;
      this.channelQueues.put(key, perChannel);
    } 
    return perChannel;
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    getOrSetPerChannel(ctx);
    this.trafficCounter.resetCumulativeTime();
    super.handlerAdded(ctx);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    this.trafficCounter.resetCumulativeTime();
    Channel channel = ctx.channel();
    Integer key = Integer.valueOf(channel.hashCode());
    PerChannel perChannel = this.channelQueues.remove(key);
    if (perChannel != null)
      synchronized (perChannel) {
        if (channel.isActive()) {
          for (ToSend toSend : perChannel.messagesQueue) {
            long size = calculateSize(toSend.toSend);
            this.trafficCounter.bytesRealWriteFlowControl(size);
            perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
            perChannel.queueSize -= size;
            this.queuesSize.addAndGet(-size);
            ctx.write(toSend.toSend, toSend.promise);
          } 
        } else {
          this.queuesSize.addAndGet(-perChannel.queueSize);
          for (ToSend toSend : perChannel.messagesQueue) {
            if (toSend.toSend instanceof ByteBuf)
              ((ByteBuf)toSend.toSend).release(); 
          } 
        } 
        perChannel.messagesQueue.clear();
      }  
    releaseWriteSuspended(ctx);
    releaseReadSuspended(ctx);
    super.handlerRemoved(ctx);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    long size = calculateSize(msg);
    long now = TrafficCounter.milliSecondFromNano();
    if (size > 0L) {
      long waitGlobal = this.trafficCounter.readTimeToWait(size, getReadLimit(), this.maxTime, now);
      Integer key = Integer.valueOf(ctx.channel().hashCode());
      PerChannel perChannel = this.channelQueues.get(key);
      long wait = 0L;
      if (perChannel != null) {
        wait = perChannel.channelTrafficCounter.readTimeToWait(size, this.readChannelLimit, this.maxTime, now);
        if (this.readDeviationActive) {
          long maxLocalRead = perChannel.channelTrafficCounter.cumulativeReadBytes();
          long maxGlobalRead = this.cumulativeReadBytes.get();
          if (maxLocalRead <= 0L)
            maxLocalRead = 0L; 
          if (maxGlobalRead < maxLocalRead)
            maxGlobalRead = maxLocalRead; 
          wait = computeBalancedWait((float)maxLocalRead, (float)maxGlobalRead, wait);
        } 
      } 
      if (wait < waitGlobal)
        wait = waitGlobal; 
      wait = checkWaitReadTime(ctx, wait, now);
      if (wait >= 10L) {
        Channel channel = ctx.channel();
        ChannelConfig config = channel.config();
        if (logger.isDebugEnabled())
          logger.debug("Read Suspend: " + wait + ':' + config.isAutoRead() + ':' + 
              isHandlerActive(ctx)); 
        if (config.isAutoRead() && isHandlerActive(ctx)) {
          config.setAutoRead(false);
          channel.attr(READ_SUSPENDED).set(Boolean.valueOf(true));
          Attribute<Runnable> attr = channel.attr(REOPEN_TASK);
          Runnable reopenTask = (Runnable)attr.get();
          if (reopenTask == null) {
            reopenTask = new AbstractTrafficShapingHandler.ReopenReadTimerTask(ctx);
            attr.set(reopenTask);
          } 
          ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
          if (logger.isDebugEnabled())
            logger.debug("Suspend final status => " + config.isAutoRead() + ':' + 
                isHandlerActive(ctx) + " will reopened at: " + wait); 
        } 
      } 
    } 
    informReadOperation(ctx, now);
    ctx.fireChannelRead(msg);
  }
  
  protected long checkWaitReadTime(ChannelHandlerContext ctx, long wait, long now) {
    Integer key = Integer.valueOf(ctx.channel().hashCode());
    PerChannel perChannel = this.channelQueues.get(key);
    if (perChannel != null && 
      wait > this.maxTime && now + wait - perChannel.lastReadTimestamp > this.maxTime)
      wait = this.maxTime; 
    return wait;
  }
  
  protected void informReadOperation(ChannelHandlerContext ctx, long now) {
    Integer key = Integer.valueOf(ctx.channel().hashCode());
    PerChannel perChannel = this.channelQueues.get(key);
    if (perChannel != null)
      perChannel.lastReadTimestamp = now; 
  }
  
  private static final class ToSend {
    final long relativeTimeAction;
    
    final Object toSend;
    
    final ChannelPromise promise;
    
    final long size;
    
    private ToSend(long delay, Object toSend, long size, ChannelPromise promise) {
      this.relativeTimeAction = delay;
      this.toSend = toSend;
      this.size = size;
      this.promise = promise;
    }
  }
  
  protected long maximumCumulativeWrittenBytes() {
    return this.cumulativeWrittenBytes.get();
  }
  
  protected long maximumCumulativeReadBytes() {
    return this.cumulativeReadBytes.get();
  }
  
  public Collection<TrafficCounter> channelTrafficCounters() {
    return new AbstractCollection<TrafficCounter>() {
        public Iterator<TrafficCounter> iterator() {
          return new Iterator<TrafficCounter>() {
              final Iterator<GlobalChannelTrafficShapingHandler.PerChannel> iter = GlobalChannelTrafficShapingHandler.this.channelQueues.values().iterator();
              
              public boolean hasNext() {
                return this.iter.hasNext();
              }
              
              public TrafficCounter next() {
                return ((GlobalChannelTrafficShapingHandler.PerChannel)this.iter.next()).channelTrafficCounter;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
        
        public int size() {
          return GlobalChannelTrafficShapingHandler.this.channelQueues.size();
        }
      };
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    long size = calculateSize(msg);
    long now = TrafficCounter.milliSecondFromNano();
    if (size > 0L) {
      long waitGlobal = this.trafficCounter.writeTimeToWait(size, getWriteLimit(), this.maxTime, now);
      Integer key = Integer.valueOf(ctx.channel().hashCode());
      PerChannel perChannel = this.channelQueues.get(key);
      long wait = 0L;
      if (perChannel != null) {
        wait = perChannel.channelTrafficCounter.writeTimeToWait(size, this.writeChannelLimit, this.maxTime, now);
        if (this.writeDeviationActive) {
          long maxLocalWrite = perChannel.channelTrafficCounter.cumulativeWrittenBytes();
          long maxGlobalWrite = this.cumulativeWrittenBytes.get();
          if (maxLocalWrite <= 0L)
            maxLocalWrite = 0L; 
          if (maxGlobalWrite < maxLocalWrite)
            maxGlobalWrite = maxLocalWrite; 
          wait = computeBalancedWait((float)maxLocalWrite, (float)maxGlobalWrite, wait);
        } 
      } 
      if (wait < waitGlobal)
        wait = waitGlobal; 
      if (wait >= 10L) {
        if (logger.isDebugEnabled())
          logger.debug("Write suspend: " + wait + ':' + ctx.channel().config().isAutoRead() + ':' + 
              isHandlerActive(ctx)); 
        submitWrite(ctx, msg, size, wait, now, promise);
        return;
      } 
    } 
    submitWrite(ctx, msg, size, 0L, now, promise);
  }
  
  protected void submitWrite(final ChannelHandlerContext ctx, Object msg, long size, long writedelay, long now, ChannelPromise promise) {
    ToSend newToSend;
    Channel channel = ctx.channel();
    Integer key = Integer.valueOf(channel.hashCode());
    PerChannel perChannel = this.channelQueues.get(key);
    if (perChannel == null)
      perChannel = getOrSetPerChannel(ctx); 
    long delay = writedelay;
    boolean globalSizeExceeded = false;
    synchronized (perChannel) {
      if (writedelay == 0L && perChannel.messagesQueue.isEmpty()) {
        this.trafficCounter.bytesRealWriteFlowControl(size);
        perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
        ctx.write(msg, promise);
        perChannel.lastWriteTimestamp = now;
        return;
      } 
      if (delay > this.maxTime && now + delay - perChannel.lastWriteTimestamp > this.maxTime)
        delay = this.maxTime; 
      newToSend = new ToSend(delay + now, msg, size, promise);
      perChannel.messagesQueue.addLast(newToSend);
      perChannel.queueSize += size;
      this.queuesSize.addAndGet(size);
      checkWriteSuspend(ctx, delay, perChannel.queueSize);
      if (this.queuesSize.get() > this.maxGlobalWriteSize)
        globalSizeExceeded = true; 
    } 
    if (globalSizeExceeded)
      setUserDefinedWritability(ctx, false); 
    final long futureNow = newToSend.relativeTimeAction;
    final PerChannel forSchedule = perChannel;
    ctx.executor().schedule(new Runnable() {
          public void run() {
            GlobalChannelTrafficShapingHandler.this.sendAllValid(ctx, forSchedule, futureNow);
          }
        }delay, TimeUnit.MILLISECONDS);
  }
  
  private void sendAllValid(ChannelHandlerContext ctx, PerChannel perChannel, long now) {
    synchronized (perChannel) {
      ToSend newToSend = perChannel.messagesQueue.pollFirst();
      for (; newToSend != null; newToSend = perChannel.messagesQueue.pollFirst()) {
        if (newToSend.relativeTimeAction <= now) {
          long size = newToSend.size;
          this.trafficCounter.bytesRealWriteFlowControl(size);
          perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
          perChannel.queueSize -= size;
          this.queuesSize.addAndGet(-size);
          ctx.write(newToSend.toSend, newToSend.promise);
          perChannel.lastWriteTimestamp = now;
        } else {
          perChannel.messagesQueue.addFirst(newToSend);
          break;
        } 
      } 
      if (perChannel.messagesQueue.isEmpty())
        releaseWriteSuspended(ctx); 
    } 
    ctx.flush();
  }
  
  public String toString() {
    return (new StringBuilder(340)).append(super.toString())
      .append(" Write Channel Limit: ").append(this.writeChannelLimit)
      .append(" Read Channel Limit: ").append(this.readChannelLimit).toString();
  }
}
