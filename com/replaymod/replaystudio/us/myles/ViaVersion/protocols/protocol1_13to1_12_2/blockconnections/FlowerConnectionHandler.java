package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowerConnectionHandler extends ConnectionHandler {
  private static Map<Integer, Integer> flowers = new HashMap<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final Set<String> baseFlower = new HashSet<>();
    baseFlower.add("minecraft:rose_bush");
    baseFlower.add("minecraft:sunflower");
    baseFlower.add("minecraft:peony");
    baseFlower.add("minecraft:tall_grass");
    baseFlower.add("minecraft:large_fern");
    baseFlower.add("minecraft:lilac");
    final FlowerConnectionHandler handler = new FlowerConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (baseFlower.contains(blockData.getMinecraftKey())) {
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), handler);
            if (blockData.getValue("half").equals("lower")) {
              blockData.set("half", "upper");
              FlowerConnectionHandler.flowers.put(Integer.valueOf(blockData.getSavedBlockStateId()), Integer.valueOf(blockData.getBlockStateId()));
            } 
          } 
        }
      };
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    int blockBelowId = getBlockData(user, position.getRelative(BlockFace.BOTTOM));
    if (flowers.containsKey(Integer.valueOf(blockBelowId))) {
      int blockAboveId = getBlockData(user, position.getRelative(BlockFace.TOP));
      if (Via.getConfig().isStemWhenBlockAbove()) {
        if (blockAboveId == 0)
          return ((Integer)flowers.get(Integer.valueOf(blockBelowId))).intValue(); 
      } else if (!flowers.containsKey(Integer.valueOf(blockAboveId))) {
        return ((Integer)flowers.get(Integer.valueOf(blockBelowId))).intValue();
      } 
    } 
    return blockState;
  }
}
