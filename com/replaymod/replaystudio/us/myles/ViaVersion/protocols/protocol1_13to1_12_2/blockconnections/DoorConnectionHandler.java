package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoorConnectionHandler extends ConnectionHandler {
  private static Map<Integer, DoorData> doorDataMap = new HashMap<>();
  
  private static Map<Short, Integer> connectedStates = new HashMap<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final List<String> baseDoors = new LinkedList<>();
    baseDoors.add("minecraft:oak_door");
    baseDoors.add("minecraft:birch_door");
    baseDoors.add("minecraft:jungle_door");
    baseDoors.add("minecraft:dark_oak_door");
    baseDoors.add("minecraft:acacia_door");
    baseDoors.add("minecraft:spruce_door");
    baseDoors.add("minecraft:iron_door");
    final DoorConnectionHandler connectionHandler = new DoorConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          int type = baseDoors.indexOf(blockData.getMinecraftKey());
          if (type == -1)
            return; 
          int id = blockData.getSavedBlockStateId();
          DoorConnectionHandler.DoorData doorData = new DoorConnectionHandler.DoorData(blockData.getValue("half").equals("lower"), blockData.getValue("hinge").equals("right"), blockData.getValue("powered").equals("true"), blockData.getValue("open").equals("true"), BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)), type);
          DoorConnectionHandler.doorDataMap.put(Integer.valueOf(id), doorData);
          DoorConnectionHandler.connectedStates.put(Short.valueOf(DoorConnectionHandler.getStates(doorData)), Integer.valueOf(id));
          ConnectionData.connectionHandlerMap.put(Integer.valueOf(id), connectionHandler);
        }
      };
  }
  
  private static short getStates(DoorData doorData) {
    short s = 0;
    if (doorData.isLower())
      s = (short)(s | 0x1); 
    if (doorData.isOpen())
      s = (short)(s | 0x2); 
    if (doorData.isPowered())
      s = (short)(s | 0x4); 
    if (doorData.isRightHinge())
      s = (short)(s | 0x8); 
    s = (short)(s | doorData.getFacing().ordinal() << 4);
    s = (short)(s | (doorData.getType() & 0x7) << 6);
    return s;
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    DoorData doorData = doorDataMap.get(Integer.valueOf(blockState));
    if (doorData == null)
      return blockState; 
    short s = 0;
    s = (short)(s | (doorData.getType() & 0x7) << 6);
    if (doorData.isLower()) {
      DoorData upperHalf = doorDataMap.get(Integer.valueOf(getBlockData(user, position.getRelative(BlockFace.TOP))));
      if (upperHalf == null)
        return blockState; 
      s = (short)(s | 0x1);
      if (doorData.isOpen())
        s = (short)(s | 0x2); 
      if (upperHalf.isPowered())
        s = (short)(s | 0x4); 
      if (upperHalf.isRightHinge())
        s = (short)(s | 0x8); 
      s = (short)(s | doorData.getFacing().ordinal() << 4);
    } else {
      DoorData lowerHalf = doorDataMap.get(Integer.valueOf(getBlockData(user, position.getRelative(BlockFace.BOTTOM))));
      if (lowerHalf == null)
        return blockState; 
      if (lowerHalf.isOpen())
        s = (short)(s | 0x2); 
      if (doorData.isPowered())
        s = (short)(s | 0x4); 
      if (doorData.isRightHinge())
        s = (short)(s | 0x8); 
      s = (short)(s | lowerHalf.getFacing().ordinal() << 4);
    } 
    Integer newBlockState = connectedStates.get(Short.valueOf(s));
    return (newBlockState == null) ? blockState : newBlockState.intValue();
  }
  
  private static class DoorData {
    private final boolean lower;
    
    private final boolean rightHinge;
    
    private final boolean powered;
    
    private final boolean open;
    
    private final BlockFace facing;
    
    private int type;
    
    public DoorData(boolean lower, boolean rightHinge, boolean powered, boolean open, BlockFace facing, int type) {
      this.lower = lower;
      this.rightHinge = rightHinge;
      this.powered = powered;
      this.open = open;
      this.facing = facing;
      this.type = type;
    }
    
    public String toString() {
      return "DoorConnectionHandler.DoorData(lower=" + isLower() + ", rightHinge=" + isRightHinge() + ", powered=" + isPowered() + ", open=" + isOpen() + ", facing=" + getFacing() + ", type=" + getType() + ")";
    }
    
    public boolean isLower() {
      return this.lower;
    }
    
    public boolean isRightHinge() {
      return this.rightHinge;
    }
    
    public boolean isPowered() {
      return this.powered;
    }
    
    public boolean isOpen() {
      return this.open;
    }
    
    public BlockFace getFacing() {
      return this.facing;
    }
    
    public int getType() {
      return this.type;
    }
  }
}
