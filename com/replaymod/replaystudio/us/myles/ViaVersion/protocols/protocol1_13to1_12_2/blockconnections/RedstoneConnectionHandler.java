package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedstoneConnectionHandler extends ConnectionHandler {
  private static Set<Integer> redstone = new HashSet<>();
  
  private static Map<Short, Integer> connectedBlockStates = new HashMap<>();
  
  private static Map<Integer, Integer> powerMappings = new HashMap<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final RedstoneConnectionHandler connectionHandler = new RedstoneConnectionHandler();
    String redstoneKey = "minecraft:redstone_wire";
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (!"minecraft:redstone_wire".equals(blockData.getMinecraftKey()))
            return; 
          RedstoneConnectionHandler.redstone.add(Integer.valueOf(blockData.getSavedBlockStateId()));
          ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), connectionHandler);
          RedstoneConnectionHandler.connectedBlockStates.put(Short.valueOf(RedstoneConnectionHandler.getStates(blockData)), Integer.valueOf(blockData.getSavedBlockStateId()));
          RedstoneConnectionHandler.powerMappings.put(Integer.valueOf(blockData.getSavedBlockStateId()), Integer.valueOf(blockData.getValue("power")));
        }
      };
  }
  
  private static short getStates(WrappedBlockData data) {
    short b = 0;
    b = (short)(b | getState(data.getValue("east")));
    b = (short)(b | getState(data.getValue("north")) << 2);
    b = (short)(b | getState(data.getValue("south")) << 4);
    b = (short)(b | getState(data.getValue("west")) << 6);
    b = (short)(b | Integer.valueOf(data.getValue("power")).intValue() << 8);
    return b;
  }
  
  private static int getState(String value) {
    switch (value) {
      case "none":
        return 0;
      case "side":
        return 1;
      case "up":
        return 2;
    } 
    return 0;
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    short b = 0;
    b = (short)(b | connects(user, position, BlockFace.EAST));
    b = (short)(b | connects(user, position, BlockFace.NORTH) << 2);
    b = (short)(b | connects(user, position, BlockFace.SOUTH) << 4);
    b = (short)(b | connects(user, position, BlockFace.WEST) << 6);
    b = (short)(b | ((Integer)powerMappings.get(Integer.valueOf(blockState))).intValue() << 8);
    Integer newBlockState = connectedBlockStates.get(Short.valueOf(b));
    return (newBlockState == null) ? blockState : newBlockState.intValue();
  }
  
  private int connects(UserConnection user, Position position, BlockFace side) {
    Position relative = position.getRelative(side);
    int blockState = getBlockData(user, relative);
    if (connects(side, blockState))
      return 1; 
    int up = getBlockData(user, relative.getRelative(BlockFace.TOP));
    if (redstone.contains(Integer.valueOf(up)) && !ConnectionData.occludingStates.contains(Integer.valueOf(getBlockData(user, position.getRelative(BlockFace.TOP)))))
      return 2; 
    int down = getBlockData(user, relative.getRelative(BlockFace.BOTTOM));
    if (redstone.contains(Integer.valueOf(down)) && !ConnectionData.occludingStates.contains(Integer.valueOf(getBlockData(user, relative))))
      return 1; 
    return 0;
  }
  
  private boolean connects(BlockFace side, int blockState) {
    BlockData blockData = ConnectionData.blockConnectionData.get(Integer.valueOf(blockState));
    return (blockData != null && blockData.connectsTo("redstoneConnections", side.opposite(), false));
  }
}
