package me.kaimson.melonclient.ingames.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.kaimson.melonclient.ingames.IngameDisplay;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SettingMode {
  public static final Setting.Type type = Setting.Type.SWITCH;
  
  IngameDisplay[] modes();
}
