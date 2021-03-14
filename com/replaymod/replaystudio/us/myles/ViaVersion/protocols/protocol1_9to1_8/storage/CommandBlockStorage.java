package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandBlockStorage extends StoredObject {
  private Map<Pair<Integer, Integer>, Map<Position, CompoundTag>> storedCommandBlocks = new ConcurrentHashMap<>();
  
  private boolean permissions = false;
  
  public void setPermissions(boolean permissions) {
    this.permissions = permissions;
  }
  
  public boolean isPermissions() {
    return this.permissions;
  }
  
  public CommandBlockStorage(UserConnection user) {
    super(user);
  }
  
  public void unloadChunk(int x, int z) {
    Pair<Integer, Integer> chunkPos = new Pair(Integer.valueOf(x), Integer.valueOf(z));
    this.storedCommandBlocks.remove(chunkPos);
  }
  
  public void addOrUpdateBlock(Position position, CompoundTag tag) {
    Pair<Integer, Integer> chunkPos = getChunkCoords(position);
    if (!this.storedCommandBlocks.containsKey(chunkPos))
      this.storedCommandBlocks.put(chunkPos, new ConcurrentHashMap<>()); 
    Map<Position, CompoundTag> blocks = this.storedCommandBlocks.get(chunkPos);
    if (blocks.containsKey(position) && (
      (CompoundTag)blocks.get(position)).equals(tag))
      return; 
    blocks.put(position, tag);
  }
  
  private Pair<Integer, Integer> getChunkCoords(Position position) {
    int chunkX = (int)Math.floor((position.getX().longValue() / 16L));
    int chunkZ = (int)Math.floor((position.getZ().longValue() / 16L));
    return new Pair(Integer.valueOf(chunkX), Integer.valueOf(chunkZ));
  }
  
  public Optional<CompoundTag> getCommandBlock(Position position) {
    Pair<Integer, Integer> chunkCoords = getChunkCoords(position);
    Map<Position, CompoundTag> blocks = this.storedCommandBlocks.get(chunkCoords);
    if (blocks == null)
      return Optional.absent(); 
    CompoundTag tag = blocks.get(position);
    if (tag == null)
      return Optional.absent(); 
    tag = tag.clone();
    tag.put((Tag)new ByteTag("powered", (byte)0));
    tag.put((Tag)new ByteTag("auto", (byte)0));
    tag.put((Tag)new ByteTag("conditionMet", (byte)0));
    return Optional.of(tag);
  }
  
  public void unloadChunks() {
    this.storedCommandBlocks.clear();
  }
}
