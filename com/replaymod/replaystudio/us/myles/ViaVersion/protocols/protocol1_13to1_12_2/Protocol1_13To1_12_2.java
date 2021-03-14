package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_13Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.ConnectionData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.RecipeData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.PaintingProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.util.EnumMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;

public class Protocol1_13To1_12_2 extends Protocol {
  public static final PacketHandler POS_TO_3_INT = new PacketHandler() {
      public void handle(PacketWrapper wrapper) throws Exception {
        Position position = (Position)wrapper.read(Type.POSITION);
        wrapper.write(Type.INT, Integer.valueOf(position.getX().intValue()));
        wrapper.write(Type.INT, Integer.valueOf(position.getY().intValue()));
        wrapper.write(Type.INT, Integer.valueOf(position.getZ().intValue()));
      }
    };
  
  public static final PacketHandler SEND_DECLARE_COMMANDS_AND_TAGS = new PacketHandler() {
      public void handle(PacketWrapper w) throws Exception {
        w.create(17, new ValueCreator() {
              public void write(PacketWrapper wrapper) {
                wrapper.write(Type.VAR_INT, Integer.valueOf(2));
                wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                wrapper.write(Type.VAR_INT, Integer.valueOf(1));
                wrapper.write(Type.VAR_INT, Integer.valueOf(1));
                wrapper.write(Type.VAR_INT, Integer.valueOf(22));
                wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                wrapper.write(Type.STRING, "args");
                wrapper.write(Type.STRING, "brigadier:string");
                wrapper.write(Type.VAR_INT, Integer.valueOf(2));
                wrapper.write(Type.STRING, "minecraft:ask_server");
                wrapper.write(Type.VAR_INT, Integer.valueOf(0));
              }
            }).send(Protocol1_13To1_12_2.class);
        w.create(85, new ValueCreator() {
              public void write(PacketWrapper wrapper) throws Exception {
                wrapper.write(Type.VAR_INT, Integer.valueOf(MappingData.blockTags.size()));
                for (Map.Entry<String, Integer[]> tag : (Iterable<Map.Entry<String, Integer[]>>)MappingData.blockTags.entrySet()) {
                  wrapper.write(Type.STRING, tag.getKey());
                  wrapper.write(Type.VAR_INT_ARRAY, ((Integer[])tag.getValue()).clone());
                } 
                wrapper.write(Type.VAR_INT, Integer.valueOf(MappingData.itemTags.size()));
                for (Map.Entry<String, Integer[]> tag : (Iterable<Map.Entry<String, Integer[]>>)MappingData.itemTags.entrySet()) {
                  wrapper.write(Type.STRING, tag.getKey());
                  wrapper.write(Type.VAR_INT_ARRAY, ((Integer[])tag.getValue()).clone());
                } 
                wrapper.write(Type.VAR_INT, Integer.valueOf(MappingData.fluidTags.size()));
                for (Map.Entry<String, Integer[]> tag : (Iterable<Map.Entry<String, Integer[]>>)MappingData.fluidTags.entrySet()) {
                  wrapper.write(Type.STRING, tag.getKey());
                  wrapper.write(Type.VAR_INT_ARRAY, ((Integer[])tag.getValue()).clone());
                } 
              }
            }).send(Protocol1_13To1_12_2.class);
      }
    };
  
  protected static final EnumMap<ChatColor, Character> SCOREBOARD_TEAM_NAME_REWRITE = new EnumMap<>(ChatColor.class);
  
