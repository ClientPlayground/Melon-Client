package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StairConnectionHandler extends ConnectionHandler {
  private static Map<Integer, StairData> stairDataMap = new HashMap<>();
  
  private static Map<Short, Integer> connectedBlocks = new HashMap<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final List<String> baseStairs = new LinkedList<>();
    baseStairs.add("minecraft:oak_stairs");
    baseStairs.add("minecraft:cobblestone_stairs");
    baseStairs.add("minecraft:brick_stairs");
    baseStairs.add("minecraft:stone_brick_stairs");
    baseStairs.add("minecraft:nether_brick_stairs");
    baseStairs.add("minecraft:sandstone_stairs");
    baseStairs.add("minecraft:spruce_stairs");
    baseStairs.add("minecraft:birch_stairs");
    baseStairs.add("minecraft:jungle_stairs");
    baseStairs.add("minecraft:quartz_stairs");
    baseStairs.add("minecraft:acacia_stairs");
    baseStairs.add("minecraft:dark_oak_stairs");
    baseStairs.add("minecraft:red_sandstone_stairs");
    baseStairs.add("minecraft:purpur_stairs");
    baseStairs.add("minecraft:prismarine_stairs");
    baseStairs.add("minecraft:prismarine_brick_stairs");
    baseStairs.add("minecraft:dark_prismarine_stairs");
    final StairConnectionHandler connectionHandler = new StairConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          byte shape;
          int type = baseStairs.indexOf(blockData.getMinecraftKey());
          if (type == -1)
            return; 
          if (blockData.getValue("waterlogged").equals("true"))
            return; 
          switch (blockData.getValue("shape")) {
            case "straight":
              shape = 0;
              break;
            case "inner_left":
              shape = 1;
              break;
            case "inner_right":
              shape = 2;
              break;
            case "outer_left":
              shape = 3;
              break;
            case "outer_right":
              shape = 4;
              break;
            default:
              return;
          } 
          StairConnectionHandler.StairData stairData = new StairConnectionHandler.StairData(blockData.getValue("half").equals("bottom"), shape, (byte)type, BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)));
          StairConnectionHandler.stairDataMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), stairData);
          StairConnectionHandler.connectedBlocks.put(Short.valueOf(StairConnectionHandler.getStates(stairData)), Integer.valueOf(blockData.getSavedBlockStateId()));
          ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), connectionHandler);
        }
      };
  }
  
  private static short getStates(StairData stairData) {
    short s = 0;
    if (stairData.isBottom())
      s = (short)(s | 0x1); 
    s = (short)(s | stairData.getShape() << 1);
    s = (short)(s | stairData.getType() << 4);
    s = (short)(s | stairData.getFacing().ordinal() << 9);
    return s;
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    StairData stairData = stairDataMap.get(Integer.valueOf(blockState));
    if (stairData == null)
      return blockState; 
    short s = 0;
    if (stairData.isBottom())
      s = (short)(s | 0x1); 
    s = (short)(s | getShape(user, position, stairData) << 1);
    s = (short)(s | stairData.getType() << 4);
    s = (short)(s | stairData.getFacing().ordinal() << 9);
    Integer newBlockState = connectedBlocks.get(Short.valueOf(s));
    return (newBlockState == null) ? blockState : newBlockState.intValue();
  }
  
  private int getShape(UserConnection user, Position position, StairData stair) {
    BlockFace facing = stair.getFacing();
    StairData relativeStair = stairDataMap.get(Integer.valueOf(getBlockData(user, position.getRelative(facing))));
    if (relativeStair != null && relativeStair.isBottom() == stair.isBottom()) {
      BlockFace facing2 = relativeStair.getFacing();
      if (facing.getAxis() != facing2.getAxis() && checkOpposite(user, stair, position, facing2.opposite()))
        return (facing2 == rotateAntiClockwise(facing)) ? 3 : 4; 
    } 
    relativeStair = stairDataMap.get(Integer.valueOf(getBlockData(user, position.getRelative(facing.opposite()))));
    if (relativeStair != null && relativeStair.isBottom() == stair.isBottom()) {
      BlockFace facing2 = relativeStair.getFacing();
      if (facing.getAxis() != facing2.getAxis() && checkOpposite(user, stair, position, facing2))
        return (facing2 == rotateAntiClockwise(facing)) ? 1 : 2; 
    } 
    return 0;
  }
  
  private boolean checkOpposite(UserConnection user, StairData stair, Position position, BlockFace face) {
    StairData relativeStair = stairDataMap.get(Integer.valueOf(getBlockData(user, position.getRelative(face))));
    return (relativeStair == null || relativeStair.getFacing() != stair.getFacing() || relativeStair.isBottom() != stair.isBottom());
  }
  
  private BlockFace rotateAntiClockwise(BlockFace face) {
    switch (face) {
      case NORTH:
        return BlockFace.WEST;
      case SOUTH:
        return BlockFace.EAST;
      case EAST:
        return BlockFace.NORTH;
      case WEST:
        return BlockFace.SOUTH;
    } 
    return face;
  }
  
  private static class StairData {
    private final boolean bottom;
    
    private final byte shape;
    
    private final byte type;
    
    private final BlockFace facing;
    
    public StairData(boolean bottom, byte shape, byte type, BlockFace facing) {
      this.bottom = bottom;
      this.shape = shape;
      this.type = type;
      this.facing = facing;
    }
    
    public String toString() {
      return "StairConnectionHandler.StairData(bottom=" + isBottom() + ", shape=" + getShape() + ", type=" + getType() + ", facing=" + getFacing() + ")";
    }
    
    public boolean isBottom() {
      return this.bottom;
    }
    
    public byte getShape() {
      return this.shape;
    }
    
    public byte getType() {
      return this.type;
    }
    
    public BlockFace getFacing() {
      return this.facing;
    }
  }
}
