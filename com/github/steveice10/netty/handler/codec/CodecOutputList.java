package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.AbstractList;
import java.util.RandomAccess;

final class CodecOutputList extends AbstractList<Object> implements RandomAccess {
  private static final CodecOutputListRecycler NOOP_RECYCLER = new CodecOutputListRecycler() {
      public void recycle(CodecOutputList object) {}
    };
  
  private static final FastThreadLocal<CodecOutputLists> CODEC_OUTPUT_LISTS_POOL = new FastThreadLocal<CodecOutputLists>() {
      protected CodecOutputList.CodecOutputLists initialValue() throws Exception {
        return new CodecOutputList.CodecOutputLists(16);
      }
    };
  
  private final CodecOutputListRecycler recycler;
  
  private int size;
  
  private Object[] array;
  
  private boolean insertSinceRecycled;
  
  private static interface CodecOutputListRecycler {
    void recycle(CodecOutputList param1CodecOutputList);
  }
  
  private static final class CodecOutputLists implements CodecOutputListRecycler {
    private final CodecOutputList[] elements;
    
    private final int mask;
    
    private int currentIdx;
    
    private int count;
    
    CodecOutputLists(int numElements) {
      this.elements = new CodecOutputList[MathUtil.safeFindNextPositivePowerOfTwo(numElements)];
      for (int i = 0; i < this.elements.length; i++)
        this.elements[i] = new CodecOutputList(this, 16); 
      this.count = this.elements.length;
      this.currentIdx = this.elements.length;
      this.mask = this.elements.length - 1;
    }
    
    public CodecOutputList getOrCreate() {
      if (this.count == 0)
        return new CodecOutputList(CodecOutputList.NOOP_RECYCLER, 4); 
      this.count--;
      int idx = this.currentIdx - 1 & this.mask;
      CodecOutputList list = this.elements[idx];
      this.currentIdx = idx;
      return list;
    }
    
    public void recycle(CodecOutputList codecOutputList) {
      int idx = this.currentIdx;
      this.elements[idx] = codecOutputList;
      this.currentIdx = idx + 1 & this.mask;
      this.count++;
      assert this.count <= this.elements.length;
    }
  }
  
  static CodecOutputList newInstance() {
    return ((CodecOutputLists)CODEC_OUTPUT_LISTS_POOL.get()).getOrCreate();
  }
  
  private CodecOutputList(CodecOutputListRecycler recycler, int size) {
    this.recycler = recycler;
    this.array = new Object[size];
  }
  
  public Object get(int index) {
    checkIndex(index);
    return this.array[index];
  }
  
  public int size() {
    return this.size;
  }
  
  public boolean add(Object element) {
    ObjectUtil.checkNotNull(element, "element");
    try {
      insert(this.size, element);
    } catch (IndexOutOfBoundsException ignore) {
      expandArray();
      insert(this.size, element);
    } 
    this.size++;
    return true;
  }
  
  public Object set(int index, Object element) {
    ObjectUtil.checkNotNull(element, "element");
    checkIndex(index);
    Object old = this.array[index];
    insert(index, element);
    return old;
  }
  
  public void add(int index, Object element) {
    ObjectUtil.checkNotNull(element, "element");
    checkIndex(index);
    if (this.size == this.array.length)
      expandArray(); 
    if (index != this.size - 1)
      System.arraycopy(this.array, index, this.array, index + 1, this.size - index); 
    insert(index, element);
    this.size++;
  }
  
  public Object remove(int index) {
    checkIndex(index);
    Object old = this.array[index];
    int len = this.size - index - 1;
    if (len > 0)
      System.arraycopy(this.array, index + 1, this.array, index, len); 
    this.array[--this.size] = null;
    return old;
  }
  
  public void clear() {
    this.size = 0;
  }
  
  boolean insertSinceRecycled() {
    return this.insertSinceRecycled;
  }
  
  void recycle() {
    for (int i = 0; i < this.size; i++)
      this.array[i] = null; 
    this.size = 0;
    this.insertSinceRecycled = false;
    this.recycler.recycle(this);
  }
  
  Object getUnsafe(int index) {
    return this.array[index];
  }
  
  private void checkIndex(int index) {
    if (index >= this.size)
      throw new IndexOutOfBoundsException(); 
  }
  
  private void insert(int index, Object element) {
    this.array[index] = element;
    this.insertSinceRecycled = true;
  }
  
  private void expandArray() {
    int newCapacity = this.array.length << 1;
    if (newCapacity < 0)
      throw new OutOfMemoryError(); 
    Object[] newArray = new Object[newCapacity];
    System.arraycopy(this.array, 0, newArray, 0, this.array.length);
    this.array = newArray;
  }
}
