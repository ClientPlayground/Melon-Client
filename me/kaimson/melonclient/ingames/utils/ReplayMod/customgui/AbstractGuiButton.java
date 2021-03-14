package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiButton<T extends AbstractGuiButton<T>> extends AbstractGuiClickable<T> implements Clickable, IGuiButton<T> {
  protected static final ResourceLocation BUTTON_SOUND = new ResourceLocation("gui.button.press");
  
  protected static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");
  
  private String label;
  
  public AbstractGuiButton() {}
  
  public AbstractGuiButton(GuiContainer container) {
    super(container);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    renderer.bindTexture(WIDGETS_TEXTURE);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    byte texture = 1;
    int color = 14737632;
    if (!isEnabled()) {
      texture = 0;
      color = 10526880;
    } else if (isMouseHovering((ReadablePoint)new Point(renderInfo.mouseX, renderInfo.mouseY))) {
      texture = 2;
      color = 16777120;
    } 
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.blendFunc(770, 771);
    int textureY = 46 + texture * 20;
    int halfWidth = size.getWidth() / 2;
    int secondHalfWidth = size.getWidth() - halfWidth;
    renderer.drawTexturedRect(0, 0, 0, textureY, halfWidth, size.getHeight());
    renderer.drawTexturedRect(halfWidth, 0, 200 - secondHalfWidth, textureY, secondHalfWidth, size.getHeight());
    renderer.drawCenteredString(halfWidth, (size.getHeight() - 8) / 2, color, this.label, true);
  }
  
  public ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    return (ReadableDimension)new Dimension(fontRenderer.getStringWidth(this.label), 20);
  }
  
  public void onClick() {
    playClickSound(getMinecraft());
    super.onClick();
  }
  
  public static void playClickSound(Minecraft mc) {
    mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.create(BUTTON_SOUND, 1.0F));
  }
  
  public T setLabel(String label) {
    this.label = label;
    return getThis();
  }
  
  public T setI18nLabel(String label, Object... args) {
    return setLabel(I18n.format(label, args));
  }
  
  public String getLabel() {
    return this.label;
  }
}
