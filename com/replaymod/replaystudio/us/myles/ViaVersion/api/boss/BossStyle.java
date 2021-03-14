package com.replaymod.replaystudio.us.myles.ViaVersion.api.boss;

public enum BossStyle {
  SOLID(0),
  SEGMENTED_6(1),
  SEGMENTED_10(2),
  SEGMENTED_12(3),
  SEGMENTED_20(4);
  
  BossStyle(int id) {
    this.id = id;
  }
  
  private final int id;
  
  public int getId() {
    return this.id;
  }
}
