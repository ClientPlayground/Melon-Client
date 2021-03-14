package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;

public class ReferenceIdentityMap<K, V> extends AbstractReferenceMap<K, V> implements Serializable {
  private static final long serialVersionUID = -1266190134568365852L;
  
  public ReferenceIdentityMap() {
    super(AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.SOFT, 16, 0.75F, false);
  }
  
  public ReferenceIdentityMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType) {
    super(keyType, valueType, 16, 0.75F, false);
  }
  
  public ReferenceIdentityMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, boolean purgeValues) {
    super(keyType, valueType, 16, 0.75F, purgeValues);
  }
  
  public ReferenceIdentityMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, int capacity, float loadFactor) {
    super(keyType, valueType, capacity, loadFactor, false);
  }
  
  public ReferenceIdentityMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, int capacity, float loadFactor, boolean purgeValues) {
    super(keyType, valueType, capacity, loadFactor, purgeValues);
  }
  
  protected int hash(Object key) {
    return System.identityHashCode(key);
  }
  
  protected int hashEntry(Object key, Object value) {
    return System.identityHashCode(key) ^ System.identityHashCode(value);
  }
  
  protected boolean isEqualKey(Object key1, Object key2) {
    key2 = isKeyType(AbstractReferenceMap.ReferenceStrength.HARD) ? key2 : ((Reference)key2).get();
    return (key1 == key2);
  }
  
  protected boolean isEqualValue(Object value1, Object value2) {
    return (value1 == value2);
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
