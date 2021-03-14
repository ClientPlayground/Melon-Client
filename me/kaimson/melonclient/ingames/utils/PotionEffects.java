package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionEffects {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");
  
  public void render(IngameDisplay display, int x, int y) {
    if (this.mc.thePlayer.getActivePotionEffects() == null)
      return; 
    float scale = display.getScale();
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1.0F);
    GlStateManager.enableTexture2D();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.enableBlend();
    display.setWidth(getWidth());
    display.setHeight(getHeight());
    int j = y;
    int l = 33;
    for (PotionEffect potionEffect : this.mc.thePlayer.getActivePotionEffects()) {
      Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(this.inventoryBackground);
      if (potion.hasStatusIcon()) {
        int i1 = potion.getStatusIconIndex();
        GuiUtils.instance.drawTexturedModalRect(x + 6, j + 7, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
      } 
      String s1 = I18n.format(potion.getName(), new Object[0]);
      switch (potionEffect.getAmplifier()) {
        case 1:
          s1 = s1 + " " + I18n.format("enchantment.level.2", new Object[0]);
          break;
        case 2:
          s1 = s1 + "  " + I18n.format("enchantment.level.3", new Object[0]);
          break;
        case 3:
          s1 = s1 + " " + I18n.format("enchantment.level.4", new Object[0]);
          break;
      } 
      String s = Potion.getDurationString(potionEffect);
      GuiUtils.drawString(s1, x + 10 + 18, j + 6, 16777215, IngameDisplay.POTION_EFFECTS_SHADOW.isEnabled());
      GuiUtils.drawString(s, x + 10 + 18, j + 6 + 10, 8355711, IngameDisplay.POTION_EFFECTS_SHADOW.isEnabled());
      j += l;
    } 
    GlStateManager.disableBlend();
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
  }
  
  private int getWidth() {
    if (this.mc.thePlayer.getActivePotionEffects().size() < 1)
      return 94; 
    int length = 0;
    for (PotionEffect potionEffect : this.mc.thePlayer.getActivePotionEffects()) {
      Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
      String s1 = I18n.format(potion.getName(), new Object[0]);
      if (potionEffect.getAmplifier() == 1) {
        s1 = s1 + " " + I18n.format("enchantment.level.2", new Object[0]);
      } else if (potionEffect.getAmplifier() == 2) {
        s1 = s1 + " " + I18n.format("enchantment.level.3", new Object[0]);
      } else if (potionEffect.getAmplifier() == 3) {
        s1 = s1 + " " + I18n.format("enchantment.level.4", new Object[0]);
      } 
      if (this.mc.fontRendererObj.getStringWidth(s1) > length)
        length = this.mc.fontRendererObj.getStringWidth(s1); 
    } 
    return length + 10 + 18;
  }
  
  private int getHeight() {
    if (this.mc.thePlayer.getActivePotionEffects().size() < 1)
      return 102; 
    int j = 0;
    int l = 33;
    j += l * this.mc.thePlayer.getActivePotionEffects().size();
    return j;
  }
}
