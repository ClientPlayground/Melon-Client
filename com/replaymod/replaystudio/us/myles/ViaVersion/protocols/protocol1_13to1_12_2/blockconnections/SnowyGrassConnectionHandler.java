package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SnowyGrassConnectionHandler extends ConnectionHandler {
  private static Map<Pair<Integer, Boolean>, Integer> grassBlocks = new HashMap<>();
  
  private static Set<Integer> snows = new HashSet<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final Set<String> snowyGrassBlocks = new HashSet<>();
    snowyGrassBlocks.add("minecraft:grass_block");
    snowyGrassBlocks.add("minecraft:podzol");
    snowyGrassBlocks.add("minecraft:mycelium");
    final SnowyGrassConnectionHandler handler = new SnowyGrassConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (snowyGrassBlocks.contains(blockData.getMinecraftKey())) {
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), handler);
            blockData.set("snowy", "true");
            SnowyGrassConnectionHandler.grassBlocks.put(new Pair(Integer.valueOf(blockData.getSavedBlockStateId()), Boolean.valueOf(true)), Integer.valueOf(blockData.getBlockStateId()));
            blockData.set("snowy", "false");
            SnowyGrassConnectionHandler.grassBlocks.put(new Pair(Integer.valueOf(blockData.getSavedBlockStateId()), Boolean.valueOf(false)), Integer.valueOf(blockData.getBlockStateId()));
          } 
          if (blockData.getMinecraftKey().equals("minecraft:snow") || blockData.getMinecraftKey().equals("minecraft:snow_block")) {
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), handler);
            SnowyGrassConnectionHandler.snows.add(Integer.valueOf(blockData.getSavedBlockStateId()));
          } 
        }
      };
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    int blockUpId = getBlockData(user, position.getRelative(BlockFace.TOP));
    Integer newId = grassBlocks.get(new Pair(Integer.valueOf(blockState), Boolean.valueOf(snows.contains(Integer.valueOf(blockUpId)))));
    if (newId != null)
      return newId.intValue(); 
    return blockState;
  }
}
