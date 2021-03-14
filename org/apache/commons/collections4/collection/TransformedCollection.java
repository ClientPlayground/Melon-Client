package org.apache.commons.collections4.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections4.Transformer;

public class TransformedCollection<E> extends AbstractCollectionDecorator<E> {
  private static final long serialVersionUID = 8692300188161871514L;
  
  protected final Transformer<? super E, ? extends E> transformer;
  
  public static <E> TransformedCollection<E> transformingCollection(Collection<E> coll, Transformer<? super E, ? extends E> transformer) {
    return new TransformedCollection<E>(coll, transformer);
  }
  
  public static <E> TransformedCollection<E> transformedCollection(Collection<E> collection, Transformer<? super E, ? extends E> transformer) {
    TransformedCollection<E> decorated = new TransformedCollection<E>(collection, transformer);
    if (collection.size() > 0) {
      E[] values = (E[])collection.toArray();
      collection.clear();
      for (E value : values)
        decorated.decorated().add(transformer.transform(value)); 
    } 
    return decorated;
  }
  
  protected TransformedCollection(Collection<E> coll, Transformer<? super E, ? extends E> transformer) {
    super(coll);
    if (transformer == null)
      throw new IllegalArgumentException("Transformer must not be null"); 
    this.transformer = transformer;
  }
  
  protected E transform(E object) {
    return (E)this.transformer.transform(object);
  }
  
  protected Collection<E> transform(Collection<? extends E> coll) {
    List<E> list = new ArrayList<E>(coll.size());
    for (E item : coll)
      list.add(transform(item)); 
    return list;
  }
  
  public boolean add(E object) {
    return decorated().add(transform(object));
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    return decorated().addAll(transform(coll));
  }
}
