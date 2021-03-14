package me.kaimson.melonclient.blur;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.Map;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.GuiScreenEvent;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

public class BlurShader {
  private List<Shader> listShaders;
  
  private Map resourceManager;
  
  public boolean isActive;
  
  private long start;
  
  public BlurShader() {
    EventHandler.register(this);
  }
  
  @TypeEvent
  private void onGuiOpen(GuiScreenEvent.Open e) {
    if (!IngameDisplay.GUI_BLUR.isEnabled())
      return; 
    onGuiOpen();
    if ((Minecraft.getMinecraft()).theWorld != null);
  }
  
  @TypeEvent
  private void onGuiClosed(GuiScreenEvent.Close e) {
    onGuiClose();
  }
  
  public void onGuiOpen() {
    if (this.resourceManager == null)
      this.resourceManager = ((SimpleReloadableResourceManager)(Minecraft.getMinecraft()).mcResourceManager).domainResourceManagers; 
    if (!this.resourceManager.containsKey("melonclient"))
      this.resourceManager.put("melonclient", new BlurResourceManager()); 
    if (this.listShaders == null)
      this.listShaders = ShaderGroup.INSTANCE.listShaders; 
    if ((Minecraft.getMinecraft()).theWorld != null) {
      EntityRenderer er = (Minecraft.getMinecraft()).entityRenderer;
      if (!er.isShaderActive()) {
        er.loadShader(new ResourceLocation("melonclient", "shaders\\post\\fade_in_blur.json"));
        this.isActive = true;
        this.start = System.currentTimeMillis();
      } 
    } 
  }
  
  public void onGuiClose() {
    if ((Minecraft.getMinecraft()).theWorld != null) {
      EntityRenderer er = (Minecraft.getMinecraft()).entityRenderer;
      if (er.isShaderActive()) {
        er.stopUseShader();
        this.isActive = false;
      } 
    } 
  }
  
  public void onDrawWorldBackground(GuiScreen screen, int tint) {
    if ((Minecraft.getMinecraft()).theWorld != null) {
      int bg1 = getBackgroundColor(false);
      int bg2 = getBackgroundColor(true);
      GuiUtils.instance.drawGradientRect(0, 0, screen.width, screen.height, bg1, bg2);
    } else {
      screen.drawBackground(tint);
    } 
  }
  
  public void onRenderTick() {
    if ((Minecraft.getMinecraft()).currentScreen != null && (Minecraft.getMinecraft()).entityRenderer.isShaderActive() && this.isActive) {
      ShaderGroup sg = (Minecraft.getMinecraft()).entityRenderer.getShaderGroup();
      try {
        List<Shader> shaders = ((Minecraft.getMinecraft()).entityRenderer.getShaderGroup()).listShaders;
        for (Shader s : shaders) {
          ShaderUniform su = s.getShaderManager().getShaderUniform("Progress");
          if (su != null)
            su.set(getProgress()); 
        } 
      } catch (IllegalArgumentException exception) {
        Throwables.propagate(exception);
      } 
    } 
  }
  
  private float getProgress() {
    return Math.min((float)(System.currentTimeMillis() - this.start) / 10.0F, 1.0F);
  }
  
  public static int getBackgroundColor(boolean second) {
    int color = second ? -804253680 : -1072689136;
    int a = color >>> 24;
    int r = color >> 16 & 0xFF;
    int g = color >> 8 & 0xFF;
    int b = color & 0xFF;
    float progress = Client.blurShader.getProgress();
    a = (int)(a * progress);
    r = (int)(r * progress);
    g = (int)(g * progress);
    b = (int)(b * progress);
    return a << 24 | r << 16 | g << 8 | b;
  }
}
