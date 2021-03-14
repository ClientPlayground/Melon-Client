package com.replaymod.replaystudio.us.myles.ViaVersion.api.boss;

public enum BossColor {
  PINK(0),
  BLUE(1),
  RED(2),
  GREEN(3),
  YELLOW(4),
  PURPLE(5),
  WHITE(6);
  
  BossColor(int id) {
    this.id = id;
  }
  
  private final int id;
  
  public int getId() {
    return this.id;
  }
}
