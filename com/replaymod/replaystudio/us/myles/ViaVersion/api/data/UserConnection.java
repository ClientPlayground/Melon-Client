package com.replaymod.replaystudio.us.myles.ViaVersion.api.data;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaVersionConfig;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.PipelineUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;

public class UserConnection {
  @NonNull
  private final Channel channel;
  
  Map<Class, StoredObject> storedObjects = (Map)new ConcurrentHashMap<>();
  
  public void setStoredObjects(Map<Class<?>, StoredObject> storedObjects) {
    this.storedObjects = storedObjects;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public void setPendingDisconnect(boolean pendingDisconnect) {
    this.pendingDisconnect = pendingDisconnect;
  }
  
  public void setLastPacket(Object lastPacket) {
    this.lastPacket = lastPacket;
  }
  
  public void setSentPackets(long sentPackets) {
    this.sentPackets = sentPackets;
  }
  
  public void setReceivedPackets(long receivedPackets) {
    this.receivedPackets = receivedPackets;
  }
  
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }
  
  public void setIntervalPackets(long intervalPackets) {
    this.intervalPackets = intervalPackets;
  }
  
  public void setPacketsPerSecond(long packetsPerSecond) {
    this.packetsPerSecond = packetsPerSecond;
  }
  
  public void setSecondsObserved(int secondsObserved) {
    this.secondsObserved = secondsObserved;
  }
  
  public void setWarnings(int warnings) {
    this.warnings = warnings;
  }
  
