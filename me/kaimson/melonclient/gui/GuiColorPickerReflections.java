package me.kaimson.melonclient.gui;

import java.io.IOException;
import java.nio.IntBuffer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.GuiSlider;
import me.kaimson.melonclient.gui.buttons.reflection.ButtonAlphaSliderReflection;
import me.kaimson.melonclient.gui.buttons.reflection.ButtonColorReflection;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiColorPickerReflections extends GuiScreen {
  private final GuiScreen parentScreen;
  
  private final IngameDisplay display;
  
  private final IngameDisplay setting;
  
  private final ResourceLocation COLOR_WHEEL = new ResourceLocation("melonclient/color_wheel.png");
  
  private final int texture = 256;
  
  private ButtonColorReflection buttonColor;
  
  private ButtonAlphaSliderReflection buttonSlider;
  
  protected int color;
  
  public GuiColorPickerReflections(GuiScreen parentScreen, IngameDisplay display, IngameDisplay setting) {
    this.parentScreen = parentScreen;
    this.display = display;
    this.setting = setting;
  }
  
  public void initGui() {
    this.buttonList.add(this.buttonColor = new ButtonColorReflection(this.width / 2 - 50, this.height / 2 - 128 + 256, 100, 20, this.parentScreen, this.display, this.setting));
    this.buttonList.add(this.buttonSlider = new ButtonAlphaSliderReflection(-1, this.width / 2 - 50, this.height / 2 - 128 + 256 + 22, 100, 20, 0.0F, 255.0F, 1.0F, 0.0F, this.display, this.setting));
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    for (GuiButton button : this.buttonList) {
      if (button instanceof GuiSlider)
        ((GuiSlider)button).render(this.mc, button.yPosition, mouseX, mouseY); 
    } 
    this.mc.getTextureManager().bindTexture(this.COLOR_WHEEL);
    drawTexturedModalRect(this.width / 2 - 128, this.height / 2 - 128, 256, 256, 256, 256);
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (!this.buttonSlider.isHovered() && !this.buttonColor.isHovered()) {
      this.color = getColorUnderMouse();
      Client.config.setReflectedObject(this.setting.name().toLowerCase(), Integer.valueOf(this.color), this.display);
    } 
  }
  
  protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    if (!this.buttonSlider.isHovered() && !this.buttonColor.isHovered()) {
      this.color = getColorUnderMouse();
      Client.config.setReflectedObject(this.setting.name().toLowerCase(), Integer.valueOf(this.color), this.display);
    } 
  }
  
  private int getColorUnderMouse() {
    IntBuffer buffer = BufferUtils.createIntBuffer(1);
    int[] ints = new int[1];
    GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, 32993, 33639, buffer);
    buffer.get(ints);
    return ints[0];
  }
}
