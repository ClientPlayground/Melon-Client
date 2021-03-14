package com.replaymod.replaystudio;

import com.replaymod.replaystudio.protocol.Packet;
import java.util.Objects;

public final class PacketData implements Cloneable {
  private final long time;
  
  private final Packet packet;
  
  public PacketData(long time, Packet packet) {
    this.time = time;
    this.packet = packet;
  }
  
  public long getTime() {
    return this.time;
  }
  
  public Packet getPacket() {
    return this.packet;
  }
  
  public PacketData retain() {
    this.packet.retain();
    return this;
  }
  
  public PacketData copy() {
    return new PacketData(this.time, this.packet.copy());
  }
  
  public boolean release() {
    return this.packet.release();
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof PacketData))
      return false; 
    PacketData other = (PacketData)o;
    if (this.time != other.time)
      return false; 
    if (!Objects.equals(this.packet, other.packet))
      return false; 
    return true;
  }
  
  public int hashCode() {
    int result = 1;
    result = result * 59 + (int)(this.time >>> 32L ^ this.time);
    result = result * 59 + ((this.packet == null) ? 0 : this.packet.hashCode());
    return result;
  }
  
  public String toString() {
    return "PacketData(time=" + this.time + ", packet=" + this.packet + ")";
  }
}