  static {
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.BLACK, Character.valueOf('g'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_BLUE, Character.valueOf('h'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_GREEN, Character.valueOf('i'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_AQUA, Character.valueOf('j'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_RED, Character.valueOf('p'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_PURPLE, Character.valueOf('q'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.GOLD, Character.valueOf('s'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.GRAY, Character.valueOf('t'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.DARK_GRAY, Character.valueOf('u'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.BLUE, Character.valueOf('v'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.GREEN, Character.valueOf('w'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.AQUA, Character.valueOf('x'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.RED, Character.valueOf('y'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.LIGHT_PURPLE, Character.valueOf('z'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.YELLOW, Character.valueOf('!'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.WHITE, Character.valueOf('?'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.MAGIC, Character.valueOf('#'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.BOLD, Character.valueOf('('));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.STRIKETHROUGH, Character.valueOf(')'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.UNDERLINE, Character.valueOf(':'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.ITALIC, Character.valueOf(';'));
    SCOREBOARD_TEAM_NAME_REWRITE.put(ChatColor.RESET, Character.valueOf('/'));
    MappingData.init();
    ConnectionData.init();
    RecipeData.init();
    BlockIdData.init();
  }
  
  protected void registerPackets() {
    EntityPackets.register(this);
    WorldPackets.register(this);
    InventoryPackets.register(this);
    registerOutgoing(State.LOGIN, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.STRING, 0, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 0)));
                  }
                });
          }
        });
    registerOutgoing(State.STATUS, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String response = (String)wrapper.get(Type.STRING, 0);
                    try {
                      JsonObject json = (JsonObject)GsonUtil.getGson().fromJson(response, JsonObject.class);
                      if (json.has("favicon"))
                        json.addProperty("favicon", json.get("favicon").getAsString().replace("\n", "")); 
                      wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson((JsonElement)json));
                    } catch (JsonParseException e) {
                      e.printStackTrace();
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 7, 7, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 12, 12, new PacketRemapper() {
          public void registerMap() {
            map(Type.UUID);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (action == 0 || action == 3)
                      wrapper.write(Type.STRING, ChatRewriter.processTranslate((String)wrapper.read(Type.STRING))); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 15, 14, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.STRING, 0, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 0)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 14, 16, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    int index, length;
                    wrapper.write(Type.VAR_INT, Integer.valueOf(((TabCompleteTracker)wrapper.user().get(TabCompleteTracker.class)).getTransactionId()));
                    String input = ((TabCompleteTracker)wrapper.user().get(TabCompleteTracker.class)).getInput();
                    if (input.endsWith(" ") || input.length() == 0) {
                      index = input.length();
                      length = 0;
                    } else {
                      int lastSpace = input.lastIndexOf(" ") + 1;
                      index = lastSpace;
                      length = input.length() - lastSpace;
                    } 
                    wrapper.write(Type.VAR_INT, Integer.valueOf(index));
                    wrapper.write(Type.VAR_INT, Integer.valueOf(length));
                    int count = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < count; i++) {
                      String suggestion = (String)wrapper.read(Type.STRING);
                      if (suggestion.startsWith("/") && index == 0)
                        suggestion = suggestion.substring(1); 
                      wrapper.write(Type.STRING, suggestion);
                      wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 17, 18);
    registerOutgoing(State.PLAY, 18, 19);
    registerOutgoing(State.PLAY, 19, 20, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.STRING, 1, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 1)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 23, 24, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int item = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    int ticks = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    wrapper.cancel();
                    if (item == 383) {
                      for (int i = 0; i < 44; ) {
                        Integer newItem = (Integer)MappingData.oldToNewItems.get(Integer.valueOf(item << 16 | i));
                        if (newItem != null) {
                          PacketWrapper packet = wrapper.create(24);
                          packet.write(Type.VAR_INT, newItem);
                          packet.write(Type.VAR_INT, Integer.valueOf(ticks));
                          packet.send(Protocol1_13To1_12_2.class);
                          i++;
                        } 
                      } 
                    } else {
                      for (int i = 0; i < 16; ) {
                        Integer newItem = (Integer)MappingData.oldToNewItems.get(Integer.valueOf(item << 4 | i));
                        if (newItem != null) {
                          PacketWrapper packet = wrapper.create(24);
                          packet.write(Type.VAR_INT, newItem);
                          packet.write(Type.VAR_INT, Integer.valueOf(ticks));
                          packet.send(Protocol1_13To1_12_2.class);
                          i++;
                        } 
                      } 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 26, 27, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.STRING, 0, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 0)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 27, 28);
    registerOutgoing(State.PLAY, 28, 30);
    registerOutgoing(State.PLAY, 30, 32);
    registerOutgoing(State.PLAY, 31, 33);
    registerOutgoing(State.PLAY, 33, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.POSITION);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    int data = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    if (id == 1010) {
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = ((Integer)MappingData.oldToNewItems.get(Integer.valueOf(data << 4))).intValue()));
                    } else if (id == 2001) {
                      int blockId = data & 0xFFF;
                      int blockData = data >> 12;
                      wrapper.set(Type.INT, 1, Integer.valueOf(data = WorldPackets.toNewId(blockId << 4 | blockData)));
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 35, 37, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).addEntity(entityId, Entity1_13Types.EntityType.PLAYER);
                    ClientWorld clientChunks = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    clientChunks.setEnvironment(dimensionId);
                  }
                });
            handler(Protocol1_13To1_12_2.SEND_DECLARE_COMMANDS_AND_TAGS);
          }
        });
    registerOutgoing(State.PLAY, 36, 38, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.BYTE);
            map(Type.BOOLEAN);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int iconCount = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < iconCount; i++) {
                      byte directionAndType = ((Byte)wrapper.read(Type.BYTE)).byteValue();
                      int type = (directionAndType & 0xF0) >> 4;
                      wrapper.write(Type.VAR_INT, Integer.valueOf(type));
                      wrapper.passthrough(Type.BYTE);
                      wrapper.passthrough(Type.BYTE);
                      byte direction = (byte)(directionAndType & 0xF);
                      wrapper.write(Type.BYTE, Byte.valueOf(direction));
                      wrapper.write(Type.OPTIONAL_CHAT, null);
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 37, 39);
    registerOutgoing(State.PLAY, 38, 40);
    registerOutgoing(State.PLAY, 39, 41);
    registerOutgoing(State.PLAY, 40, 42);
    registerOutgoing(State.PLAY, 41, 43);
    registerOutgoing(State.PLAY, 42, 44);
    registerOutgoing(State.PLAY, 43, 45, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "viaversion:legacy/" + wrapper.read(Type.VAR_INT));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 44, 46);
    registerOutgoing(State.PLAY, 45, 47, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (((Integer)wrapper.get(Type.VAR_INT, 0)).intValue() == 2) {
                      wrapper.passthrough(Type.VAR_INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.write(Type.STRING, ChatRewriter.processTranslate((String)wrapper.read(Type.STRING)));
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 46, 48);
    registerOutgoing(State.PLAY, 47, 50);
    registerOutgoing(State.PLAY, 48, 51);
    registerOutgoing(State.PLAY, 49, 52, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.BOOLEAN);
            map(Type.BOOLEAN);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    for (int i = 0; i < ((action == 0) ? 2 : 1); i++) {
                      Integer[] ids = (Integer[])wrapper.read(Type.VAR_INT_ARRAY);
                      String[] stringIds = new String[ids.length];
                      for (int j = 0; j < ids.length; j++)
                        stringIds[j] = "viaversion:legacy/" + ids[j]; 
                      wrapper.write(Type.STRING_ARRAY, stringIds);
                    } 
                    if (action == 0)
                      wrapper.create(84, new ValueCreator() {
                            public void write(PacketWrapper wrapper) throws Exception {
                              wrapper.write(Type.VAR_INT, Integer.valueOf(RecipeData.recipes.size()));
                              for (Map.Entry<String, RecipeData.Recipe> entry : (Iterable<Map.Entry<String, RecipeData.Recipe>>)RecipeData.recipes.entrySet()) {
                                Item[] clone;
                                int i;
                                wrapper.write(Type.STRING, entry.getKey());
                                wrapper.write(Type.STRING, ((RecipeData.Recipe)entry.getValue()).getType());
                                switch (((RecipeData.Recipe)entry.getValue()).getType()) {
                                  case "crafting_shapeless":
                                    wrapper.write(Type.STRING, ((RecipeData.Recipe)entry.getValue()).getGroup());
                                    wrapper.write(Type.VAR_INT, Integer.valueOf((((RecipeData.Recipe)entry.getValue()).getIngredients()).length));
                                    for (Item[] ingredient : ((RecipeData.Recipe)entry.getValue()).getIngredients()) {
                                      Item[] arrayOfItem1 = (Item[])ingredient.clone();
                                      for (int j = 0; j < arrayOfItem1.length; j++) {
                                        if (arrayOfItem1[j] != null)
                                          arrayOfItem1[j] = new Item(arrayOfItem1[j].getId(), arrayOfItem1[j].getAmount(), (short)0, null); 
                                      } 
                                      wrapper.write(Type.FLAT_ITEM_ARRAY_VAR_INT, arrayOfItem1);
                                    } 
                                    wrapper.write(Type.FLAT_ITEM, new Item(((RecipeData.Recipe)entry
                                          .getValue()).getResult().getId(), ((RecipeData.Recipe)entry
                                          .getValue()).getResult().getAmount(), (short)0, null));
                                  case "crafting_shaped":
                                    wrapper.write(Type.VAR_INT, Integer.valueOf(((RecipeData.Recipe)entry.getValue()).getWidth()));
                                    wrapper.write(Type.VAR_INT, Integer.valueOf(((RecipeData.Recipe)entry.getValue()).getHeight()));
                                    wrapper.write(Type.STRING, ((RecipeData.Recipe)entry.getValue()).getGroup());
                                    for (Item[] ingredient : ((RecipeData.Recipe)entry.getValue()).getIngredients()) {
                                      Item[] arrayOfItem1 = (Item[])ingredient.clone();
                                      for (int j = 0; j < arrayOfItem1.length; j++) {
                                        if (arrayOfItem1[j] != null)
                                          arrayOfItem1[j] = new Item(arrayOfItem1[j].getId(), arrayOfItem1[j].getAmount(), (short)0, null); 
                                      } 
                                      wrapper.write(Type.FLAT_ITEM_ARRAY_VAR_INT, arrayOfItem1);
                                    } 
                                    wrapper.write(Type.FLAT_ITEM, new Item(((RecipeData.Recipe)entry
                                          .getValue()).getResult().getId(), ((RecipeData.Recipe)entry
                                          .getValue()).getResult().getAmount(), (short)0, null));
                                  case "smelting":
                                    wrapper.write(Type.STRING, ((RecipeData.Recipe)entry.getValue()).getGroup());
                                    clone = (Item[])((RecipeData.Recipe)entry.getValue()).getIngredient().clone();
                                    for (i = 0; i < clone.length; i++) {
                                      if (clone[i] != null)
                                        clone[i] = new Item(clone[i].getId(), clone[i].getAmount(), (short)0, null); 
                                    } 
                                    wrapper.write(Type.FLAT_ITEM_ARRAY_VAR_INT, clone);
                                    wrapper.write(Type.FLAT_ITEM, new Item(((RecipeData.Recipe)entry
                                          .getValue()).getResult().getId(), ((RecipeData.Recipe)entry
                                          .getValue()).getResult().getAmount(), (short)0, null));
                                    wrapper.write(Type.FLOAT, Float.valueOf(((RecipeData.Recipe)entry.getValue()).getExperience()));
                                    wrapper.write(Type.VAR_INT, Integer.valueOf(((RecipeData.Recipe)entry.getValue()).getCookingTime()));
                                } 
                              } 
                            }
                          }).send(Protocol1_13To1_12_2.class, true, true); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 51, 54);
    registerOutgoing(State.PLAY, 52, 55);
    registerOutgoing(State.PLAY, 53, 56, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    clientWorld.setEnvironment(dimensionId);
                    if (Via.getConfig().isServersideBlockConnections())
                      ConnectionData.clearBlockStorage(wrapper.user()); 
                  }
                });
            handler(Protocol1_13To1_12_2.SEND_DECLARE_COMMANDS_AND_TAGS);
          }
        });
    registerOutgoing(State.PLAY, 54, 57);
    registerOutgoing(State.PLAY, 55, 58);
    registerOutgoing(State.PLAY, 56, 59);
    registerOutgoing(State.PLAY, 57, 60);
    registerOutgoing(State.PLAY, 58, 61);
    registerOutgoing(State.PLAY, 59, 62);
    registerOutgoing(State.PLAY, 61, 64);
    registerOutgoing(State.PLAY, 62, 65);
    registerOutgoing(State.PLAY, 64, 67);
    registerOutgoing(State.PLAY, 65, 68);
    registerOutgoing(State.PLAY, 66, 69, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    byte mode = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    if (mode == 0 || mode == 2) {
                      String value = (String)wrapper.read(Type.STRING);
                      value = ChatRewriter.legacyTextToJson(value);
                      wrapper.write(Type.STRING, value);
                      String type = (String)wrapper.read(Type.STRING);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(type.equals("integer") ? 0 : 1));
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 67, 70);
    registerOutgoing(State.PLAY, 68, 71, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    byte action = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    if (action == 0 || action == 2) {
                      String displayName = (String)wrapper.read(Type.STRING);
                      displayName = ChatRewriter.legacyTextToJson(displayName);
                      wrapper.write(Type.STRING, displayName);
                      String prefix = (String)wrapper.read(Type.STRING);
                      String suffix = (String)wrapper.read(Type.STRING);
                      wrapper.passthrough(Type.BYTE);
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.STRING);
                      int colour = ((Byte)wrapper.read(Type.BYTE)).intValue();
                      if (colour == -1)
                        colour = 21; 
                      if (Via.getConfig().is1_13TeamColourFix()) {
                        colour = Protocol1_13To1_12_2.this.getLastColor(prefix).ordinal();
                        suffix = Protocol1_13To1_12_2.this.getLastColor(prefix).toString() + suffix;
                      } 
                      wrapper.write(Type.VAR_INT, Integer.valueOf(colour));
                      wrapper.write(Type.STRING, ChatRewriter.legacyTextToJson(prefix));
                      wrapper.write(Type.STRING, ChatRewriter.legacyTextToJson(suffix));
                    } 
                    if (action == 0 || action == 3 || action == 4) {
                      String[] names = (String[])wrapper.read(Type.STRING_ARRAY);
                      for (int i = 0; i < names.length; i++)
                        names[i] = Protocol1_13To1_12_2.this.rewriteTeamMemberName(names[i]); 
                      wrapper.write(Type.STRING_ARRAY, names);
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 69, 72, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String displayName = (String)wrapper.read(Type.STRING);
                    displayName = Protocol1_13To1_12_2.this.rewriteTeamMemberName(displayName);
                    wrapper.write(Type.STRING, displayName);
                    byte action = ((Byte)wrapper.read(Type.BYTE)).byteValue();
                    wrapper.write(Type.BYTE, Byte.valueOf(action));
                    wrapper.passthrough(Type.STRING);
                    if (action != 1)
                      wrapper.passthrough(Type.VAR_INT); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 70, 73);
    registerOutgoing(State.PLAY, 71, 74);
    registerOutgoing(State.PLAY, 72, 75, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (action >= 0 && action <= 2)
                      wrapper.write(Type.STRING, ChatRewriter.processTranslate((String)wrapper.read(Type.STRING))); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 73, 77, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int soundId = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_13To1_12_2.this.getNewSoundID(soundId)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 74, 78, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.STRING, 0, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 0)));
                    wrapper.set(Type.STRING, 1, ChatRewriter.processTranslate((String)wrapper.get(Type.STRING, 1)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 75, 79);
    registerOutgoing(State.PLAY, 76, 80);
    registerOutgoing(State.PLAY, 77, 81, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.passthrough(Type.BOOLEAN);
                    int size = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < size; i++) {
                      wrapper.passthrough(Type.STRING);
                      if (((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue())
                        wrapper.passthrough(Type.STRING); 
                      if (((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue()) {
                        wrapper.write(Type.STRING, ChatRewriter.processTranslate((String)wrapper.read(Type.STRING)));
                        wrapper.write(Type.STRING, ChatRewriter.processTranslate((String)wrapper.read(Type.STRING)));
                        Item icon = (Item)wrapper.read(Type.ITEM);
                        InventoryPackets.toClient(icon);
                        wrapper.write(Type.FLAT_ITEM, icon);
                        wrapper.passthrough(Type.VAR_INT);
                        int flags = ((Integer)wrapper.passthrough(Type.INT)).intValue();
                        if ((flags & 0x1) != 0)
                          wrapper.passthrough(Type.STRING); 
                        wrapper.passthrough(Type.FLOAT);
                        wrapper.passthrough(Type.FLOAT);
                      } 
                      wrapper.passthrough(Type.STRING_ARRAY);
                      int arrayLength = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                      for (int array = 0; array < arrayLength; array++)
                        wrapper.passthrough(Type.STRING_ARRAY); 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 78, 82);
    registerOutgoing(State.PLAY, 79, 83);
    registerIncoming(State.LOGIN, -1, 2, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, -1, 1, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 1, 5, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (Via.getConfig().isDisable1_13AutoComplete())
                      wrapper.cancel(); 
                    int tid = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    ((TabCompleteTracker)wrapper.user().get(TabCompleteTracker.class)).setTransactionId(tid);
                  }
                });
            map(Type.STRING, new ValueTransformer<String, String>(Type.STRING) {
                  public String transform(PacketWrapper wrapper, String inputValue) {
                    ((TabCompleteTracker)wrapper.user().get(TabCompleteTracker.class)).setInput(inputValue);
                    return "/" + inputValue;
                  }
                });
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(false));
                    wrapper.write(Type.OPTIONAL_POSITION, null);
                    if (!wrapper.isCancelled() && Via.getConfig().get1_13TabCompleteDelay() > 0) {
                      TabCompleteTracker tracker = (TabCompleteTracker)wrapper.user().get(TabCompleteTracker.class);
                      wrapper.cancel();
                      tracker.setTimeToSend(System.currentTimeMillis() + (Via.getConfig().get1_13TabCompleteDelay() * 50));
                      tracker.setLastTabComplete((String)wrapper.get(Type.STRING, 0));
                    } 
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 5, 6);
    registerIncoming(State.PLAY, 6, 7);
    registerIncoming(State.PLAY, 8, 9);
    registerIncoming(State.PLAY, 9, 11, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.read(Type.FLAT_ITEM);
                    boolean isSigning = ((Boolean)wrapper.read(Type.BOOLEAN)).booleanValue();
                    InventoryPackets.toServer(item);
                    wrapper.write(Type.STRING, isSigning ? "MC|BSign" : "MC|BEdit");
                    wrapper.write(Type.ITEM, item);
                  }
                });
          }
        });
    registerIncoming(State.PLAY, -1, 12, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 10, 13);
    registerIncoming(State.PLAY, 11, 14);
    registerIncoming(State.PLAY, 12, 15);
    registerIncoming(State.PLAY, 13, 16);
    registerIncoming(State.PLAY, 14, 17);
    registerIncoming(State.PLAY, 15, 18);
    registerIncoming(State.PLAY, 16, 19);
    registerIncoming(State.PLAY, 17, 20);
    registerIncoming(State.PLAY, 9, 21, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|PickItem");
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 18, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.VAR_INT, Integer.valueOf(Integer.parseInt(((String)wrapper.read(Type.STRING)).substring(18))));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 19, 23);
    registerIncoming(State.PLAY, 20, 24);
    registerIncoming(State.PLAY, 21, 25);
    registerIncoming(State.PLAY, 22, 26);
    registerIncoming(State.PLAY, 23, 27, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (type == 0)
                      wrapper.write(Type.INT, Integer.valueOf(Integer.parseInt(((String)wrapper.read(Type.STRING)).substring(18)))); 
                    if (type == 1) {
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                    } 
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 9, 28, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|ItemName");
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 24, 29);
    registerIncoming(State.PLAY, 25, 30);
    registerIncoming(State.PLAY, 9, 31, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|TrSel");
                  }
                });
            map(Type.VAR_INT, Type.INT);
          }
        });
    registerIncoming(State.PLAY, 9, 32, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|Beacon");
                  }
                });
            map(Type.VAR_INT, Type.INT);
            map(Type.VAR_INT, Type.INT);
          }
        });
    registerIncoming(State.PLAY, 26, 33);
    registerIncoming(State.PLAY, 9, 34, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|AutoCmd");
                  }
                });
            handler(Protocol1_13To1_12_2.POS_TO_3_INT);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int mode = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    byte flags = ((Byte)wrapper.read(Type.BYTE)).byteValue();
                    String stringMode = (mode == 0) ? "SEQUENCE" : ((mode == 1) ? "AUTO" : "REDSTONE");
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x1) != 0)));
                    wrapper.write(Type.STRING, stringMode);
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x2) != 0)));
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x4) != 0)));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 9, 35, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|AdvCmd");
                    wrapper.write(Type.BYTE, Byte.valueOf((byte)1));
                  }
                });
            map(Type.VAR_INT, Type.INT);
          }
        });
    registerIncoming(State.PLAY, 9, 37, new PacketRemapper() {
          public void registerMap() {
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.STRING, "MC|Struct");
                  }
                });
            handler(Protocol1_13To1_12_2.POS_TO_3_INT);
            map(Type.VAR_INT, new ValueTransformer<Integer, Byte>(Type.BYTE) {
                  public Byte transform(PacketWrapper wrapper, Integer action) throws Exception {
                    return Byte.valueOf((byte)(action.intValue() + 1));
                  }
                });
            map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) {
                  public String transform(PacketWrapper wrapper, Integer mode) throws Exception {
                    return (mode.intValue() == 0) ? "SAVE" : (
                      (mode.intValue() == 1) ? "LOAD" : (
                      (mode.intValue() == 2) ? "CORNER" : "DATA"));
                  }
                });
            map(Type.STRING);
            map(Type.BYTE, Type.INT);
            map(Type.BYTE, Type.INT);
            map(Type.BYTE, Type.INT);
            map(Type.BYTE, Type.INT);
            map(Type.BYTE, Type.INT);
            map(Type.BYTE, Type.INT);
            map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) {
                  public String transform(PacketWrapper wrapper, Integer mirror) throws Exception {
                    return (mirror.intValue() == 0) ? "NONE" : (
                      (mirror.intValue() == 1) ? "LEFT_RIGHT" : "FRONT_BACK");
                  }
                });
            map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) {
                  public String transform(PacketWrapper wrapper, Integer rotation) throws Exception {
                    return (rotation.intValue() == 0) ? "NONE" : (
                      (rotation.intValue() == 1) ? "CLOCKWISE_90" : (
                      (rotation.intValue() == 2) ? "CLOCKWISE_180" : "COUNTERCLOCKWISE_90"));
                  }
                });
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    float integrity = ((Float)wrapper.read(Type.FLOAT)).floatValue();
                    long seed = ((Long)wrapper.read(Type.VAR_LONG)).longValue();
                    byte flags = ((Byte)wrapper.read(Type.BYTE)).byteValue();
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x1) != 0)));
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x2) != 0)));
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(((flags & 0x4) != 0)));
                    wrapper.write(Type.FLOAT, Float.valueOf(integrity));
                    wrapper.write(Type.VAR_LONG, Long.valueOf(seed));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 28, 38);
    registerIncoming(State.PLAY, 29, 39);
    registerIncoming(State.PLAY, 30, 40);
    registerIncoming(State.PLAY, 31, 41);
    registerIncoming(State.PLAY, 32, 42);
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    userConnection.put((StoredObject)new TabCompleteTracker(userConnection));
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
    userConnection.put((StoredObject)new BlockStorage(userConnection));
    if (Via.getConfig().isServersideBlockConnections() && 
      Via.getManager().getProviders().get(BlockConnectionProvider.class) instanceof com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.PacketBlockConnectionProvider)
      userConnection.put((StoredObject)new BlockConnectionStorage(userConnection)); 
  }
  
  protected void register(ViaProviders providers) {
    providers.register(BlockEntityProvider.class, (Provider)new BlockEntityProvider());
    providers.register(PaintingProvider.class, (Provider)new PaintingProvider());
    if (Via.getConfig().get1_13TabCompleteDelay() > 0)
      Via.getPlatform().runRepeatingSync(new TabCompleteThread(), Long.valueOf(1L)); 
  }
  
  private int getNewSoundID(int oldID) {
    return MappingData.soundMappings.getNewSound(oldID);
  }
  
  public ChatColor getLastColor(String input) {
    int length = input.length();
    for (int index = length - 1; index > -1; index--) {
      char section = input.charAt(index);
      if (section == '§' && index < length - 1) {
        char c = input.charAt(index + 1);
        ChatColor color = ChatColor.getByChar(c);
        if (color != null)
          switch (color) {
            case MAGIC:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case ITALIC:
            case RESET:
              break;
            default:
              return color;
          }  
      } 
    } 
    return ChatColor.RESET;
  }
  
  protected String rewriteTeamMemberName(String name) {
    if (ChatColor.stripColor(name).length() == 0) {
      StringBuilder newName = new StringBuilder();
      for (int i = 1; i < name.length(); i += 2) {
        char colorChar = name.charAt(i);
        Character rewrite = SCOREBOARD_TEAM_NAME_REWRITE.get(ChatColor.getByChar(colorChar));
        if (rewrite == null)
          rewrite = Character.valueOf(colorChar); 
        newName.append('§').append(rewrite);
      } 
      name = newName.toString();
    } 
    return name;
  }
}
