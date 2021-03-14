package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Transformer;

public class CloneTransformer<T> implements Transformer<T, T>, Serializable {
  private static final long serialVersionUID = -8188742709499652567L;
  
  public static final Transformer INSTANCE = new CloneTransformer();
  
  public static <T> Transformer<T, T> cloneTransformer() {
    return INSTANCE;
  }
  
  public T transform(T input) {
    if (input == null)
      return null; 
    return (T)PrototypeFactory.<T>prototypeFactory(input).create();
  }
  
  private Object readResolve() {
    return INSTANCE;
  }
}
