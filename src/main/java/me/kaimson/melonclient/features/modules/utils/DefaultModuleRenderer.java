package me.kaimson.melonclient.features.modules.utils;

import me.kaimson.melonclient.features.*;
import java.awt.*;
import me.kaimson.melonclient.gui.settings.*;
import me.kaimson.melonclient.utils.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.gui.utils.*;

public abstract class DefaultModuleRenderer extends IModuleRenderer
{
    private int width;
    private int height;
    private final Setting background;
    private final Setting backgroundColor;
    private final Setting backgroundWidth;
    private final Setting backgroundHeight;
    private final Setting textShadow;
    private final Setting textColor;
    
    public DefaultModuleRenderer(final String displayName) {
        this(displayName, -1);
    }
    
    public DefaultModuleRenderer(final String displayName, final int textureIndex) {
        super(displayName, textureIndex);
        new Setting(this, "General Options");
        this.background = new Setting(this, "Background").setDefault(true);
        this.backgroundColor = new Setting(this, "Background Color").setDefault(new Color(0, 0, 0, 75).getRGB(), 0);
        this.backgroundWidth = new Setting(this, "Background Width").setDefault(5).setRange(2, 20, 1);
        this.backgroundHeight = new Setting(this, "Background Height").setDefault(5).setRange(2, 12, 1);
        this.textShadow = new Setting(this, "Text Shadow").setDefault(false);
        this.textColor = new Setting(this, "Text Color").setDefault(new Color(255, 255, 255).getRGB(), 0);
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public void render(final float x, final float y) {
        if (this.getValue() != null || (this.mc.currentScreen instanceof GuiHUDEditor && this.getDummy() != null)) {
            final String text = this.getFormat().replace("%value%", String.valueOf((this.getValue() != null) ? this.getValue() : this.getDummy()));
            this.width = this.mc.fontRendererObj.getStringWidth(text);
            this.height = this.mc.fontRendererObj.FONT_HEIGHT;
            if (this.background.getBoolean()) {
                this.width += this.backgroundWidth.getInt() * 2;
                this.height += this.backgroundHeight.getInt() * 2 - 2;
                this.renderBackground(x + this.width / 2.0f, y + this.height / 2.0f - this.mc.fontRendererObj.FONT_HEIGHT / 2.0f);
                this.drawCenteredString(text, x + this.width / 2.0f, y + this.height / 2.0f - (this.mc.fontRendererObj.FONT_HEIGHT - 2) / 2.0f, this.textColor.getColorObject(), this.textShadow.getBoolean());
            }
            else {
                ++this.width;
                this.drawString(text, x + 1.0f, y + 1.0f, this.textColor.getColorObject(), this.textShadow.getBoolean());
            }
        }
    }
    
    protected void drawString(final String text, final float xIn, final float y, final ColorObject colorObject, final boolean shadow) {
        if (colorObject.isChroma()) {
            float x = xIn;
            for (final char c : text.toCharArray()) {
                final long dif = (long)(x * 10.0f - y * 10.0f);
                final long l = System.currentTimeMillis() - dif;
                final float chromaSpeed = (100 - colorObject.getChromaSpeed()) * 100;
                FontUtils.drawString(String.valueOf(c), x, y, Color.HSBtoRGB(l % (int)chromaSpeed / chromaSpeed, 0.8f, 0.8f), shadow);
                x += this.mc.fontRendererObj.getStringWidth(String.valueOf(c));
            }
        }
        else {
            FontUtils.drawString(text, xIn, y, colorObject.getColor(), shadow);
        }
    }
    
    protected void drawCenteredString(final String text, final float x, final float y, final ColorObject colorObject, final boolean shadow) {
        this.drawString(text, x - this.mc.fontRendererObj.getStringWidth(String.valueOf(text)) / 2.0f, y, colorObject, shadow);
    }
    
    protected void renderBackground(final float x, final float y) {
        GLRectUtils.drawRect(x - this.width / 2, y - this.height / 2 + this.mc.fontRendererObj.FONT_HEIGHT / 2, x + this.width / 2, y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + this.height / 2 - this.mc.fontRendererObj.FONT_HEIGHT / 2, this.backgroundColor.getColor());
    }
    
    public String getFormat() {
        return "[%value% " + this.displayName + "]";
    }
    
    public abstract Object getValue();
    
    public Object getDummy() {
        return null;
    }
}
