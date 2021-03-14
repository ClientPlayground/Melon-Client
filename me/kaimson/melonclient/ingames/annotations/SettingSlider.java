package me.kaimson.melonclient.ingames.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SettingSlider {
  public static final Setting.Type type = Setting.Type.SLIDER;
  
  float min();
  
  float max();
  
  float step();
  
  float current();
}
