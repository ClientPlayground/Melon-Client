package com.replaymod.lib.de.johni0702.minecraft.gui.versions;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import java.util.concurrent.Callable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReportCategory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class MCVer {
  public static Minecraft getMinecraft() {
    return Minecraft.func_71410_x();
  }
  
  public static ScaledResolution newScaledResolution(Minecraft mc) {
    return new ScaledResolution(mc);
  }
  
  public static void addDetail(CrashReportCategory category, String name, Callable<String> callable) {
    category.func_71500_a(name, callable);
  }
  
  public static void drawRect(int right, int bottom, int left, int top) {
    Tessellator tessellator = Tessellator.func_178181_a();
    WorldRenderer vertexBuffer = tessellator.func_178180_c();
    vertexBuffer.func_181668_a(7, DefaultVertexFormats.field_181705_e);
    vertexBuffer.func_181662_b(right, top, 0.0D).func_181675_d();
    vertexBuffer.func_181662_b(left, top, 0.0D).func_181675_d();
    vertexBuffer.func_181662_b(left, bottom, 0.0D).func_181675_d();
    vertexBuffer.func_181662_b(right, bottom, 0.0D).func_181675_d();
    tessellator.func_78381_a();
  }
  
  public static void invertColors(GuiRenderer guiRenderer, int right, int bottom, int left, int top) {
    if (left >= right || top >= bottom)
      return; 
    int x = guiRenderer.getOpenGlOffset().getX();
    int y = guiRenderer.getOpenGlOffset().getY();
    right += x;
    left += x;
    bottom += y;
    top += y;
    GlStateManager.func_179131_c(0.0F, 0.0F, 255.0F, 255.0F);
    GlStateManager.func_179090_x();
    GlStateManager.func_179115_u();
    GlStateManager.func_179116_f(5387);
    drawRect(right, bottom, left, top);
    GlStateManager.func_179134_v();
    GlStateManager.func_179098_w();
    GlStateManager.func_179131_c(255.0F, 255.0F, 255.0F, 255.0F);
  }
  
  public static void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr, ReadableColor bl, ReadableColor br) {
    Tessellator tessellator = Tessellator.func_178181_a();
    WorldRenderer vertexBuffer = tessellator.func_178180_c();
    vertexBuffer.func_181668_a(7, DefaultVertexFormats.field_181706_f);
    vertexBuffer.func_181662_b(x, (y + height), 0.0D).func_181669_b(bl.getRed(), bl.getGreen(), bl.getBlue(), bl.getAlpha()).func_181675_d();
    vertexBuffer.func_181662_b((x + width), (y + height), 0.0D).func_181669_b(br.getRed(), br.getGreen(), br.getBlue(), br.getAlpha()).func_181675_d();
    vertexBuffer.func_181662_b((x + width), y, 0.0D).func_181669_b(tr.getRed(), tr.getGreen(), tr.getBlue(), tr.getAlpha()).func_181675_d();
    vertexBuffer.func_181662_b(x, y, 0.0D).func_181669_b(tl.getRed(), tl.getGreen(), tl.getBlue(), tl.getAlpha()).func_181675_d();
    tessellator.func_78381_a();
  }
  
  public static FontRenderer getFontRenderer() {
    return (getMinecraft()).field_71466_p;
  }
  
  public static RenderGameOverlayEvent.ElementType getType(RenderGameOverlayEvent event) {
    return event.type;
  }
  
  public static float getPartialTicks(RenderGameOverlayEvent event) {
    return event.partialTicks;
  }
  
  public static float getPartialTicks(GuiScreenEvent.DrawScreenEvent.Post event) {
    return event.renderPartialTicks;
  }
  
  public static int getMouseX(GuiScreenEvent.DrawScreenEvent.Post event) {
    return event.mouseX;
  }
  
  public static int getMouseY(GuiScreenEvent.DrawScreenEvent.Post event) {
    return event.mouseY;
  }
  
  public static void setClipboardString(String text) {
    GuiScreen.func_146275_d(text);
  }
  
  public static String getClipboardString() {
    return GuiScreen.func_146277_j();
  }
}
