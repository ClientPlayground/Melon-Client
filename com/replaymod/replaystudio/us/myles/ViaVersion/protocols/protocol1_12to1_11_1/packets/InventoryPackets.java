package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.BedRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;

public class InventoryPackets {
  public static void register(Protocol1_12To1_11_1 protocol) {
    protocol.registerOutgoing(State.PLAY, 22, 22, new PacketRemapper() {
          public void registerMap() {
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    BedRewriter.toClientItem(stack);
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
                      BedRewriter.toClientItem(stack); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 60, 62, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item stack = (Item)wrapper.get(Type.ITEM, 0);
                    BedRewriter.toClientItem(stack);
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
                        BedRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM));
                        BedRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM));
                        boolean secondItem = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (secondItem)
                          BedRewriter.toClientItem((Item)wrapper.passthrough(Type.ITEM)); 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                      } 
                    } 
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
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                      BedRewriter.toServerItem(item);
                      return;
                    } 
                    byte button = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    int mode = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (mode == 1 && button == 0 && item == null) {
                      short windowId = ((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue();
                      short slotId = ((Short)wrapper.get(Type.SHORT, 0)).shortValue();
                      short actionId = ((Short)wrapper.get(Type.SHORT, 1)).shortValue();
                      InventoryQuickMoveProvider provider = (InventoryQuickMoveProvider)Via.getManager().getProviders().get(InventoryQuickMoveProvider.class);
                      boolean succeed = provider.registerQuickMoveAction(windowId, slotId, actionId, wrapper.user());
                      if (succeed)
                        wrapper.cancel(); 
                    } else {
                      BedRewriter.toServerItem(item);
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 24, 27, new PacketRemapper() {
          public void registerMap() {
            map(Type.SHORT);
            map(Type.ITEM);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.ITEM, 0);
                    BedRewriter.toServerItem(item);
                  }
                });
          }
        });
  }
}
