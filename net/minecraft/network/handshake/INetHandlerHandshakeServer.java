package net.minecraft.network.handshake;

import net.minecraft.network.INetHandler;
import net.minecraft.network.handshake.client.C00Handshake;

public interface INetHandlerHandshakeServer extends INetHandler {
  void processHandshake(C00Handshake paramC00Handshake);
}
