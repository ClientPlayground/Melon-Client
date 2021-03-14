package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;

public final class Setting<T> extends SettingsRegistry.SettingKeys<T> {
  public static final Setting<Boolean> PATH_PREVIEW = make("pathpreview", "pathpreview", Boolean.valueOf(true));
  
  public static final SettingsRegistry.MultipleChoiceSettingKeys<String> DEFAULT_INTERPOLATION;
  
  static {
    String format = "replaymod.gui.editkeyframe.interpolator.%s.name";
    DEFAULT_INTERPOLATION = new SettingsRegistry.MultipleChoiceSettingKeys("simplepathing", "interpolator", "replaymod.gui.settings.interpolator", String.format(format, new Object[] { InterpolatorType.fromString("invalid returns default").getLocalizationKey() }));
    DEFAULT_INTERPOLATION.setChoices(
        (List)Arrays.<InterpolatorType>stream(InterpolatorType.values()).filter(i -> (i != InterpolatorType.DEFAULT))
        .map(i -> String.format(format, new Object[] { i.getLocalizationKey() })).collect(Collectors.toList()));
  }
  
  private static <T> Setting<T> make(String key, String displayName, T defaultValue) {
    return new Setting<>(key, displayName, defaultValue);
  }
  
  public Setting(String key, String displayString, T defaultValue) {
    super("simplepathing", key, "replaymod.gui.settings." + displayString, defaultValue);
  }
}
