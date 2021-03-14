package com.github.steveice10.netty.util;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant<T> {
  private static final AtomicLong uniqueIdGenerator = new AtomicLong();
  
  private final int id;
  
  private final String name;
  
  private final long uniquifier;
  
  protected AbstractConstant(int id, String name) {
    this.id = id;
    this.name = name;
    this.uniquifier = uniqueIdGenerator.getAndIncrement();
  }
  
  public final String name() {
    return this.name;
  }
  
  public final int id() {
    return this.id;
  }
  
  public final String toString() {
    return name();
  }
  
  public final int hashCode() {
    return super.hashCode();
  }
  
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }
  
  public final int compareTo(T o) {
    if (this == o)
      return 0; 
    T t = o;
    int returnCode = hashCode() - t.hashCode();
    if (returnCode != 0)
      return returnCode; 
    if (this.uniquifier < ((AbstractConstant)t).uniquifier)
      return -1; 
    if (this.uniquifier > ((AbstractConstant)t).uniquifier)
      return 1; 
    throw new Error("failed to compare two different constants");
  }
}
