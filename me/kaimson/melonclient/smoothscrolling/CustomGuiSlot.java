package me.kaimson.melonclient.smoothscrolling;

import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;

public class CustomGuiSlot {
  public static void setScroller(GuiSlot list) {
    list.scroller = new GuiSlotScroller(list);
  }
  
  public static void setScrollAmount(GuiSlot list) {
    setScroller(list);
    list.scrollVelocity = 0.0D;
  }
  
  public static void renderScrollbar(GuiSlot list, int mouseX, int mouseY) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer buffer = tessellator.getWorldRenderer();
    int scrollbarPositionMinX = list.getScrollbarX();
    int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
    int maxScroll = list.func_148135_f();
    int contentHeight = list.contentHeight();
    if (maxScroll > 0) {
      int height = (list.bottom - list.top) * (list.bottom - list.top) / contentHeight;
      height = MathHelper.clamp_int(height, 32, list.bottom - list.top - 8);
      height -= (int)Math.min((list.amountScrolled < 0.0D) ? (int)-list.amountScrolled : ((list.amountScrolled > list.func_148135_f()) ? ((int)list.amountScrolled - list.func_148135_f()) : false), height * 0.75D);
      int minY = Math.min(Math.max(list.getAmountScrolled() * (list.bottom - list.top - height) / maxScroll + list.top, list.top), list.bottom - height);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, list.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, list.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, list.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, list.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, (minY + height), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, (minY + height), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, minY, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, (minY + height - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos((scrollbarPositionMaxX - 1), minY, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
      tessellator.draw();
    } 
    list.func_148142_b_c(mouseX, mouseY);
    GlStateManager.enableTexture2D();
    GlStateManager.shadeModel(7424);
    GlStateManager.enableAlpha();
    GlStateManager.disableBlend();
  }
  
  public static void mouseScrolled(GuiSlot list, double amount) {
    setScroller(list);
    double scrollVelocity = list.scrollVelocity;
    GuiSlotScroller scroller = list.scroller;
    double velo = list.scrollVelocity;
    RunSixtyTimesEverySec sec = list.scroller;
    if (list.amountScrolled <= list.func_148135_f() && amount < 0.0D)
      velo += 16.0D; 
    if (list.amountScrolled >= 0.0D && amount > 0.0D)
      velo -= 16.0D; 
    list.scrollVelocity = velo;
    if (!sec.isRegistered())
      sec.registerTick(); 
  }
}
