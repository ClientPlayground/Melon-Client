package com.github.steveice10.packetlib.event.server;

import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;

public class SessionAddedEvent implements ServerEvent {
  private Server server;
  
  private Session session;
  
  public SessionAddedEvent(Server server, Session session) {
    this.server = server;
    this.session = session;
  }
  
  public Server getServer() {
    return this.server;
  }
  
  public Session getSession() {
    return this.session;
  }
  
  public void call(ServerListener listener) {
    listener.sessionAdded(this);
  }
}
