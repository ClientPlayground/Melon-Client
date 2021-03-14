package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public enum EnumFacing implements IStringSerializable {
  DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
  UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
  NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
  SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
  WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
  EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));
  
  private final int index;
  
  private final int opposite;
  
  private final int horizontalIndex;
  
  private final String name;
  
  private final Axis axis;
  
  private final AxisDirection axisDirection;
  
  private final Vec3i directionVec;
  
  public static final EnumFacing[] VALUES;
  
  private static final EnumFacing[] HORIZONTALS;
  
  private static final Map<String, EnumFacing> NAME_LOOKUP;
  
  static {
    VALUES = new EnumFacing[6];
    HORIZONTALS = new EnumFacing[4];
    NAME_LOOKUP = Maps.newHashMap();
    for (EnumFacing enumfacing : values()) {
      VALUES[enumfacing.index] = enumfacing;
      if (enumfacing.getAxis().isHorizontal())
        HORIZONTALS[enumfacing.horizontalIndex] = enumfacing; 
      NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(), enumfacing);
    } 
  }
  
  EnumFacing(int indexIn, int oppositeIn, int horizontalIndexIn, String nameIn, AxisDirection axisDirectionIn, Axis axisIn, Vec3i directionVecIn) {
    this.index = indexIn;
    this.horizontalIndex = horizontalIndexIn;
    this.opposite = oppositeIn;
    this.name = nameIn;
    this.axis = axisIn;
    this.axisDirection = axisDirectionIn;
    this.directionVec = directionVecIn;
  }
  
  public int getIndex() {
    return this.index;
  }
  
  public int getHorizontalIndex() {
    return this.horizontalIndex;
  }
  
  public AxisDirection getAxisDirection() {
    return this.axisDirection;
  }
  
  public EnumFacing getOpposite() {
    return VALUES[this.opposite];
  }
  
  public EnumFacing rotateAround(Axis axis) {
    switch (axis) {
      case HORIZONTAL:
        if (this != WEST && this != EAST)
          return rotateX(); 
        return this;
      case VERTICAL:
        if (this != UP && this != DOWN)
          return rotateY(); 
        return this;
      case null:
        if (this != NORTH && this != SOUTH)
          return rotateZ(); 
        return this;
    } 
    throw new IllegalStateException("Unable to get CW facing for axis " + axis);
  }
  
  public EnumFacing rotateY() {
    switch (this) {
      case HORIZONTAL:
        return EAST;
      case VERTICAL:
        return SOUTH;
      case null:
        return WEST;
      case null:
        return NORTH;
    } 
    throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
  }
  
  private EnumFacing rotateX() {
    switch (this) {
      case HORIZONTAL:
        return DOWN;
      default:
        throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      case null:
        return UP;
      case null:
        return NORTH;
      case null:
        break;
    } 
    return SOUTH;
  }
  
  private EnumFacing rotateZ() {
    switch (this) {
      case VERTICAL:
        return DOWN;
      default:
        throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
      case null:
        return UP;
      case null:
        return EAST;
      case null:
        break;
    } 
    return WEST;
  }
  
  public EnumFacing rotateYCCW() {
    switch (this) {
      case HORIZONTAL:
        return WEST;
      case VERTICAL:
        return NORTH;
      case null:
        return EAST;
      case null:
        return SOUTH;
    } 
    throw new IllegalStateException("Unable to get CCW facing of " + this);
  }
  
  public int getFrontOffsetX() {
    return (this.axis == Axis.X) ? this.axisDirection.getOffset() : 0;
  }
  
  public int getFrontOffsetY() {
    return (this.axis == Axis.Y) ? this.axisDirection.getOffset() : 0;
  }
  
  public int getFrontOffsetZ() {
    return (this.axis == Axis.Z) ? this.axisDirection.getOffset() : 0;
  }
  
  public String getName2() {
    return this.name;
  }
  
  public Axis getAxis() {
    return this.axis;
  }
  
  public static EnumFacing byName(String name) {
    return (name == null) ? null : NAME_LOOKUP.get(name.toLowerCase());
  }
  
  public static EnumFacing getFront(int index) {
    return VALUES[MathHelper.abs_int(index % VALUES.length)];
  }
  
  public static EnumFacing getHorizontal(int p_176731_0_) {
    return HORIZONTALS[MathHelper.abs_int(p_176731_0_ % HORIZONTALS.length)];
  }
  
  public static EnumFacing fromAngle(double angle) {
    return getHorizontal(MathHelper.floor_double(angle / 90.0D + 0.5D) & 0x3);
  }
  
  public static EnumFacing random(Random rand) {
    return values()[rand.nextInt((values()).length)];
  }
  
  public static EnumFacing getFacingFromVector(float p_176737_0_, float p_176737_1_, float p_176737_2_) {
    EnumFacing enumfacing = NORTH;
    float f = Float.MIN_VALUE;
    for (EnumFacing enumfacing1 : values()) {
      float f1 = p_176737_0_ * enumfacing1.directionVec.getX() + p_176737_1_ * enumfacing1.directionVec.getY() + p_176737_2_ * enumfacing1.directionVec.getZ();
      if (f1 > f) {
        f = f1;
        enumfacing = enumfacing1;
      } 
    } 
    return enumfacing;
  }
  
  public String toString() {
    return this.name;
  }
  
  public String getName() {
    return this.name;
  }
  
  public static EnumFacing getFacingFromAxis(AxisDirection p_181076_0_, Axis p_181076_1_) {
    for (EnumFacing enumfacing : values()) {
      if (enumfacing.getAxisDirection() == p_181076_0_ && enumfacing.getAxis() == p_181076_1_)
        return enumfacing; 
    } 
    throw new IllegalArgumentException("No such direction: " + p_181076_0_ + " " + p_181076_1_);
  }
  
  public Vec3i getDirectionVec() {
    return this.directionVec;
  }
  
  public enum Axis implements Predicate<EnumFacing>, IStringSerializable {
    X("x", EnumFacing.Plane.HORIZONTAL),
    Y("y", EnumFacing.Plane.VERTICAL),
    Z("z", EnumFacing.Plane.HORIZONTAL);
    
    private static final Map<String, Axis> NAME_LOOKUP = Maps.newHashMap();
    
    private final String name;
    
    private final EnumFacing.Plane plane;
    
    static {
      for (Axis enumfacing$axis : values())
        NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(), enumfacing$axis); 
    }
    
    Axis(String name, EnumFacing.Plane plane) {
      this.name = name;
      this.plane = plane;
    }
    
    public static Axis byName(String name) {
      return (name == null) ? null : NAME_LOOKUP.get(name.toLowerCase());
    }
    
    public String getName2() {
      return this.name;
    }
    
    public boolean isVertical() {
      return (this.plane == EnumFacing.Plane.VERTICAL);
    }
    
    public boolean isHorizontal() {
      return (this.plane == EnumFacing.Plane.HORIZONTAL);
    }
    
    public String toString() {
      return this.name;
    }
    
    public boolean apply(EnumFacing p_apply_1_) {
      return (p_apply_1_ != null && p_apply_1_.getAxis() == this);
    }
    
    public EnumFacing.Plane getPlane() {
      return this.plane;
    }
    
    public String getName() {
      return this.name;
    }
  }
  
  public enum AxisDirection {
    POSITIVE(1, "Towards positive"),
    NEGATIVE(-1, "Towards negative");
    
    private final int offset;
    
    private final String description;
    
    AxisDirection(int offset, String description) {
      this.offset = offset;
      this.description = description;
    }
    
    public int getOffset() {
      return this.offset;
    }
    
    public String toString() {
      return this.description;
    }
  }
  
  public enum Plane implements Predicate<EnumFacing>, Iterable<EnumFacing> {
    HORIZONTAL, VERTICAL;
    
    public EnumFacing[] facings() {
      switch (this) {
        case HORIZONTAL:
          return new EnumFacing[] { EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST };
        case VERTICAL:
          return new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
      } 
      throw new Error("Someone's been tampering with the universe!");
    }
    
    public EnumFacing random(Random rand) {
      EnumFacing[] aenumfacing = facings();
      return aenumfacing[rand.nextInt(aenumfacing.length)];
    }
    
    public boolean apply(EnumFacing p_apply_1_) {
      return (p_apply_1_ != null && p_apply_1_.getAxis().getPlane() == this);
    }
    
    public Iterator<EnumFacing> iterator() {
      return (Iterator<EnumFacing>)Iterators.forArray((Object[])facings());
    }
  }
}
