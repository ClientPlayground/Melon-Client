package net.minecraft.network.status;

import net.minecraft.network.INetHandler;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;

public interface INetHandlerStatusClient extends INetHandler {
  void handleServerInfo(S00PacketServerInfo paramS00PacketServerInfo);
  
  void handlePong(S01PacketPong paramS01PacketPong);
}
