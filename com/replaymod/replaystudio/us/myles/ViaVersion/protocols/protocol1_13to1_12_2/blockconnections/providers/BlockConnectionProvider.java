package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

public class BlockConnectionProvider implements Provider {
  public int getBlockdata(UserConnection connection, Position position) {
    int oldId = getWorldBlockData(connection, position);
    return MappingData.blockMappings.getNewBlock(oldId);
  }
  
  public int getWorldBlockData(UserConnection connection, Position position) {
    return -1;
  }
  
  public void storeBlock(UserConnection connection, Position position, int blockState) {}
  
  public void removeBlock(UserConnection connection, Position position) {}
  
  public void storeBlock(UserConnection connection, long x, long y, long z, int blockState) {
    storeBlock(connection, new Position(Long.valueOf(x), Long.valueOf(y), Long.valueOf(z)), blockState);
  }
  
  public void clearStorage(UserConnection connection) {}
  
  public void unloadChunk(UserConnection connection, int x, int z) {}
  
  public boolean storesBlocks() {
    return false;
  }
}
