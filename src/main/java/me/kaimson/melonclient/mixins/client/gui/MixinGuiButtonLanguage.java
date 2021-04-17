package me.kaimson.melonclient.mixins.client.gui;

import net.minecraft.client.gui.*;
import net.minecraft.util.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.*;

import java.awt.*;

import me.kaimson.melonclient.gui.utils.*;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.*;

@Mixin(GuiButtonLanguage.class)
public abstract class MixinGuiButtonLanguage extends GuiButton {
    private final ResourceLocation language = new ResourceLocation("melonclient/icons/language.png");

    public MixinGuiButtonLanguage(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    /**
     * @author Kaimson the Clown
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
            GLRectUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 3.0f, 2.0f, this.enabled ? (this.hovered ? Client.getMainColor(255) : Client.getMainColor(150)) : Client.getMainColor(100));
            GLRectUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 3.0f, this.enabled ? (this.hovered ? new Color(0, 0, 0, 100).getRGB() : new Color(30, 30, 30, 100).getRGB()) : new Color(70, 70, 70, 50).getRGB());
            mc.getTextureManager().bindTexture(this.language);
            GuiUtils.setGlColor(Client.getMainColor(255));
            GlStateManager.enableBlend();
            int b = 12;
            GuiUtils.drawModalRectWithCustomSizedTexture(this.xPosition + b / 3f, this.yPosition + b / 3f - 0.5f, 0.0f, 0.0f, b, b, b, b);
        }
    }
}
