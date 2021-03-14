package me.kaimson.melonclient.util;

import java.util.function.Consumer;
import net.minecraft.client.settings.KeyBinding;

public class Keybind {
  private final String category;
  
  private final String description;
  
  private final int keycode;
  
  private final Consumer<Keybind> onPress;
  
  private final KeyBinding keyBinding;
  
  public Keybind(String category, String description, int keycode, Consumer<Keybind> onPress, KeyBinding keyBinding) {
    this.category = category;
    this.description = description;
    this.keycode = keycode;
    this.onPress = onPress;
    this.keyBinding = keyBinding;
  }
  
  public String getCategory() {
    return this.category;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public int getKeycode() {
    return this.keycode;
  }
  
  public Consumer<Keybind> getOnPress() {
    return this.onPress;
  }
  
  public KeyBinding getKeyBinding() {
    return this.keyBinding;
  }
  
  public Keybind(String description, int keycode, Consumer<Keybind> onPress) {
    this.category = "Melon Client";
    this.description = description;
    this.keycode = keycode;
    this.onPress = onPress;
    this.keyBinding = new KeyBinding(description, keycode, this.category);
  }
}
