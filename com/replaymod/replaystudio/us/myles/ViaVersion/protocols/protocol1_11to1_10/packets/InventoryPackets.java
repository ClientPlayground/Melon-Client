package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.EntityIdRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;

public class InventoryPackets {
  public static void register(Protocol1_11To1_10 protocol) {
    protocol.registerOutgoing(State.PLAY, 22, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    EntityIdRewriter.toClientItem(stack);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 20, 20, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.ITEM_ARRAY);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item[] stacks = (Item[])wrapper.get(Type.ITEM_ARRAY, 0);
                    for (Item stack : stacks)
                      EntityIdRewriter.toClientItem(stack); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 60, 60, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    EntityIdRewriter.toClientItem(stack);
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 24, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (((String)wrapper.get(Type.STRING, 0)).equalsIgnoreCase("MC|TrList")) {
                      wrapper.passthrough(Type.INT);
                      int size = ((Short)wrapper.passthrough(Type.UNSIGNED_BYTE)).shortValue();
                      for (int i = 0; i < size; i++) {
                        EntityIdRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM));
                        EntityIdRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM));
                        boolean secondItem = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (secondItem)
                          EntityIdRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM)); 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 7, 7, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.SHORT);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.VAR_INT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    EntityIdRewriter.toServerItem(item);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 24, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    EntityIdRewriter.toServerItem(item);
                  }
                });
          }
        });
  }
}
