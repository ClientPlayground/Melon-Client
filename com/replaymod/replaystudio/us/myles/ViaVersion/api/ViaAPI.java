package com.replaymod.replaystudio.us.myles.ViaVersion.api;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossBar;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossColor;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossStyle;
import java.util.SortedSet;
import java.util.UUID;

public interface ViaAPI<T> {
  int getPlayerVersion(T paramT);
  
  int getPlayerVersion(UUID paramUUID);
  
  @Deprecated
  boolean isPorted(UUID paramUUID);
  
  String getVersion();
  
  void sendRawPacket(T paramT, ByteBuf paramByteBuf) throws IllegalArgumentException;
  
  void sendRawPacket(UUID paramUUID, ByteBuf paramByteBuf) throws IllegalArgumentException;
  
  BossBar createBossBar(String paramString, BossColor paramBossColor, BossStyle paramBossStyle);
  
  BossBar createBossBar(String paramString, float paramFloat, BossColor paramBossColor, BossStyle paramBossStyle);
  
  SortedSet<Integer> getSupportedVersions();
}
