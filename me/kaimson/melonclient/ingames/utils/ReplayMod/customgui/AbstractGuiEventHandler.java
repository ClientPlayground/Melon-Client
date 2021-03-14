package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class AbstractGuiEventHandler {
  private final AbstractGuiOverlay<?> overlay;
  
  private MinecraftGuiRenderer renderer;
  
  public AbstractGuiEventHandler(AbstractGuiOverlay<?> overlay) {
    this.overlay = overlay;
  }
  
  @TypeEvent
  public void renderOverlay(TickEvent.RenderTick event) {
    if ((Minecraft.getMinecraft()).currentScreen != null && !((Minecraft.getMinecraft()).currentScreen instanceof AbstractGuiOverlay.UserInputGuiScreen))
      return; 
    float partialTicks = event.renderTickTime;
    updateRenderer();
    int layers = this.overlay.getMaxLayer();
    int mouseX = -1;
    int mouseY = -1;
    if (this.overlay.mouseVisible) {
      Point mouse = MouseUtils.getMousePos();
      mouseX = mouse.getX();
      mouseY = mouse.getY();
    } 
    RenderInfo renderInfo = new RenderInfo(partialTicks, mouseX, mouseY, 0);
    int layer;
    for (layer = 0; layer <= layers; layer++)
      this.overlay.layout((ReadableDimension)this.overlay.screenSize, renderInfo.layer(layer)); 
    for (layer = 0; layer <= layers; layer++)
      this.overlay.draw(this.renderer, (ReadableDimension)this.overlay.screenSize, renderInfo.layer(layer)); 
  }
  
  @TypeEvent
  public void tickOverlay(TickEvent.ClientTick event) {
    if (event.phase == TickEvent.Phase.START)
      ((Tickable)this.overlay.forEach(Tickable.class)).tick(); 
  }
  
  private void updateRenderer() {
    Minecraft mc = Minecraft.getMinecraft();
    ScaledResolution res = new ScaledResolution(mc);
    if (this.overlay.screenSize == null || this.overlay.screenSize.getWidth() != res.getScaledWidth() || this.overlay.screenSize.getHeight() != res.getScaledHeight()) {
      this.overlay.screenSize = new Dimension(res.getScaledWidth(), res.getScaledWidth());
      this.renderer = new MinecraftGuiRenderer(res);
    } 
  }
}
