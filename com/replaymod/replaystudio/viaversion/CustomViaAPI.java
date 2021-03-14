package com.replaymod.replaystudio.viaversion;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaAPI;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossBar;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossColor;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossStyle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.util.SortedSet;
import java.util.UUID;

class CustomViaAPI implements ViaAPI<Void> {
  static final ThreadLocal<CustomViaAPI> INSTANCE = new ThreadLocal<>();
  
  private final int sourceVersion;
  
  private final UserConnection userConnection;
  
  CustomViaAPI(int sourceVersion, UserConnection userConnection) {
    this.sourceVersion = sourceVersion;
    this.userConnection = userConnection;
  }
  
  UserConnection user() {
    return this.userConnection;
  }
  
  public int getPlayerVersion(Void aVoid) {
    throw new UnsupportedOperationException();
  }
  
  public int getPlayerVersion(UUID uuid) {
    if (uuid.equals(((ProtocolInfo)this.userConnection.get(ProtocolInfo.class)).getUuid()))
      return this.sourceVersion; 
    throw new UnsupportedOperationException();
  }
  
  public boolean isPorted(UUID uuid) {
    return (this.sourceVersion >= 107);
  }
  
  public String getVersion() {
    return Via.getPlatform().getPluginVersion();
  }
  
  public void sendRawPacket(Void aVoid, ByteBuf byteBuf) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }
  
  public void sendRawPacket(UUID uuid, ByteBuf byteBuf) throws IllegalArgumentException {
    if (uuid.equals(((ProtocolInfo)this.userConnection.get(ProtocolInfo.class)).getUuid())) {
      this.userConnection.sendRawPacket(byteBuf);
      return;
    } 
    throw new UnsupportedOperationException();
  }
  
  public BossBar createBossBar(String title, BossColor color, BossStyle style) {
    return createBossBar(title, 1.0F, color, style);
  }
  
  public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
    return (BossBar)new CustomBossBar(title, health, color, style);
  }
  
  public SortedSet<Integer> getSupportedVersions() {
    return ProtocolRegistry.getSupportedVersions();
  }
}
