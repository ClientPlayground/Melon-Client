package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks.FakeTileEntity;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import java.util.List;

public class Protocol1_9_3To1_9_1_2 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 71, 70);
    registerOutgoing(State.PLAY, 72, 71);
    registerOutgoing(State.PLAY, 73, 72);
    registerOutgoing(State.PLAY, 74, 73);
    registerOutgoing(State.PLAY, 75, 74);
    registerOutgoing(State.PLAY, 76, 75);
    registerOutgoing(State.PLAY, 70, -1, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    Position position = (Position)wrapper.read(Type.POSITION);
                    String[] lines = new String[4];
                    for (int i = 0; i < 4; i++)
                      lines[i] = (String)wrapper.read(Type.STRING); 
                    wrapper.clearInputBuffer();
                    wrapper.setId(9);
                    wrapper.write(Type.POSITION, position);
                    wrapper.write(Type.UNSIGNED_BYTE, Short.valueOf((short)9));
                    CompoundTag tag = new CompoundTag("");
                    tag.put((Tag)new StringTag("id", "Sign"));
                    tag.put((Tag)new IntTag("x", position.getX().intValue()));
                    tag.put((Tag)new IntTag("y", position.getY().intValue()));
                    tag.put((Tag)new IntTag("z", position.getZ().intValue()));
                    for (int j = 0; j < lines.length; j++)
                      tag.put((Tag)new StringTag("Text" + (j + 1), lines[j])); 
                    wrapper.write(Type.NBT, tag);
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 32, 32, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientWorld clientWorld = (ClientWorld)wrapper.user().get(ClientWorld.class);
                    Chunk1_9_1_2Type type = new Chunk1_9_1_2Type(clientWorld);
                    Chunk chunk = (Chunk)wrapper.passthrough((Type)type);
                    List<CompoundTag> tags = chunk.getBlockEntities();
                    for (int i = 0; i < (chunk.getSections()).length; i++) {
                      ChunkSection section = chunk.getSections()[i];
                      if (section != null)
                        for (int y = 0; y < 16; y++) {
                          for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                              int block = section.getBlockId(x, y, z);
                              if (FakeTileEntity.hasBlock(block))
                                tags.add(FakeTileEntity.getFromBlock(x + (chunk.getX() << 4), y + (i << 4), z + (chunk.getZ() << 4), block)); 
                            } 
                          } 
                        }  
                    } 
                    wrapper.write(Type.NBT_ARRAY, chunk.getBlockEntities().toArray((Object[])new CompoundTag[0]));
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
  
  public void init(UserConnection user) {
    if (!user.has(ClientWorld.class))
      user.put((StoredObject)new ClientWorld(user)); 
  }
}
