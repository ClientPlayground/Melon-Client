package com.github.steveice10.packetlib.event.session;

import com.github.steveice10.packetlib.Session;

public class DisconnectedEvent implements SessionEvent {
  private Session session;
  
  private String reason;
  
  private Throwable cause;
  
  public DisconnectedEvent(Session session, String reason) {
    this(session, reason, null);
  }
  
  public DisconnectedEvent(Session session, String reason, Throwable cause) {
    this.session = session;
    this.reason = reason;
    this.cause = cause;
  }
  
  public Session getSession() {
    return this.session;
  }
  
  public String getReason() {
    return this.reason;
  }
  
  public Throwable getCause() {
    return this.cause;
  }
  
  public void call(SessionListener listener) {
    listener.disconnected(this);
  }
}
