package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class InventoryPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 23, 23, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.FLAT_ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toClient(stack);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 21, 21, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.FLAT_ITEM_ARRAY, Type.FLAT_ITEM_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item[] stacks = (Item[])wrapper.get(Type.FLAT_ITEM_ARRAY, 0);
                    for (Item stack : stacks)
                      InventoryPackets.toClient(stack); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 25, 25, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String channel = (String)wrapper.get(Type.STRING, 0);
                    if (channel.equals("minecraft:trader_list") || channel.equals("trader_list")) {
                      wrapper.passthrough(Type.INT);
                      int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                      for (int i = 0; i < size; i++) {
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                        boolean secondItem = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (secondItem)
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM)); 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 66, 66, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.FLAT_ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toClient(stack);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 84, 84, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int recipesNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < recipesNo; i++) {
                      wrapper.passthrough(Type.STRING);
                      String type = (String)wrapper.passthrough(Type.STRING);
                      if (type.equals("crafting_shapeless")) {
                        wrapper.passthrough(Type.STRING);
                        int ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                        for (int i1 = 0; i1 < ingredientsNo; i1++) {
                          Item[] items = (Item[])wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                          for (int i2 = 0; i2 < items.length; i2++)
                            InventoryPackets.toClient(items[i2]); 
                        } 
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                      } else if (type.equals("crafting_shaped")) {
                        int ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue() * ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                        wrapper.passthrough(Type.STRING);
                        for (int i1 = 0; i1 < ingredientsNo; i1++) {
                          Item[] items = (Item[])wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                          for (int i2 = 0; i2 < items.length; i2++)
                            InventoryPackets.toClient(items[i2]); 
                        } 
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                      } else if (type.equals("smelting")) {
                        wrapper.passthrough(Type.STRING);
                        Item[] items = (Item[])wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                        for (int i2 = 0; i2 < items.length; i2++)
                          InventoryPackets.toClient(items[i2]); 
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_ITEM));
                        wrapper.passthrough(Type.FLOAT);
                        wrapper.passthrough(Type.VAR_INT);
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 8, 8, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.VAR_INT);
            map(Type.FLAT_ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toServer(item);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 36, 36, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.FLAT_ITEM, Type.FLAT_ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toServer(item);
                  }
                });
          }
        });
  }
  
  public static void toClient(Item item) {
    if (item == null)
      return; 
    item.setIdentifier(getNewItemId(item.getIdentifier()));
  }
  
  public static int getNewItemId(int itemId) {
    if (itemId >= 443)
      return itemId + 5; 
    return itemId;
  }
  
  public static void toServer(Item item) {
    if (item == null)
      return; 
    item.setIdentifier(getOldItemId(item.getIdentifier()));
  }
  
  public static int getOldItemId(int newId) {
    if (newId >= 448)
      return newId - 5; 
    return newId;
  }
}
