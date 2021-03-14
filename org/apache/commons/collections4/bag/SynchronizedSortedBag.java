package org.apache.commons.collections4.bag;

import java.util.Comparator;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;

public class SynchronizedSortedBag<E> extends SynchronizedBag<E> implements SortedBag<E> {
  private static final long serialVersionUID = 722374056718497858L;
  
  public static <E> SynchronizedSortedBag<E> synchronizedSortedBag(SortedBag<E> bag) {
    return new SynchronizedSortedBag<E>(bag);
  }
  
  protected SynchronizedSortedBag(SortedBag<E> bag) {
    super((Bag<E>)bag);
  }
  
  protected SynchronizedSortedBag(Bag<E> bag, Object lock) {
    super(bag, lock);
  }
  
  protected SortedBag<E> getSortedBag() {
    return (SortedBag<E>)decorated();
  }
  
  public synchronized E first() {
    synchronized (this.lock) {
      return (E)getSortedBag().first();
    } 
  }
  
  public synchronized E last() {
    synchronized (this.lock) {
      return (E)getSortedBag().last();
    } 
  }
  
  public synchronized Comparator<? super E> comparator() {
    synchronized (this.lock) {
      return getSortedBag().comparator();
    } 
  }
}
