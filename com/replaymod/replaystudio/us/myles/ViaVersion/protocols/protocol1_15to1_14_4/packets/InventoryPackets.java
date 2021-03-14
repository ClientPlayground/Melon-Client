package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;

public class InventoryPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 20, 21, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 39, 40, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                    for (int i = 0; i < size; i++) {
                      Item input = (Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                      InventoryPackets.toClient(input);
                      Item output = (Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                      InventoryPackets.toClient(output);
                      if (((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue()) {
                        Item second = (Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        InventoryPackets.toClient(second);
                      } 
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.FLOAT);
                      wrapper.passthrough(Type.INT);
                    } 
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BOOLEAN);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 22, 23, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 70, 71, new PacketRemapper() {
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
    protocol.registerOutgoing(State.PLAY, 90, 91, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int size = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < size; i++) {
                      int ingredientsNo;
                      Item[] items;
                      int j;
                      String type = ((String)wrapper.passthrough(Type.STRING)).replace("minecraft:", "");
                      String id = (String)wrapper.passthrough(Type.STRING);
                      switch (type) {
                        case "crafting_shapeless":
                          wrapper.passthrough(Type.STRING);
                          ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                          for (j = 0; j < ingredientsNo; j++) {
                            Item[] arrayOfItem = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                            for (Item item : arrayOfItem)
                              InventoryPackets.toClient(item); 
                          } 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                          break;
                        case "crafting_shaped":
                          ingredientsNo = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue() * ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                          wrapper.passthrough(Type.STRING);
                          for (j = 0; j < ingredientsNo; j++) {
                            Item[] arrayOfItem = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                            for (Item item : arrayOfItem)
                              InventoryPackets.toClient(item); 
                          } 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                          break;
                        case "blasting":
                        case "smoking":
                        case "campfire_cooking":
                        case "smelting":
                          wrapper.passthrough(Type.STRING);
                          items = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                          for (Item item : items)
                            InventoryPackets.toClient(item); 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                          wrapper.passthrough(Type.FLOAT);
                          wrapper.passthrough(Type.VAR_INT);
                          break;
                        case "stonecutting":
                          wrapper.passthrough(Type.STRING);
                          items = (Item[])wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                          for (Item item : items)
                            InventoryPackets.toClient(item); 
                          InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                          break;
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 9, 9, new PacketRemapper() {
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
    protocol.registerIncoming(State.PLAY, 38, 38, new PacketRemapper() {
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
  }
  
  public static void toServer(Item item) {
    if (item == null)
      return; 
    item.setIdentifier(getOldItemId(item.getIdentifier()));
  }
  
  public static int getNewItemId(int id) {
    Integer newId = (Integer)MappingData.oldToNewItems.get(Integer.valueOf(id));
    if (newId == null) {
      Via.getPlatform().getLogger().warning("Missing 1.15 item for 1.14 item " + id);
      return 1;
    } 
    return newId.intValue();
  }
  
  public static int getOldItemId(int id) {
    Integer oldId = (Integer)MappingData.oldToNewItems.inverse().get(Integer.valueOf(id));
    return (oldId != null) ? oldId.intValue() : 1;
  }
}
