package com.github.steveice10.netty.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultAttributeMap implements AttributeMap {
  private static final AtomicReferenceFieldUpdater<DefaultAttributeMap, AtomicReferenceArray> updater = AtomicReferenceFieldUpdater.newUpdater(DefaultAttributeMap.class, AtomicReferenceArray.class, "attributes");
  
  private static final int BUCKET_SIZE = 4;
  
  private static final int MASK = 3;
  
  private volatile AtomicReferenceArray<DefaultAttribute<?>> attributes;
  
  public <T> Attribute<T> attr(AttributeKey<T> key) {
    if (key == null)
      throw new NullPointerException("key"); 
    AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
    if (attributes == null) {
      attributes = new AtomicReferenceArray<DefaultAttribute<?>>(4);
      if (!updater.compareAndSet(this, null, attributes))
        attributes = this.attributes; 
    } 
    int i = index(key);
    DefaultAttribute<?> head = attributes.get(i);
    if (head == null) {
      head = new DefaultAttribute();
      DefaultAttribute<T> attr = new DefaultAttribute<T>(head, key);
      head.next = attr;
      attr.prev = head;
      if (attributes.compareAndSet(i, null, head))
        return attr; 
      head = attributes.get(i);
    } 
    synchronized (head) {
      DefaultAttribute<?> curr = head;
      while (true) {
        DefaultAttribute<?> next = curr.next;
        if (next == null) {
          DefaultAttribute<T> attr = new DefaultAttribute<T>(head, key);
          curr.next = attr;
          attr.prev = curr;
          return attr;
        } 
        if (next.key == key && !next.removed)
          return (Attribute)next; 
        curr = next;
      } 
    } 
  }
  
  public <T> boolean hasAttr(AttributeKey<T> key) {
    if (key == null)
      throw new NullPointerException("key"); 
    AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
    if (attributes == null)
      return false; 
    int i = index(key);
    DefaultAttribute<?> head = attributes.get(i);
    if (head == null)
      return false; 
    synchronized (head) {
      DefaultAttribute<?> curr = head.next;
      while (curr != null) {
        if (curr.key == key && !curr.removed)
          return true; 
        curr = curr.next;
      } 
      return false;
    } 
  }
  
  private static int index(AttributeKey<?> key) {
    return key.id() & 0x3;
  }
  
  private static final class DefaultAttribute<T> extends AtomicReference<T> implements Attribute<T> {
    private static final long serialVersionUID = -2661411462200283011L;
    
    private final DefaultAttribute<?> head;
    
    private final AttributeKey<T> key;
    
    private DefaultAttribute<?> prev;
    
    private DefaultAttribute<?> next;
    
    private volatile boolean removed;
    
    DefaultAttribute(DefaultAttribute<?> head, AttributeKey<T> key) {
      this.head = head;
      this.key = key;
    }
    
    DefaultAttribute() {
      this.head = this;
      this.key = null;
    }
    
    public AttributeKey<T> key() {
      return this.key;
    }
    
    public T setIfAbsent(T value) {
      while (!compareAndSet(null, value)) {
        T old = get();
        if (old != null)
          return old; 
      } 
      return null;
    }
    
    public T getAndRemove() {
      this.removed = true;
      T oldValue = getAndSet(null);
      remove0();
      return oldValue;
    }
    
    public void remove() {
      this.removed = true;
      set(null);
      remove0();
    }
    
    private void remove0() {
      synchronized (this.head) {
        if (this.prev == null)
          return; 
        this.prev.next = this.next;
        if (this.next != null)
          this.next.prev = this.prev; 
        this.prev = null;
        this.next = null;
      } 
    }
  }
}
