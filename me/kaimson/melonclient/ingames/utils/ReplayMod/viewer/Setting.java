package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;

public final class Setting<T> extends SettingsRegistry.SettingKeys<T> {
  public static final Setting<Boolean> SHOW_CHAT = make("showChat", "showchat", Boolean.valueOf(true));
  
  public static final Setting<Boolean> SHOW_SERVER_IPS = new Setting("showServerIPs", (T)Boolean.valueOf(true));
  
  public static final SettingsRegistry.MultipleChoiceSettingKeys<String> CAMERA = new SettingsRegistry.MultipleChoiceSettingKeys("replay", "camera", "replaymod.gui.settings.camera", "replaymod.camera.classic");
  
  private static <T> Setting<T> make(String key, String displayName, T defaultValue) {
    return new Setting<>(key, displayName, defaultValue);
  }
  
  public Setting(String key, String displayString, T defaultValue) {
    super("replay", key, "replaymod.gui.settings." + displayString, defaultValue);
  }
  
  public Setting(String key, T defaultValue) {
    super("replay", key, null, defaultValue);
  }
}
