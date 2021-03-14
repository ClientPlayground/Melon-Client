package com.github.steveice10.netty.handler.codec.xml;

public class XmlElementEnd extends XmlElement {
  public XmlElementEnd(String name, String namespace, String prefix) {
    super(name, namespace, prefix);
  }
  
  public String toString() {
    return "XmlElementStart{" + super
      .toString() + "} ";
  }
}
