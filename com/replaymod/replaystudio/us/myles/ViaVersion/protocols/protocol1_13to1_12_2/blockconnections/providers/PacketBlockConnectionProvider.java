package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;

public class PacketBlockConnectionProvider extends BlockConnectionProvider {
  public void storeBlock(UserConnection connection, Position position, int blockState) {
    ((BlockConnectionStorage)connection.get(BlockConnectionStorage.class)).store(position, blockState);
  }
  
  public void removeBlock(UserConnection connection, Position position) {
    ((BlockConnectionStorage)connection.get(BlockConnectionStorage.class)).remove(position);
  }
  
  public int getBlockdata(UserConnection connection, Position position) {
    return ((BlockConnectionStorage)connection.get(BlockConnectionStorage.class)).get(position);
  }
  
  public void clearStorage(UserConnection connection) {
    ((BlockConnectionStorage)connection.get(BlockConnectionStorage.class)).clear();
  }
  
  public void unloadChunk(UserConnection connection, int x, int z) {
    ((BlockConnectionStorage)connection.get(BlockConnectionStorage.class)).unloadChunk(x, z);
  }
  
  public boolean storesBlocks() {
    return true;
  }
}
