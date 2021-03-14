package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.util.Keybind;
import net.minecraft.client.Minecraft;

public class BehindYou {
  public static final BehindYou INSTANCE = new BehindYou();
  
  private boolean held = false;
  
  public void onTick(Keybind keybind) {
    if (keybind.getKeyBinding().isPressed())
      this.held = true; 
    if (this.held && keybind.getKeyBinding().isKeyDown()) {
      (Minecraft.getMinecraft()).gameSettings.thirdPersonView = 2;
    } else if (this.held) {
      (Minecraft.getMinecraft()).gameSettings.thirdPersonView = 0;
      this.held = false;
    } 
  }
}
