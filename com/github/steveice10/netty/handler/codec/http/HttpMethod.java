package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class HttpMethod implements Comparable<HttpMethod> {
  public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");
  
  public static final HttpMethod GET = new HttpMethod("GET");
  
  public static final HttpMethod HEAD = new HttpMethod("HEAD");
  
  public static final HttpMethod POST = new HttpMethod("POST");
  
  public static final HttpMethod PUT = new HttpMethod("PUT");
  
  public static final HttpMethod PATCH = new HttpMethod("PATCH");
  
  public static final HttpMethod DELETE = new HttpMethod("DELETE");
  
  public static final HttpMethod TRACE = new HttpMethod("TRACE");
  
  public static final HttpMethod CONNECT = new HttpMethod("CONNECT");
  
  private static final EnumNameMap<HttpMethod> methodMap = new EnumNameMap<HttpMethod>((EnumNameMap.Node<HttpMethod>[])new EnumNameMap.Node[] { new EnumNameMap.Node<HttpMethod>(OPTIONS
          .toString(), OPTIONS), new EnumNameMap.Node<HttpMethod>(GET
          .toString(), GET), new EnumNameMap.Node<HttpMethod>(HEAD
          .toString(), HEAD), new EnumNameMap.Node<HttpMethod>(POST
          .toString(), POST), new EnumNameMap.Node<HttpMethod>(PUT
          .toString(), PUT), new EnumNameMap.Node<HttpMethod>(PATCH
          .toString(), PATCH), new EnumNameMap.Node<HttpMethod>(DELETE
          .toString(), DELETE), new EnumNameMap.Node<HttpMethod>(TRACE
          .toString(), TRACE), new EnumNameMap.Node<HttpMethod>(CONNECT
          .toString(), CONNECT) });
  
  private final AsciiString name;
  
  public static HttpMethod valueOf(String name) {
    HttpMethod result = methodMap.get(name);
    return (result != null) ? result : new HttpMethod(name);
  }
  
  public HttpMethod(String name) {
    name = ((String)ObjectUtil.checkNotNull(name, "name")).trim();
    if (name.isEmpty())
      throw new IllegalArgumentException("empty name"); 
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isISOControl(c) || Character.isWhitespace(c))
        throw new IllegalArgumentException("invalid character in name"); 
    } 
    this.name = AsciiString.cached(name);
  }
  
  public String name() {
    return this.name.toString();
  }
  
  public AsciiString asciiName() {
    return this.name;
  }
  
  public int hashCode() {
    return name().hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof HttpMethod))
      return false; 
    HttpMethod that = (HttpMethod)o;
    return name().equals(that.name());
  }
  
  public String toString() {
    return this.name.toString();
  }
  
  public int compareTo(HttpMethod o) {
    return name().compareTo(o.name());
  }
  
  private static final class EnumNameMap<T> {
    private final Node<T>[] values;
    
    private final int valuesMask;
    
    EnumNameMap(Node<T>... nodes) {
      this.values = (Node<T>[])new Node[MathUtil.findNextPositivePowerOfTwo(nodes.length)];
      this.valuesMask = this.values.length - 1;
      for (Node<T> node : nodes) {
        int i = hashCode(node.key) & this.valuesMask;
        if (this.values[i] != null)
          throw new IllegalArgumentException("index " + i + " collision between values: [" + (this.values[i]).key + ", " + node.key + ']'); 
        this.values[i] = node;
      } 
    }
    
    T get(String name) {
      Node<T> node = this.values[hashCode(name) & this.valuesMask];
      return (node == null || !node.key.equals(name)) ? null : node.value;
    }
    
    private static int hashCode(String name) {
      return name.hashCode() >>> 6;
    }
    
    private static final class Node<T> {
      final String key;
      
      final T value;
      
      Node(String key, T value) {
        this.key = key;
        this.value = value;
      }
    }
  }
  
  private static final class Node<T> {
    final String key;
    
    final T value;
    
    Node(String key, T value) {
      this.key = key;
      this.value = value;
    }
  }
}
