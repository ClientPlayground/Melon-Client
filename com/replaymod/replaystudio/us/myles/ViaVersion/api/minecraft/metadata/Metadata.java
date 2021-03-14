package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata;

public class Metadata {
  private int id;
  
  private MetaType metaType;
  
  private Object value;
  
  public Metadata(int id, MetaType metaType, Object value) {
    this.id = id;
    this.metaType = metaType;
    this.value = value;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public void setMetaType(MetaType metaType) {
    this.metaType = metaType;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Metadata))
      return false; 
    Metadata other = (Metadata)o;
    if (!other.canEqual(this))
      return false; 
    if (getId() != other.getId())
      return false; 
    Object this$metaType = getMetaType(), other$metaType = other.getMetaType();
    if ((this$metaType == null) ? (other$metaType != null) : !this$metaType.equals(other$metaType))
      return false; 
    Object this$value = getValue(), other$value = other.getValue();
    return !((this$value == null) ? (other$value != null) : !this$value.equals(other$value));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Metadata;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getId();
    Object $metaType = getMetaType();
    result = result * 59 + (($metaType == null) ? 43 : $metaType.hashCode());
    Object $value = getValue();
    return result * 59 + (($value == null) ? 43 : $value.hashCode());
  }
  
  public String toString() {
    return "Metadata(id=" + getId() + ", metaType=" + getMetaType() + ", value=" + getValue() + ")";
  }
  
  public int getId() {
    return this.id;
  }
  
  public MetaType getMetaType() {
    return this.metaType;
  }
  
  public Object getValue() {
    return this.value;
  }
}
