package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.InventoryNameRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class InventoryPackets {
  private static String NBT_TAG_NAME;
  
  private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet((Object[])new String[] { "crafting_special_banneraddpattern", "crafting_special_repairitem" });
  
  public static void register(Protocol protocol) {
    NBT_TAG_NAME = "ViaVersion|" + protocol.getClass().getSimpleName();
    protocol.registerOutgoing(State.PLAY, 20, -1, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Short windowsId = (Short)wrapper.read(Type.UNSIGNED_BYTE);
                    String type = (String)wrapper.read(Type.STRING);
                    String title = InventoryNameRewriter.processTranslate((String)wrapper.read(Type.STRING));
                    Short slots = (Short)wrapper.read(Type.UNSIGNED_BYTE);
                    if (type.equals("EntityHorse")) {
                      wrapper.setId(31);
                      int entityId = ((Integer)wrapper.read(Type.INT)).intValue();
                      wrapper.write(Type.UNSIGNED_BYTE, windowsId);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(slots.intValue()));
                      wrapper.write(Type.INT, Integer.valueOf(entityId));
                    } else {
                      wrapper.setId(46);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(windowsId.intValue()));
                      int typeId = -1;
                      switch (type) {
                        case "minecraft:container":
                        case "minecraft:chest":
                          typeId = slots.shortValue() / 9 - 1;
                          break;
                        case "minecraft:crafting_table":
                          typeId = 11;
                          break;
                        case "minecraft:furnace":
                          typeId = 13;
                          break;
                        case "minecraft:dropper":
                        case "minecraft:dispenser":
                          typeId = 6;
                          break;
                        case "minecraft:enchanting_table":
                          typeId = 12;
                          break;
                        case "minecraft:brewing_stand":
                          typeId = 10;
                          break;
                        case "minecraft:villager":
                          typeId = 18;
                          break;
                        case "minecraft:beacon":
                          typeId = 8;
                          break;
                        case "minecraft:anvil":
                          typeId = 7;
                          break;
                        case "minecraft:hopper":
                          typeId = 15;
                          break;
                        case "minecraft:shulker_box":
                          typeId = 19;
                          break;
                      } 
                      if (typeId == -1)
                        Via.getPlatform().getLogger().warning("Can't open inventory for 1.14 player! Type: " + type + " Size: " + slots); 
                      wrapper.write(Type.VAR_INT, Integer.valueOf(typeId));
                      wrapper.write(Type.STRING, title);
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 21, 20, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.FLAT_VAR_INT_ITEM_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item[] stacks = (Item[])wrapper.get(Type.FLAT_VAR_INT_ITEM_ARRAY, 0);
                    for (Item stack : stacks)
                      InventoryPackets.toClient(stack); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 23, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.FLAT_VAR_INT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryPackets.toClient((Item)wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 25, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String channel = (String)wrapper.get(Type.STRING, 0);
                    if (channel.equals("minecraft:trader_list") || channel.equals("trader_list")) {
                      wrapper.setId(39);
                      wrapper.resetReader();
                      wrapper.read(Type.STRING);
                      int windowId = ((Integer)wrapper.read(Type.INT)).intValue();
                      ((EntityTracker)wrapper.user().get(EntityTracker.class)).setLatestTradeWindowId(windowId);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(windowId));
                      int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                      for (int i = 0; i < size; i++) {
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        boolean secondItem = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (secondItem)
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                        wrapper.write(Type.INT, Integer.valueOf(0));
                        wrapper.write(Type.INT, Integer.valueOf(0));
                        wrapper.write(Type.FLOAT, Float.valueOf(0.0F));
                      } 
                      wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                      wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                      wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                    } else if (channel.equals("minecraft:book_open") || channel.equals("book_open")) {
                      int hand = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                      wrapper.clearPacket();
                      wrapper.setId(45);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(hand));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 66, 70, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.FLAT_VAR_INT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryPackets.toClient((Item)wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 84, 90, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int size = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    int deleted = 0;
                    for (int i = 0; i < size; i++) {
                      String id = (String)wrapper.read(Type.STRING);
                      String type = (String)wrapper.read(Type.STRING);
                      if (InventoryPackets.REMOVED_RECIPE_TYPES.contains(type)) {
                        deleted++;
                      } else {
                        wrapper.write(Type.STRING, type);
                        wrapper.write(Type.STRING, id);
                        if (type.equals("crafting_shapeless")) {
                          wrapper.passthrough(Type.STRING);
                          int ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                          for (int j = 0; j < ingredientsNo; j++) {
                            Item[] items = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                            for (Item item : items)
                              InventoryPackets.toClient(item); 
                          } 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        } else if (type.equals("crafting_shaped")) {
                          int ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue() * ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                          wrapper.passthrough(Type.STRING);
                          for (int j = 0; j < ingredientsNo; j++) {
                            Item[] items = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                            for (Item item : items)
                              InventoryPackets.toClient(item); 
                          } 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        } else if (type.equals("smelting")) {
                          wrapper.passthrough(Type.STRING);
                          Item[] items = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                          for (Item item : items)
                            InventoryPackets.toClient(item); 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                          wrapper.passthrough(Type.FLOAT);
                          wrapper.passthrough(Type.VAR_INT);
                        } 
                      } 
                    } 
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(size - deleted));
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 8, 9, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.VAR_INT);
            map(Type.FLAT_VAR_INT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryPackets.toServer((Item)wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 31, 33, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    PacketWrapper resyncPacket = wrapper.create(8);
                    resyncPacket.write(Type.UNSIGNED_BYTE, Short.valueOf((short)((EntityTracker)wrapper.user().get(EntityTracker.class)).getLatestTradeWindowId()));
                    resyncPacket.write(Type.SHORT, Short.valueOf((short)-999));
                    resyncPacket.write(Type.BYTE, Byte.valueOf((byte)2));
                    resyncPacket.write(Type.SHORT, Short.valueOf((short)ThreadLocalRandom.current().nextInt()));
                    resyncPacket.write(Type.VAR_INT, Integer.valueOf(5));
                    CompoundTag tag = new CompoundTag("");
                    tag.put((Tag)new DoubleTag("force_resync", Double.NaN));
                    resyncPacket.write(Type.FLAT_VAR_INT_ITEM, new Item(1, (byte)1, (short)0, tag));
                    resyncPacket.sendToServer(Protocol1_14To1_13_2.class, true, false);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 36, 38, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.FLAT_VAR_INT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryPackets.toServer((Item)wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                  }
                });
          }
        });
  }
  
  public static void toClient(Item item) {
    if (item == null)
      return; 
    item.setIdentifier(getNewItemId(item.getIdentifier()));
    CompoundTag tag;
    if ((tag = item.getTag()) != null) {
      Tag displayTag = tag.get("display");
      if (displayTag instanceof CompoundTag) {
        CompoundTag display = (CompoundTag)displayTag;
        Tag loreTag = display.get("Lore");
        if (loreTag instanceof ListTag) {
          ListTag lore = (ListTag)loreTag;
          display.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|Lore", ConverterRegistry.convertToValue((Tag)lore)));
          for (Tag loreEntry : lore) {
            if (loreEntry instanceof StringTag)
              ((StringTag)loreEntry).setValue(
                  ChatRewriter.legacyTextToJson(((StringTag)loreEntry)
                    .getValue())); 
          } 
        } 
      } 
    } 
  }
  
  public static int getNewItemId(int id) {
    Integer newId = (Integer)MappingData.oldToNewItems.get(Integer.valueOf(id));
    if (newId == null) {
      Via.getPlatform().getLogger().warning("Missing 1.14 item for 1.13.2 item " + id);
      return 1;
    } 
    return newId.intValue();
  }
  
  public static void toServer(Item item) {
    if (item == null)
      return; 
    item.setIdentifier(getOldItemId(item.getIdentifier()));
    CompoundTag tag;
    if ((tag = item.getTag()) != null) {
      Tag displayTag = tag.get("display");
      if (displayTag instanceof CompoundTag) {
        CompoundTag display = (CompoundTag)displayTag;
        Tag loreTag = display.get("Lore");
        if (loreTag instanceof ListTag) {
          ListTag lore = (ListTag)loreTag;
          ListTag via = (ListTag)display.get(NBT_TAG_NAME + "|Lore");
          if (via != null) {
            display.put(ConverterRegistry.convertToTag("Lore", ConverterRegistry.convertToValue((Tag)via)));
          } else {
            for (Tag loreEntry : lore) {
              if (loreEntry instanceof StringTag)
                ((StringTag)loreEntry).setValue(
                    ChatRewriter.jsonTextToLegacy(((StringTag)loreEntry)
                      .getValue())); 
            } 
          } 
          display.remove(NBT_TAG_NAME + "|Lore");
        } 
      } 
    } 
  }
  
  public static int getOldItemId(int id) {
    Integer oldId = (Integer)MappingData.oldToNewItems.inverse().get(Integer.valueOf(id));
    return (oldId != null) ? oldId.intValue() : 1;
  }
}
