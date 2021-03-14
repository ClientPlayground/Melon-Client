package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.Recycler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class RecyclableArrayList extends ArrayList<Object> {
  private static final long serialVersionUID = -8605125654176467947L;
  
  private static final int DEFAULT_INITIAL_CAPACITY = 8;
  
  private static final Recycler<RecyclableArrayList> RECYCLER = new Recycler<RecyclableArrayList>() {
      protected RecyclableArrayList newObject(Recycler.Handle<RecyclableArrayList> handle) {
        return new RecyclableArrayList(handle);
      }
    };
  
  private boolean insertSinceRecycled;
  
  private final Recycler.Handle<RecyclableArrayList> handle;
  
  public static RecyclableArrayList newInstance() {
    return newInstance(8);
  }
  
  public static RecyclableArrayList newInstance(int minCapacity) {
    RecyclableArrayList ret = (RecyclableArrayList)RECYCLER.get();
    ret.ensureCapacity(minCapacity);
    return ret;
  }
  
  private RecyclableArrayList(Recycler.Handle<RecyclableArrayList> handle) {
    this(handle, 8);
  }
  
  private RecyclableArrayList(Recycler.Handle<RecyclableArrayList> handle, int initialCapacity) {
    super(initialCapacity);
    this.handle = handle;
  }
  
  public boolean addAll(Collection<?> c) {
    checkNullElements(c);
    if (super.addAll(c)) {
      this.insertSinceRecycled = true;
      return true;
    } 
    return false;
  }
  
  public boolean addAll(int index, Collection<?> c) {
    checkNullElements(c);
    if (super.addAll(index, c)) {
      this.insertSinceRecycled = true;
      return true;
    } 
    return false;
  }
  
  private static void checkNullElements(Collection<?> c) {
    if (c instanceof java.util.RandomAccess && c instanceof List) {
      List<?> list = (List)c;
      int size = list.size();
      for (int i = 0; i < size; i++) {
        if (list.get(i) == null)
          throw new IllegalArgumentException("c contains null values"); 
      } 
    } else {
      for (Object element : c) {
        if (element == null)
          throw new IllegalArgumentException("c contains null values"); 
      } 
    } 
  }
  
  public boolean add(Object element) {
    if (element == null)
      throw new NullPointerException("element"); 
    if (super.add(element)) {
      this.insertSinceRecycled = true;
      return true;
    } 
    return false;
  }
  
  public void add(int index, Object element) {
    if (element == null)
      throw new NullPointerException("element"); 
    super.add(index, element);
    this.insertSinceRecycled = true;
  }
  
  public Object set(int index, Object element) {
    if (element == null)
      throw new NullPointerException("element"); 
    Object old = super.set(index, element);
    this.insertSinceRecycled = true;
    return old;
  }
  
  public boolean insertSinceRecycled() {
    return this.insertSinceRecycled;
  }
  
  public boolean recycle() {
    clear();
    this.insertSinceRecycled = false;
    this.handle.recycle(this);
    return true;
  }
}
