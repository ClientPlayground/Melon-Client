package com.replaymod.replaystudio.us.myles.ViaVersion.api.boss;

public enum BossFlag {
  DARKEN_SKY(1),
  PLAY_BOSS_MUSIC(2);
  
  BossFlag(int id) {
    this.id = id;
  }
  
  private final int id;
  
  public int getId() {
    return this.id;
  }
}
