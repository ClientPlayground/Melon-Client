package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.Player.PlayerDamagedEvent;
import me.kaimson.melonclient.Events.imp.Player.PlayerHitEntityEvent;
import me.kaimson.melonclient.Events.imp.WorldEvent;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.IngameDisplay;

public class Combo extends Ingame {
  private static int combo;
  
  private static Integer lastCombo;
  
  public void init() {
    super.init();
    EventHandler.register(this);
  }
  
  public void render(IngameDisplay display, int x, int y) {
    if (this.mc.currentScreen instanceof me.kaimson.melonclient.gui.GuiHudEditor && combo == 0) {
      lastCombo = Integer.valueOf(combo);
      combo = 1;
    } else if (!(this.mc.currentScreen instanceof me.kaimson.melonclient.gui.GuiHudEditor) && lastCombo != null) {
      combo = lastCombo.intValue();
      lastCombo = null;
    } 
    if (combo != 0)
      Client.renderManager.renderIngame("Combo: " + combo, display, x, y); 
  }
  
  @TypeEvent
  private void onPlayerHit(PlayerHitEntityEvent e) {
    if (e.getEntity() instanceof net.minecraft.client.entity.EntityOtherPlayerMP)
      combo++; 
  }
  
  @TypeEvent
  private void onDamage(PlayerDamagedEvent e) {
    combo = 0;
  }
  
  @TypeEvent
  private void onWorldChange(WorldEvent.Change e) {
    combo = 0;
  }
}
