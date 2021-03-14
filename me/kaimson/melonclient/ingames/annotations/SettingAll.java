package me.kaimson.melonclient.ingames.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SettingAll {
  Setting.Type type();
  
  Target target();
  
  boolean wrap() default true;
  
  public enum Target {
    DISPLAY_ITEM("DisplayItem"),
    DISPLAY_ITEM_RENDERTYPE_TEXT("DisplayItem.RenderType.Text"),
    EVENT_ITEM("EventItem"),
    SETTING("Setting"),
    SETTING_ALL("SettingAll"),
    SETTING_SLIDER("SettingSlider");
    
    private final String name;
    
    Target(String name) {
      this.name = name;
    }
  }
}
