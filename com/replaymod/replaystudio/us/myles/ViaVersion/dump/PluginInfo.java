package com.replaymod.replaystudio.us.myles.ViaVersion.dump;

import java.util.List;

public class PluginInfo {
  private boolean enabled;
  
  private String name;
  
  private String version;
  
  private String main;
  
  private List<String> authors;
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public void setMain(String main) {
    this.main = main;
  }
  
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof PluginInfo))
      return false; 
    PluginInfo other = (PluginInfo)o;
    if (!other.canEqual(this))
      return false; 
    if (isEnabled() != other.isEnabled())
      return false; 
    Object this$name = getName(), other$name = other.getName();
    if ((this$name == null) ? (other$name != null) : !this$name.equals(other$name))
      return false; 
    Object this$version = getVersion(), other$version = other.getVersion();
    if ((this$version == null) ? (other$version != null) : !this$version.equals(other$version))
      return false; 
    Object this$main = getMain(), other$main = other.getMain();
    if ((this$main == null) ? (other$main != null) : !this$main.equals(other$main))
      return false; 
    Object<String> this$authors = (Object<String>)getAuthors(), other$authors = (Object<String>)other.getAuthors();
    return !((this$authors == null) ? (other$authors != null) : !this$authors.equals(other$authors));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof PluginInfo;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + (isEnabled() ? 79 : 97);
    Object $name = getName();
    result = result * 59 + (($name == null) ? 43 : $name.hashCode());
    Object $version = getVersion();
    result = result * 59 + (($version == null) ? 43 : $version.hashCode());
    Object $main = getMain();
    result = result * 59 + (($main == null) ? 43 : $main.hashCode());
    Object<String> $authors = (Object<String>)getAuthors();
    return result * 59 + (($authors == null) ? 43 : $authors.hashCode());
  }
  
  public String toString() {
    return "PluginInfo(enabled=" + isEnabled() + ", name=" + getName() + ", version=" + getVersion() + ", main=" + getMain() + ", authors=" + getAuthors() + ")";
  }
  
  public PluginInfo(boolean enabled, String name, String version, String main, List<String> authors) {
    this.enabled = enabled;
    this.name = name;
    this.version = version;
    this.main = main;
    this.authors = authors;
  }
  
  public boolean isEnabled() {
    return this.enabled;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getVersion() {
    return this.version;
  }
  
  public String getMain() {
    return this.main;
  }
  
  public List<String> getAuthors() {
    return this.authors;
  }
}
