package org.apache.commons.collections4.bidimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.BidiMap;

public class DualHashBidiMap<K, V> extends AbstractDualBidiMap<K, V> implements Serializable {
  private static final long serialVersionUID = 721969328361808L;
  
  public DualHashBidiMap() {
    super(new HashMap<K, V>(), new HashMap<V, K>());
  }
  
  public DualHashBidiMap(Map<? extends K, ? extends V> map) {
    super(new HashMap<K, V>(), new HashMap<V, K>());
    putAll(map);
  }
  
  protected DualHashBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseBidiMap) {
    super(normalMap, reverseMap, inverseBidiMap);
  }
  
  protected BidiMap<V, K> createBidiMap(Map<V, K> normalMap, Map<K, V> reverseMap, BidiMap<K, V> inverseBidiMap) {
    return new DualHashBidiMap(normalMap, reverseMap, inverseBidiMap);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(this.normalMap);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.normalMap = new HashMap<K, V>();
    this.reverseMap = new HashMap<V, K>();
    Map<K, V> map = (Map<K, V>)in.readObject();
    putAll(map);
  }
}
