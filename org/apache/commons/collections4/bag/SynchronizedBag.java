package org.apache.commons.collections4.bag;

import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.collection.SynchronizedCollection;

public class SynchronizedBag<E> extends SynchronizedCollection<E> implements Bag<E> {
  private static final long serialVersionUID = 8084674570753837109L;
  
  public static <E> SynchronizedBag<E> synchronizedBag(Bag<E> bag) {
    return new SynchronizedBag<E>(bag);
  }
  
  protected SynchronizedBag(Bag<E> bag) {
    super((Collection)bag);
  }
  
  protected SynchronizedBag(Bag<E> bag, Object lock) {
    super((Collection)bag, lock);
  }
  
  protected Bag<E> getBag() {
    return (Bag<E>)decorated();
  }
  
  public boolean add(E object, int count) {
    synchronized (this.lock) {
      return getBag().add(object, count);
    } 
  }
  
  public boolean remove(Object object, int count) {
    synchronized (this.lock) {
      return getBag().remove(object, count);
    } 
  }
  
  public Set<E> uniqueSet() {
    synchronized (this.lock) {
      Set<E> set = getBag().uniqueSet();
      return new SynchronizedBagSet(set, this.lock);
    } 
  }
  
  public int getCount(Object object) {
    synchronized (this.lock) {
      return getBag().getCount(object);
    } 
  }
  
  class SynchronizedBagSet extends SynchronizedCollection<E> implements Set<E> {
    private static final long serialVersionUID = 2990565892366827855L;
    
    SynchronizedBagSet(Set<E> set, Object lock) {
      super(set, lock);
    }
  }
}
