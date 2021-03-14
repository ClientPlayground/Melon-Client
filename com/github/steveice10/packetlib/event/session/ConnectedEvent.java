package com.github.steveice10.packetlib.event.session;

import com.github.steveice10.packetlib.Session;

public class ConnectedEvent implements SessionEvent {
  private Session session;
  
  public ConnectedEvent(Session session) {
    this.session = session;
  }
  
  public Session getSession() {
    return this.session;
  }
  
  public void call(SessionListener listener) {
    listener.connected(this);
  }
}
