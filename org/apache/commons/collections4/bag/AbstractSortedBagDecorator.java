package org.apache.commons.collections4.bag;

import java.util.Collection;
import java.util.Comparator;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;

public abstract class AbstractSortedBagDecorator<E> extends AbstractBagDecorator<E> implements SortedBag<E> {
  private static final long serialVersionUID = -8223473624050467718L;
  
  protected AbstractSortedBagDecorator() {}
  
  protected AbstractSortedBagDecorator(SortedBag<E> bag) {
    super((Bag<E>)bag);
  }
  
  protected SortedBag<E> decorated() {
    return (SortedBag<E>)super.decorated();
  }
  
  public E first() {
    return (E)decorated().first();
  }
  
  public E last() {
    return (E)decorated().last();
  }
  
  public Comparator<? super E> comparator() {
    return decorated().comparator();
  }
}
