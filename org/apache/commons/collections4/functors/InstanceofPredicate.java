package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Predicate;

public final class InstanceofPredicate implements Predicate<Object>, Serializable {
  private static final long serialVersionUID = -6682656911025165584L;
  
  private final Class<?> iType;
  
  public static Predicate<Object> instanceOfPredicate(Class<?> type) {
    if (type == null)
      throw new IllegalArgumentException("The type to check instanceof must not be null"); 
    return new InstanceofPredicate(type);
  }
  
  public InstanceofPredicate(Class<?> type) {
    this.iType = type;
  }
  
  public boolean evaluate(Object object) {
    return this.iType.isInstance(object);
  }
  
  public Class<?> getType() {
    return this.iType;
  }
}
