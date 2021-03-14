package com.github.steveice10.netty.handler.codec.xml;

import java.util.LinkedList;
import java.util.List;

public class XmlElementStart extends XmlElement {
  private final List<XmlAttribute> attributes = new LinkedList<XmlAttribute>();
  
  public XmlElementStart(String name, String namespace, String prefix) {
    super(name, namespace, prefix);
  }
  
  public List<XmlAttribute> attributes() {
    return this.attributes;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    if (!super.equals(o))
      return false; 
    XmlElementStart that = (XmlElementStart)o;
    if ((this.attributes != null) ? !this.attributes.equals(that.attributes) : (that.attributes != null))
      return false; 
    return true;
  }
  
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + ((this.attributes != null) ? this.attributes.hashCode() : 0);
    return result;
  }
  
  public String toString() {
    return "XmlElementStart{attributes=" + this.attributes + super
      
      .toString() + "} ";
  }
}
