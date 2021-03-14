package com.github.steveice10.packetlib.event.server;

public interface ServerListener {
  void serverBound(ServerBoundEvent paramServerBoundEvent);
  
  void serverClosing(ServerClosingEvent paramServerClosingEvent);
  
  void serverClosed(ServerClosedEvent paramServerClosedEvent);
  
  void sessionAdded(SessionAddedEvent paramSessionAddedEvent);
  
  void sessionRemoved(SessionRemovedEvent paramSessionRemovedEvent);
}
