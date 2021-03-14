package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class DatagramDnsQuery extends DefaultDnsQuery implements AddressedEnvelope<DatagramDnsQuery, InetSocketAddress> {
  private final InetSocketAddress sender;
  
  private final InetSocketAddress recipient;
  
  public DatagramDnsQuery(InetSocketAddress sender, InetSocketAddress recipient, int id) {
    this(sender, recipient, id, DnsOpCode.QUERY);
  }
  
  public DatagramDnsQuery(InetSocketAddress sender, InetSocketAddress recipient, int id, DnsOpCode opCode) {
    super(id, opCode);
    if (recipient == null && sender == null)
      throw new NullPointerException("recipient and sender"); 
    this.sender = sender;
    this.recipient = recipient;
  }
  
  public DatagramDnsQuery content() {
    return this;
  }
  
  public InetSocketAddress sender() {
    return this.sender;
  }
  
  public InetSocketAddress recipient() {
    return this.recipient;
  }
  
  public DatagramDnsQuery setId(int id) {
    return (DatagramDnsQuery)super.setId(id);
  }
  
  public DatagramDnsQuery setOpCode(DnsOpCode opCode) {
    return (DatagramDnsQuery)super.setOpCode(opCode);
  }
  
  public DatagramDnsQuery setRecursionDesired(boolean recursionDesired) {
    return (DatagramDnsQuery)super.setRecursionDesired(recursionDesired);
  }
  
  public DatagramDnsQuery setZ(int z) {
    return (DatagramDnsQuery)super.setZ(z);
  }
  
  public DatagramDnsQuery setRecord(DnsSection section, DnsRecord record) {
    return (DatagramDnsQuery)super.setRecord(section, record);
  }
  
  public DatagramDnsQuery addRecord(DnsSection section, DnsRecord record) {
    return (DatagramDnsQuery)super.addRecord(section, record);
  }
  
  public DatagramDnsQuery addRecord(DnsSection section, int index, DnsRecord record) {
    return (DatagramDnsQuery)super.addRecord(section, index, record);
  }
  
  public DatagramDnsQuery clear(DnsSection section) {
    return (DatagramDnsQuery)super.clear(section);
  }
  
  public DatagramDnsQuery clear() {
    return (DatagramDnsQuery)super.clear();
  }
  
  public DatagramDnsQuery touch() {
    return (DatagramDnsQuery)super.touch();
  }
  
  public DatagramDnsQuery touch(Object hint) {
    return (DatagramDnsQuery)super.touch(hint);
  }
  
  public DatagramDnsQuery retain() {
    return (DatagramDnsQuery)super.retain();
  }
  
  public DatagramDnsQuery retain(int increment) {
    return (DatagramDnsQuery)super.retain(increment);
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!super.equals(obj))
      return false; 
    if (!(obj instanceof AddressedEnvelope))
      return false; 
    AddressedEnvelope<?, SocketAddress> that = (AddressedEnvelope<?, SocketAddress>)obj;
    if (sender() == null) {
      if (that.sender() != null)
        return false; 
    } else if (!sender().equals(that.sender())) {
      return false;
    } 
    if (recipient() == null) {
      if (that.recipient() != null)
        return false; 
    } else if (!recipient().equals(that.recipient())) {
      return false;
    } 
    return true;
  }
  
  public int hashCode() {
    int hashCode = super.hashCode();
    if (sender() != null)
      hashCode = hashCode * 31 + sender().hashCode(); 
    if (recipient() != null)
      hashCode = hashCode * 31 + recipient().hashCode(); 
    return hashCode;
  }
}
