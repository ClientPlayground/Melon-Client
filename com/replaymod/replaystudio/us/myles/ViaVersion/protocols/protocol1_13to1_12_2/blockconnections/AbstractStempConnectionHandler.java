package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AbstractStempConnectionHandler extends ConnectionHandler {
  private static final BlockFace[] BLOCK_FACES = new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
  
  private int baseStateId;
  
  private Set<Integer> blockId = new HashSet<>();
  
  private Map<BlockFace, Integer> stemps = new HashMap<>();
  
  public AbstractStempConnectionHandler(String baseStateId) {
    this.baseStateId = ConnectionData.getId(baseStateId);
  }
  
  public ConnectionData.ConnectorInitAction getInitAction(final String blockId, final String toKey) {
    final AbstractStempConnectionHandler handler = this;
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (blockData.getSavedBlockStateId() == AbstractStempConnectionHandler.this.baseStateId || blockId.equals(blockData.getMinecraftKey())) {
            if (blockData.getSavedBlockStateId() != AbstractStempConnectionHandler.this.baseStateId)
              handler.blockId.add(Integer.valueOf(blockData.getSavedBlockStateId())); 
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), handler);
          } 
          if (blockData.getMinecraftKey().equals(toKey)) {
            String facing = blockData.getValue("facing").toUpperCase(Locale.ROOT);
            AbstractStempConnectionHandler.this.stemps.put(BlockFace.valueOf(facing), Integer.valueOf(blockData.getSavedBlockStateId()));
          } 
        }
      };
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    if (blockState != this.baseStateId)
      return blockState; 
    for (BlockFace blockFace : BLOCK_FACES) {
      if (this.blockId.contains(Integer.valueOf(getBlockData(user, position.getRelative(blockFace)))))
        return ((Integer)this.stemps.get(blockFace)).intValue(); 
    } 
    return this.baseStateId;
  }
}
