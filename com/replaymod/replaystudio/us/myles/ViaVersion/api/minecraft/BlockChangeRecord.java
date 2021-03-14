package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

public class BlockChangeRecord {
  private short horizontal;
  
  private short y;
  
  private int blockId;
  
  public void setHorizontal(short horizontal) {
    this.horizontal = horizontal;
  }
  
  public void setY(short y) {
    this.y = y;
  }
  
  public void setBlockId(int blockId) {
    this.blockId = blockId;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof BlockChangeRecord))
      return false; 
    BlockChangeRecord other = (BlockChangeRecord)o;
    return !other.canEqual(this) ? false : ((getHorizontal() != other.getHorizontal()) ? false : ((getY() != other.getY()) ? false : (!(getBlockId() != other.getBlockId()))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof BlockChangeRecord;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getHorizontal();
    result = result * 59 + getY();
    return result * 59 + getBlockId();
  }
  
  public String toString() {
    return "BlockChangeRecord(horizontal=" + getHorizontal() + ", y=" + getY() + ", blockId=" + getBlockId() + ")";
  }
  
  public BlockChangeRecord(short horizontal, short y, int blockId) {
    this.horizontal = horizontal;
    this.y = y;
    this.blockId = blockId;
  }
  
  public short getHorizontal() {
    return this.horizontal;
  }
  
  public short getY() {
    return this.y;
  }
  
  public int getBlockId() {
    return this.blockId;
  }
}
