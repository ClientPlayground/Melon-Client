package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.WorldPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_13_1To1_13 extends Protocol {
  protected void registerPackets() {
    EntityPackets.register(this);
    InventoryPackets.register(this);
    WorldPackets.register(this);
    registerIncoming(State.PLAY, 5, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.STRING, new ValueTransformer<String, String>(Type.STRING) {
                  public String transform(PacketWrapper wrapper, String inputValue) {
                    return inputValue.startsWith("/") ? inputValue.substring(1) : inputValue;
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 11, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.FLAT_ITEM);
            map(Type.BOOLEAN);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.get(Type.FLAT_ITEM, 0);
                    InventoryPackets.toServer(item);
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int hand = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    if (hand == 1)
                      wrapper.cancel(); 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 16, 16, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int start = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    wrapper.set(Type.VAR_INT, 1, Integer.valueOf(start + 1));
                    int count = ((Integer)wrapper.get(Type.VAR_INT, 3)).intValue();
                    for (int i = 0; i < count; i++) {
                      wrapper.passthrough(Type.STRING);
                      boolean hasTooltip = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                      if (hasTooltip)
                        wrapper.passthrough(Type.STRING); 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 24, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, 
                        Integer.valueOf(InventoryPackets.getNewItemId(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
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
                    if (action == 0) {
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.FLOAT);
                      wrapper.passthrough(Type.VAR_INT);
                      wrapper.passthrough(Type.VAR_INT);
                      short flags = (short)((Byte)wrapper.read(Type.BYTE)).byteValue();
                      if ((flags & 0x2) != 0)
                        flags = (short)(flags | 0x4); 
                      wrapper.write(Type.UNSIGNED_BYTE, Short.valueOf(flags));
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 81, 81, new PacketRemapper() {
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
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.STRING);
                        Item icon = (Item)wrapper.passthrough(Type.FLAT_ITEM);
                        InventoryPackets.toClient(icon);
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
    registerOutgoing(State.PLAY, 85, 85, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int blockTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < blockTagsSize; i++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] blocks = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int k = 0; k < blocks.length; k++)
                        blocks[k] = Integer.valueOf(Protocol1_13_1To1_13.getNewBlockId(blocks[k].intValue())); 
                    } 
                    int itemTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int j = 0; j < itemTagsSize; j++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] items = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int k = 0; k < items.length; k++)
                        items[k] = Integer.valueOf(InventoryPackets.getNewItemId(items[k].intValue())); 
                    } 
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
  }
  
  public static int getNewBlockStateId(int blockId) {
    if (blockId > 8573) {
      blockId += 17;
    } else if (blockId > 8463) {
      blockId += 16;
    } else if (blockId > 8458) {
      blockId = 8470 + (blockId - 8459) * 2;
    } else if (blockId > 1126) {
      blockId++;
    } 
    return blockId;
  }
  
  public static int getNewBlockId(int oldBlockId) {
    int blockId = oldBlockId;
    if (oldBlockId >= 561)
      blockId += 5; 
    return blockId;
  }
}
