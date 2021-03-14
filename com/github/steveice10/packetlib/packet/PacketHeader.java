package com.github.steveice10.packetlib.packet;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;

public interface PacketHeader {
  boolean isLengthVariable();
  
  int getLengthSize();
  
  int getLengthSize(int paramInt);
  
  int readLength(NetInput paramNetInput, int paramInt) throws IOException;
  
  void writeLength(NetOutput paramNetOutput, int paramInt) throws IOException;
  
  int readPacketId(NetInput paramNetInput) throws IOException;
  
  void writePacketId(NetOutput paramNetOutput, int paramInt) throws IOException;
}
