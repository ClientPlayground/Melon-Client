package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds;

import java.util.HashMap;
import java.util.Map;

public class Effect {
  private static final Map<Integer, Integer> effects = new HashMap<>();
  
  static {
    addRewrite(1005, 1010);
    addRewrite(1003, 1005);
    addRewrite(1006, 1011);
    addRewrite(1004, 1009);
    addRewrite(1007, 1015);
    addRewrite(1008, 1016);
    addRewrite(1009, 1016);
    addRewrite(1010, 1019);
    addRewrite(1011, 1020);
    addRewrite(1012, 1021);
    addRewrite(1014, 1024);
    addRewrite(1015, 1025);
    addRewrite(1016, 1026);
    addRewrite(1017, 1027);
    addRewrite(1020, 1029);
    addRewrite(1021, 1030);
    addRewrite(1022, 1031);
  }
  
  public static int getNewId(int id) {
    Integer newId = effects.get(Integer.valueOf(id));
    return (newId != null) ? newId.intValue() : id;
  }
  
  public static boolean contains(int oldId) {
    return effects.containsKey(Integer.valueOf(oldId));
  }
  
  private static void addRewrite(int oldId, int newId) {
    effects.put(Integer.valueOf(oldId), Integer.valueOf(newId));
  }
}
