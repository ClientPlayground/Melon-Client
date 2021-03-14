package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.ingames.Ingame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class Perspective extends Ingame {
  public static KeyBinding key;
  
  public static float cameraYaw;
  
  public static float cameraPitch;
  
  public void init() {
    super.init();
    EventHandler.register(this);
  }
  
  public static boolean enabled = false;
  
  public void patch() {
    super.patch();
    Client.instance.registerKeybind(key = new KeyBinding("Perspective", 33, "Melon Client"));
  }
  
  @TypeEvent
  public void onTick(TickEvent.ClientTick e) {
    if (key.isPressed()) {
      Minecraft mc = Minecraft.getMinecraft();
      cameraYaw = mc.thePlayer.rotationYaw;
      cameraPitch = mc.thePlayer.rotationPitch;
      enabled = !enabled;
      if (enabled) {
        mc.gameSettings.thirdPersonView = 1;
      } else {
        mc.gameSettings.thirdPersonView = 0;
      } 
    } 
    if (isToggled())
      if ((Minecraft.getMinecraft()).gameSettings.thirdPersonView <= 0) {
        (Minecraft.getMinecraft()).gameSettings.thirdPersonView = 0;
        enabled = !enabled;
      }  
  }
  
  public static boolean isToggled() {
    return enabled;
  }
}
