package me.kaimson.melonclient.ingames.utils;

import java.text.DecimalFormat;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.Player.PlayerHitEntityEvent;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.util.Vec3;

public class Reach extends Ingame {
  private static double reach;
  
  private static Double lastReach;
  
  public void init() {
    super.init();
    EventHandler.register(this);
  }
  
  public void render(IngameDisplay display, int x, int y) {
    if (this.mc.currentScreen instanceof me.kaimson.melonclient.gui.GuiHudEditor && reach == 0.0D) {
      lastReach = Double.valueOf(reach);
      reach = 2.5D;
    } else if (!(this.mc.currentScreen instanceof me.kaimson.melonclient.gui.GuiHudEditor) && lastReach != null) {
      reach = lastReach.doubleValue();
      lastReach = null;
    } 
    if (reach != 0.0D)
      Client.renderManager.renderIngame("Reach: " + (new DecimalFormat("#.##")).format(reach), display, x, y); 
  }
  
  @TypeEvent
  private void onHit(PlayerHitEntityEvent e) {
    if (e.getEntity() instanceof net.minecraft.client.entity.EntityOtherPlayerMP) {
      Vec3 vec3 = this.mc.getRenderViewEntity().getPositionEyes(1.0F);
      double hitRange = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
      reach = hitRange;
    } 
  }
}
