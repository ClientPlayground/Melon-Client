package com.github.steveice10.packetlib.event.server;

import com.github.steveice10.packetlib.Server;

public class ServerClosedEvent implements ServerEvent {
  private Server server;
  
  public ServerClosedEvent(Server server) {
    this.server = server;
  }
  
  public Server getServer() {
    return this.server;
  }
  
  public void call(ServerListener listener) {
    listener.serverClosed(this);
  }
}
