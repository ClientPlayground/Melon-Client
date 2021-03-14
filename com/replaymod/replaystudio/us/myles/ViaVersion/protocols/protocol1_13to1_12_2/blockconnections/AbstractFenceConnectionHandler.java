package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFenceConnectionHandler extends ConnectionHandler {
  private final String blockConnections;
  
  private Set<Integer> blockStates = new HashSet<>();
  
  public Set<Integer> getBlockStates() {
    return this.blockStates;
  }
  
  private Map<Byte, Integer> connectedBlockStates = new HashMap<>();
  
  private static final StairConnectionHandler STAIR_CONNECTION_HANDLER = new StairConnectionHandler();
  
  public AbstractFenceConnectionHandler(String blockConnections) {
    this.blockConnections = blockConnections;
  }
  
  public ConnectionData.ConnectorInitAction getInitAction(final String key) {
    final AbstractFenceConnectionHandler handler = this;
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (key.equals(blockData.getMinecraftKey())) {
            if (blockData.hasData("waterlogged") && blockData.getValue("waterlogged").equals("true"))
              return; 
            AbstractFenceConnectionHandler.this.blockStates.add(Integer.valueOf(blockData.getSavedBlockStateId()));
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), handler);
            AbstractFenceConnectionHandler.this.connectedBlockStates.put(Byte.valueOf(AbstractFenceConnectionHandler.this.getStates(blockData)), Integer.valueOf(blockData.getSavedBlockStateId()));
          } 
        }
      };
  }
  
  protected byte getStates(WrappedBlockData blockData) {
    byte states = 0;
    if (blockData.getValue("east").equals("true"))
      states = (byte)(states | 0x1); 
    if (blockData.getValue("north").equals("true"))
      states = (byte)(states | 0x2); 
    if (blockData.getValue("south").equals("true"))
      states = (byte)(states | 0x4); 
    if (blockData.getValue("west").equals("true"))
      states = (byte)(states | 0x8); 
    return states;
  }
  
  protected byte getStates(UserConnection user, Position position, int blockState) {
    byte states = 0;
    boolean pre1_12 = (((ProtocolInfo)user.get(ProtocolInfo.class)).getServerProtocolVersion() < ProtocolVersion.v1_12.getId());
    if (connects(BlockFace.EAST, getBlockData(user, position.getRelative(BlockFace.EAST)), pre1_12))
      states = (byte)(states | 0x1); 
    if (connects(BlockFace.NORTH, getBlockData(user, position.getRelative(BlockFace.NORTH)), pre1_12))
      states = (byte)(states | 0x2); 
    if (connects(BlockFace.SOUTH, getBlockData(user, position.getRelative(BlockFace.SOUTH)), pre1_12))
      states = (byte)(states | 0x4); 
    if (connects(BlockFace.WEST, getBlockData(user, position.getRelative(BlockFace.WEST)), pre1_12))
      states = (byte)(states | 0x8); 
    return states;
  }
  
  public int getBlockData(UserConnection user, Position position) {
    return STAIR_CONNECTION_HANDLER.connect(user, position, super.getBlockData(user, position));
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    Integer newBlockState = this.connectedBlockStates.get(Byte.valueOf(getStates(user, position, blockState)));
    return (newBlockState == null) ? blockState : newBlockState.intValue();
  }
  
  protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
    return (this.blockStates.contains(Integer.valueOf(blockState)) || (this.blockConnections != null && ConnectionData.blockConnectionData
      .containsKey(Integer.valueOf(blockState)) && ((BlockData)ConnectionData.blockConnectionData
      .get(Integer.valueOf(blockState))).connectsTo(this.blockConnections, side.opposite(), pre1_12)));
  }
}
