package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.CloneTransformer;
import org.apache.commons.collections4.functors.ClosureTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.functors.ExceptionTransformer;
import org.apache.commons.collections4.functors.FactoryTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.functors.MapTransformer;
import org.apache.commons.collections4.functors.NOPTransformer;
import org.apache.commons.collections4.functors.PredicateTransformer;
import org.apache.commons.collections4.functors.StringValueTransformer;
import org.apache.commons.collections4.functors.SwitchTransformer;

public class TransformerUtils {
  public static <I, O> Transformer<I, O> exceptionTransformer() {
    return ExceptionTransformer.exceptionTransformer();
  }
  
  public static <I, O> Transformer<I, O> nullTransformer() {
    return ConstantTransformer.nullTransformer();
  }
  
  public static <T> Transformer<T, T> nopTransformer() {
    return NOPTransformer.nopTransformer();
  }
  
  public static <T> Transformer<T, T> cloneTransformer() {
    return CloneTransformer.cloneTransformer();
  }
  
  public static <I, O> Transformer<I, O> constantTransformer(O constantToReturn) {
    return ConstantTransformer.constantTransformer(constantToReturn);
  }
  
  public static <T> Transformer<T, T> asTransformer(Closure<? super T> closure) {
    return ClosureTransformer.closureTransformer(closure);
  }
  
  public static <T> Transformer<T, Boolean> asTransformer(Predicate<? super T> predicate) {
    return PredicateTransformer.predicateTransformer(predicate);
  }
  
  public static <I, O> Transformer<I, O> asTransformer(Factory<? extends O> factory) {
    return FactoryTransformer.factoryTransformer(factory);
  }
  
  public static <T> Transformer<T, T> chainedTransformer(Transformer<? super T, ? extends T>... transformers) {
    return ChainedTransformer.chainedTransformer((Transformer[])transformers);
  }
  
  public static <T> Transformer<T, T> chainedTransformer(Collection<? extends Transformer<T, T>> transformers) {
    return ChainedTransformer.chainedTransformer(transformers);
  }
  
  public static <I, O> Transformer<I, O> switchTransformer(Predicate<? super I> predicate, Transformer<? super I, ? extends O> trueTransformer, Transformer<? super I, ? extends O> falseTransformer) {
    return SwitchTransformer.switchTransformer(new Predicate[] { predicate }, new Transformer[] { trueTransformer }, falseTransformer);
  }
  
  public static <I, O> Transformer<I, O> switchTransformer(Predicate<? super I>[] predicates, Transformer<? super I, ? extends O>[] transformers) {
    return SwitchTransformer.switchTransformer((Predicate[])predicates, (Transformer[])transformers, null);
  }
  
  public static <I, O> Transformer<I, O> switchTransformer(Predicate<? super I>[] predicates, Transformer<? super I, ? extends O>[] transformers, Transformer<? super I, ? extends O> defaultTransformer) {
    return SwitchTransformer.switchTransformer((Predicate[])predicates, (Transformer[])transformers, defaultTransformer);
  }
  
  public static <I, O> Transformer<I, O> switchTransformer(Map<Predicate<I>, Transformer<I, O>> predicatesAndTransformers) {
    return SwitchTransformer.switchTransformer(predicatesAndTransformers);
  }
  
  public static <I, O> Transformer<I, O> switchMapTransformer(Map<I, Transformer<I, O>> objectsAndTransformers) {
    if (objectsAndTransformers == null)
      throw new IllegalArgumentException("The object and transformer map must not be null"); 
    Transformer<? super I, ? extends O> def = objectsAndTransformers.remove(null);
    int size = objectsAndTransformers.size();
    Transformer[] arrayOfTransformer = new Transformer[size];
    Predicate[] arrayOfPredicate = new Predicate[size];
    int i = 0;
    for (Map.Entry<I, Transformer<I, O>> entry : objectsAndTransformers.entrySet()) {
      arrayOfPredicate[i] = EqualPredicate.equalPredicate(entry.getKey());
      arrayOfTransformer[i++] = entry.getValue();
    } 
    return switchTransformer((Predicate<? super I>[])arrayOfPredicate, (Transformer<? super I, ? extends O>[])arrayOfTransformer, def);
  }
  
  public static <T> Transformer<Class<? extends T>, T> instantiateTransformer() {
    return InstantiateTransformer.instantiateTransformer();
  }
  
  public static <T> Transformer<Class<? extends T>, T> instantiateTransformer(Class<?>[] paramTypes, Object[] args) {
    return InstantiateTransformer.instantiateTransformer(paramTypes, args);
  }
  
  public static <I, O> Transformer<I, O> mapTransformer(Map<? super I, ? extends O> map) {
    return MapTransformer.mapTransformer(map);
  }
  
  public static <I, O> Transformer<I, O> invokerTransformer(String methodName) {
    return InvokerTransformer.invokerTransformer(methodName, null, null);
  }
  
  public static <I, O> Transformer<I, O> invokerTransformer(String methodName, Class<?>[] paramTypes, Object[] args) {
    return InvokerTransformer.invokerTransformer(methodName, paramTypes, args);
  }
  
  public static <T> Transformer<T, String> stringValueTransformer() {
    return StringValueTransformer.stringValueTransformer();
  }
}
