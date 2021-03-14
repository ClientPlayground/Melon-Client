package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.InventoryTracker;

public class InventoryPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 49, 21, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    final short windowId = ((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue();
                    final short property = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                    short value = ((Short)wrapper.get(Type.SHORT, 1)).shortValue();
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && 
                      inventoryTracker.getInventory().equalsIgnoreCase("minecraft:enchanting_table") && 
                      property > 3 && property < 7) {
                      short level = (short)(value >> 8);
                      final short enchantID = (short)(value & 0xFF);
                      wrapper.create(wrapper.getId(), new ValueCreator() {
                            public void write(PacketWrapper wrapper) throws Exception {
                              wrapper.write(Type.UNSIGNED_BYTE, Short.valueOf(windowId));
                              wrapper.write(Type.SHORT, Short.valueOf(property));
                              wrapper.write(Type.SHORT, Short.valueOf(enchantID));
                            }
                          }).send(Protocol1_9To1_8.class);
                      wrapper.set(Type.SHORT, 0, Short.valueOf((short)(property + 3)));
                      wrapper.set(Type.SHORT, 1, Short.valueOf(level));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 45, 19, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
            map(Type.UNSIGNED_BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String inventory = (String)wrapper.get(Type.STRING, 0);
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(inventory);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String inventory = (String)wrapper.get(Type.STRING, 0);
                    if (inventory.equals("minecraft:brewing_stand"))
                      wrapper.set(Type.UNSIGNED_BYTE, 1, Short.valueOf((short)(((Short)wrapper.get(Type.UNSIGNED_BYTE, 1)).shortValue() + 1))); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 47, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    ItemRewriter.toClient(stack);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    short slotID = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                    if (inventoryTracker.getInventory() != null && 
                      inventoryTracker.getInventory().equals("minecraft:brewing_stand") && 
                      slotID >= 4)
                      wrapper.set(Type.SHORT, 0, Short.valueOf((short)(slotID + 1))); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 48, 20, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.ITEM_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item[] stacks = (Item[])wrapper.get(Type.ITEM_ARRAY, 0);
                    for (Item stack : stacks)
                      ItemRewriter.toClient(stack); 
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && 
                      inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                      Item[] oldStack = (Item[])wrapper.get(Type.ITEM_ARRAY, 0);
                      Item[] newStack = new Item[oldStack.length + 1];
                      for (int i = 0; i < newStack.length; i++) {
                        if (i > 4) {
                          newStack[i] = oldStack[i - 1];
                        } else if (i != 4) {
                          newStack[i] = oldStack[i];
                        } 
                      } 
                      wrapper.set(Type.ITEM_ARRAY, 0, newStack);
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 46, 18, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(null);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 52, 36, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.BYTE);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) {
                    wrapper.write(Type.BOOLEAN, Boolean.valueOf(true));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 9, 55);
    protocol.registerOutgoing(State.PLAY, 50, 17);
    protocol.registerIncoming(State.PLAY, 16, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    ItemRewriter.toServer(stack);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    final short slot = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                    boolean throwItem = (slot == 45);
                    if (throwItem) {
                      wrapper.create(22, new ValueCreator() {
                            public void write(PacketWrapper wrapper) throws Exception {
                              wrapper.write(Type.BYTE, Byte.valueOf((byte)0));
                              wrapper.write(Type.SHORT, Short.valueOf(slot));
                              wrapper.write(Type.ITEM, null);
                            }
                          }).send(Protocol1_9To1_8.class);
                      wrapper.set(Type.SHORT, 0, Short.valueOf((short)-999));
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 14, 7, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.VAR_INT, Type.BYTE);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    ItemRewriter.toServer(stack);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    final short windowID = ((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue();
                    final short slot = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                    boolean throwItem = (slot == 45 && windowID == 0);
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && 
                      inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                      if (slot == 4)
                        throwItem = true; 
                      if (slot > 4)
                        wrapper.set(Type.SHORT, 0, Short.valueOf((short)(slot - 1))); 
                    } 
                    if (throwItem) {
                      wrapper.create(22, new ValueCreator() {
                            public void write(PacketWrapper wrapper) throws Exception {
                              wrapper.write(Type.BYTE, Byte.valueOf((byte)windowID));
                              wrapper.write(Type.SHORT, Short.valueOf(slot));
                              wrapper.write(Type.ITEM, null);
                            }
                          }).send(Protocol1_9To1_8.class);
                      wrapper.set(Type.BYTE, 0, Byte.valueOf((byte)0));
                      wrapper.set(Type.BYTE, 1, Byte.valueOf((byte)0));
                      wrapper.set(Type.SHORT, 0, Short.valueOf((short)-999));
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 13, 8, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryTracker inventoryTracker = (InventoryTracker)wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(null);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 9, 23, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    if (entityTracker.isBlocking()) {
                      entityTracker.setBlocking(false);
                      entityTracker.setSecondHand(null);
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 15, 5);
    protocol.registerIncoming(State.PLAY, 17, 6);
  }
}
