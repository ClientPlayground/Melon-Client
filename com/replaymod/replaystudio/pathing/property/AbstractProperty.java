package com.replaymod.replaystudio.pathing.property;

import com.replaymod.replaystudio.util.I18n;

public abstract class AbstractProperty<T> implements Property<T> {
  private final String id;
  
  private final String localizationKey;
  
  private final PropertyGroup propertyGroup;
  
  private final T initialValue;
  
  public AbstractProperty(String id, String localizationKey, PropertyGroup propertyGroup, T initialValue) {
    this.id = id;
    this.localizationKey = localizationKey;
    this.propertyGroup = propertyGroup;
    this.initialValue = initialValue;
    if (propertyGroup != null)
      propertyGroup.getProperties().add(this); 
  }
  
  public String getLocalizedName() {
    return I18n.format(this.localizationKey, new Object[0]);
  }
  
  public PropertyGroup getGroup() {
    return this.propertyGroup;
  }
  
  public String getId() {
    return this.id;
  }
  
  public T getNewValue() {
    return this.initialValue;
  }
}
