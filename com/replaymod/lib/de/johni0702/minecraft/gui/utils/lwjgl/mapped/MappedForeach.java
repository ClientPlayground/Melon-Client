package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.mapped;

import java.util.Iterator;

final class MappedForeach<T extends MappedObject> implements Iterable<T> {
  final T mapped;
  
  final int elementCount;
  
  MappedForeach(T mapped, int elementCount) {
    this.mapped = mapped;
    this.elementCount = elementCount;
  }
  
  public Iterator<T> iterator() {
    return new Iterator<T>() {
        private int index;
        
        public boolean hasNext() {
          return (this.index < MappedForeach.this.elementCount);
        }
        
        public T next() {
          MappedForeach.this.mapped.setViewAddress(MappedForeach.this.mapped.getViewAddress(this.index++));
          return MappedForeach.this.mapped;
        }
        
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }
}
