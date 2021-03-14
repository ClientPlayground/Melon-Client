package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;

public final class Setting<T> extends SettingsRegistry.SettingKeys<T> {
  public static final Setting<Boolean> RECORD_SINGLEPLAYER = make("recordSingleplayer", "recordsingleplayer", Boolean.valueOf(true));
  
  public static final Setting<Boolean> RECORD_SERVER = make("recordServer", "recordserver", Boolean.valueOf(true));
  
  public static final Setting<Boolean> INDICATOR = make("indicator", "indicator", Boolean.valueOf(true));
  
  public static final Setting<Boolean> AUTO_START_RECORDING = make("autoStartRecording", "autostartrecording", Boolean.valueOf(true));
  
  public static final Setting<Boolean> AUTO_POST_PROCESS = make("autoPostProcess", null, Boolean.valueOf(true));
  
  private static <T> Setting<T> make(String key, String displayName, T defaultValue) {
    return new Setting<>(key, displayName, defaultValue);
  }
  
  public Setting(String key, String displayString, T defaultValue) {
    super("recording", key, (displayString == null) ? null : ("replaymod.gui.settings." + displayString), defaultValue);
  }
}
