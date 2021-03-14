package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.http.cookie.DefaultCookie;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

@Deprecated
public class DefaultCookie extends DefaultCookie implements Cookie {
  private String comment;
  
  private String commentUrl;
  
  private boolean discard;
  
  private Set<Integer> ports = Collections.emptySet();
  
  private Set<Integer> unmodifiablePorts = this.ports;
  
  private int version;
  
  public DefaultCookie(String name, String value) {
    super(name, value);
  }
  
  @Deprecated
  public String getName() {
    return name();
  }
  
  @Deprecated
  public String getValue() {
    return value();
  }
  
  @Deprecated
  public String getDomain() {
    return domain();
  }
  
  @Deprecated
  public String getPath() {
    return path();
  }
  
  @Deprecated
  public String getComment() {
    return comment();
  }
  
  @Deprecated
  public String comment() {
    return this.comment;
  }
  
  @Deprecated
  public void setComment(String comment) {
    this.comment = validateValue("comment", comment);
  }
  
  @Deprecated
  public String getCommentUrl() {
    return commentUrl();
  }
  
  @Deprecated
  public String commentUrl() {
    return this.commentUrl;
  }
  
  @Deprecated
  public void setCommentUrl(String commentUrl) {
    this.commentUrl = validateValue("commentUrl", commentUrl);
  }
  
  @Deprecated
  public boolean isDiscard() {
    return this.discard;
  }
  
  @Deprecated
  public void setDiscard(boolean discard) {
    this.discard = discard;
  }
  
  @Deprecated
  public Set<Integer> getPorts() {
    return ports();
  }
  
  @Deprecated
  public Set<Integer> ports() {
    if (this.unmodifiablePorts == null)
      this.unmodifiablePorts = Collections.unmodifiableSet(this.ports); 
    return this.unmodifiablePorts;
  }
  
  @Deprecated
  public void setPorts(int... ports) {
    if (ports == null)
      throw new NullPointerException("ports"); 
    int[] portsCopy = (int[])ports.clone();
    if (portsCopy.length == 0) {
      this.unmodifiablePorts = this.ports = Collections.emptySet();
    } else {
      Set<Integer> newPorts = new TreeSet<Integer>();
      for (int p : portsCopy) {
        if (p <= 0 || p > 65535)
          throw new IllegalArgumentException("port out of range: " + p); 
        newPorts.add(Integer.valueOf(p));
      } 
      this.ports = newPorts;
      this.unmodifiablePorts = null;
    } 
  }
  
  @Deprecated
  public void setPorts(Iterable<Integer> ports) {
    Set<Integer> newPorts = new TreeSet<Integer>();
    for (Iterator<Integer> iterator = ports.iterator(); iterator.hasNext(); ) {
      int p = ((Integer)iterator.next()).intValue();
      if (p <= 0 || p > 65535)
        throw new IllegalArgumentException("port out of range: " + p); 
      newPorts.add(Integer.valueOf(p));
    } 
    if (newPorts.isEmpty()) {
      this.unmodifiablePorts = this.ports = Collections.emptySet();
    } else {
      this.ports = newPorts;
      this.unmodifiablePorts = null;
    } 
  }
  
  @Deprecated
  public long getMaxAge() {
    return maxAge();
  }
  
  @Deprecated
  public int getVersion() {
    return version();
  }
  
  @Deprecated
  public int version() {
    return this.version;
  }
  
  @Deprecated
  public void setVersion(int version) {
    this.version = version;
  }
}
