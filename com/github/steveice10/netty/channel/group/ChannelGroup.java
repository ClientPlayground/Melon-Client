package com.github.steveice10.netty.channel.group;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelId;
import java.util.Set;

public interface ChannelGroup extends Set<Channel>, Comparable<ChannelGroup> {
  String name();
  
  Channel find(ChannelId paramChannelId);
  
  ChannelGroupFuture write(Object paramObject);
  
  ChannelGroupFuture write(Object paramObject, ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture write(Object paramObject, ChannelMatcher paramChannelMatcher, boolean paramBoolean);
  
  ChannelGroup flush();
  
  ChannelGroup flush(ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture writeAndFlush(Object paramObject);
  
  @Deprecated
  ChannelGroupFuture flushAndWrite(Object paramObject);
  
  ChannelGroupFuture writeAndFlush(Object paramObject, ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture writeAndFlush(Object paramObject, ChannelMatcher paramChannelMatcher, boolean paramBoolean);
  
  @Deprecated
  ChannelGroupFuture flushAndWrite(Object paramObject, ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture disconnect();
  
  ChannelGroupFuture disconnect(ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture close();
  
  ChannelGroupFuture close(ChannelMatcher paramChannelMatcher);
  
  @Deprecated
  ChannelGroupFuture deregister();
  
  @Deprecated
  ChannelGroupFuture deregister(ChannelMatcher paramChannelMatcher);
  
  ChannelGroupFuture newCloseFuture();
  
  ChannelGroupFuture newCloseFuture(ChannelMatcher paramChannelMatcher);
}
