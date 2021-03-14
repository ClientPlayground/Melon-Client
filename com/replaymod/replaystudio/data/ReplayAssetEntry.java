package com.replaymod.replaystudio.data;

import java.util.UUID;

public class ReplayAssetEntry {
  private final UUID uuid;
  
  private final String fileExtension;
  
  private String name;
  
  public ReplayAssetEntry(UUID uuid, String fileExtension) {
    this.uuid = uuid;
    this.fileExtension = fileExtension;
  }
  
  public ReplayAssetEntry(UUID uuid, String fileExtension, String name) {
    this.uuid = uuid;
    this.fileExtension = fileExtension;
    this.name = name;
  }
  
  public UUID getUuid() {
    return this.uuid;
  }
  
  public String getFileExtension() {
    return this.fileExtension;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    ReplayAssetEntry that = (ReplayAssetEntry)o;
    return this.uuid.equals(that.uuid);
  }
  
  public int hashCode() {
    return this.uuid.hashCode();
  }
  
  public String toString() {
    return "ReplayAssetEntry{uuid=" + this.uuid + ", fileExtension='" + this.fileExtension + '\'' + ", name='" + this.name + '\'' + '}';
  }
}
