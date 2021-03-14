package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft;

public class VillagerData {
  private int type;
  
  private int profession;
  
  private int level;
  
  public VillagerData(int type, int profession, int level) {
    this.type = type;
    this.profession = profession;
    this.level = level;
  }
  
  public void setType(int type) {
    this.type = type;
  }
  
  public void setProfession(int profession) {
    this.profession = profession;
  }
  
  public void setLevel(int level) {
    this.level = level;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof VillagerData))
      return false; 
    VillagerData other = (VillagerData)o;
    return !other.canEqual(this) ? false : ((getType() != other.getType()) ? false : ((getProfession() != other.getProfession()) ? false : (!(getLevel() != other.getLevel()))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof VillagerData;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getType();
    result = result * 59 + getProfession();
    return result * 59 + getLevel();
  }
  
  public String toString() {
    return "VillagerData(type=" + getType() + ", profession=" + getProfession() + ", level=" + getLevel() + ")";
  }
  
  public int getType() {
    return this.type;
  }
  
  public int getProfession() {
    return this.profession;
  }
  
  public int getLevel() {
    return this.level;
  }
}
