package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DefaultSpdySettingsFrame implements SpdySettingsFrame {
  private boolean clear;
  
  private final Map<Integer, Setting> settingsMap = new TreeMap<Integer, Setting>();
  
  public Set<Integer> ids() {
    return this.settingsMap.keySet();
  }
  
  public boolean isSet(int id) {
    return this.settingsMap.containsKey(Integer.valueOf(id));
  }
  
  public int getValue(int id) {
    Setting setting = this.settingsMap.get(Integer.valueOf(id));
    return (setting != null) ? setting.getValue() : -1;
  }
  
  public SpdySettingsFrame setValue(int id, int value) {
    return setValue(id, value, false, false);
  }
  
  public SpdySettingsFrame setValue(int id, int value, boolean persistValue, boolean persisted) {
    if (id < 0 || id > 16777215)
      throw new IllegalArgumentException("Setting ID is not valid: " + id); 
    Integer key = Integer.valueOf(id);
    Setting setting = this.settingsMap.get(key);
    if (setting != null) {
      setting.setValue(value);
      setting.setPersist(persistValue);
      setting.setPersisted(persisted);
    } else {
      this.settingsMap.put(key, new Setting(value, persistValue, persisted));
    } 
    return this;
  }
  
  public SpdySettingsFrame removeValue(int id) {
    this.settingsMap.remove(Integer.valueOf(id));
    return this;
  }
  
  public boolean isPersistValue(int id) {
    Setting setting = this.settingsMap.get(Integer.valueOf(id));
    return (setting != null && setting.isPersist());
  }
  
  public SpdySettingsFrame setPersistValue(int id, boolean persistValue) {
    Setting setting = this.settingsMap.get(Integer.valueOf(id));
    if (setting != null)
      setting.setPersist(persistValue); 
    return this;
  }
  
  public boolean isPersisted(int id) {
    Setting setting = this.settingsMap.get(Integer.valueOf(id));
    return (setting != null && setting.isPersisted());
  }
  
  public SpdySettingsFrame setPersisted(int id, boolean persisted) {
    Setting setting = this.settingsMap.get(Integer.valueOf(id));
    if (setting != null)
      setting.setPersisted(persisted); 
    return this;
  }
  
  public boolean clearPreviouslyPersistedSettings() {
    return this.clear;
  }
  
  public SpdySettingsFrame setClearPreviouslyPersistedSettings(boolean clear) {
    this.clear = clear;
    return this;
  }
  
  private Set<Map.Entry<Integer, Setting>> getSettings() {
    return this.settingsMap.entrySet();
  }
  
  private void appendSettings(StringBuilder buf) {
    for (Map.Entry<Integer, Setting> e : getSettings()) {
      Setting setting = e.getValue();
      buf.append("--> ");
      buf.append(e.getKey());
      buf.append(':');
      buf.append(setting.getValue());
      buf.append(" (persist value: ");
      buf.append(setting.isPersist());
      buf.append("; persisted: ");
      buf.append(setting.isPersisted());
      buf.append(')');
      buf.append(StringUtil.NEWLINE);
    } 
  }
  
  public String toString() {
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append(StringUtil.NEWLINE);
    appendSettings(buf);
    buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    return buf.toString();
  }
  
  private static final class Setting {
    private int value;
    
    private boolean persist;
    
    private boolean persisted;
    
    Setting(int value, boolean persist, boolean persisted) {
      this.value = value;
      this.persist = persist;
      this.persisted = persisted;
    }
    
    int getValue() {
      return this.value;
    }
    
    void setValue(int value) {
      this.value = value;
    }
    
    boolean isPersist() {
      return this.persist;
    }
    
    void setPersist(boolean persist) {
      this.persist = persist;
    }
    
    boolean isPersisted() {
      return this.persisted;
    }
    
    void setPersisted(boolean persisted) {
      this.persisted = persisted;
    }
  }
}
