package net.minecraft.client.renderer.vertex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VertexFormatElement {
  private static final Logger LOGGER = LogManager.getLogger();
  
  private final EnumType type;
  
  private final EnumUsage usage;
  
  private int index;
  
  private int elementCount;
  
  public VertexFormatElement(int indexIn, EnumType typeIn, EnumUsage usageIn, int count) {
    if (!func_177372_a(indexIn, usageIn)) {
      LOGGER.warn("Multiple vertex elements of the same type other than UVs are not supported. Forcing type to UV.");
      this.usage = EnumUsage.UV;
    } else {
      this.usage = usageIn;
    } 
    this.type = typeIn;
    this.index = indexIn;
    this.elementCount = count;
  }
  
  private final boolean func_177372_a(int p_177372_1_, EnumUsage p_177372_2_) {
    return (p_177372_1_ == 0 || p_177372_2_ == EnumUsage.UV);
  }
  
  public final EnumType getType() {
    return this.type;
  }
  
  public final EnumUsage getUsage() {
    return this.usage;
  }
  
  public final int getElementCount() {
    return this.elementCount;
  }
  
  public final int getIndex() {
    return this.index;
  }
  
  public String toString() {
    return this.elementCount + "," + this.usage.getDisplayName() + "," + this.type.getDisplayName();
  }
  
  public final int getSize() {
    return this.type.getSize() * this.elementCount;
  }
  
  public final boolean isPositionElement() {
    return (this.usage == EnumUsage.POSITION);
  }
  
  public boolean equals(Object p_equals_1_) {
    if (this == p_equals_1_)
      return true; 
    if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
      VertexFormatElement vertexformatelement = (VertexFormatElement)p_equals_1_;
      return (this.elementCount != vertexformatelement.elementCount) ? false : ((this.index != vertexformatelement.index) ? false : ((this.type != vertexformatelement.type) ? false : ((this.usage == vertexformatelement.usage))));
    } 
    return false;
  }
  
  public int hashCode() {
    int i = this.type.hashCode();
    i = 31 * i + this.usage.hashCode();
    i = 31 * i + this.index;
    i = 31 * i + this.elementCount;
    return i;
  }
  
  public enum EnumType {
    FLOAT(4, "Float", 5126),
    UBYTE(1, "Unsigned Byte", 5121),
    BYTE(1, "Byte", 5120),
    USHORT(2, "Unsigned Short", 5123),
    SHORT(2, "Short", 5122),
    UINT(4, "Unsigned Int", 5125),
    INT(4, "Int", 5124);
    
    private final int size;
    
    private final String displayName;
    
    private final int glConstant;
    
    EnumType(int sizeIn, String displayNameIn, int glConstantIn) {
      this.size = sizeIn;
      this.displayName = displayNameIn;
      this.glConstant = glConstantIn;
    }
    
    public int getSize() {
      return this.size;
    }
    
    public String getDisplayName() {
      return this.displayName;
    }
    
    public int getGlConstant() {
      return this.glConstant;
    }
  }
  
  public enum EnumUsage {
    POSITION("Position"),
    NORMAL("Normal"),
    COLOR("Vertex Color"),
    UV("UV"),
    MATRIX("Bone Matrix"),
    BLEND_WEIGHT("Blend Weight"),
    PADDING("Padding");
    
    private final String displayName;
    
    EnumUsage(String displayNameIn) {
      this.displayName = displayNameIn;
    }
    
    public String getDisplayName() {
      return this.displayName;
    }
  }
}
