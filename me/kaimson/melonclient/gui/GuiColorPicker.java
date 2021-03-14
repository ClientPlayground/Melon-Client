package me.kaimson.melonclient.gui;

import java.awt.Color;
import java.io.IOException;
import java.nio.IntBuffer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.ButtonColor;
import me.kaimson.melonclient.gui.buttons.GuiSlider;
import me.kaimson.melonclient.gui.buttons.slider.ButtonAlphaSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.MathUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiColorPicker extends GuiScreen {
  private final GuiScreen parentScreen;
  
  private final IngameDisplay display;
  
  private final ResourceLocation COLOR_WHEEL = new ResourceLocation("melonclient/color_wheel.png");
  
  protected final int texture = 256;
  
  private ButtonColor buttonColor;
  
  private ButtonAlphaSlider buttonSlider;
  
  private int color;
  
  public GuiColorPicker(GuiScreen parentScreen, IngameDisplay display) {
    this.parentScreen = parentScreen;
    this.display = display;
  }
  
  public void initGui() {
    this.buttonList.add(this.buttonColor = new ButtonColor(this.width / 2 - 50, this.height / 2 - 128 + 256, 100, 20, "Select Color", this.parentScreen, this.display));
    this.buttonList.add(this.buttonSlider = new ButtonAlphaSlider(-1, this.width / 2 - 50, this.height / 2 - 128 + 256 + 22, 100, 20, 0.0F, 255.0F, 1.0F, 0.0F, this.display));
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
      Client.config.getCustoms().put(this.display, Integer.valueOf(this.color));
    } 
  }
  
  protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    if (!this.buttonSlider.isHovered() && !this.buttonColor.isHovered()) {
      this.color = getColorUnderMouse();
      Color c = new Color(this.color);
      Client.config.getCustoms().put(this.display, Integer.valueOf((new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)MathUtil.denormalizeValue(this.buttonSlider.sliderValue, 0.0F, 255.0F, 1.0F))).getRGB()));
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
