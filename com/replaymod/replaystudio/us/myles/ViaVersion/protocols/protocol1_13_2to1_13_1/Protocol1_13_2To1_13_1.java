package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.WorldPackets;

public class Protocol1_13_2To1_13_1 extends Protocol {
  protected void registerPackets() {
    InventoryPackets.register(this);
    WorldPackets.register(this);
    EntityPackets.register(this);
    registerIncoming(State.PLAY, 11, 11, new PacketRemapper() {
          public void registerMap() {
            map(Type.FLAT_VAR_INT_ITEM, Type.FLAT_ITEM);
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
                        Item icon = (Item)wrapper.read(Type.FLAT_ITEM);
                        wrapper.write(Type.FLAT_VAR_INT_ITEM, icon);
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
  }
  
  public void init(UserConnection userConnection) {}
}
