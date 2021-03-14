package net.optifine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class IteratorCache {
  private static Deque<IteratorReusable<Object>> dequeIterators = new ArrayDeque<>();
  
  public static Iterator<Object> getReadOnly(List list) {
    synchronized (dequeIterators) {
      IteratorReusable<Object> iteratorreusable = dequeIterators.pollFirst();
      if (iteratorreusable == null)
        iteratorreusable = new IteratorReadOnly(); 
      iteratorreusable.setList(list);
      return iteratorreusable;
    } 
  }
  
  private static void finished(IteratorReusable<Object> iterator) {
    synchronized (dequeIterators) {
      if (dequeIterators.size() <= 1000) {
        iterator.setList(null);
        dequeIterators.addLast(iterator);
      } 
    } 
  }
  
  static {
    for (int i = 0; i < 1000; i++) {
      IteratorReadOnly iteratorcache$iteratorreadonly = new IteratorReadOnly();
      dequeIterators.add(iteratorcache$iteratorreadonly);
    } 
  }
  
  public static interface IteratorReusable<E> extends Iterator<E> {
    void setList(List<E> param1List);
  }
  
  public static class IteratorReadOnly implements IteratorReusable<Object> {
    private List<Object> list;
    
    private int index;
    
    private boolean hasNext;
    
    public void setList(List<Object> list) {
      if (this.hasNext)
        throw new RuntimeException("Iterator still used, oldList: " + this.list + ", newList: " + list); 
      this.list = list;
      this.index = 0;
      this.hasNext = (list != null && this.index < list.size());
    }
    
    public Object next() {
      if (!this.hasNext)
        return null; 
      Object object = this.list.get(this.index);
      this.index++;
      this.hasNext = (this.index < this.list.size());
      return object;
    }
    
    public boolean hasNext() {
      if (!this.hasNext) {
        IteratorCache.finished(this);
        return false;
      } 
      return this.hasNext;
    }
    
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
