package me.kaimson.melonclient.ingames.utils.itemphysics;

import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.ItemRenderEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.entity.Entity;

public class ItemPhysics {
  public static final ItemPhysics INSTANCE = new ItemPhysics();
  
  private final ClientSide client = new ClientSide();
  
  public void init() {
    EventHandler.register(this);
  }
  
  @TypeEvent
  private void onItemRender(ItemRenderEvent e) {
    if (!IngameDisplay.ITEM_PHYSICS.isEnabled())
      return; 
    this.client.doRender((Entity)e.getEntity(), e.getX(), e.getY(), e.getZ());
    e.cancel();
  }
  
  @TypeEvent
  private void onRenderTick(TickEvent.RenderTick e) {
    if (IngameDisplay.ITEM_PHYSICS.isEnabled() && e.phase == TickEvent.Phase.END)
      this.client.tick = System.nanoTime(); 
  }
}
