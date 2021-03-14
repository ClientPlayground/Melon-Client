package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.DefaultAddressedEnvelope;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.net.InetSocketAddress;

public final class DatagramPacket extends DefaultAddressedEnvelope<ByteBuf, InetSocketAddress> implements ByteBufHolder {
  public DatagramPacket(ByteBuf data, InetSocketAddress recipient) {
    super(data, recipient);
  }
  
  public DatagramPacket(ByteBuf data, InetSocketAddress recipient, InetSocketAddress sender) {
    super(data, recipient, sender);
  }
  
  public DatagramPacket copy() {
    return replace(((ByteBuf)content()).copy());
  }
  
  public DatagramPacket duplicate() {
    return replace(((ByteBuf)content()).duplicate());
  }
  
  public DatagramPacket retainedDuplicate() {
    return replace(((ByteBuf)content()).retainedDuplicate());
  }
  
  public DatagramPacket replace(ByteBuf content) {
    return new DatagramPacket(content, (InetSocketAddress)recipient(), (InetSocketAddress)sender());
  }
  
  public DatagramPacket retain() {
    super.retain();
    return this;
  }
  
  public DatagramPacket retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public DatagramPacket touch() {
    super.touch();
    return this;
  }
  
  public DatagramPacket touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
