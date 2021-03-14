package com.sun.jna;

public final class WString implements CharSequence, Comparable {
  private String string;
  
  public WString(String s) {
    if (s == null)
      throw new NullPointerException("String initializer must be non-null"); 
    this.string = s;
  }
  
  public String toString() {
    return this.string;
  }
  
  public boolean equals(Object o) {
    return (o instanceof WString && toString().equals(o.toString()));
  }
  
  public int hashCode() {
    return toString().hashCode();
  }
  
  public int compareTo(Object o) {
    return toString().compareTo(o.toString());
  }
  
  public int length() {
    return toString().length();
  }
  
  public char charAt(int index) {
    return toString().charAt(index);
  }
  
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }
}
