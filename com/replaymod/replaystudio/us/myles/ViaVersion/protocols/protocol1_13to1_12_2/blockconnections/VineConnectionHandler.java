package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashSet;
import java.util.Set;

class VineConnectionHandler extends ConnectionHandler {
  private static final Set<Integer> vines = new HashSet<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final VineConnectionHandler connectionHandler = new VineConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (!blockData.getMinecraftKey().equals("minecraft:vine"))
            return; 
          VineConnectionHandler.vines.add(Integer.valueOf(blockData.getSavedBlockStateId()));
          ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), connectionHandler);
        }
      };
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    if (isAttachedToBlock(user, position))
      return blockState; 
    Position upperPos = position.getRelative(BlockFace.TOP);
    int upperBlock = getBlockData(user, upperPos);
    if (vines.contains(Integer.valueOf(upperBlock)) && isAttachedToBlock(user, upperPos))
      return blockState; 
    return 0;
  }
  
  private boolean isAttachedToBlock(UserConnection user, Position position) {
    return (isAttachedToBlock(user, position, BlockFace.EAST) || 
      isAttachedToBlock(user, position, BlockFace.WEST) || 
      isAttachedToBlock(user, position, BlockFace.NORTH) || 
      isAttachedToBlock(user, position, BlockFace.SOUTH));
  }
  
  private boolean isAttachedToBlock(UserConnection user, Position position, BlockFace blockFace) {
    return ConnectionData.occludingStates.contains(Integer.valueOf(getBlockData(user, position.getRelative(blockFace))));
  }
}
