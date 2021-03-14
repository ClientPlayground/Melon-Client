package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.PlayerPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.WorldPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_14To1_13_2 extends Protocol {
  static {
    MappingData.init();
  }
  
  protected void registerPackets() {
    InventoryPackets.register(this);
    EntityPackets.register(this);
    WorldPackets.register(this);
    PlayerPackets.register(this);
    registerOutgoing(State.PLAY, 22, 21);
    registerOutgoing(State.PLAY, 24, 23);
    registerOutgoing(State.PLAY, 26, 25);
    registerOutgoing(State.PLAY, 27, 26);
    registerOutgoing(State.PLAY, 28, 27);
    registerOutgoing(State.PLAY, 29, 84);
    registerOutgoing(State.PLAY, 31, 29);
    registerOutgoing(State.PLAY, 32, 30);
    registerOutgoing(State.PLAY, 33, 32);
    registerOutgoing(State.PLAY, 39, 43);
    registerOutgoing(State.PLAY, 43, 44);
    registerOutgoing(State.PLAY, 45, 48);
    registerOutgoing(State.PLAY, 46, 49);
    registerOutgoing(State.PLAY, 47, 50);
    registerOutgoing(State.PLAY, 48, 51);
    registerOutgoing(State.PLAY, 49, 52);
    registerOutgoing(State.PLAY, 50, 53);
    registerOutgoing(State.PLAY, 52, 54);
    registerOutgoing(State.PLAY, 54, 56);
    registerOutgoing(State.PLAY, 55, 57);
    registerOutgoing(State.PLAY, 57, 59);
    registerOutgoing(State.PLAY, 58, 60);
    registerOutgoing(State.PLAY, 59, 61);
    registerOutgoing(State.PLAY, 60, 62);
    registerOutgoing(State.PLAY, 61, 63);
    registerOutgoing(State.PLAY, 62, 66);
    registerOutgoing(State.PLAY, 64, 68);
    registerOutgoing(State.PLAY, 65, 69);
    registerOutgoing(State.PLAY, 67, 71);
    registerOutgoing(State.PLAY, 68, 72);
    registerOutgoing(State.PLAY, 69, 73);
    registerOutgoing(State.PLAY, 70, 74);
    registerOutgoing(State.PLAY, 71, 75);
    registerOutgoing(State.PLAY, 72, 76);
    registerOutgoing(State.PLAY, 74, 78);
    registerOutgoing(State.PLAY, 75, 79);
    registerOutgoing(State.PLAY, 76, 82);
    registerOutgoing(State.PLAY, 77, 81, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_14To1_13_2.getNewSoundId(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 78, 83);
    registerOutgoing(State.PLAY, 79, 85);
    registerOutgoing(State.PLAY, 80, 86);
    registerOutgoing(State.PLAY, 81, 87, new PacketRemapper() {
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
                        InventoryPackets.toClient((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
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
    registerOutgoing(State.PLAY, 82, 88);
    registerOutgoing(State.PLAY, 83, 89);
    registerOutgoing(State.PLAY, 85, 91, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int blockTagsSize = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    wrapper.write(Type.VAR_INT, Integer.valueOf(blockTagsSize + 5));
                    for (int i = 0; i < blockTagsSize; i++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] blockIds = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int m = 0; m < blockIds.length; m++)
                        blockIds[m] = Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(blockIds[m].intValue())); 
                    } 
                    wrapper.write(Type.STRING, "minecraft:signs");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(150)), Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(155)) });
                    wrapper.write(Type.STRING, "minecraft:wall_signs");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(155)) });
                    wrapper.write(Type.STRING, "minecraft:standing_signs");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(Protocol1_14To1_13_2.getNewBlockId(150)) });
                    wrapper.write(Type.STRING, "minecraft:fences");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(189), 
                          Integer.valueOf(248), 
                          Integer.valueOf(472), 
                          Integer.valueOf(473), 
                          Integer.valueOf(474), 
                          Integer.valueOf(475) });
                    wrapper.write(Type.STRING, "minecraft:walls");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(271), 
                          Integer.valueOf(272) });
                    int itemTagsSize = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    wrapper.write(Type.VAR_INT, Integer.valueOf(itemTagsSize + 2));
                    for (int j = 0; j < itemTagsSize; j++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] itemIds = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int m = 0; m < itemIds.length; m++)
                        itemIds[m] = Integer.valueOf(InventoryPackets.getNewItemId(itemIds[m].intValue())); 
                    } 
                    wrapper.write(Type.STRING, "minecraft:signs");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(InventoryPackets.getNewItemId(541)) });
                    wrapper.write(Type.STRING, "minecraft:arrows");
                    wrapper.write(Type.VAR_INT_ARRAY, new Integer[] { Integer.valueOf(526), Integer.valueOf(825), Integer.valueOf(826) });
                    int fluidTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int k = 0; k < fluidTagsSize; k++) {
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.VAR_INT_ARRAY);
                    } 
                    wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, -1, 2, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 2, 3);
    registerIncoming(State.PLAY, 3, 4);
    registerIncoming(State.PLAY, 4, 5);
    registerIncoming(State.PLAY, 5, 6);
    registerIncoming(State.PLAY, 6, 7);
    registerIncoming(State.PLAY, 7, 8);
    registerIncoming(State.PLAY, 9, 10);
    registerIncoming(State.PLAY, 10, 11);
    registerIncoming(State.PLAY, 12, 13);
    registerIncoming(State.PLAY, 13, 14);
    registerIncoming(State.PLAY, -1, 16, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 14, 15);
    registerIncoming(State.PLAY, 15, 20);
    registerIncoming(State.PLAY, 16, 17);
    registerIncoming(State.PLAY, 17, 18);
    registerIncoming(State.PLAY, 18, 19);
    registerIncoming(State.PLAY, 19, 21);
    registerIncoming(State.PLAY, 20, 22);
    registerIncoming(State.PLAY, 21, 23);
    registerIncoming(State.PLAY, 22, 24);
    registerIncoming(State.PLAY, 23, 25);
    registerIncoming(State.PLAY, 25, 27);
    registerIncoming(State.PLAY, 26, 28);
    registerIncoming(State.PLAY, 28, 30);
    registerIncoming(State.PLAY, 29, 31);
    registerIncoming(State.PLAY, 30, 32);
    registerIncoming(State.PLAY, 32, 34);
    registerIncoming(State.PLAY, 33, 35);
    registerIncoming(State.PLAY, 35, 37);
    registerIncoming(State.PLAY, -1, 39, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) {
                    wrapper.cancel();
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 39, 42);
    registerIncoming(State.PLAY, 40, 43);
    registerIncoming(State.PLAY, 42, 45);
  }
  
  public static int getNewSoundId(int id) {
    int newId = MappingData.soundMappings.getNewSound(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.14 sound for 1.13.2 sound " + id);
      return 0;
    } 
    return newId;
  }
  
  public static int getNewBlockStateId(int id) {
    int newId = MappingData.blockStateMappings.getNewBlock(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.14 blockstate for 1.13.2 blockstate " + id);
      return 0;
    } 
    return newId;
  }
  
  public static int getNewBlockId(int id) {
    int newId = MappingData.blockMappings.getNewBlock(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.14 block for 1.13.2 block " + id);
      return 0;
    } 
    return newId;
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
  }
}
