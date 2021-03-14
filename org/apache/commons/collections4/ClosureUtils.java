package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.functors.ChainedClosure;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.functors.ExceptionClosure;
import org.apache.commons.collections4.functors.ForClosure;
import org.apache.commons.collections4.functors.IfClosure;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.functors.NOPClosure;
import org.apache.commons.collections4.functors.SwitchClosure;
import org.apache.commons.collections4.functors.TransformerClosure;
import org.apache.commons.collections4.functors.WhileClosure;

public class ClosureUtils {
  public static <E> Closure<E> exceptionClosure() {
    return ExceptionClosure.exceptionClosure();
  }
  
  public static <E> Closure<E> nopClosure() {
    return NOPClosure.nopClosure();
  }
  
  public static <E> Closure<E> asClosure(Transformer<? super E, ?> transformer) {
    return TransformerClosure.transformerClosure(transformer);
  }
  
  public static <E> Closure<E> forClosure(int count, Closure<? super E> closure) {
    return ForClosure.forClosure(count, closure);
  }
  
  public static <E> Closure<E> whileClosure(Predicate<? super E> predicate, Closure<? super E> closure) {
    return WhileClosure.whileClosure(predicate, closure, false);
  }
  
  public static <E> Closure<E> doWhileClosure(Closure<? super E> closure, Predicate<? super E> predicate) {
    return WhileClosure.whileClosure(predicate, closure, true);
  }
  
  public static <E> Closure<E> invokerClosure(String methodName) {
    return asClosure(InvokerTransformer.invokerTransformer(methodName));
  }
  
  public static <E> Closure<E> invokerClosure(String methodName, Class<?>[] paramTypes, Object[] args) {
    return asClosure(InvokerTransformer.invokerTransformer(methodName, paramTypes, args));
  }
  
  public static <E> Closure<E> chainedClosure(Closure<? super E>... closures) {
    return ChainedClosure.chainedClosure((Closure[])closures);
  }
  
  public static <E> Closure<E> chainedClosure(Collection<Closure<E>> closures) {
    return ChainedClosure.chainedClosure(closures);
  }
  
  public static <E> Closure<E> ifClosure(Predicate<? super E> predicate, Closure<? super E> trueClosure) {
    return IfClosure.ifClosure(predicate, trueClosure);
  }
  
  public static <E> Closure<E> ifClosure(Predicate<? super E> predicate, Closure<? super E> trueClosure, Closure<? super E> falseClosure) {
    return IfClosure.ifClosure(predicate, trueClosure, falseClosure);
  }
  
  public static <E> Closure<E> switchClosure(Predicate<? super E>[] predicates, Closure<? super E>[] closures) {
    return SwitchClosure.switchClosure((Predicate[])predicates, (Closure[])closures, null);
  }
  
  public static <E> Closure<E> switchClosure(Predicate<? super E>[] predicates, Closure<? super E>[] closures, Closure<? super E> defaultClosure) {
    return SwitchClosure.switchClosure((Predicate[])predicates, (Closure[])closures, defaultClosure);
  }
  
  public static <E> Closure<E> switchClosure(Map<Predicate<E>, Closure<E>> predicatesAndClosures) {
    return SwitchClosure.switchClosure(predicatesAndClosures);
  }
  
  public static <E> Closure<E> switchMapClosure(Map<? extends E, Closure<E>> objectsAndClosures) {
    if (objectsAndClosures == null)
      throw new IllegalArgumentException("The object and closure map must not be null"); 
    Closure<? super E> def = objectsAndClosures.remove(null);
    int size = objectsAndClosures.size();
    Closure[] arrayOfClosure = new Closure[size];
    Predicate[] arrayOfPredicate = new Predicate[size];
    int i = 0;
    for (Map.Entry<? extends E, Closure<E>> entry : objectsAndClosures.entrySet()) {
      arrayOfPredicate[i] = EqualPredicate.equalPredicate(entry.getKey());
      arrayOfClosure[i] = entry.getValue();
      i++;
    } 
    return switchClosure((Predicate<? super E>[])arrayOfPredicate, (Closure<? super E>[])arrayOfClosure, def);
  }
}
