package com.replaymod.replaystudio.us.myles.ViaVersion.dump;

import java.util.Set;

public class VersionInfo {
  private String javaVersion;
  
  private String operatingSystem;
  
  private int serverProtocol;
  
  private Set<Integer> enabledProtocols;
  
  private String platformName;
  
  private String platformVersion;
  
  private String pluginVersion;
  
  public void setJavaVersion(String javaVersion) {
    this.javaVersion = javaVersion;
  }
  
  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }
  
  public void setServerProtocol(int serverProtocol) {
    this.serverProtocol = serverProtocol;
  }
  
  public void setEnabledProtocols(Set<Integer> enabledProtocols) {
    this.enabledProtocols = enabledProtocols;
  }
  
  public void setPlatformName(String platformName) {
    this.platformName = platformName;
  }
  
  public void setPlatformVersion(String platformVersion) {
    this.platformVersion = platformVersion;
  }
  
  public void setPluginVersion(String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof VersionInfo))
      return false; 
    VersionInfo other = (VersionInfo)o;
    if (!other.canEqual(this))
      return false; 
    Object this$javaVersion = getJavaVersion(), other$javaVersion = other.getJavaVersion();
    if ((this$javaVersion == null) ? (other$javaVersion != null) : !this$javaVersion.equals(other$javaVersion))
      return false; 
    Object this$operatingSystem = getOperatingSystem(), other$operatingSystem = other.getOperatingSystem();
    if ((this$operatingSystem == null) ? (other$operatingSystem != null) : !this$operatingSystem.equals(other$operatingSystem))
      return false; 
    if (getServerProtocol() != other.getServerProtocol())
      return false; 
    Object<Integer> this$enabledProtocols = (Object<Integer>)getEnabledProtocols(), other$enabledProtocols = (Object<Integer>)other.getEnabledProtocols();
    if ((this$enabledProtocols == null) ? (other$enabledProtocols != null) : !this$enabledProtocols.equals(other$enabledProtocols))
      return false; 
    Object this$platformName = getPlatformName(), other$platformName = other.getPlatformName();
    if ((this$platformName == null) ? (other$platformName != null) : !this$platformName.equals(other$platformName))
      return false; 
    Object this$platformVersion = getPlatformVersion(), other$platformVersion = other.getPlatformVersion();
    if ((this$platformVersion == null) ? (other$platformVersion != null) : !this$platformVersion.equals(other$platformVersion))
      return false; 
    Object this$pluginVersion = getPluginVersion(), other$pluginVersion = other.getPluginVersion();
    return !((this$pluginVersion == null) ? (other$pluginVersion != null) : !this$pluginVersion.equals(other$pluginVersion));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof VersionInfo;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $javaVersion = getJavaVersion();
    result = result * 59 + (($javaVersion == null) ? 43 : $javaVersion.hashCode());
    Object $operatingSystem = getOperatingSystem();
    result = result * 59 + (($operatingSystem == null) ? 43 : $operatingSystem.hashCode());
    result = result * 59 + getServerProtocol();
    Object<Integer> $enabledProtocols = (Object<Integer>)getEnabledProtocols();
    result = result * 59 + (($enabledProtocols == null) ? 43 : $enabledProtocols.hashCode());
    Object $platformName = getPlatformName();
    result = result * 59 + (($platformName == null) ? 43 : $platformName.hashCode());
    Object $platformVersion = getPlatformVersion();
    result = result * 59 + (($platformVersion == null) ? 43 : $platformVersion.hashCode());
    Object $pluginVersion = getPluginVersion();
    return result * 59 + (($pluginVersion == null) ? 43 : $pluginVersion.hashCode());
  }
  
  public String toString() {
    return "VersionInfo(javaVersion=" + getJavaVersion() + ", operatingSystem=" + getOperatingSystem() + ", serverProtocol=" + getServerProtocol() + ", enabledProtocols=" + getEnabledProtocols() + ", platformName=" + getPlatformName() + ", platformVersion=" + getPlatformVersion() + ", pluginVersion=" + getPluginVersion() + ")";
  }
  
  public VersionInfo(String javaVersion, String operatingSystem, int serverProtocol, Set<Integer> enabledProtocols, String platformName, String platformVersion, String pluginVersion) {
    this.javaVersion = javaVersion;
    this.operatingSystem = operatingSystem;
    this.serverProtocol = serverProtocol;
    this.enabledProtocols = enabledProtocols;
    this.platformName = platformName;
    this.platformVersion = platformVersion;
    this.pluginVersion = pluginVersion;
  }
  
  public String getJavaVersion() {
    return this.javaVersion;
  }
  
  public String getOperatingSystem() {
    return this.operatingSystem;
  }
  
  public int getServerProtocol() {
    return this.serverProtocol;
  }
  
  public Set<Integer> getEnabledProtocols() {
    return this.enabledProtocols;
  }
  
  public String getPlatformName() {
    return this.platformName;
  }
  
  public String getPlatformVersion() {
    return this.platformVersion;
  }
  
  public String getPluginVersion() {
    return this.pluginVersion;
  }
}
