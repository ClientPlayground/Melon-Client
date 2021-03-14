package com.github.steveice10.packetlib.event.server;

public class ServerAdapter implements ServerListener {
  public void serverBound(ServerBoundEvent event) {}
  
  public void serverClosing(ServerClosingEvent event) {}
  
  public void serverClosed(ServerClosedEvent event) {}
  
  public void sessionAdded(SessionAddedEvent event) {}
  
  public void sessionRemoved(SessionRemovedEvent event) {}
}
