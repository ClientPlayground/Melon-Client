package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ReferenceMap<K, V> extends AbstractReferenceMap<K, V> implements Serializable {
  private static final long serialVersionUID = 1555089888138299607L;
  
  public ReferenceMap() {
    super(AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.SOFT, 16, 0.75F, false);
  }
  
  public ReferenceMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType) {
    super(keyType, valueType, 16, 0.75F, false);
  }
  
  public ReferenceMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, boolean purgeValues) {
    super(keyType, valueType, 16, 0.75F, purgeValues);
  }
  
  public ReferenceMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, int capacity, float loadFactor) {
    super(keyType, valueType, capacity, loadFactor, false);
  }
  
  public ReferenceMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, int capacity, float loadFactor, boolean purgeValues) {
    super(keyType, valueType, capacity, loadFactor, purgeValues);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    doWriteObject(out);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    doReadObject(in);
  }
}
