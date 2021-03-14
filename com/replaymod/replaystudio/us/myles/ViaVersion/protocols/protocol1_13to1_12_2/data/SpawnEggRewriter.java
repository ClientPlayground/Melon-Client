package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SpawnEggRewriter {
  private static final BiMap<String, Integer> spawnEggs = (BiMap<String, Integer>)HashBiMap.create();
  
  static {
    registerSpawnEgg("minecraft:bat");
    registerSpawnEgg("minecraft:blaze");
    registerSpawnEgg("minecraft:cave_spider");
    registerSpawnEgg("minecraft:chicken");
    registerSpawnEgg("minecraft:cow");
    registerSpawnEgg("minecraft:creeper");
    registerSpawnEgg("minecraft:donkey");
    registerSpawnEgg("minecraft:elder_guardian");
    registerSpawnEgg("minecraft:enderman");
    registerSpawnEgg("minecraft:endermite");
    registerSpawnEgg("minecraft:evocation_illager");
    registerSpawnEgg("minecraft:ghast");
    registerSpawnEgg("minecraft:guardian");
    registerSpawnEgg("minecraft:horse");
    registerSpawnEgg("minecraft:husk");
    registerSpawnEgg("minecraft:llama");
    registerSpawnEgg("minecraft:magma_cube");
    registerSpawnEgg("minecraft:mooshroom");
    registerSpawnEgg("minecraft:mule");
    registerSpawnEgg("minecraft:ocelot");
    registerSpawnEgg("minecraft:parrot");
    registerSpawnEgg("minecraft:pig");
    registerSpawnEgg("minecraft:polar_bear");
    registerSpawnEgg("minecraft:rabbit");
    registerSpawnEgg("minecraft:sheep");
    registerSpawnEgg("minecraft:shulker");
    registerSpawnEgg("minecraft:silverfish");
    registerSpawnEgg("minecraft:skeleton");
    registerSpawnEgg("minecraft:skeleton_horse");
    registerSpawnEgg("minecraft:slime");
    registerSpawnEgg("minecraft:spider");
    registerSpawnEgg("minecraft:squid");
    registerSpawnEgg("minecraft:stray");
    registerSpawnEgg("minecraft:vex");
    registerSpawnEgg("minecraft:villager");
    registerSpawnEgg("minecraft:vindication_illager");
    registerSpawnEgg("minecraft:witch");
    registerSpawnEgg("minecraft:wither_skeleton");
    registerSpawnEgg("minecraft:wolf");
    registerSpawnEgg("minecraft:zombie");
    registerSpawnEgg("minecraft:zombie_horse");
    registerSpawnEgg("minecraft:zombie_pigman");
    registerSpawnEgg("minecraft:zombie_villager");
  }
  
  private static void registerSpawnEgg(String key) {
    spawnEggs.put(key, Integer.valueOf(spawnEggs.size()));
  }
  
  public static int getSpawnEggId(String entityIdentifier) {
    if (!spawnEggs.containsKey(entityIdentifier))
      return -1; 
    return 0x17F0000 | ((Integer)spawnEggs.get(entityIdentifier)).intValue() & 0xFFFF;
  }
  
  public static Optional<String> getEntityId(int spawnEggId) {
    if (spawnEggId >> 16 != 383)
      return Optional.absent(); 
    return Optional.fromNullable(spawnEggs.inverse().get(Integer.valueOf(spawnEggId & 0xFFFF)));
  }
}
