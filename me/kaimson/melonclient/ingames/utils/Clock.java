package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class Clock {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public void render(IngameDisplay display, int x, int y) {
    GlStateManager.pushMatrix();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    RenderHelper.enableGUIStandardItemLighting();
    float scale = display.getScale();
    display.setWidth(15);
    display.setHeight(15);
    GlStateManager.scale(scale, scale, 1.0F);
    ItemStack clock = new ItemStack(Items.clock);
    x--;
    y--;
    this.mc.getRenderItem().renderItemAndEffectIntoGUI(clock, x, y);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableBlend();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
  }
}
