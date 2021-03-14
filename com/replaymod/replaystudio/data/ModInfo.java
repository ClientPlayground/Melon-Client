package com.replaymod.replaystudio.data;

import java.beans.ConstructorProperties;

public class ModInfo {
  private final String id;
  
  private final String name;
  
  private final String version;
  
  @ConstructorProperties({"id", "name", "version"})
  public ModInfo(String id, String name, String version) {
    this.id = id;
    this.name = name;
    this.version = version;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof ModInfo))
      return false; 
    ModInfo other = (ModInfo)o;
    if (!other.canEqual(this))
      return false; 
    Object this$id = getId(), other$id = other.getId();
    if ((this$id == null) ? (other$id != null) : !this$id.equals(other$id))
      return false; 
    Object this$name = getName(), other$name = other.getName();
    if ((this$name == null) ? (other$name != null) : !this$name.equals(other$name))
      return false; 
    Object this$version = getVersion(), other$version = other.getVersion();
    return !((this$version == null) ? (other$version != null) : !this$version.equals(other$version));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof ModInfo;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $id = getId();
    result = result * 59 + (($id == null) ? 43 : $id.hashCode());
    Object $name = getName();
    result = result * 59 + (($name == null) ? 43 : $name.hashCode());
    Object $version = getVersion();
    return result * 59 + (($version == null) ? 43 : $version.hashCode());
  }
  
  public String toString() {
    return "ModInfo(id=" + getId() + ", name=" + getName() + ", version=" + getVersion() + ")";
  }
  
  public String getId() {
    return this.id;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getVersion() {
    return this.version;
  }
}
