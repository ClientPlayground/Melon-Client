package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;

class HpackHeaderField {
  static final int HEADER_ENTRY_OVERHEAD = 32;
  
  final CharSequence name;
  
  final CharSequence value;
  
  static long sizeOf(CharSequence name, CharSequence value) {
    return (name.length() + value.length() + 32);
  }
  
  HpackHeaderField(CharSequence name, CharSequence value) {
    this.name = (CharSequence)ObjectUtil.checkNotNull(name, "name");
    this.value = (CharSequence)ObjectUtil.checkNotNull(value, "value");
  }
  
  final int size() {
    return this.name.length() + this.value.length() + 32;
  }
  
  public final int hashCode() {
    return super.hashCode();
  }
  
  public final boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof HpackHeaderField))
      return false; 
    HpackHeaderField other = (HpackHeaderField)obj;
    return ((HpackUtil.equalsConstantTime(this.name, other.name) & HpackUtil.equalsConstantTime(this.value, other.value)) != 0);
  }
  
  public String toString() {
    return this.name + ": " + this.value;
  }
}
