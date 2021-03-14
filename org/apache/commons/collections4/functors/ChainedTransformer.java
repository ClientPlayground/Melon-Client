package org.apache.commons.collections4.functors;

import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.collections4.Transformer;

public class ChainedTransformer<T> implements Transformer<T, T>, Serializable {
  private static final long serialVersionUID = 3514945074733160196L;
  
  private final Transformer<? super T, ? extends T>[] iTransformers;
  
  public static <T> Transformer<T, T> chainedTransformer(Transformer<? super T, ? extends T>... transformers) {
    FunctorUtils.validate((Transformer<?, ?>[])transformers);
    if (transformers.length == 0)
      return NOPTransformer.nopTransformer(); 
    return new ChainedTransformer<T>(transformers);
  }
  
  public static <T> Transformer<T, T> chainedTransformer(Collection<? extends Transformer<T, T>> transformers) {
    if (transformers == null)
      throw new IllegalArgumentException("Transformer collection must not be null"); 
    if (transformers.size() == 0)
      return NOPTransformer.nopTransformer(); 
    Transformer[] arrayOfTransformer = transformers.<Transformer>toArray(new Transformer[transformers.size()]);
    FunctorUtils.validate((Transformer<?, ?>[])arrayOfTransformer);
    return new ChainedTransformer<T>(false, (Transformer<? super T, ? extends T>[])arrayOfTransformer);
  }
  
  private ChainedTransformer(boolean clone, Transformer<? super T, ? extends T>[] transformers) {
    this.iTransformers = clone ? (Transformer<? super T, ? extends T>[])FunctorUtils.<T, T>copy(transformers) : transformers;
  }
  
  public ChainedTransformer(Transformer<? super T, ? extends T>... transformers) {
    this(true, transformers);
  }
  
  public T transform(T object) {
    for (Transformer<? super T, ? extends T> iTransformer : this.iTransformers)
      object = (T)iTransformer.transform(object); 
    return object;
  }
  
  public Transformer<? super T, ? extends T>[] getTransformers() {
    return (Transformer<? super T, ? extends T>[])FunctorUtils.copy(this.iTransformers);
  }
}
