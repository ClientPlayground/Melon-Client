package me.kaimson.melonclient.gui.elements;

import net.minecraft.util.*;
import java.util.function.*;
import net.minecraft.client.renderer.*;
import java.awt.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.gui.utils.*;

public class ElementButtonIcon extends Element
{
    protected final ResourceLocation ICON;
    private final int textureIndex;
    private double backgroundFade;
    
    public ElementButtonIcon(final int width, final int height, final String iconLocation, final Consumer<Element> consumer) {
        this(0, 0, width, height, iconLocation, -1, false, consumer);
    }
    
    public ElementButtonIcon(final int x, final int y, final int width, final int height, final String iconLocation, final int textureIndex, final Consumer<Element> consumer) {
        this(x, y, width, height, iconLocation, textureIndex, false, consumer);
    }
    
    public ElementButtonIcon(final int x, final int y, final int width, final int height, final String iconLocation, final int textureIndex, final boolean shouldScissor, final Consumer<Element> consumer) {
        super(x, y, width, height, shouldScissor, consumer);
        this.textureIndex = textureIndex;
        this.ICON = new ResourceLocation("melonclient/" + iconLocation);
    }
    
    @Override
    public void renderElement(final float partialTicks) {
        this.mc.getTextureManager().bindTexture(this.ICON);
        final int b = 8;
        GlStateManager.enableBlend();
        GuiUtils.setGlColor(new Color(0, 0, 0, 75).getRGB());
        GuiUtils.drawScaledCustomSizeModalRect(this.getX() + (this.width - b) / 2.0f + 0.75f, this.getY() + (this.height - b) / 2.0f + 0.75f, 0.0f, 0.0f, b, b, b, b, b, b);
        GuiUtils.setGlColor(this.enabled ? (this.hovered ? new Color(255, 255, 255, 255).getRGB() : Client.getMainColor(255)) : new Color(120, 120, 120, 255).getRGB());
        GuiUtils.drawScaledCustomSizeModalRect(this.getX() + (this.width - b) / 2, this.getY() + (this.height - b) / 2, 0.0f, 0.0f, b, b, b, b, (float)b, (float)b);
    }
    
    @Override
    public void renderBackground(final float partialTicks) {
        if (this.backgroundFade > 0.0 && this.enabled) {
            GLRectUtils.drawRoundedRect(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 3.0f, Client.getMainColor((int)this.backgroundFade));
        }
    }
    
    @Override
    public void update() {
        if (this.hovered && this.backgroundFade + 12.25 <= 255.0) {
            this.backgroundFade += 12.25;
        }
        else if (!this.hovered && this.backgroundFade - 12.25 >= 0.0) {
            this.backgroundFade -= 12.25;
        }
    }
}
