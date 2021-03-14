package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Transformer;

public class ConstantTransformer<I, O> implements Transformer<I, O>, Serializable {
  private static final long serialVersionUID = 6374440726369055124L;
  
  public static final Transformer NULL_INSTANCE = new ConstantTransformer(null);
  
  private final O iConstant;
  
  public static <I, O> Transformer<I, O> nullTransformer() {
    return NULL_INSTANCE;
  }
  
  public static <I, O> Transformer<I, O> constantTransformer(O constantToReturn) {
    if (constantToReturn == null)
      return nullTransformer(); 
    return new ConstantTransformer<I, O>(constantToReturn);
  }
  
  public ConstantTransformer(O constantToReturn) {
    this.iConstant = constantToReturn;
  }
  
  public O transform(I input) {
    return this.iConstant;
  }
  
  public O getConstant() {
    return this.iConstant;
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof ConstantTransformer))
      return false; 
    Object otherConstant = ((ConstantTransformer)obj).getConstant();
    return (otherConstant == getConstant() || (otherConstant != null && otherConstant.equals(getConstant())));
  }
  
  public int hashCode() {
    int result = "ConstantTransformer".hashCode() << 2;
    if (getConstant() != null)
      result |= getConstant().hashCode(); 
    return result;
  }
}
