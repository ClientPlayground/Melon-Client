package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorStatus {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public void render(IngameDisplay display, int x, int y) {
    GlStateManager.pushMatrix();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    RenderHelper.enableGUIStandardItemLighting();
    float scale = display.getScale();
    GlStateManager.scale(scale, scale, 1.0F);
    boolean flag = IngameDisplay.ARMOR_STATUS_HORIZONTAL.isEnabled();
    int j = 0;
    for (int i = 3; i > -1; i--) {
      if (this.mc.thePlayer.getCurrentArmor(i) != null) {
        ItemStack currentPeace = this.mc.thePlayer.getCurrentArmor(i);
        boolean hasNext = (i + 1 <= 3 && this.mc.thePlayer.getCurrentArmor(i + 1) != null);
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(currentPeace, x - 2, y - 2);
        if (IngameDisplay.ARMOR_STATUS_DURABILITY.isEnabled())
          this.mc.getRenderItem().renderItemOverlays(this.mc.fontRendererObj, currentPeace, x - 2, y - 2); 
        j++;
        x += flag ? 13 : 0;
        y += flag ? 0 : 13;
      } 
    } 
    display.setWidth(flag ? (j * 12 + 5) : 12);
    display.setHeight(flag ? 12 : (j * 12 + 5));
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableBlend();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
  }
}
