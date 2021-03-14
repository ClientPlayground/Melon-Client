package org.apache.commons.collections4.queue;

import java.util.Queue;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.collection.TransformedCollection;

public class TransformedQueue<E> extends TransformedCollection<E> implements Queue<E> {
  private static final long serialVersionUID = -7901091318986132033L;
  
  public static <E> TransformedQueue<E> transformingQueue(Queue<E> queue, Transformer<? super E, ? extends E> transformer) {
    return new TransformedQueue<E>(queue, transformer);
  }
  
  public static <E> TransformedQueue<E> transformedQueue(Queue<E> queue, Transformer<? super E, ? extends E> transformer) {
    TransformedQueue<E> decorated = new TransformedQueue<E>(queue, transformer);
    if (queue.size() > 0) {
      E[] values = (E[])queue.toArray();
      queue.clear();
      for (E value : values)
        decorated.decorated().add(transformer.transform(value)); 
    } 
    return decorated;
  }
  
  protected TransformedQueue(Queue<E> queue, Transformer<? super E, ? extends E> transformer) {
    super(queue, transformer);
  }
  
  protected Queue<E> getQueue() {
    return (Queue<E>)decorated();
  }
  
  public boolean offer(E obj) {
    return getQueue().offer((E)transform(obj));
  }
  
  public E poll() {
    return getQueue().poll();
  }
  
  public E peek() {
    return getQueue().peek();
  }
  
  public E element() {
    return getQueue().element();
  }
  
  public E remove() {
    return getQueue().remove();
  }
}
