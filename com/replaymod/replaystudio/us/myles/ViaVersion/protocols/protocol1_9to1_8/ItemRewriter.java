package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemRewriter {
  private static final Map<String, Integer> ENTTIY_NAME_TO_ID = new HashMap<>();
  
  private static final Map<Integer, String> ENTTIY_ID_TO_NAME = new HashMap<>();
  
  private static final Map<String, Integer> POTION_NAME_TO_ID = new HashMap<>();
  
  private static final Map<Integer, String> POTION_ID_TO_NAME = new HashMap<>();
  
  private static final Map<Integer, Integer> POTION_INDEX = new HashMap<>();
  
  static {
    registerEntity(Integer.valueOf(1), "Item");
    registerEntity(Integer.valueOf(2), "XPOrb");
    registerEntity(Integer.valueOf(7), "ThrownEgg");
    registerEntity(Integer.valueOf(8), "LeashKnot");
    registerEntity(Integer.valueOf(9), "Painting");
    registerEntity(Integer.valueOf(10), "Arrow");
    registerEntity(Integer.valueOf(11), "Snowball");
    registerEntity(Integer.valueOf(12), "Fireball");
    registerEntity(Integer.valueOf(13), "SmallFireball");
    registerEntity(Integer.valueOf(14), "ThrownEnderpearl");
    registerEntity(Integer.valueOf(15), "EyeOfEnderSignal");
    registerEntity(Integer.valueOf(16), "ThrownPotion");
    registerEntity(Integer.valueOf(17), "ThrownExpBottle");
    registerEntity(Integer.valueOf(18), "ItemFrame");
    registerEntity(Integer.valueOf(19), "WitherSkull");
    registerEntity(Integer.valueOf(20), "PrimedTnt");
    registerEntity(Integer.valueOf(21), "FallingSand");
    registerEntity(Integer.valueOf(22), "FireworksRocketEntity");
    registerEntity(Integer.valueOf(30), "ArmorStand");
    registerEntity(Integer.valueOf(40), "MinecartCommandBlock");
    registerEntity(Integer.valueOf(41), "Boat");
    registerEntity(Integer.valueOf(42), "MinecartRideable");
    registerEntity(Integer.valueOf(43), "MinecartChest");
    registerEntity(Integer.valueOf(44), "MinecartFurnace");
    registerEntity(Integer.valueOf(45), "MinecartTNT");
    registerEntity(Integer.valueOf(46), "MinecartHopper");
    registerEntity(Integer.valueOf(47), "MinecartSpawner");
    registerEntity(Integer.valueOf(48), "Mob");
    registerEntity(Integer.valueOf(49), "Monster");
    registerEntity(Integer.valueOf(50), "Creeper");
    registerEntity(Integer.valueOf(51), "Skeleton");
    registerEntity(Integer.valueOf(52), "Spider");
    registerEntity(Integer.valueOf(53), "Giant");
    registerEntity(Integer.valueOf(54), "Zombie");
    registerEntity(Integer.valueOf(55), "Slime");
    registerEntity(Integer.valueOf(56), "Ghast");
    registerEntity(Integer.valueOf(57), "PigZombie");
    registerEntity(Integer.valueOf(58), "Enderman");
    registerEntity(Integer.valueOf(59), "CaveSpider");
    registerEntity(Integer.valueOf(60), "Silverfish");
    registerEntity(Integer.valueOf(61), "Blaze");
    registerEntity(Integer.valueOf(62), "LavaSlime");
    registerEntity(Integer.valueOf(63), "EnderDragon");
    registerEntity(Integer.valueOf(64), "WitherBoss");
    registerEntity(Integer.valueOf(65), "Bat");
    registerEntity(Integer.valueOf(66), "Witch");
    registerEntity(Integer.valueOf(67), "Endermite");
    registerEntity(Integer.valueOf(68), "Guardian");
    registerEntity(Integer.valueOf(90), "Pig");
    registerEntity(Integer.valueOf(91), "Sheep");
    registerEntity(Integer.valueOf(92), "Cow");
    registerEntity(Integer.valueOf(93), "Chicken");
    registerEntity(Integer.valueOf(94), "Squid");
    registerEntity(Integer.valueOf(95), "Wolf");
    registerEntity(Integer.valueOf(96), "MushroomCow");
    registerEntity(Integer.valueOf(97), "SnowMan");
    registerEntity(Integer.valueOf(98), "Ozelot");
    registerEntity(Integer.valueOf(99), "VillagerGolem");
    registerEntity(Integer.valueOf(100), "EntityHorse");
    registerEntity(Integer.valueOf(101), "Rabbit");
    registerEntity(Integer.valueOf(120), "Villager");
    registerEntity(Integer.valueOf(200), "EnderCrystal");
    registerPotion(Integer.valueOf(-1), "empty");
    registerPotion(Integer.valueOf(0), "water");
    registerPotion(Integer.valueOf(64), "mundane");
    registerPotion(Integer.valueOf(32), "thick");
    registerPotion(Integer.valueOf(16), "awkward");
    registerPotion(Integer.valueOf(8198), "night_vision");
    registerPotion(Integer.valueOf(8262), "long_night_vision");
    registerPotion(Integer.valueOf(8206), "invisibility");
    registerPotion(Integer.valueOf(8270), "long_invisibility");
    registerPotion(Integer.valueOf(8203), "leaping");
    registerPotion(Integer.valueOf(8267), "long_leaping");
    registerPotion(Integer.valueOf(8235), "strong_leaping");
    registerPotion(Integer.valueOf(8195), "fire_resistance");
    registerPotion(Integer.valueOf(8259), "long_fire_resistance");
    registerPotion(Integer.valueOf(8194), "swiftness");
    registerPotion(Integer.valueOf(8258), "long_swiftness");
    registerPotion(Integer.valueOf(8226), "strong_swiftness");
    registerPotion(Integer.valueOf(8202), "slowness");
    registerPotion(Integer.valueOf(8266), "long_slowness");
    registerPotion(Integer.valueOf(8205), "water_breathing");
    registerPotion(Integer.valueOf(8269), "long_water_breathing");
    registerPotion(Integer.valueOf(8261), "healing");
    registerPotion(Integer.valueOf(8229), "strong_healing");
    registerPotion(Integer.valueOf(8204), "harming");
    registerPotion(Integer.valueOf(8236), "strong_harming");
    registerPotion(Integer.valueOf(8196), "poison");
    registerPotion(Integer.valueOf(8260), "long_poison");
    registerPotion(Integer.valueOf(8228), "strong_poison");
    registerPotion(Integer.valueOf(8193), "regeneration");
    registerPotion(Integer.valueOf(8257), "long_regeneration");
    registerPotion(Integer.valueOf(8225), "strong_regeneration");
    registerPotion(Integer.valueOf(8201), "strength");
    registerPotion(Integer.valueOf(8265), "long_strength");
    registerPotion(Integer.valueOf(8233), "strong_strength");
    registerPotion(Integer.valueOf(8200), "weakness");
    registerPotion(Integer.valueOf(8264), "long_weakness");
  }
  
  public static void toServer(Item item) {
    if (item != null) {
      if (item.getIdentifier() == 383 && item.getData() == 0) {
        CompoundTag tag = item.getTag();
        int data = 0;
        if (tag != null && tag.get("EntityTag") instanceof CompoundTag) {
          CompoundTag entityTag = (CompoundTag)tag.get("EntityTag");
          if (entityTag.get("id") instanceof StringTag) {
            StringTag id = (StringTag)entityTag.get("id");
            if (ENTTIY_NAME_TO_ID.containsKey(id.getValue()))
              data = ((Integer)ENTTIY_NAME_TO_ID.get(id.getValue())).intValue(); 
          } 
          tag.remove("EntityTag");
        } 
        item.setTag(tag);
        item.setData((short)data);
      } 
      if (item.getIdentifier() == 373) {
        CompoundTag tag = item.getTag();
        int data = 0;
        if (tag != null && tag.get("Potion") instanceof StringTag) {
          StringTag potion = (StringTag)tag.get("Potion");
          String potionName = potion.getValue().replace("minecraft:", "");
          if (POTION_NAME_TO_ID.containsKey(potionName))
            data = ((Integer)POTION_NAME_TO_ID.get(potionName)).intValue(); 
          tag.remove("Potion");
        } 
        item.setTag(tag);
        item.setData((short)data);
      } 
      if (item.getIdentifier() == 438) {
        CompoundTag tag = item.getTag();
        int data = 0;
        item.setIdentifier(373);
        if (tag != null && tag.get("Potion") instanceof StringTag) {
          StringTag potion = (StringTag)tag.get("Potion");
          String potionName = potion.getValue().replace("minecraft:", "");
          if (POTION_NAME_TO_ID.containsKey(potionName))
            data = ((Integer)POTION_NAME_TO_ID.get(potionName)).intValue() + 8192; 
          tag.remove("Potion");
        } 
        item.setTag(tag);
        item.setData((short)data);
      } 
    } 
  }
  
  public static void rewriteBookToServer(Item item) {
    int id = item.getIdentifier();
    if (id != 387)
      return; 
    CompoundTag tag = item.getTag();
    ListTag pages = (ListTag)tag.get("pages");
    if (pages == null)
      return; 
    for (int i = 0; i < pages.size(); i++) {
      Tag pageTag = pages.get(i);
      if (pageTag instanceof StringTag) {
        StringTag stag = (StringTag)pageTag;
        String value = stag.getValue();
        if (value.replaceAll(" ", "").isEmpty()) {
          value = "\"" + fixBookSpaceChars(value) + "\"";
        } else {
          value = fixBookSpaceChars(value);
        } 
        stag.setValue(value);
      } 
    } 
  }
  
  private static String fixBookSpaceChars(String str) {
    if (!str.startsWith(" "))
      return str; 
    str = "§r" + str;
    return str;
  }
  
  public static void toClient(Item item) {
    if (item != null) {
      if (item.getIdentifier() == 383 && item.getData() != 0) {
        CompoundTag tag = item.getTag();
        if (tag == null)
          tag = new CompoundTag("tag"); 
        CompoundTag entityTag = new CompoundTag("EntityTag");
        String entityName = ENTTIY_ID_TO_NAME.get(Integer.valueOf(item.getData()));
        if (entityName != null) {
          StringTag id = new StringTag("id", entityName);
          entityTag.put((Tag)id);
          tag.put((Tag)entityTag);
        } 
        item.setTag(tag);
        item.setData((short)0);
      } 
      if (item.getIdentifier() == 373) {
        CompoundTag tag = item.getTag();
        if (tag == null)
          tag = new CompoundTag("tag"); 
        if (item.getData() >= 16384) {
          item.setIdentifier(438);
          item.setData((short)(item.getData() - 8192));
        } 
        String name = potionNameFromDamage(item.getData());
        StringTag potion = new StringTag("Potion", "minecraft:" + name);
        tag.put((Tag)potion);
        item.setTag(tag);
        item.setData((short)0);
      } 
      if (item.getIdentifier() == 387) {
        CompoundTag tag = item.getTag();
        if (tag == null)
          tag = new CompoundTag("tag"); 
        ListTag pages = (ListTag)tag.get("pages");
        if (pages == null) {
          pages = new ListTag("pages", Collections.singletonList(new StringTag(Protocol1_9To1_8.fixJson(""))));
          tag.put((Tag)pages);
          item.setTag(tag);
          return;
        } 
        for (int i = 0; i < pages.size(); i++) {
          if (pages.get(i) instanceof StringTag) {
            StringTag page = (StringTag)pages.get(i);
            page.setValue(Protocol1_9To1_8.fixJson(page.getValue()));
          } 
        } 
        item.setTag(tag);
      } 
    } 
  }
  
  public static String potionNameFromDamage(short damage) {
    String id, cached = POTION_ID_TO_NAME.get(Integer.valueOf(damage));
    if (cached != null)
      return cached; 
    if (damage == 0)
      return "water"; 
    int effect = damage & 0xF;
    int name = damage & 0x3F;
    boolean enhanced = ((damage & 0x20) > 0);
    boolean extended = ((damage & 0x40) > 0);
    boolean canEnhance = true;
    boolean canExtend = true;
    switch (effect) {
      case 1:
        id = "regeneration";
        break;
      case 2:
        id = "swiftness";
        break;
      case 3:
        id = "fire_resistance";
        canEnhance = false;
        break;
      case 4:
        id = "poison";
        break;
      case 5:
        id = "healing";
        canExtend = false;
        break;
      case 6:
        id = "night_vision";
        canEnhance = false;
        break;
      case 8:
        id = "weakness";
        canEnhance = false;
        break;
      case 9:
        id = "strength";
        break;
      case 10:
        id = "slowness";
        canEnhance = false;
        break;
      case 11:
        id = "leaping";
        break;
      case 12:
        id = "harming";
        canExtend = false;
        break;
      case 13:
        id = "water_breathing";
        canEnhance = false;
        break;
      case 14:
        id = "invisibility";
        canEnhance = false;
        break;
      default:
        canEnhance = false;
        canExtend = false;
        switch (name) {
          case 0:
            id = "mundane";
            break;
          case 16:
            id = "awkward";
            break;
          case 32:
            id = "thick";
            break;
        } 
        id = "empty";
        break;
    } 
    if (effect > 0)
      if (canEnhance && enhanced) {
        id = "strong_" + id;
      } else if (canExtend && extended) {
        id = "long_" + id;
      }  
    return id;
  }
  
  public static int getNewEffectID(int oldID) {
    if (oldID >= 16384)
      oldID -= 8192; 
    Integer index = POTION_INDEX.get(Integer.valueOf(oldID));
    if (index != null)
      return index.intValue(); 
    oldID = ((Integer)POTION_NAME_TO_ID.get(potionNameFromDamage((short)oldID))).intValue();
    return 
      ((index = POTION_INDEX.get(Integer.valueOf(oldID))) != null) ? index.intValue() : 0;
  }
  
  private static void registerEntity(Integer id, String name) {
    ENTTIY_ID_TO_NAME.put(id, name);
    ENTTIY_NAME_TO_ID.put(name, id);
  }
  
  private static void registerPotion(Integer id, String name) {
    POTION_INDEX.put(id, Integer.valueOf(POTION_ID_TO_NAME.size()));
    POTION_ID_TO_NAME.put(id, name);
    POTION_NAME_TO_ID.put(name, id);
  }
}
