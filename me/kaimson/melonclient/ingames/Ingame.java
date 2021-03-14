package me.kaimson.melonclient.ingames;

import me.kaimson.melonclient.Client;
import net.minecraft.client.Minecraft;

public abstract class Ingame {
  protected final Minecraft mc = Minecraft.getMinecraft();
  
  public void init() {
    Client.log("Initializing " + getClass().getSimpleName() + "...");
  }
  
  public void patch() {}
}
