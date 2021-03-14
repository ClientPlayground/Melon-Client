package com.github.steveice10.netty.bootstrap;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.ServerChannel;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Map;

public final class ServerBootstrapConfig extends AbstractBootstrapConfig<ServerBootstrap, ServerChannel> {
  ServerBootstrapConfig(ServerBootstrap bootstrap) {
    super(bootstrap);
  }
  
  public EventLoopGroup childGroup() {
    return this.bootstrap.childGroup();
  }
  
  public ChannelHandler childHandler() {
    return this.bootstrap.childHandler();
  }
  
  public Map<ChannelOption<?>, Object> childOptions() {
    return this.bootstrap.childOptions();
  }
  
  public Map<AttributeKey<?>, Object> childAttrs() {
    return this.bootstrap.childAttrs();
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(super.toString());
    buf.setLength(buf.length() - 1);
    buf.append(", ");
    EventLoopGroup childGroup = childGroup();
    if (childGroup != null) {
      buf.append("childGroup: ");
      buf.append(StringUtil.simpleClassName(childGroup));
      buf.append(", ");
    } 
    Map<ChannelOption<?>, Object> childOptions = childOptions();
    if (!childOptions.isEmpty()) {
      buf.append("childOptions: ");
      buf.append(childOptions);
      buf.append(", ");
    } 
    Map<AttributeKey<?>, Object> childAttrs = childAttrs();
    if (!childAttrs.isEmpty()) {
      buf.append("childAttrs: ");
      buf.append(childAttrs);
      buf.append(", ");
    } 
    ChannelHandler childHandler = childHandler();
    if (childHandler != null) {
      buf.append("childHandler: ");
      buf.append(childHandler);
      buf.append(", ");
    } 
    if (buf.charAt(buf.length() - 1) == '(') {
      buf.append(')');
    } else {
      buf.setCharAt(buf.length() - 2, ')');
      buf.setLength(buf.length() - 1);
    } 
    return buf.toString();
  }
}
