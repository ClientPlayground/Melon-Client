package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

public class Position {
  private Long x;
  
  private Long y;
  
  private Long z;
  
  public Position(Long x, Long y, Long z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public String toString() {
    return "Position(x=" + getX() + ", y=" + getY() + ", z=" + getZ() + ")";
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Position))
      return false; 
    Position other = (Position)o;
    if (!other.canEqual(this))
      return false; 
    Object this$x = getX(), other$x = other.getX();
    if ((this$x == null) ? (other$x != null) : !this$x.equals(other$x))
      return false; 
    Object this$y = getY(), other$y = other.getY();
    if ((this$y == null) ? (other$y != null) : !this$y.equals(other$y))
      return false; 
    Object this$z = getZ(), other$z = other.getZ();
    return !((this$z == null) ? (other$z != null) : !this$z.equals(other$z));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Position;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $x = getX();
    result = result * 59 + (($x == null) ? 43 : $x.hashCode());
    Object $y = getY();
    result = result * 59 + (($y == null) ? 43 : $y.hashCode());
    Object $z = getZ();
    return result * 59 + (($z == null) ? 43 : $z.hashCode());
  }
  
  public Long getX() {
    return this.x;
  }
  
  public Long getY() {
    return this.y;
  }
  
  public Long getZ() {
    return this.z;
  }
  
  public Position getRelative(BlockFace face) {
    return new Position(Long.valueOf(this.x.longValue() + face.getModX()), Long.valueOf(this.y.longValue() + face.getModY()), Long.valueOf(this.z.longValue() + face.getModZ()));
  }
  
  public Position shift(BlockFace face) {
    Position position = this;
    position.x = Long.valueOf(position.x.longValue() + face.getModX());
    position = this;
    position.y = Long.valueOf(position.y.longValue() + face.getModY());
    position = this;
    position.z = Long.valueOf(position.z.longValue() + face.getModZ());
    return this;
  }
}
