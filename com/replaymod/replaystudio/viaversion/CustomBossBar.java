package com.replaymod.replaystudio.viaversion;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossColor;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossStyle;
import com.replaymod.replaystudio.us.myles.ViaVersion.boss.CommonBoss;

public class CustomBossBar extends CommonBoss<Void> {
  public CustomBossBar(String title, float health, BossColor color, BossStyle style) {
    super(title, health, color, style);
  }
}
