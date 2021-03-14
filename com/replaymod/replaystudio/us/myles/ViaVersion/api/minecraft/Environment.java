package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

import java.util.HashMap;
import java.util.Map;

public enum Environment {
  NORMAL(0),
  NETHER(-1),
  END(1);
  
  private final int id;
  
  private static final Map<Integer, Environment> lookup;
  
  static {
    lookup = new HashMap<>();
    for (Environment env : values())
      lookup.put(Integer.valueOf(env.getId()), env); 
  }
  
  Environment(int id) {
    this.id = id;
  }
  
  public int getId() {
    return this.id;
  }
  
  public static Environment getEnvironmentById(int id) {
    return lookup.get(Integer.valueOf(id));
  }
}
