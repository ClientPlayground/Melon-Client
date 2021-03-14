package net.minecraft.network;

import java.io.IOException;

public interface Packet<T extends INetHandler> {
  void readPacketData(PacketBuffer paramPacketBuffer) throws IOException;
  
  void writePacketData(PacketBuffer paramPacketBuffer) throws IOException;
  
  void processPacket(T paramT);
}
