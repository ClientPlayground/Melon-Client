package me.kaimson.melonclient.gui.buttons;

import java.util.function.Consumer;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonSettings extends GuiButton {
  private final IngameDisplay display;
  
  public IngameDisplay getDisplay() {
    return this.display;
  }
  
  private final ResourceLocation ICON = new ResourceLocation("melonclient/settings-icon.png");
  
  private final int textureWidth = 32;
  
  private final int textureHeight = 32;
  
  public ButtonSettings(int x, int y, IngameDisplay display, Consumer<GuiButton> runnable) {
    super(-1, x, y, 8, 8, "", runnable);
    this.display = display;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(Minecraft mc, int xPosition, int yPosition, int mouseX, int mouseY) {
    if (this.visible) {
      this.hovered = (mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + this.width && mouseY < yPosition + this.height);
      mc.getTextureManager().bindTexture(this.ICON);
      float scale = 0.3F;
      GlStateManager.scale(scale, scale, 1.0F);
      drawModalRectWithCustomSizedTexture((int)(xPosition / scale), (int)(yPosition / scale), 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
      GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    } 
  }
}
