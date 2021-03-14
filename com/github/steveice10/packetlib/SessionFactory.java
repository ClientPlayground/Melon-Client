package com.github.steveice10.packetlib;

public interface SessionFactory {
  Session createClientSession(Client paramClient);
  
  ConnectionListener createServerListener(Server paramServer);
}
