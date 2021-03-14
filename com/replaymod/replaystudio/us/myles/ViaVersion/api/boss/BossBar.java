package com.replaymod.replaystudio.us.myles.ViaVersion.api.boss;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import java.util.Set;
import java.util.UUID;

public abstract class BossBar<T> {
  public abstract String getTitle();
  
  public abstract BossBar setTitle(String paramString);
  
  public abstract float getHealth();
  
  public abstract BossBar setHealth(float paramFloat);
  
  public abstract BossColor getColor();
  
  public abstract BossBar setColor(BossColor paramBossColor);
  
  public abstract BossStyle getStyle();
  
  public abstract BossBar setStyle(BossStyle paramBossStyle);
  
  @Deprecated
  public BossBar addPlayer(T player) {
    throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
  }
  
  public abstract BossBar addPlayer(UUID paramUUID);
  
  @Deprecated
  public BossBar addPlayers(T... players) {
    throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
  }
  
  @Deprecated
  public BossBar removePlayer(T player) {
    throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
  }
  
  public abstract BossBar removePlayer(UUID paramUUID);
  
  public abstract BossBar addFlag(BossFlag paramBossFlag);
  
  public abstract BossBar removeFlag(BossFlag paramBossFlag);
  
  public abstract boolean hasFlag(BossFlag paramBossFlag);
  
  public abstract Set<UUID> getPlayers();
  
  public abstract BossBar show();
  
  public abstract BossBar hide();
  
  public abstract boolean isVisible();
  
  public abstract UUID getId();
}
