package org.apache.commons.collections4.queue;

import java.util.Collection;
import java.util.Queue;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;

public abstract class AbstractQueueDecorator<E> extends AbstractCollectionDecorator<E> implements Queue<E> {
  private static final long serialVersionUID = -2629815475789577029L;
  
  protected AbstractQueueDecorator() {}
  
  protected AbstractQueueDecorator(Queue<E> queue) {
    super(queue);
  }
  
  protected Queue<E> decorated() {
    return (Queue<E>)super.decorated();
  }
  
  public boolean offer(E obj) {
    return decorated().offer(obj);
  }
  
  public E poll() {
    return decorated().poll();
  }
  
  public E peek() {
    return decorated().peek();
  }
  
  public E element() {
    return decorated().element();
  }
  
  public E remove() {
    return decorated().remove();
  }
}
