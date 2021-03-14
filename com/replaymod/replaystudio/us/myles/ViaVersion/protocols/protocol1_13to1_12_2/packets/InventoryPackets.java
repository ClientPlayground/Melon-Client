package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.primitives.Ints;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SoundSource;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryPackets {
  private static String NBT_TAG_NAME;
  
  public static void register(Protocol protocol) {
    NBT_TAG_NAME = "ViaVersion|" + protocol.getClass().getSimpleName();
    protocol.registerOutgoing(State.PLAY, 22, 23, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toClient(stack);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 20, 21, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.ITEM_ARRAY, Type.FLAT_ITEM_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item[] stacks = (Item[])wrapper.get(Type.FLAT_ITEM_ARRAY, 0);
                    for (Item stack : stacks)
                      InventoryPackets.toClient(stack); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 21, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    short property = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                    if (property >= 4 && property <= 6)
                      wrapper.set(Type.SHORT, 1, Short.valueOf((short)MappingData.enchantmentMappings.getNewEnchantment(((Short)wrapper
                              .get(Type.SHORT, 1)).shortValue()))); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 24, 25, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String channel = (String)wrapper.get(Type.STRING, 0);
                    if (channel.equalsIgnoreCase("MC|StopSound")) {
                      String originalSource = (String)wrapper.read(Type.STRING);
                      String originalSound = (String)wrapper.read(Type.STRING);
                      wrapper.clearPacket();
                      wrapper.setId(76);
                      byte flags = 0;
                      wrapper.write(Type.BYTE, Byte.valueOf(flags));
                      if (!originalSource.isEmpty()) {
                        flags = (byte)(flags | 0x1);
                        Optional<SoundSource> finalSource = SoundSource.findBySource(originalSource);
                        if (!finalSource.isPresent()) {
                          if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
                            Via.getPlatform().getLogger().info("Could not handle unknown sound source " + originalSource + " falling back to default: master"); 
                          finalSource = Optional.of(SoundSource.MASTER);
                        } 
                        wrapper.write(Type.VAR_INT, Integer.valueOf(((SoundSource)finalSource.get()).getId()));
                      } 
                      if (!originalSound.isEmpty()) {
                        flags = (byte)(flags | 0x2);
                        wrapper.write(Type.STRING, originalSound);
                      } 
                      wrapper.set(Type.BYTE, 0, Byte.valueOf(flags));
                      return;
                    } 
                    if (channel.equalsIgnoreCase("MC|TrList")) {
                      channel = "minecraft:trader_list";
                      wrapper.passthrough(Type.INT);
                      int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                      for (int i = 0; i < size; i++) {
                        Item input = (Item)wrapper.read(Type.ITEM);
                        InventoryPackets.toClient(input);
                        wrapper.write(Type.FLAT_ITEM, input);
                        Item output = (Item)wrapper.read(Type.ITEM);
                        InventoryPackets.toClient(output);
                        wrapper.write(Type.FLAT_ITEM, output);
                        boolean secondItem = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (secondItem) {
                          Item second = (Item)wrapper.read(Type.ITEM);
                          InventoryPackets.toClient(second);
                          wrapper.write(Type.FLAT_ITEM, second);
                        } 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                      } 
                    } else {
                      String old = channel;
                      channel = InventoryPackets.getNewPluginChannelId(channel);
                      if (channel == null) {
                        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
                          Via.getPlatform().getLogger().warning("Ignoring outgoing plugin message with channel: " + old); 
                        wrapper.cancel();
                        return;
                      } 
                      if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                        String[] channels = (new String((byte[])wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8)).split("\000");
                        List<String> rewrittenChannels = new ArrayList<>();
                        for (int i = 0; i < channels.length; i++) {
                          String rewritten = InventoryPackets.getNewPluginChannelId(channels[i]);
                          if (rewritten != null) {
                            rewrittenChannels.add(rewritten);
                          } else if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                            Via.getPlatform().getLogger().warning("Ignoring plugin channel in outgoing REGISTER: " + channels[i]);
                          } 
                        } 
                        if (!rewrittenChannels.isEmpty()) {
                          wrapper.write(Type.REMAINING_BYTES, Joiner.on(false).join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                        } else {
                          wrapper.cancel();
                          return;
                        } 
                      } 
                    } 
                    wrapper.set(Type.STRING, 0, channel);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 63, 66, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toClient(stack);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 7, 8, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.VAR_INT);
            map(Type.FLAT_ITEM, Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    InventoryPackets.toServer(item);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 9, 10, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String channel = (String)wrapper.get(Type.STRING, 0);
                    String old = channel;
                    channel = InventoryPackets.getOldPluginChannelId(channel);
                    if (channel == null) {
                      if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
                        Via.getPlatform().getLogger().warning("Ignoring incoming plugin message with channel: " + old); 
                      wrapper.cancel();
                      return;
                    } 
                    if (channel.equals("REGISTER") || channel.equals("UNREGISTER")) {
                      String[] channels = (new String((byte[])wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8)).split("\000");
                      List<String> rewrittenChannels = new ArrayList<>();
                      for (int i = 0; i < channels.length; i++) {
                        String rewritten = InventoryPackets.getOldPluginChannelId(channels[i]);
                        if (rewritten != null) {
                          rewrittenChannels.add(rewritten);
                        } else if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                          Via.getPlatform().getLogger().warning("Ignoring plugin channel in incoming REGISTER: " + channels[i]);
                        } 
                      } 
                      wrapper.write(Type.REMAINING_BYTES, Joiner.on(false).join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                    } 
                    wrapper.set(Type.STRING, 0, channel);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 27, 36, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.FLAT_ITEM, Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    InventoryPackets.toServer(item);
                  }
                });
          }
        });
  }
  
  public static void toClient(Item item) {
    if (item == null)
      return; 
    CompoundTag tag = item.getTag();
    int originalId = item.getIdentifier() << 16 | item.getData() & 0xFFFF;
    int rawId = item.getIdentifier() << 4 | item.getData() & 0xF;
    if (isDamageable(item.getIdentifier())) {
      if (tag == null)
        item.setTag(tag = new CompoundTag("tag")); 
      tag.put((Tag)new IntTag("Damage", item.getData()));
    } 
    if (item.getIdentifier() == 358) {
      if (tag == null)
        item.setTag(tag = new CompoundTag("tag")); 
      tag.put((Tag)new IntTag("map", item.getData()));
    } 
    if (tag != null) {
      if ((item.getIdentifier() == 442 || item.getIdentifier() == 425) && 
        tag.get("BlockEntityTag") instanceof CompoundTag) {
        CompoundTag blockEntityTag = (CompoundTag)tag.get("BlockEntityTag");
        if (blockEntityTag.get("Base") instanceof IntTag) {
          IntTag base = (IntTag)blockEntityTag.get("Base");
          base.setValue(15 - base.getValue().intValue());
        } 
        if (blockEntityTag.get("Patterns") instanceof ListTag)
          for (Tag pattern : blockEntityTag.get("Patterns")) {
            if (pattern instanceof CompoundTag) {
              IntTag c = (IntTag)((CompoundTag)pattern).get("Color");
              c.setValue(15 - c.getValue().intValue());
            } 
          }  
      } 
      if (tag.get("display") instanceof CompoundTag) {
        CompoundTag display = (CompoundTag)tag.get("display");
        if (display.get("Name") instanceof StringTag) {
          StringTag name = (StringTag)display.get("Name");
          display.put((Tag)new StringTag(NBT_TAG_NAME + "|Name", name.getValue()));
          name.setValue(
              ChatRewriter.legacyTextToJson(name
                .getValue()));
        } 
      } 
      if (tag.get("ench") instanceof ListTag) {
        ListTag ench = (ListTag)tag.get("ench");
        ListTag enchantments = new ListTag("Enchantments", CompoundTag.class);
        for (Tag enchEntry : ench) {
          if (enchEntry instanceof CompoundTag) {
            CompoundTag enchantmentEntry = new CompoundTag("");
            short oldId = ((Number)((CompoundTag)enchEntry).get("id").getValue()).shortValue();
            String newId = (String)MappingData.oldEnchantmentsIds.get(Short.valueOf(oldId));
            if (newId == null)
              newId = "viaversion:legacy/" + oldId; 
            enchantmentEntry.put((Tag)new StringTag("id", newId));
            enchantmentEntry.put((Tag)new ShortTag("lvl", ((Number)((CompoundTag)enchEntry).get("lvl").getValue()).shortValue()));
            enchantments.add((Tag)enchantmentEntry);
          } 
        } 
        tag.remove("ench");
        tag.put((Tag)enchantments);
      } 
      if (tag.get("StoredEnchantments") instanceof ListTag) {
        ListTag storedEnch = (ListTag)tag.get("StoredEnchantments");
        ListTag newStoredEnch = new ListTag("StoredEnchantments", CompoundTag.class);
        for (Tag enchEntry : storedEnch) {
          if (enchEntry instanceof CompoundTag) {
            CompoundTag enchantmentEntry = new CompoundTag("");
            short oldId = ((Number)((CompoundTag)enchEntry).get("id").getValue()).shortValue();
            String newId = (String)MappingData.oldEnchantmentsIds.get(Short.valueOf(oldId));
            if (newId == null)
              newId = "viaversion:legacy/" + oldId; 
            enchantmentEntry.put((Tag)new StringTag("id", newId));
            enchantmentEntry.put((Tag)new ShortTag("lvl", ((Number)((CompoundTag)enchEntry).get("lvl").getValue()).shortValue()));
            newStoredEnch.add((Tag)enchantmentEntry);
          } 
        } 
        tag.remove("StoredEnchantments");
        tag.put((Tag)newStoredEnch);
      } 
      if (tag.get("CanPlaceOn") instanceof ListTag) {
        ListTag old = (ListTag)tag.get("CanPlaceOn");
        ListTag newCanPlaceOn = new ListTag("CanPlaceOn", StringTag.class);
        tag.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|CanPlaceOn", ConverterRegistry.convertToValue((Tag)old)));
        for (Tag oldTag : old) {
          Object value = oldTag.getValue();
          String oldId = value.toString().replace("minecraft:", "");
          String numberConverted = (String)BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
          if (numberConverted != null)
            oldId = numberConverted; 
          String[] newValues = (String[])BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT));
          if (newValues != null) {
            for (String newValue : newValues)
              newCanPlaceOn.add((Tag)new StringTag("", newValue)); 
            continue;
          } 
          newCanPlaceOn.add((Tag)new StringTag("", oldId.toLowerCase(Locale.ROOT)));
        } 
        tag.put((Tag)newCanPlaceOn);
      } 
      if (tag.get("CanDestroy") instanceof ListTag) {
        ListTag old = (ListTag)tag.get("CanDestroy");
        ListTag newCanDestroy = new ListTag("CanDestroy", StringTag.class);
        tag.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|CanDestroy", ConverterRegistry.convertToValue((Tag)old)));
        for (Tag oldTag : old) {
          Object value = oldTag.getValue();
          String oldId = value.toString().replace("minecraft:", "");
          String numberConverted = (String)BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
          if (numberConverted != null)
            oldId = numberConverted; 
          String[] newValues = (String[])BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT));
          if (newValues != null) {
            for (String newValue : newValues)
              newCanDestroy.add((Tag)new StringTag("", newValue)); 
            continue;
          } 
          newCanDestroy.add((Tag)new StringTag("", oldId.toLowerCase(Locale.ROOT)));
        } 
        tag.put((Tag)newCanDestroy);
      } 
      if (item.getIdentifier() == 383)
        if (tag.get("EntityTag") instanceof CompoundTag) {
          CompoundTag entityTag = (CompoundTag)tag.get("EntityTag");
          if (entityTag.get("id") instanceof StringTag) {
            StringTag identifier = (StringTag)entityTag.get("id");
            rawId = SpawnEggRewriter.getSpawnEggId(identifier.getValue());
            if (rawId == -1) {
              rawId = 25100288;
            } else {
              entityTag.remove("id");
              if (entityTag.isEmpty())
                tag.remove("EntityTag"); 
            } 
          } else {
            rawId = 25100288;
          } 
        } else {
          rawId = 25100288;
        }  
      if (tag.isEmpty())
        item.setTag(tag = null); 
    } 
    if (!MappingData.oldToNewItems.containsKey(Integer.valueOf(rawId))) {
      if (!isDamageable(item.getIdentifier()) && item.getIdentifier() != 358) {
        if (tag == null)
          item.setTag(tag = new CompoundTag("tag")); 
        tag.put((Tag)new IntTag(NBT_TAG_NAME, originalId));
      } 
      if (item.getIdentifier() == 31 && item.getData() == 0) {
        rawId = 512;
      } else if (MappingData.oldToNewItems.containsKey(Integer.valueOf(rawId & 0xFFFFFFF0))) {
        rawId &= 0xFFFFFFF0;
      } else {
        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.getIdentifier()); 
        rawId = 16;
      } 
    } 
    item.setIdentifier(((Integer)MappingData.oldToNewItems.get(Integer.valueOf(rawId))).shortValue());
    item.setData((short)0);
  }
  
  public static String getNewPluginChannelId(String old) {
    switch (old) {
      case "MC|TrList":
        return "minecraft:trader_list";
      case "MC|Brand":
        return "minecraft:brand";
      case "MC|BOpen":
        return "minecraft:book_open";
      case "MC|DebugPath":
        return "minecraft:debug/paths";
      case "MC|DebugNeighborsUpdate":
        return "minecraft:debug/neighbors_update";
      case "REGISTER":
        return "minecraft:register";
      case "UNREGISTER":
        return "minecraft:unregister";
      case "BungeeCord":
        return "bungeecord:main";
      case "WDL|INIT":
        return "wdl:init";
      case "WDL|CONTROL":
        return "wdl:control";
      case "WDL|REQUEST":
        return "wdl:request";
      case "bungeecord:main":
        return null;
      case "FML|MP":
        return "fml:mp";
      case "FML|HS":
        return "fml:hs";
    } 
    return old.matches("([0-9a-z_.-]+):([0-9a-z_/.-]+)") ? old : null;
  }
  
  public static void toServer(Item item) {
    if (item == null)
      return; 
    Integer rawId = null;
    boolean gotRawIdFromTag = false;
    CompoundTag tag = item.getTag();
    if (tag != null)
      if (tag.get(NBT_TAG_NAME) instanceof IntTag) {
        rawId = (Integer)tag.get(NBT_TAG_NAME).getValue();
        tag.remove(NBT_TAG_NAME);
        gotRawIdFromTag = true;
      }  
    if (rawId == null) {
      Integer oldId = (Integer)MappingData.oldToNewItems.inverse().get(Integer.valueOf(item.getIdentifier()));
      if (oldId != null) {
        Optional<String> eggEntityId = SpawnEggRewriter.getEntityId(oldId.intValue());
        if (eggEntityId.isPresent()) {
          rawId = Integer.valueOf(25100288);
          if (tag == null)
            item.setTag(tag = new CompoundTag("tag")); 
          if (!tag.contains("EntityTag")) {
            CompoundTag entityTag = new CompoundTag("EntityTag");
            entityTag.put((Tag)new StringTag("id", (String)eggEntityId.get()));
            tag.put((Tag)entityTag);
          } 
        } else {
          rawId = Integer.valueOf(oldId.intValue() >> 4 << 16 | oldId.intValue() & 0xF);
        } 
      } 
    } 
    if (rawId == null) {
      if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
        Via.getPlatform().getLogger().warning("Failed to get 1.12 item for " + item.getIdentifier()); 
      rawId = Integer.valueOf(65536);
    } 
    item.setIdentifier((short)(rawId.intValue() >> 16));
    item.setData((short)(rawId.intValue() & 0xFFFF));
    if (tag != null) {
      if (isDamageable(item.getIdentifier()) && 
        tag.get("Damage") instanceof IntTag) {
        if (!gotRawIdFromTag)
          item.setData((short)((Integer)tag.get("Damage").getValue()).intValue()); 
        tag.remove("Damage");
      } 
      if (item.getIdentifier() == 358 && 
        tag.get("map") instanceof IntTag) {
        if (!gotRawIdFromTag)
          item.setData((short)((Integer)tag.get("map").getValue()).intValue()); 
        tag.remove("map");
      } 
      if ((item.getIdentifier() == 442 || item.getIdentifier() == 425) && 
        tag.get("BlockEntityTag") instanceof CompoundTag) {
        CompoundTag blockEntityTag = (CompoundTag)tag.get("BlockEntityTag");
        if (blockEntityTag.get("Base") instanceof IntTag) {
          IntTag base = (IntTag)blockEntityTag.get("Base");
          base.setValue(15 - base.getValue().intValue());
        } 
        if (blockEntityTag.get("Patterns") instanceof ListTag)
          for (Tag pattern : blockEntityTag.get("Patterns")) {
            if (pattern instanceof CompoundTag) {
              IntTag c = (IntTag)((CompoundTag)pattern).get("Color");
              c.setValue(15 - c.getValue().intValue());
            } 
          }  
      } 
      if (tag.get("display") instanceof CompoundTag) {
        CompoundTag display = (CompoundTag)tag.get("display");
        if (((CompoundTag)tag.get("display")).get("Name") instanceof StringTag) {
          StringTag name = (StringTag)display.get("Name");
          StringTag via = (StringTag)display.get(NBT_TAG_NAME + "|Name");
          name.setValue((via != null) ? via
              .getValue() : ChatRewriter.jsonTextToLegacy(name
                .getValue()));
          display.remove(NBT_TAG_NAME + "|Name");
        } 
      } 
      if (tag.get("Enchantments") instanceof ListTag) {
        ListTag enchantments = (ListTag)tag.get("Enchantments");
        ListTag ench = new ListTag("ench", CompoundTag.class);
        for (Tag enchantmentEntry : enchantments) {
          if (enchantmentEntry instanceof CompoundTag) {
            CompoundTag enchEntry = new CompoundTag("");
            String newId = (String)((CompoundTag)enchantmentEntry).get("id").getValue();
            Short oldId = (Short)MappingData.oldEnchantmentsIds.inverse().get(newId);
            if (oldId == null && newId.startsWith("viaversion:legacy/"))
              oldId = Short.valueOf(newId.substring(18)); 
            enchEntry.put((Tag)new ShortTag("id", oldId
                  
                  .shortValue()));
            enchEntry.put((Tag)new ShortTag("lvl", ((Short)((CompoundTag)enchantmentEntry).get("lvl").getValue()).shortValue()));
            ench.add((Tag)enchEntry);
          } 
        } 
        tag.remove("Enchantments");
        tag.put((Tag)ench);
      } 
      if (tag.get("StoredEnchantments") instanceof ListTag) {
        ListTag storedEnch = (ListTag)tag.get("StoredEnchantments");
        ListTag newStoredEnch = new ListTag("StoredEnchantments", CompoundTag.class);
        for (Tag enchantmentEntry : storedEnch) {
          if (enchantmentEntry instanceof CompoundTag) {
            CompoundTag enchEntry = new CompoundTag("");
            String newId = (String)((CompoundTag)enchantmentEntry).get("id").getValue();
            Short oldId = (Short)MappingData.oldEnchantmentsIds.inverse().get(newId);
            if (oldId == null && newId.startsWith("viaversion:legacy/"))
              oldId = Short.valueOf(newId.substring(18)); 
            enchEntry.put((Tag)new ShortTag("id", oldId
                  
                  .shortValue()));
            enchEntry.put((Tag)new ShortTag("lvl", ((Short)((CompoundTag)enchantmentEntry).get("lvl").getValue()).shortValue()));
            newStoredEnch.add((Tag)enchEntry);
          } 
        } 
        tag.remove("StoredEnchantments");
        tag.put((Tag)newStoredEnch);
      } 
      if (tag.get(NBT_TAG_NAME + "|CanPlaceOn") instanceof ListTag) {
        tag.put(ConverterRegistry.convertToTag("CanPlaceOn", 
              
              ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanPlaceOn"))));
        tag.remove(NBT_TAG_NAME + "|CanPlaceOn");
      } else if (tag.get("CanPlaceOn") instanceof ListTag) {
        ListTag old = (ListTag)tag.get("CanPlaceOn");
        ListTag newCanPlaceOn = new ListTag("CanPlaceOn", StringTag.class);
        for (Tag oldTag : old) {
          Object value = oldTag.getValue();
          String[] newValues = (String[])BlockIdData.fallbackReverseMapping.get((value instanceof String) ? ((String)value)
              .replace("minecraft:", "") : null);
          if (newValues != null) {
            for (String newValue : newValues)
              newCanPlaceOn.add((Tag)new StringTag("", newValue)); 
            continue;
          } 
          newCanPlaceOn.add(oldTag);
        } 
        tag.put((Tag)newCanPlaceOn);
      } 
      if (tag.get(NBT_TAG_NAME + "|CanDestroy") instanceof ListTag) {
        tag.put(ConverterRegistry.convertToTag("CanDestroy", 
              
              ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanDestroy"))));
        tag.remove(NBT_TAG_NAME + "|CanDestroy");
      } else if (tag.get("CanDestroy") instanceof ListTag) {
        ListTag old = (ListTag)tag.get("CanDestroy");
        ListTag newCanDestroy = new ListTag("CanDestroy", StringTag.class);
        for (Tag oldTag : old) {
          Object value = oldTag.getValue();
          String[] newValues = (String[])BlockIdData.fallbackReverseMapping.get((value instanceof String) ? ((String)value)
              .replace("minecraft:", "") : null);
          if (newValues != null) {
            for (String newValue : newValues)
              newCanDestroy.add((Tag)new StringTag("", newValue)); 
            continue;
          } 
          newCanDestroy.add(oldTag);
        } 
        tag.put((Tag)newCanDestroy);
      } 
    } 
  }
  
  public static String getOldPluginChannelId(String newId) {
    if (!newId.matches("([0-9a-z_.-]+):([0-9a-z_/.-]+)"))
      return null; 
    int separatorIndex = newId.indexOf(':');
    if ((separatorIndex == -1 || separatorIndex == 0) && newId.length() <= 10)
      newId = "minecraft:" + newId; 
    switch (newId) {
      case "minecraft:trader_list":
        return "MC|TrList";
      case "minecraft:book_open":
        return "MC|BOpen";
      case "minecraft:debug/paths":
        return "MC|DebugPath";
      case "minecraft:debug/neighbors_update":
        return "MC|DebugNeighborsUpdate";
      case "minecraft:register":
        return "REGISTER";
      case "minecraft:unregister":
        return "UNREGISTER";
      case "minecraft:brand":
        return "MC|Brand";
      case "bungeecord:main":
        return "BungeeCord";
      case "wdl:init":
        return "WDL|INIT";
      case "wdl:control":
        return "WDL|CONTROL";
      case "wdl:request":
        return "WDL|REQUEST";
      case "fml:hs":
        return "FML|HS";
      case "fml:mp":
        return "FML:MP";
    } 
    return (newId.length() > 20) ? newId.substring(0, 20) : newId;
  }
  
  public static boolean isDamageable(int id) {
    return ((id >= 256 && id <= 259) || id == 261 || (id >= 267 && id <= 279) || (id >= 283 && id <= 286) || (id >= 290 && id <= 294) || (id >= 298 && id <= 317) || id == 346 || id == 359 || id == 398 || id == 442 || id == 443);
  }
}
