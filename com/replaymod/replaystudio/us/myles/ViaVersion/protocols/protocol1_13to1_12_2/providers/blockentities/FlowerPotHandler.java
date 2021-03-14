package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowerPotHandler implements BlockEntityProvider.BlockEntityHandler {
  private static final Map<Pair<?, Byte>, Integer> flowers = new ConcurrentHashMap<>();
  
  static {
    register("air", (byte)0, (byte)0, 5265);
    register("sapling", (byte)6, (byte)0, 5266);
    register("sapling", (byte)6, (byte)1, 5267);
    register("sapling", (byte)6, (byte)2, 5268);
    register("sapling", (byte)6, (byte)3, 5269);
    register("sapling", (byte)6, (byte)4, 5270);
    register("sapling", (byte)6, (byte)5, 5271);
    register("tallgrass", (byte)31, (byte)2, 5272);
    register("yellow_flower", (byte)37, (byte)0, 5273);
    register("red_flower", (byte)38, (byte)0, 5274);
    register("red_flower", (byte)38, (byte)1, 5275);
    register("red_flower", (byte)38, (byte)2, 5276);
    register("red_flower", (byte)38, (byte)3, 5277);
    register("red_flower", (byte)38, (byte)4, 5278);
    register("red_flower", (byte)38, (byte)5, 5279);
    register("red_flower", (byte)38, (byte)6, 5280);
    register("red_flower", (byte)38, (byte)7, 5281);
    register("red_flower", (byte)38, (byte)8, 5282);
    register("red_mushroom", (byte)40, (byte)0, 5283);
    register("brown_mushroom", (byte)39, (byte)0, 5284);
    register("deadbush", (byte)32, (byte)0, 5285);
    register("cactus", (byte)81, (byte)0, 5286);
  }
  
  public static void register(String identifier, byte numbericBlockId, byte blockData, int newId) {
    flowers.put(new Pair(identifier, Byte.valueOf(blockData)), Integer.valueOf(newId));
    flowers.put(new Pair(Byte.valueOf(numbericBlockId), Byte.valueOf(blockData)), Integer.valueOf(newId));
  }
  
  public int transform(UserConnection user, CompoundTag tag) {
    Object item = tag.contains("Item") ? tag.get("Item").getValue() : null;
    Object data = tag.contains("Data") ? tag.get("Data").getValue() : null;
    if (item instanceof String) {
      item = ((String)item).replace("minecraft:", "");
    } else if (item instanceof Number) {
      item = Byte.valueOf(((Number)item).byteValue());
    } else {
      item = Byte.valueOf((byte)0);
    } 
    if (data instanceof Number) {
      data = Byte.valueOf(((Number)data).byteValue());
    } else {
      data = Byte.valueOf((byte)0);
    } 
    Integer flower = flowers.get(new Pair(item, Byte.valueOf(((Byte)data).byteValue())));
    if (flower != null)
      return flower.intValue(); 
    flower = flowers.get(new Pair(item, Byte.valueOf((byte)0)));
    if (flower != null)
      return flower.intValue(); 
    return 5265;
  }
}
