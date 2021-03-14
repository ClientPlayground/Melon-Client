package com.github.steveice10.packetlib.event.session;

public interface SessionListener {
  void packetReceived(PacketReceivedEvent paramPacketReceivedEvent);
  
  void packetSending(PacketSendingEvent paramPacketSendingEvent);
  
  void packetSent(PacketSentEvent paramPacketSentEvent);
  
  void connected(ConnectedEvent paramConnectedEvent);
  
  void disconnecting(DisconnectingEvent paramDisconnectingEvent);
  
  void disconnected(DisconnectedEvent paramDisconnectedEvent);
}
