package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

import java.util.HashMap;
import java.util.Map;

public enum BlockFace {
  NORTH(0, 0, -1, EnumAxis.Z),
  SOUTH(0, 0, 1, EnumAxis.Z),
  EAST(1, 0, 0, EnumAxis.X),
  WEST(-1, 0, 0, EnumAxis.X),
  TOP(0, 1, 0, EnumAxis.Y),
  BOTTOM(0, -1, 0, EnumAxis.Y);
  
  BlockFace(int modX, int modY, int modZ, EnumAxis axis) {
    this.modX = modX;
    this.modY = modY;
    this.modZ = modZ;
    this.axis = axis;
  }
  
  private static Map<BlockFace, BlockFace> opposites;
  
  private int modX;
  
  private int modY;
  
  private int modZ;
  
  private EnumAxis axis;
  
  static {
    opposites = new HashMap<>();
    opposites.put(NORTH, SOUTH);
    opposites.put(SOUTH, NORTH);
    opposites.put(EAST, WEST);
    opposites.put(WEST, EAST);
    opposites.put(TOP, BOTTOM);
    opposites.put(BOTTOM, TOP);
  }
  
  public int getModX() {
    return this.modX;
  }
  
  public int getModY() {
    return this.modY;
  }
  
  public int getModZ() {
    return this.modZ;
  }
  
  public EnumAxis getAxis() {
    return this.axis;
  }
  
  public BlockFace opposite() {
    return opposites.get(this);
  }
  
  public enum EnumAxis {
    X, Y, Z;
  }
}
