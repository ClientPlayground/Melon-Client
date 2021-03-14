package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.ArrayList;
import java.util.List;

public class ChorusPlantConnectionHandler extends AbstractFenceConnectionHandler {
  private int endstone;
  
  static List<ConnectionData.ConnectorInitAction> init() {
    List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
    ChorusPlantConnectionHandler handler = new ChorusPlantConnectionHandler();
    actions.add(handler.getInitAction("minecraft:chorus_plant"));
    actions.add(handler.getExtraAction());
    return actions;
  }
  
  public ChorusPlantConnectionHandler() {
    super(null);
    this.endstone = ConnectionData.getId("minecraft:end_stone");
  }
  
  public ConnectionData.ConnectorInitAction getExtraAction() {
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (blockData.getMinecraftKey().equals("minecraft:chorus_flower"))
            ChorusPlantConnectionHandler.this.getBlockStates().add(Integer.valueOf(blockData.getSavedBlockStateId())); 
        }
      };
  }
  
  protected byte getStates(WrappedBlockData blockData) {
    byte states = super.getStates(blockData);
    if (blockData.getValue("up").equals("true"))
      states = (byte)(states | 0x10); 
    if (blockData.getValue("down").equals("true"))
      states = (byte)(states | 0x20); 
    return states;
  }
  
  protected byte getStates(UserConnection user, Position position, int blockState) {
    byte states = super.getStates(user, position, blockState);
    if (connects(BlockFace.TOP, getBlockData(user, position.getRelative(BlockFace.TOP)), false))
      states = (byte)(states | 0x10); 
    if (connects(BlockFace.BOTTOM, getBlockData(user, position.getRelative(BlockFace.BOTTOM)), false))
      states = (byte)(states | 0x20); 
    return states;
  }
  
  protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
    return (getBlockStates().contains(Integer.valueOf(blockState)) || (side == BlockFace.BOTTOM && blockState == this.endstone));
  }
}
