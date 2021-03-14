package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TripwireConnectionHandler extends ConnectionHandler {
  private static Map<Integer, TripwireData> tripwireDataMap = new HashMap<>();
  
  private static Map<Byte, Integer> connectedBlocks = new HashMap<>();
  
  private static Map<Integer, BlockFace> tripwireHooks = new HashMap<>();
  
  static ConnectionData.ConnectorInitAction init() {
    final TripwireConnectionHandler connectionHandler = new TripwireConnectionHandler();
    return new ConnectionData.ConnectorInitAction() {
        public void check(WrappedBlockData blockData) {
          if (blockData.getMinecraftKey().equals("minecraft:tripwire_hook")) {
            TripwireConnectionHandler.tripwireHooks.put(Integer.valueOf(blockData.getSavedBlockStateId()), BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)));
          } else if (blockData.getMinecraftKey().equals("minecraft:tripwire")) {
            TripwireConnectionHandler.TripwireData tripwireData = new TripwireConnectionHandler.TripwireData(blockData.getValue("attached").equals("true"), blockData.getValue("disarmed").equals("true"), blockData.getValue("powered").equals("true"));
            TripwireConnectionHandler.tripwireDataMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), tripwireData);
            TripwireConnectionHandler.connectedBlocks.put(Byte.valueOf(TripwireConnectionHandler.getStates(blockData)), Integer.valueOf(blockData.getSavedBlockStateId()));
            ConnectionData.connectionHandlerMap.put(Integer.valueOf(blockData.getSavedBlockStateId()), connectionHandler);
          } 
        }
      };
  }
  
  private static byte getStates(WrappedBlockData blockData) {
    byte b = 0;
    if (blockData.getValue("attached").equals("true"))
      b = (byte)(b | 0x1); 
    if (blockData.getValue("disarmed").equals("true"))
      b = (byte)(b | 0x2); 
    if (blockData.getValue("powered").equals("true"))
      b = (byte)(b | 0x4); 
    if (blockData.getValue("east").equals("true"))
      b = (byte)(b | 0x8); 
    if (blockData.getValue("north").equals("true"))
      b = (byte)(b | 0x10); 
    if (blockData.getValue("south").equals("true"))
      b = (byte)(b | 0x20); 
    if (blockData.getValue("west").equals("true"))
      b = (byte)(b | 0x40); 
    return b;
  }
  
  public int connect(UserConnection user, Position position, int blockState) {
    TripwireData tripwireData = tripwireDataMap.get(Integer.valueOf(blockState));
    if (tripwireData == null)
      return blockState; 
    byte b = 0;
    if (tripwireData.isAttached())
      b = (byte)(b | 0x1); 
    if (tripwireData.isDisarmed())
      b = (byte)(b | 0x2); 
    if (tripwireData.isPowered())
      b = (byte)(b | 0x4); 
    int east = getBlockData(user, position.getRelative(BlockFace.EAST));
    int north = getBlockData(user, position.getRelative(BlockFace.NORTH));
    int south = getBlockData(user, position.getRelative(BlockFace.SOUTH));
    int west = getBlockData(user, position.getRelative(BlockFace.WEST));
    if (tripwireDataMap.containsKey(Integer.valueOf(east)) || tripwireHooks.get(Integer.valueOf(east)) == BlockFace.WEST)
      b = (byte)(b | 0x8); 
    if (tripwireDataMap.containsKey(Integer.valueOf(north)) || tripwireHooks.get(Integer.valueOf(north)) == BlockFace.SOUTH)
      b = (byte)(b | 0x10); 
    if (tripwireDataMap.containsKey(Integer.valueOf(south)) || tripwireHooks.get(Integer.valueOf(south)) == BlockFace.NORTH)
      b = (byte)(b | 0x20); 
    if (tripwireDataMap.containsKey(Integer.valueOf(west)) || tripwireHooks.get(Integer.valueOf(west)) == BlockFace.EAST)
      b = (byte)(b | 0x40); 
    Integer newBlockState = connectedBlocks.get(Byte.valueOf(b));
    return (newBlockState == null) ? blockState : newBlockState.intValue();
  }
  
  private static class TripwireData {
    private final boolean attached;
    
    private final boolean disarmed;
    
    private final boolean powered;
    
    public TripwireData(boolean attached, boolean disarmed, boolean powered) {
      this.attached = attached;
      this.disarmed = disarmed;
      this.powered = powered;
    }
    
    public String toString() {
      return "TripwireConnectionHandler.TripwireData(attached=" + isAttached() + ", disarmed=" + isDisarmed() + ", powered=" + isPowered() + ")";
    }
    
    public boolean isAttached() {
      return this.attached;
    }
    
    public boolean isDisarmed() {
      return this.disarmed;
    }
    
    public boolean isPowered() {
      return this.powered;
    }
  }
}
