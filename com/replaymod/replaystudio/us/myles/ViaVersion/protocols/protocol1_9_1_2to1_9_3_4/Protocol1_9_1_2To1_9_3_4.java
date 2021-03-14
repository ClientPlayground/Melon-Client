package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.chunks.BlockEntity;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;

public class Protocol1_9_1_2To1_9_3_4 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 70, 71);
    registerOutgoing(State.PLAY, 71, 72);
    registerOutgoing(State.PLAY, 72, 73);
    registerOutgoing(State.PLAY, 73, 74);
    registerOutgoing(State.PLAY, 74, 75);
    registerOutgoing(State.PLAY, 75, 76);
    registerOutgoing(State.PLAY, 9, 9, new PacketRemapper() {
          public void registerMap() {
            map(Type.POSITION);
            map(Type.UNSIGNED_BYTE);
            map(Type.NBT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue() == 9) {
                      Position position = (Position)wrapper.get(Type.POSITION, 0);
                      CompoundTag tag = (CompoundTag)wrapper.get(Type.NBT, 0);
                      wrapper.clearPacket();
                      wrapper.setId(70);
                      wrapper.write(Type.POSITION, position);
                      for (int i = 1; i < 5; i++)
                        wrapper.write(Type.STRING, tag.get("Text" + i).getValue()); 
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 32, 32, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk1_9_3_4Type newType = new Chunk1_9_3_4Type(clientWorld);
                    Chunk1_9_1_2Type oldType = new Chunk1_9_1_2Type(clientWorld);
                    Chunk chunk = (Chunk)wrapper.read((Type)newType);
                    wrapper.write((Type)oldType, chunk);
                    BlockEntity.handle(chunk.getBlockEntities(), wrapper.user());
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 35, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientChunks = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 1)).intValue();
                    clientChunks.setEnvironment(dimensionId);
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 51, 51, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    int dimensionId = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    clientWorld.setEnvironment(dimensionId);
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {
    if (!userConnection.has(ClientWorld.class))
      userConnection.put((StoredObject)new ClientWorld(userConnection)); 
  }
}
