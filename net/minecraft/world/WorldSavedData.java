package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

public abstract class WorldSavedData {
  public final String mapName;
  
  private boolean dirty;
  
  public WorldSavedData(String name) {
    this.mapName = name;
  }
  
  public abstract void readFromNBT(NBTTagCompound paramNBTTagCompound);
  
  public abstract void writeToNBT(NBTTagCompound paramNBTTagCompound);
  
  public void markDirty() {
    setDirty(true);
  }
  
  public void setDirty(boolean isDirty) {
    this.dirty = isDirty;
  }
  
  public boolean isDirty() {
    return this.dirty;
  }
}
