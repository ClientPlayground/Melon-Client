package org.apache.commons.collections4.functors;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

public class SwitchTransformer<I, O> implements Transformer<I, O>, Serializable {
  private static final long serialVersionUID = -6404460890903469332L;
  
  private final Predicate<? super I>[] iPredicates;
  
  private final Transformer<? super I, ? extends O>[] iTransformers;
  
  private final Transformer<? super I, ? extends O> iDefault;
  
  public static <I, O> Transformer<I, O> switchTransformer(Predicate<? super I>[] predicates, Transformer<? super I, ? extends O>[] transformers, Transformer<? super I, ? extends O> defaultTransformer) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    FunctorUtils.validate((Transformer<?, ?>[])transformers);
    if (predicates.length != transformers.length)
      throw new IllegalArgumentException("The predicate and transformer arrays must be the same size"); 
    if (predicates.length == 0)
      return (defaultTransformer == null) ? ConstantTransformer.<I, O>nullTransformer() : (Transformer)defaultTransformer; 
    return new SwitchTransformer<I, O>(predicates, transformers, defaultTransformer);
  }
  
  public static <I, O> Transformer<I, O> switchTransformer(Map<? extends Predicate<? super I>, ? extends Transformer<? super I, ? extends O>> map) {
    if (map == null)
      throw new IllegalArgumentException("The predicate and transformer map must not be null"); 
    if (map.size() == 0)
      return ConstantTransformer.nullTransformer(); 
    Transformer<? super I, ? extends O> defaultTransformer = map.remove(null);
    int size = map.size();
    if (size == 0)
      return (defaultTransformer == null) ? ConstantTransformer.<I, O>nullTransformer() : (Transformer)defaultTransformer; 
    Transformer[] arrayOfTransformer = new Transformer[size];
    Predicate[] arrayOfPredicate = new Predicate[size];
    int i = 0;
    for (Map.Entry<? extends Predicate<? super I>, ? extends Transformer<? super I, ? extends O>> entry : map.entrySet()) {
      arrayOfPredicate[i] = entry.getKey();
      arrayOfTransformer[i] = entry.getValue();
      i++;
    } 
    return new SwitchTransformer<I, O>(false, (Predicate<? super I>[])arrayOfPredicate, (Transformer<? super I, ? extends O>[])arrayOfTransformer, defaultTransformer);
  }
  
  private SwitchTransformer(boolean clone, Predicate<? super I>[] predicates, Transformer<? super I, ? extends O>[] transformers, Transformer<? super I, ? extends O> defaultTransformer) {
    this.iPredicates = clone ? (Predicate<? super I>[])FunctorUtils.<I>copy(predicates) : predicates;
    this.iTransformers = clone ? (Transformer<? super I, ? extends O>[])FunctorUtils.<I, O>copy(transformers) : transformers;
    this.iDefault = (defaultTransformer == null) ? ConstantTransformer.<I, O>nullTransformer() : defaultTransformer;
  }
  
  public SwitchTransformer(Predicate<? super I>[] predicates, Transformer<? super I, ? extends O>[] transformers, Transformer<? super I, ? extends O> defaultTransformer) {
    this(true, predicates, transformers, defaultTransformer);
  }
  
  public O transform(I input) {
    for (int i = 0; i < this.iPredicates.length; i++) {
      if (this.iPredicates[i].evaluate(input) == true)
        return (O)this.iTransformers[i].transform(input); 
    } 
    return (O)this.iDefault.transform(input);
  }
  
  public Predicate<? super I>[] getPredicates() {
    return (Predicate<? super I>[])FunctorUtils.copy(this.iPredicates);
  }
  
  public Transformer<? super I, ? extends O>[] getTransformers() {
    return (Transformer<? super I, ? extends O>[])FunctorUtils.copy(this.iTransformers);
  }
  
  public Transformer<? super I, ? extends O> getDefaultTransformer() {
    return this.iDefault;
  }
}
