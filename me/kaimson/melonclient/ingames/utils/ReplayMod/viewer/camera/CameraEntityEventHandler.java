package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera;

import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.EntityViewRenderEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import net.minecraft.client.Minecraft;

public class CameraEntityEventHandler {
  private final CameraEntity cameraEntity;
  
  public CameraEntityEventHandler(CameraEntity cameraEntity) {
    this.cameraEntity = cameraEntity;
  }
  
  @TypeEvent
  public void onClientTick(TickEvent.ClientTick event) {
    if (event.phase == TickEvent.Phase.START) {
      this.cameraEntity.update();
      this.cameraEntity.updateArmYawAndPitch();
    } 
  }
  
  @TypeEvent
  public void onRenderTick(TickEvent.RenderTick event) {
    if (event.phase == TickEvent.Phase.START)
      this.cameraEntity.update(); 
  }
  
  @TypeEvent
  public void onEntityViewRenderEvent(EntityViewRenderEvent.CameraSetup event) {
    if (Minecraft.getMinecraft().getRenderViewEntity() == this.cameraEntity)
      event.roll = this.cameraEntity.roll; 
  }
}
