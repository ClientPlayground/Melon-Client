package org.apache.commons.collections4.queue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class UnmodifiableQueue<E> extends AbstractQueueDecorator<E> implements Unmodifiable {
  private static final long serialVersionUID = 1832948656215393357L;
  
  public static <E> Queue<E> unmodifiableQueue(Queue<? extends E> queue) {
    if (queue instanceof Unmodifiable)
      return (Queue)queue; 
    return new UnmodifiableQueue<E>(queue);
  }
  
  private UnmodifiableQueue(Queue<? extends E> queue) {
    super((Queue)queue);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(decorated());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    setCollection((Collection)in.readObject());
  }
  
  public Iterator<E> iterator() {
    return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
  }
  
  public boolean add(Object object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    throw new UnsupportedOperationException();
  }
  
  public void clear() {
    throw new UnsupportedOperationException();
  }
  
  public boolean remove(Object object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean removeAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }
  
  public boolean retainAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }
  
  public boolean offer(E obj) {
    throw new UnsupportedOperationException();
  }
  
  public E poll() {
    throw new UnsupportedOperationException();
  }
  
  public E remove() {
    throw new UnsupportedOperationException();
  }
}
