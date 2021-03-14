package com.github.steveice10.packetlib.packet;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;

public interface Packet {
  void read(NetInput paramNetInput) throws IOException;
  
  void write(NetOutput paramNetOutput) throws IOException;
  
  boolean isPriority();
}
