package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.internal.StringUtil;

public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {
  private final Class<? extends T> clazz;
  
  public ReflectiveChannelFactory(Class<? extends T> clazz) {
    if (clazz == null)
      throw new NullPointerException("clazz"); 
    this.clazz = clazz;
  }
  
  public T newChannel() {
    try {
      return (T)this.clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
    } catch (Throwable t) {
      throw new ChannelException("Unable to create Channel from class " + this.clazz, t);
    } 
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this.clazz) + ".class";
  }
}
