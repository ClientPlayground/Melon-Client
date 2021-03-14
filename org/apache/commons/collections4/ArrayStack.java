package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.EmptyStackException;

@Deprecated
public class ArrayStack<E> extends ArrayList<E> {
  private static final long serialVersionUID = 2130079159931574599L;
  
  public ArrayStack() {}
  
  public ArrayStack(int initialSize) {
    super(initialSize);
  }
  
  public boolean empty() {
    return isEmpty();
  }
  
  public E peek() throws EmptyStackException {
    int n = size();
    if (n <= 0)
      throw new EmptyStackException(); 
    return get(n - 1);
  }
  
  public E peek(int n) throws EmptyStackException {
    int m = size() - n - 1;
    if (m < 0)
      throw new EmptyStackException(); 
    return get(m);
  }
  
  public E pop() throws EmptyStackException {
    int n = size();
    if (n <= 0)
      throw new EmptyStackException(); 
    return remove(n - 1);
  }
  
  public E push(E item) {
    add(item);
    return item;
  }
  
  public int search(Object object) {
    int i = size() - 1;
    int n = 1;
    while (i >= 0) {
      Object current = get(i);
      if ((object == null && current == null) || (object != null && object.equals(current)))
        return n; 
      i--;
      n++;
    } 
    return -1;
  }
}
