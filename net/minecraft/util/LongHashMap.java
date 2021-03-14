package net.minecraft.util;

public class LongHashMap<V> {
  private transient Entry<V>[] hashArray = (Entry<V>[])new Entry[4096];
  
  private transient int numHashElements;
  
  private int mask;
  
  private int capacity = 3072;
  
  private final float percentUseable = 0.75F;
  
  private volatile transient int modCount;
  
  public LongHashMap() {
    this.mask = this.hashArray.length - 1;
  }
  
  private static int getHashedKey(long originalKey) {
    return (int)(originalKey ^ originalKey >>> 27L);
  }
  
  private static int hash(int integer) {
    integer = integer ^ integer >>> 20 ^ integer >>> 12;
    return integer ^ integer >>> 7 ^ integer >>> 4;
  }
  
  private static int getHashIndex(int p_76158_0_, int p_76158_1_) {
    return p_76158_0_ & p_76158_1_;
  }
  
  public int getNumHashElements() {
    return this.numHashElements;
  }
  
  public V getValueByKey(long p_76164_1_) {
    int i = getHashedKey(p_76164_1_);
    for (Entry<V> entry = this.hashArray[getHashIndex(i, this.mask)]; entry != null; entry = entry.nextEntry) {
      if (entry.key == p_76164_1_)
        return entry.value; 
    } 
    return null;
  }
  
  public boolean containsItem(long p_76161_1_) {
    return (getEntry(p_76161_1_) != null);
  }
  
  final Entry<V> getEntry(long p_76160_1_) {
    int i = getHashedKey(p_76160_1_);
    for (Entry<V> entry = this.hashArray[getHashIndex(i, this.mask)]; entry != null; entry = entry.nextEntry) {
      if (entry.key == p_76160_1_)
        return entry; 
    } 
    return null;
  }
  
  public void add(long p_76163_1_, V p_76163_3_) {
    int i = getHashedKey(p_76163_1_);
    int j = getHashIndex(i, this.mask);
    for (Entry<V> entry = this.hashArray[j]; entry != null; entry = entry.nextEntry) {
      if (entry.key == p_76163_1_) {
        entry.value = p_76163_3_;
        return;
      } 
    } 
    this.modCount++;
    createKey(i, p_76163_1_, p_76163_3_, j);
  }
  
  private void resizeTable(int p_76153_1_) {
    Entry<V>[] entry = this.hashArray;
    int i = entry.length;
    if (i == 1073741824) {
      this.capacity = Integer.MAX_VALUE;
    } else {
      Entry[] arrayOfEntry = new Entry[p_76153_1_];
      copyHashTableTo((Entry<V>[])arrayOfEntry);
      this.hashArray = (Entry<V>[])arrayOfEntry;
      this.mask = this.hashArray.length - 1;
      float f = p_76153_1_;
      getClass();
      this.capacity = (int)(f * 0.75F);
    } 
  }
  
  private void copyHashTableTo(Entry<V>[] p_76154_1_) {
    Entry<V>[] entry = this.hashArray;
    int i = p_76154_1_.length;
    for (int j = 0; j < entry.length; j++) {
      Entry<V> entry1 = entry[j];
      if (entry1 != null) {
        Entry<V> entry2;
        entry[j] = null;
        do {
          entry2 = entry1.nextEntry;
          int k = getHashIndex(entry1.hash, i - 1);
          entry1.nextEntry = p_76154_1_[k];
          p_76154_1_[k] = entry1;
          entry1 = entry2;
        } while (entry2 != null);
      } 
    } 
  }
  
  public V remove(long p_76159_1_) {
    Entry<V> entry = removeKey(p_76159_1_);
    return (entry == null) ? null : entry.value;
  }
  
  final Entry<V> removeKey(long p_76152_1_) {
    int i = getHashedKey(p_76152_1_);
    int j = getHashIndex(i, this.mask);
    Entry<V> entry = this.hashArray[j];
    Entry<V> entry1;
    for (entry1 = entry; entry1 != null; entry1 = entry2) {
      Entry<V> entry2 = entry1.nextEntry;
      if (entry1.key == p_76152_1_) {
        this.modCount++;
        this.numHashElements--;
        if (entry == entry1) {
          this.hashArray[j] = entry2;
        } else {
          entry.nextEntry = entry2;
        } 
        return entry1;
      } 
      entry = entry1;
    } 
    return entry1;
  }
  
  private void createKey(int p_76156_1_, long p_76156_2_, V p_76156_4_, int p_76156_5_) {
    Entry<V> entry = this.hashArray[p_76156_5_];
    this.hashArray[p_76156_5_] = new Entry<>(p_76156_1_, p_76156_2_, p_76156_4_, entry);
    if (this.numHashElements++ >= this.capacity)
      resizeTable(2 * this.hashArray.length); 
  }
  
  public double getKeyDistribution() {
    int i = 0;
    for (int j = 0; j < this.hashArray.length; j++) {
      if (this.hashArray[j] != null)
        i++; 
    } 
    return 1.0D * i / this.numHashElements;
  }
  
  static class Entry<V> {
    final long key;
    
    V value;
    
    Entry<V> nextEntry;
    
    final int hash;
    
    Entry(int p_i1553_1_, long p_i1553_2_, V p_i1553_4_, Entry<V> p_i1553_5_) {
      this.value = p_i1553_4_;
      this.nextEntry = p_i1553_5_;
      this.key = p_i1553_2_;
      this.hash = p_i1553_1_;
    }
    
    public final long getKey() {
      return this.key;
    }
    
    public final V getValue() {
      return this.value;
    }
    
    public final boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof Entry))
        return false; 
      Entry<V> entry = (Entry<V>)p_equals_1_;
      Object object = Long.valueOf(getKey());
      Object object1 = Long.valueOf(entry.getKey());
      if (object == object1 || (object != null && object.equals(object1))) {
        Object object2 = getValue();
        Object object3 = entry.getValue();
        if (object2 == object3 || (object2 != null && object2.equals(object3)))
          return true; 
      } 
      return false;
    }
    
    public final int hashCode() {
      return LongHashMap.getHashedKey(this.key);
    }
    
    public final String toString() {
      return getKey() + "=" + getValue();
    }
  }
}
