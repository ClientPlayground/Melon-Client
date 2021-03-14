package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.Keybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ToggleSprint {
  private boolean sprint;
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public void render(IngameDisplay display, int x, int y) {
    if (this.sprint && IngameDisplay.TOGGLE_SPRINT_SHOW_TEXT.isEnabled())
      Client.renderManager.renderIngame("Sprinting <Toggled>", display, x, y); 
  }
  
  public void onTick(Keybind keybind) {
    KeyBinding keyBinding;
    if (IngameDisplay.TOGGLE_SPRINT_DEFAULT_KEYBIND.isEnabled()) {
      keyBinding = this.mc.gameSettings.keyBindSprint;
    } else {
      keyBinding = keybind.getKeyBinding();
    } 
    if (keyBinding.isPressed()) {
      this.sprint = !this.sprint;
      if (!this.sprint && 
        keyBinding.getKeyCode() > 0)
        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSprint.getKeyCode(), Keyboard.isKeyDown(keyBinding.getKeyCode())); 
    } 
    if (this.sprint)
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSprint.getKeyCode(), true); 
  }
}
