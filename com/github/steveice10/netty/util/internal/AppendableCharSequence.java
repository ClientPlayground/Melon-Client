package com.github.steveice10.netty.util.internal;

import java.io.IOException;
import java.util.Arrays;

public final class AppendableCharSequence implements CharSequence, Appendable {
  private char[] chars;
  
  private int pos;
  
  public AppendableCharSequence(int length) {
    if (length < 1)
      throw new IllegalArgumentException("length: " + length + " (length: >= 1)"); 
    this.chars = new char[length];
  }
  
  private AppendableCharSequence(char[] chars) {
    if (chars.length < 1)
      throw new IllegalArgumentException("length: " + chars.length + " (length: >= 1)"); 
    this.chars = chars;
    this.pos = chars.length;
  }
  
  public int length() {
    return this.pos;
  }
  
  public char charAt(int index) {
    if (index > this.pos)
      throw new IndexOutOfBoundsException(); 
    return this.chars[index];
  }
  
  public char charAtUnsafe(int index) {
    return this.chars[index];
  }
  
  public AppendableCharSequence subSequence(int start, int end) {
    return new AppendableCharSequence(Arrays.copyOfRange(this.chars, start, end));
  }
  
  public AppendableCharSequence append(char c) {
    if (this.pos == this.chars.length) {
      char[] old = this.chars;
      this.chars = new char[old.length << 1];
      System.arraycopy(old, 0, this.chars, 0, old.length);
    } 
    this.chars[this.pos++] = c;
    return this;
  }
  
  public AppendableCharSequence append(CharSequence csq) {
    return append(csq, 0, csq.length());
  }
  
  public AppendableCharSequence append(CharSequence csq, int start, int end) {
    if (csq.length() < end)
      throw new IndexOutOfBoundsException(); 
    int length = end - start;
    if (length > this.chars.length - this.pos)
      this.chars = expand(this.chars, this.pos + length, this.pos); 
    if (csq instanceof AppendableCharSequence) {
      AppendableCharSequence seq = (AppendableCharSequence)csq;
      char[] src = seq.chars;
      System.arraycopy(src, start, this.chars, this.pos, length);
      this.pos += length;
      return this;
    } 
    for (int i = start; i < end; i++)
      this.chars[this.pos++] = csq.charAt(i); 
    return this;
  }
  
  public void reset() {
    this.pos = 0;
  }
  
  public String toString() {
    return new String(this.chars, 0, this.pos);
  }
  
  public String substring(int start, int end) {
    int length = end - start;
    if (start > this.pos || length > this.pos)
      throw new IndexOutOfBoundsException(); 
    return new String(this.chars, start, length);
  }
  
  public String subStringUnsafe(int start, int end) {
    return new String(this.chars, start, end - start);
  }
  
  private static char[] expand(char[] array, int neededSpace, int size) {
    int newCapacity = array.length;
    do {
      newCapacity <<= 1;
      if (newCapacity < 0)
        throw new IllegalStateException(); 
    } while (neededSpace > newCapacity);
    char[] newArray = new char[newCapacity];
    System.arraycopy(array, 0, newArray, 0, size);
    return newArray;
  }
}
