package com.github.steveice10.netty.channel.nio;

import java.nio.channels.SelectionKey;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

final class SelectedSelectionKeySet extends AbstractSet<SelectionKey> {
  SelectionKey[] keys = new SelectionKey[1024];
  
  int size;
  
  public boolean add(SelectionKey o) {
    if (o == null)
      return false; 
    this.keys[this.size++] = o;
    if (this.size == this.keys.length)
      increaseCapacity(); 
    return true;
  }
  
  public int size() {
    return this.size;
  }
  
  public boolean remove(Object o) {
    return false;
  }
  
  public boolean contains(Object o) {
    return false;
  }
  
  public Iterator<SelectionKey> iterator() {
    throw new UnsupportedOperationException();
  }
  
  void reset() {
    reset(0);
  }
  
  void reset(int start) {
    Arrays.fill((Object[])this.keys, start, this.size, (Object)null);
    this.size = 0;
  }
  
  private void increaseCapacity() {
    SelectionKey[] newKeys = new SelectionKey[this.keys.length << 1];
    System.arraycopy(this.keys, 0, newKeys, 0, this.size);
    this.keys = newKeys;
  }
}
