package com.replaymod.replaystudio.us.myles.ViaVersion.dump;

import com.google.gson.JsonObject;
import java.util.Map;

public class DumpTemplate {
  private VersionInfo versionInfo;
  
  private Map<String, Object> configuration;
  
  private JsonObject platformDump;
  
  private JsonObject injectionDump;
  
  public void setVersionInfo(VersionInfo versionInfo) {
    this.versionInfo = versionInfo;
  }
  
  public void setConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }
  
  public void setPlatformDump(JsonObject platformDump) {
    this.platformDump = platformDump;
  }
  
  public void setInjectionDump(JsonObject injectionDump) {
    this.injectionDump = injectionDump;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof DumpTemplate))
      return false; 
    DumpTemplate other = (DumpTemplate)o;
    if (!other.canEqual(this))
      return false; 
    Object this$versionInfo = getVersionInfo(), other$versionInfo = other.getVersionInfo();
    if ((this$versionInfo == null) ? (other$versionInfo != null) : !this$versionInfo.equals(other$versionInfo))
      return false; 
    Object<String, Object> this$configuration = (Object<String, Object>)getConfiguration(), other$configuration = (Object<String, Object>)other.getConfiguration();
    if ((this$configuration == null) ? (other$configuration != null) : !this$configuration.equals(other$configuration))
      return false; 
    Object this$platformDump = getPlatformDump(), other$platformDump = other.getPlatformDump();
    if ((this$platformDump == null) ? (other$platformDump != null) : !this$platformDump.equals(other$platformDump))
      return false; 
    Object this$injectionDump = getInjectionDump(), other$injectionDump = other.getInjectionDump();
    return !((this$injectionDump == null) ? (other$injectionDump != null) : !this$injectionDump.equals(other$injectionDump));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof DumpTemplate;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $versionInfo = getVersionInfo();
    result = result * 59 + (($versionInfo == null) ? 43 : $versionInfo.hashCode());
    Object<String, Object> $configuration = (Object<String, Object>)getConfiguration();
    result = result * 59 + (($configuration == null) ? 43 : $configuration.hashCode());
    Object $platformDump = getPlatformDump();
    result = result * 59 + (($platformDump == null) ? 43 : $platformDump.hashCode());
    Object $injectionDump = getInjectionDump();
    return result * 59 + (($injectionDump == null) ? 43 : $injectionDump.hashCode());
  }
  
  public String toString() {
    return "DumpTemplate(versionInfo=" + getVersionInfo() + ", configuration=" + getConfiguration() + ", platformDump=" + getPlatformDump() + ", injectionDump=" + getInjectionDump() + ")";
  }
  
  public DumpTemplate(VersionInfo versionInfo, Map<String, Object> configuration, JsonObject platformDump, JsonObject injectionDump) {
    this.versionInfo = versionInfo;
    this.configuration = configuration;
    this.platformDump = platformDump;
    this.injectionDump = injectionDump;
  }
  
  public VersionInfo getVersionInfo() {
    return this.versionInfo;
  }
  
  public Map<String, Object> getConfiguration() {
    return this.configuration;
  }
  
  public JsonObject getPlatformDump() {
    return this.platformDump;
  }
  
  public JsonObject getInjectionDump() {
    return this.injectionDump;
  }
}
