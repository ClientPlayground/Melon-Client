package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTileEntity {
  private static final Map<Integer, CompoundTag> tileEntities = new ConcurrentHashMap<>();
  
  static {
    register(Arrays.asList(new Integer[] { Integer.valueOf(61), Integer.valueOf(62) }, ), "Furnace");
    register(Arrays.asList(new Integer[] { Integer.valueOf(54), Integer.valueOf(146) }, ), "Chest");
    register(Integer.valueOf(130), "EnderChest");
    register(Integer.valueOf(84), "RecordPlayer");
    register(Integer.valueOf(23), "Trap");
    register(Integer.valueOf(158), "Dropper");
    register(Arrays.asList(new Integer[] { Integer.valueOf(63), Integer.valueOf(68) }, ), "Sign");
    register(Integer.valueOf(52), "MobSpawner");
    register(Integer.valueOf(25), "Music");
    register(Arrays.asList(new Integer[] { Integer.valueOf(33), Integer.valueOf(34), Integer.valueOf(29), Integer.valueOf(36) }, ), "Piston");
    register(Integer.valueOf(117), "Cauldron");
    register(Integer.valueOf(116), "EnchantTable");
    register(Arrays.asList(new Integer[] { Integer.valueOf(119), Integer.valueOf(120) }, ), "Airportal");
    register(Integer.valueOf(138), "Beacon");
    register(Integer.valueOf(144), "Skull");
    register(Arrays.asList(new Integer[] { Integer.valueOf(178), Integer.valueOf(151) }, ), "DLDetector");
    register(Integer.valueOf(154), "Hopper");
    register(Arrays.asList(new Integer[] { Integer.valueOf(149), Integer.valueOf(150) }, ), "Comparator");
    register(Integer.valueOf(140), "FlowerPot");
    register(Arrays.asList(new Integer[] { Integer.valueOf(176), Integer.valueOf(177) }, ), "Banner");
    register(Integer.valueOf(209), "EndGateway");
    register(Integer.valueOf(137), "Control");
  }
  
  private static void register(Integer material, String name) {
    CompoundTag comp = new CompoundTag("");
    comp.put((Tag)new StringTag(name));
    tileEntities.put(material, comp);
  }
  
  private static void register(List<Integer> materials, String name) {
    for (Iterator<Integer> iterator = materials.iterator(); iterator.hasNext(); ) {
      int m = ((Integer)iterator.next()).intValue();
      register(Integer.valueOf(m), name);
    } 
  }
  
  public static boolean hasBlock(int block) {
    return tileEntities.containsKey(Integer.valueOf(block));
  }
  
  public static CompoundTag getFromBlock(int x, int y, int z, int block) {
    CompoundTag originalTag = tileEntities.get(Integer.valueOf(block));
    if (originalTag != null) {
      CompoundTag tag = originalTag.clone();
      tag.put((Tag)new IntTag("x", x));
      tag.put((Tag)new IntTag("y", y));
      tag.put((Tag)new IntTag("z", z));
      return tag;
    } 
    return null;
  }
}
