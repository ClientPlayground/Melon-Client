package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.base.Optional;

public enum SoundSource {
  MASTER("master", 0),
  MUSIC("music", 1),
  RECORD("record", 2),
  WEATHER("weather", 3),
  BLOCK("block", 4),
  HOSTILE("hostile", 5),
  NEUTRAL("neutral", 6),
  PLAYER("player", 7),
  AMBIENT("ambient", 8),
  VOICE("voice", 9);
  
  SoundSource(String name, int id) {
    this.name = name;
    this.id = id;
  }
  
  private final String name;
  
  private final int id;
  
  public String getName() {
    return this.name;
  }
  
  public int getId() {
    return this.id;
  }
  
  public static Optional<SoundSource> findBySource(String source) {
    for (SoundSource item : values()) {
      if (item.getName().equalsIgnoreCase(source))
        return Optional.of(item); 
    } 
    return Optional.absent();
  }
}
