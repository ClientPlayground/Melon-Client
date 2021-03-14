package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class PlayerPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 44, 47, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION, Type.POSITION1_14);
          }
        });
    protocol.registerIncoming(State.PLAY, 1, 1, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.POSITION1_14, Type.POSITION);
          }
        });
    protocol.registerIncoming(State.PLAY, 11, 12, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Item item = (Item)wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                    InventoryPackets.toServer(item);
                    if (Via.getConfig().isTruncate1_14Books()) {
                      if (item == null)
                        return; 
                      CompoundTag tag = item.getTag();
                      if (tag == null)
                        return; 
                      Tag pages = tag.get("pages");
                      if (!(pages instanceof ListTag))
                        return; 
                      ListTag listTag = (ListTag)pages;
                      if (listTag.size() <= 50)
                        return; 
                      listTag.setValue(listTag.getValue().subList(0, 50));
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 24, 26, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.POSITION1_14, Type.POSITION);
            map(Type.BYTE);
          }
        });
    protocol.registerIncoming(State.PLAY, 27, 29, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int type = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (type == 0) {
                      wrapper.passthrough(Type.STRING);
                    } else if (type == 1) {
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                      wrapper.read(Type.BOOLEAN);
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 34, 36, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14, Type.POSITION);
          }
        });
    protocol.registerIncoming(State.PLAY, 37, 40, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14, Type.POSITION);
          }
        });
    protocol.registerIncoming(State.PLAY, 38, 41, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION1_14, Type.POSITION);
          }
        });
    protocol.registerIncoming(State.PLAY, 41, 44, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int hand = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    Position position = (Position)wrapper.read(Type.POSITION1_14);
                    int face = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    float x = ((Float)wrapper.read(Type.FLOAT)).floatValue();
                    float y = ((Float)wrapper.read(Type.FLOAT)).floatValue();
                    float z = ((Float)wrapper.read(Type.FLOAT)).floatValue();
                    wrapper.read(Type.BOOLEAN);
                    wrapper.write(Type.POSITION, position);
                    wrapper.write(Type.VAR_INT, Integer.valueOf(face));
                    wrapper.write(Type.VAR_INT, Integer.valueOf(hand));
                    wrapper.write(Type.FLOAT, Float.valueOf(x));
                    wrapper.write(Type.FLOAT, Float.valueOf(y));
                    wrapper.write(Type.FLOAT, Float.valueOf(z));
                  }
                });
          }
        });
  }
}
