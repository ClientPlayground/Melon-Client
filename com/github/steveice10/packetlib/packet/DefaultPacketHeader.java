package com.github.steveice10.packetlib.packet;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;

public class DefaultPacketHeader implements PacketHeader {
  public boolean isLengthVariable() {
    return true;
  }
  
  public int getLengthSize() {
    return 5;
  }
  
  public int getLengthSize(int length) {
    if ((length & 0xFFFFFF80) == 0)
      return 1; 
    if ((length & 0xFFFFC000) == 0)
      return 2; 
    if ((length & 0xFFE00000) == 0)
      return 3; 
    if ((length & 0xF0000000) == 0)
      return 4; 
    return 5;
  }
  
  public int readLength(NetInput in, int available) throws IOException {
    return in.readVarInt();
  }
  
  public void writeLength(NetOutput out, int length) throws IOException {
    out.writeVarInt(length);
  }
  
  public int readPacketId(NetInput in) throws IOException {
    return in.readVarInt();
  }
  
  public void writePacketId(NetOutput out, int packetId) throws IOException {
    out.writeVarInt(packetId);
  }
}
