package org.apache.commons.collections4.bag;

import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;

public abstract class AbstractBagDecorator<E> extends AbstractCollectionDecorator<E> implements Bag<E> {
  private static final long serialVersionUID = -3768146017343785417L;
  
  protected AbstractBagDecorator() {}
  
  protected AbstractBagDecorator(Bag<E> bag) {
    super((Collection)bag);
  }
  
  protected Bag<E> decorated() {
    return (Bag<E>)super.decorated();
  }
  
  public int getCount(Object object) {
    return decorated().getCount(object);
  }
  
  public boolean add(E object, int count) {
    return decorated().add(object, count);
  }
  
  public boolean remove(Object object, int count) {
    return decorated().remove(object, count);
  }
  
  public Set<E> uniqueSet() {
    return decorated().uniqueSet();
  }
}
