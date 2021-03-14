package me.kaimson.melonclient.ingames.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
  Type type();
  
  public enum Type {
    COLOR, SLIDER, CHECKBOX, SWITCH, TEXT, ARRAY, SCALE, MODE;
  }
}
