package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

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
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.PlayerPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.WorldPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_15To1_14_4 extends Protocol {
  protected void registerPackets() {
    MappingData.init();
    EntityPackets.register(this);
    PlayerPackets.register(this);
    WorldPackets.register(this);
    InventoryPackets.register(this);
    registerOutgoing(State.PLAY, 80, 81, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(MappingData.soundMappings.getNewSound(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 81, 82, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(MappingData.soundMappings.getNewSound(((Integer)wrapper.get(Type.VAR_INT, 0)).intValue())));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 12, 12, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    InventoryPackets.toServer((Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 87, 88, new PacketRemapper() {
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
    registerOutgoing(State.PLAY, 91, 92, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int blockTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int i = 0; i < blockTagsSize; i++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] blockIds = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int n = 0; n < blockIds.length; n++)
                        blockIds[n] = Integer.valueOf(Protocol1_15To1_14_4.getNewBlockId(blockIds[n].intValue())); 
                    } 
                    int itemTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int j = 0; j < itemTagsSize; j++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] itemIds = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int n = 0; n < itemIds.length; n++)
                        itemIds[n] = Integer.valueOf(InventoryPackets.getNewItemId(itemIds[n].intValue())); 
                    } 
                    int fluidTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int k = 0; k < fluidTagsSize; k++) {
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.VAR_INT_ARRAY);
                    } 
                    int entityTagsSize = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                    for (int m = 0; m < entityTagsSize; m++) {
                      wrapper.passthrough(Type.STRING);
                      Integer[] entitIds = (Integer[])wrapper.passthrough(Type.VAR_INT_ARRAY);
                      for (int n = 0; n < entitIds.length; n++)
                        entitIds[n] = Integer.valueOf(EntityPackets.getNewEntityId(entitIds[n].intValue())); 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 8, 9);
    registerOutgoing(State.PLAY, 9, 10);
    registerOutgoing(State.PLAY, 12, 13);
    registerOutgoing(State.PLAY, 13, 14);
    registerOutgoing(State.PLAY, 14, 15);
    registerOutgoing(State.PLAY, 16, 17);
    registerOutgoing(State.PLAY, 17, 18);
    registerOutgoing(State.PLAY, 18, 19);
    registerOutgoing(State.PLAY, 19, 20);
    registerOutgoing(State.PLAY, 21, 22);
    registerOutgoing(State.PLAY, 23, 24);
    registerOutgoing(State.PLAY, 24, 25);
    registerOutgoing(State.PLAY, 25, 26);
    registerOutgoing(State.PLAY, 26, 27);
    registerOutgoing(State.PLAY, 27, 28);
    registerOutgoing(State.PLAY, 28, 29);
    registerOutgoing(State.PLAY, 29, 30);
    registerOutgoing(State.PLAY, 30, 31);
    registerOutgoing(State.PLAY, 31, 32);
    registerOutgoing(State.PLAY, 32, 33);
    registerOutgoing(State.PLAY, 36, 37);
    registerOutgoing(State.PLAY, 38, 39);
    registerOutgoing(State.PLAY, 40, 41);
    registerOutgoing(State.PLAY, 41, 42);
    registerOutgoing(State.PLAY, 42, 43);
    registerOutgoing(State.PLAY, 43, 44);
    registerOutgoing(State.PLAY, 44, 45);
    registerOutgoing(State.PLAY, 45, 46);
    registerOutgoing(State.PLAY, 46, 47);
    registerOutgoing(State.PLAY, 47, 48);
    registerOutgoing(State.PLAY, 48, 49);
    registerOutgoing(State.PLAY, 49, 50);
    registerOutgoing(State.PLAY, 50, 51);
    registerOutgoing(State.PLAY, 51, 52);
    registerOutgoing(State.PLAY, 52, 53);
    registerOutgoing(State.PLAY, 53, 54);
    registerOutgoing(State.PLAY, 54, 55);
    registerOutgoing(State.PLAY, 56, 57);
    registerOutgoing(State.PLAY, 57, 58);
    registerOutgoing(State.PLAY, 59, 60);
    registerOutgoing(State.PLAY, 60, 61);
    registerOutgoing(State.PLAY, 61, 62);
    registerOutgoing(State.PLAY, 62, 63);
    registerOutgoing(State.PLAY, 63, 64);
    registerOutgoing(State.PLAY, 64, 65);
    registerOutgoing(State.PLAY, 65, 66);
    registerOutgoing(State.PLAY, 66, 67);
    registerOutgoing(State.PLAY, 68, 69);
    registerOutgoing(State.PLAY, 69, 70);
    registerOutgoing(State.PLAY, 71, 72);
    registerOutgoing(State.PLAY, 72, 73);
    registerOutgoing(State.PLAY, 73, 74);
    registerOutgoing(State.PLAY, 74, 75);
    registerOutgoing(State.PLAY, 75, 76);
    registerOutgoing(State.PLAY, 76, 77);
    registerOutgoing(State.PLAY, 77, 78);
    registerOutgoing(State.PLAY, 78, 79);
    registerOutgoing(State.PLAY, 79, 80);
    registerOutgoing(State.PLAY, 82, 83);
    registerOutgoing(State.PLAY, 83, 84);
    registerOutgoing(State.PLAY, 84, 85);
    registerOutgoing(State.PLAY, 85, 86);
    registerOutgoing(State.PLAY, 86, 87);
    registerOutgoing(State.PLAY, 88, 89);
    registerOutgoing(State.PLAY, 89, 90);
  }
  
  public static int getNewSoundId(int id) {
    int newId = MappingData.soundMappings.getNewSound(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.15 sound for 1.14.4 sound " + id);
      return 0;
    } 
    return newId;
  }
  
  public static int getNewBlockStateId(int id) {
    int newId = MappingData.blockStateMappings.getNewBlock(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.15 blockstate for 1.14.4 blockstate " + id);
      return 0;
    } 
    return newId;
  }
  
  public static int getNewBlockId(int id) {
    int newId = MappingData.blockMappings.getNewBlock(id);
    if (newId == -1) {
      Via.getPlatform().getLogger().warning("Missing 1.15 block for 1.14.4 block " + id);
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
