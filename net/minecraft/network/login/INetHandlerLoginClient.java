package net.minecraft.network.login;

import net.minecraft.network.INetHandler;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;

public interface INetHandlerLoginClient extends INetHandler {
  void handleEncryptionRequest(S01PacketEncryptionRequest paramS01PacketEncryptionRequest);
  
  void handleLoginSuccess(S02PacketLoginSuccess paramS02PacketLoginSuccess);
  
  void handleDisconnect(S00PacketDisconnect paramS00PacketDisconnect);
  
  void handleEnableCompression(S03PacketEnableCompression paramS03PacketEnableCompression);
}
