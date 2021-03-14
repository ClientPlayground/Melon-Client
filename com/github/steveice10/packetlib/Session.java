package com.github.steveice10.packetlib;

import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public interface Session {
  void connect();
  
  void connect(boolean paramBoolean);
  
  String getHost();
  
  int getPort();
  
  SocketAddress getLocalAddress();
  
  SocketAddress getRemoteAddress();
  
  PacketProtocol getPacketProtocol();
  
  Map<String, Object> getFlags();
  
  boolean hasFlag(String paramString);
  
  <T> T getFlag(String paramString);
  
  void setFlag(String paramString, Object paramObject);
  
  List<SessionListener> getListeners();
  
  void addListener(SessionListener paramSessionListener);
  
  void removeListener(SessionListener paramSessionListener);
  
  void callEvent(SessionEvent paramSessionEvent);
  
  int getCompressionThreshold();
  
  void setCompressionThreshold(int paramInt);
  
  int getConnectTimeout();
  
  void setConnectTimeout(int paramInt);
  
  int getReadTimeout();
  
  void setReadTimeout(int paramInt);
  
  int getWriteTimeout();
  
  void setWriteTimeout(int paramInt);
  
  boolean isConnected();
  
  void send(Packet paramPacket);
  
  void disconnect(String paramString);
  
  void disconnect(String paramString, boolean paramBoolean);
  
  void disconnect(String paramString, Throwable paramThrowable);
  
  void disconnect(String paramString, Throwable paramThrowable, boolean paramBoolean);
}
