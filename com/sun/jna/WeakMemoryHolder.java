package com.sun.jna;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;

public class WeakMemoryHolder {
  ReferenceQueue<Object> referenceQueue = new ReferenceQueue();
  
  IdentityHashMap<Reference<Object>, Memory> backingMap = new IdentityHashMap<Reference<Object>, Memory>();
  
  public synchronized void put(Object o, Memory m) {
    clean();
    Reference<Object> reference = new WeakReference(o, this.referenceQueue);
    this.backingMap.put(reference, m);
  }
  
  public synchronized void clean() {
    for (Reference<?> ref = this.referenceQueue.poll(); ref != null; ref = this.referenceQueue.poll())
      this.backingMap.remove(ref); 
  }
}
