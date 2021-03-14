package me.kaimson.melonclient.ingames.utils.ReplayMod.Events;

import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;

public class SettingsChangedEvent extends Event {
  private final SettingsRegistry registry;
  
  private final SettingsRegistry.SettingKey<?> key;
  
  public SettingsRegistry getRegistry() {
    return this.registry;
  }
  
  public SettingsRegistry.SettingKey<?> getKey() {
    return this.key;
  }
  
  public SettingsChangedEvent(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
    this.registry = registry;
    this.key = key;
  }
}
