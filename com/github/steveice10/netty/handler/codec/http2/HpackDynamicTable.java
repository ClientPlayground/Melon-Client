package com.github.steveice10.netty.handler.codec.http2;

final class HpackDynamicTable {
  HpackHeaderField[] hpackHeaderFields;
  
  int head;
  
  int tail;
  
  private long size;
  
  private long capacity = -1L;
  
  HpackDynamicTable(long initialCapacity) {
    setCapacity(initialCapacity);
  }
  
  public int length() {
    int length;
    if (this.head < this.tail) {
      length = this.hpackHeaderFields.length - this.tail + this.head;
    } else {
      length = this.head - this.tail;
    } 
    return length;
  }
  
  public long size() {
    return this.size;
  }
  
  public long capacity() {
    return this.capacity;
  }
  
  public HpackHeaderField getEntry(int index) {
    if (index <= 0 || index > length())
      throw new IndexOutOfBoundsException(); 
    int i = this.head - index;
    if (i < 0)
      return this.hpackHeaderFields[i + this.hpackHeaderFields.length]; 
    return this.hpackHeaderFields[i];
  }
  
  public void add(HpackHeaderField header) {
    int headerSize = header.size();
    if (headerSize > this.capacity) {
      clear();
      return;
    } 
    while (this.capacity - this.size < headerSize)
      remove(); 
    this.hpackHeaderFields[this.head++] = header;
    this.size += header.size();
    if (this.head == this.hpackHeaderFields.length)
      this.head = 0; 
  }
  
  public HpackHeaderField remove() {
    HpackHeaderField removed = this.hpackHeaderFields[this.tail];
    if (removed == null)
      return null; 
    this.size -= removed.size();
    this.hpackHeaderFields[this.tail++] = null;
    if (this.tail == this.hpackHeaderFields.length)
      this.tail = 0; 
    return removed;
  }
  
  public void clear() {
    while (this.tail != this.head) {
      this.hpackHeaderFields[this.tail++] = null;
      if (this.tail == this.hpackHeaderFields.length)
        this.tail = 0; 
    } 
    this.head = 0;
    this.tail = 0;
    this.size = 0L;
  }
  
  public void setCapacity(long capacity) {
    if (capacity < 0L || capacity > 4294967295L)
      throw new IllegalArgumentException("capacity is invalid: " + capacity); 
    if (this.capacity == capacity)
      return; 
    this.capacity = capacity;
    if (capacity == 0L) {
      clear();
    } else {
      while (this.size > capacity)
        remove(); 
    } 
    int maxEntries = (int)(capacity / 32L);
    if (capacity % 32L != 0L)
      maxEntries++; 
    if (this.hpackHeaderFields != null && this.hpackHeaderFields.length == maxEntries)
      return; 
    HpackHeaderField[] tmp = new HpackHeaderField[maxEntries];
    int len = length();
    int cursor = this.tail;
    for (int i = 0; i < len; i++) {
      HpackHeaderField entry = this.hpackHeaderFields[cursor++];
      tmp[i] = entry;
      if (cursor == this.hpackHeaderFields.length)
        cursor = 0; 
    } 
    this.tail = 0;
    this.head = this.tail + len;
    this.hpackHeaderFields = tmp;
  }
}
