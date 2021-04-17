package me.kaimson.melonclient.gui;

import net.minecraft.client.gui.*;
import net.minecraft.util.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.*;
import java.awt.*;
import net.minecraft.client.renderer.*;
import me.kaimson.melonclient.gui.utils.*;

public class GuiButtonIcon extends GuiButton
{
    private final ResourceLocation ICON;
    
    public GuiButtonIcon(final int buttonId, final int x, final int y, final int width, final int height, final String iconName) {
        super(buttonId, x, y, width, height, "");
        this.ICON = new ResourceLocation("melonclient/icons/" + iconName);
    }
    
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.visible) {
            this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
            GLRectUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 3.0f, 2.0f, this.enabled ? (this.hovered ? Client.getMainColor(255) : Client.getMainColor(150)) : Client.getMainColor(100));
            GLRectUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 3.0f, this.enabled ? (this.hovered ? new Color(0, 0, 0, 100).getRGB() : new Color(30, 30, 30, 100).getRGB()) : new Color(70, 70, 70, 50).getRGB());
            mc.getTextureManager().bindTexture(this.ICON);
            final int b = 10;
            GlStateManager.enableBlend();
            GuiUtils.setGlColor(new Color(0, 0, 0, 75).getRGB());
            GuiUtils.drawScaledCustomSizeModalRect(this.xPosition + (this.width - b) / 2.0f + 0.75f, this.yPosition + (this.height - b) / 2.0f + 0.75f, 0.0f, 0.0f, b, b, b, b, b, b);
            GuiUtils.setGlColor(Client.getMainColor(255));
            GuiUtils.drawScaledCustomSizeModalRect(this.xPosition + (this.width - b) / 2, this.yPosition + (this.height - b) / 2, 0.0f, 0.0f, b, b, b, b, (float)b, (float)b);
        }
    }
}
