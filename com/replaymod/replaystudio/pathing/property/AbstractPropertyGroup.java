package com.replaymod.replaystudio.pathing.property;

import com.replaymod.replaystudio.util.I18n;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPropertyGroup implements PropertyGroup {
  private final String id;
  
  private final String localizationKey;
  
  private final List<Property> properties;
  
  @ConstructorProperties({"id", "localizationKey"})
  public AbstractPropertyGroup(String id, String localizationKey) {
    this.properties = new ArrayList<>();
    this.id = id;
    this.localizationKey = localizationKey;
  }
  
  public String getLocalizedName() {
    return I18n.format(this.localizationKey, new Object[0]);
  }
  
  public String getId() {
    return this.id;
  }
  
  public List<Property> getProperties() {
    return this.properties;
  }
}