  public void setVelocityLock(ReadWriteLock velocityLock) {
    this.velocityLock = velocityLock;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof UserConnection))
      return false; 
    UserConnection other = (UserConnection)o;
    if (!other.canEqual(this))
      return false; 
    Object this$channel = getChannel(), other$channel = other.getChannel();
    if ((this$channel == null) ? (other$channel != null) : !this$channel.equals(other$channel))
      return false; 
    Object<Class, StoredObject> this$storedObjects = (Object<Class, StoredObject>)getStoredObjects(), other$storedObjects = (Object<Class, StoredObject>)other.getStoredObjects();
    if ((this$storedObjects == null) ? (other$storedObjects != null) : !this$storedObjects.equals(other$storedObjects))
      return false; 
    if (isActive() != other.isActive())
      return false; 
    if (isPendingDisconnect() != other.isPendingDisconnect())
      return false; 
    Object this$lastPacket = getLastPacket(), other$lastPacket = other.getLastPacket();
    if ((this$lastPacket == null) ? (other$lastPacket != null) : !this$lastPacket.equals(other$lastPacket))
      return false; 
    if (getSentPackets() != other.getSentPackets())
      return false; 
    if (getReceivedPackets() != other.getReceivedPackets())
      return false; 
    if (getStartTime() != other.getStartTime())
      return false; 
    if (getIntervalPackets() != other.getIntervalPackets())
      return false; 
    if (getPacketsPerSecond() != other.getPacketsPerSecond())
      return false; 
    if (getSecondsObserved() != other.getSecondsObserved())
      return false; 
    if (getWarnings() != other.getWarnings())
      return false; 
    Object this$velocityLock = getVelocityLock(), other$velocityLock = other.getVelocityLock();
    return !((this$velocityLock == null) ? (other$velocityLock != null) : !this$velocityLock.equals(other$velocityLock));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof UserConnection;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $channel = getChannel();
    result = result * 59 + (($channel == null) ? 43 : $channel.hashCode());
    Object<Class, StoredObject> $storedObjects = (Object<Class, StoredObject>)getStoredObjects();
    result = result * 59 + (($storedObjects == null) ? 43 : $storedObjects.hashCode());
    result = result * 59 + (isActive() ? 79 : 97);
    result = result * 59 + (isPendingDisconnect() ? 79 : 97);
    Object $lastPacket = getLastPacket();
    result = result * 59 + (($lastPacket == null) ? 43 : $lastPacket.hashCode());
    long $sentPackets = getSentPackets();
    result = result * 59 + (int)($sentPackets >>> 32L ^ $sentPackets);
    long $receivedPackets = getReceivedPackets();
    result = result * 59 + (int)($receivedPackets >>> 32L ^ $receivedPackets);
    long $startTime = getStartTime();
    result = result * 59 + (int)($startTime >>> 32L ^ $startTime);
    long $intervalPackets = getIntervalPackets();
    result = result * 59 + (int)($intervalPackets >>> 32L ^ $intervalPackets);
    long $packetsPerSecond = getPacketsPerSecond();
    result = result * 59 + (int)($packetsPerSecond >>> 32L ^ $packetsPerSecond);
    result = result * 59 + getSecondsObserved();
    result = result * 59 + getWarnings();
    Object $velocityLock = getVelocityLock();
    return result * 59 + (($velocityLock == null) ? 43 : $velocityLock.hashCode());
  }
  
  public String toString() {
    return "UserConnection(channel=" + getChannel() + ", storedObjects=" + getStoredObjects() + ", active=" + isActive() + ", pendingDisconnect=" + isPendingDisconnect() + ", lastPacket=" + getLastPacket() + ", sentPackets=" + getSentPackets() + ", receivedPackets=" + getReceivedPackets() + ", startTime=" + getStartTime() + ", intervalPackets=" + getIntervalPackets() + ", packetsPerSecond=" + getPacketsPerSecond() + ", secondsObserved=" + getSecondsObserved() + ", warnings=" + getWarnings() + ", velocityLock=" + getVelocityLock() + ")";
  }
  
  @NonNull
  public Channel getChannel() {
    return this.channel;
  }
  
  public Map<Class, StoredObject> getStoredObjects() {
    return this.storedObjects;
  }
  
  private boolean active = true;
  
  public boolean isActive() {
    return this.active;
  }
  
  private boolean pendingDisconnect = false;
  
  private Object lastPacket;
  
  public boolean isPendingDisconnect() {
    return this.pendingDisconnect;
  }
  
  public Object getLastPacket() {
    return this.lastPacket;
  }
  
  private long sentPackets = 0L;
  
  public long getSentPackets() {
    return this.sentPackets;
  }
  
  private long receivedPackets = 0L;
  
  public long getReceivedPackets() {
    return this.receivedPackets;
  }
  
  private long startTime = 0L;
  
  public long getStartTime() {
    return this.startTime;
  }
  
  private long intervalPackets = 0L;
  
  public long getIntervalPackets() {
    return this.intervalPackets;
  }
  
  private long packetsPerSecond = -1L;
  
  public long getPacketsPerSecond() {
    return this.packetsPerSecond;
  }
  
  private int secondsObserved = 0;
  
  public int getSecondsObserved() {
    return this.secondsObserved;
  }
  
  private int warnings = 0;
  
  public int getWarnings() {
    return this.warnings;
  }
  
  private ReadWriteLock velocityLock = new ReentrantReadWriteLock();
  
  public ReadWriteLock getVelocityLock() {
    return this.velocityLock;
  }
  
  public UserConnection(Channel channel) {
    this.channel = channel;
  }
  
  public <T extends StoredObject> T get(Class<T> objectClass) {
    return (T)this.storedObjects.get(objectClass);
  }
  
  public boolean has(Class<? extends StoredObject> objectClass) {
    return this.storedObjects.containsKey(objectClass);
  }
  
  public void put(StoredObject object) {
    this.storedObjects.put(object.getClass(), object);
  }
  
  public void clearStoredObjects() {
    this.storedObjects.clear();
  }
  
  public void sendRawPacket(final ByteBuf packet, boolean currentThread) {
    final ChannelHandler handler = this.channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
    if (currentThread) {
      this.channel.pipeline().context(handler).writeAndFlush(packet);
    } else {
      this.channel.eventLoop().submit(new Runnable() {
            public void run() {
              UserConnection.this.channel.pipeline().context(handler).writeAndFlush(packet);
            }
          });
    } 
  }
  
  public ChannelFuture sendRawPacketFuture(ByteBuf packet) {
    ChannelHandler handler = this.channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
    ChannelFuture future = this.channel.pipeline().context(handler).writeAndFlush(packet);
    return future;
  }
  
  public void sendRawPacket(ByteBuf packet) {
    sendRawPacket(packet, false);
  }
  
  public void incrementSent() {
    this.sentPackets++;
  }
  
  public boolean incrementReceived() {
    long diff = System.currentTimeMillis() - this.startTime;
    if (diff >= 1000L) {
      this.packetsPerSecond = this.intervalPackets;
      this.startTime = System.currentTimeMillis();
      this.intervalPackets = 1L;
      return true;
    } 
    this.intervalPackets++;
    this.receivedPackets++;
    return false;
  }
  
  public boolean handlePPS() {
    ViaVersionConfig conf = Via.getConfig();
    if (conf.getMaxPPS() > 0 && 
      getPacketsPerSecond() >= conf.getMaxPPS()) {
      disconnect(conf.getMaxPPSKickMessage().replace("%pps", Long.toString(getPacketsPerSecond())));
      return true;
    } 
    if (conf.getMaxWarnings() > 0 && conf.getTrackingPeriod() > 0)
      if (getSecondsObserved() > conf.getTrackingPeriod()) {
        setWarnings(0);
        setSecondsObserved(1);
      } else {
        setSecondsObserved(getSecondsObserved() + 1);
        if (getPacketsPerSecond() >= conf.getWarningPPS())
          setWarnings(getWarnings() + 1); 
        if (getWarnings() >= conf.getMaxWarnings()) {
          disconnect(conf.getMaxWarningsKickMessage().replace("%pps", Long.toString(getPacketsPerSecond())));
          return true;
        } 
      }  
    return false;
  }
  
  public void disconnect(final String reason) {
    if (!getChannel().isOpen())
      return; 
    if (this.pendingDisconnect)
      return; 
    this.pendingDisconnect = true;
    if (((ProtocolInfo)get(ProtocolInfo.class)).getUuid() != null) {
      final UUID uuid = ((ProtocolInfo)get(ProtocolInfo.class)).getUuid();
      Via.getPlatform().runSync(new Runnable() {
            public void run() {
              if (!Via.getPlatform().kickPlayer(uuid, ChatColor.translateAlternateColorCodes('&', reason)))
                UserConnection.this.getChannel().close(); 
            }
          });
    } 
  }
  
  public void sendRawPacketToServer(ByteBuf packet, boolean currentThread) {
    final ByteBuf buf = packet.alloc().buffer();
    try {
      try {
        Type.VAR_INT.write(buf, Integer.valueOf(1000));
      } catch (Exception e) {
        Via.getPlatform().getLogger().warning("Type.VAR_INT.write thrown an exception: " + e);
      } 
      buf.writeBytes(packet);
      final ChannelHandlerContext context = PipelineUtil.getPreviousContext(Via.getManager().getInjector().getDecoderName(), getChannel().pipeline());
      if (currentThread) {
        if (context != null) {
          context.fireChannelRead(buf);
        } else {
          getChannel().pipeline().fireChannelRead(buf);
        } 
      } else {
        try {
          this.channel.eventLoop().submit(new Runnable() {
                public void run() {
                  if (context != null) {
                    context.fireChannelRead(buf);
                  } else {
                    UserConnection.this.getChannel().pipeline().fireChannelRead(buf);
                  } 
                }
              });
        } catch (Throwable t) {
          buf.release();
          throw t;
        } 
      } 
    } finally {
      packet.release();
    } 
  }
  
  public void sendRawPacketToServer(ByteBuf packet) {
    sendRawPacketToServer(packet, false);
  }
}
