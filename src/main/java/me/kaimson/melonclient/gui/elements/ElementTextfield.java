package me.kaimson.melonclient.gui.elements;

import me.kaimson.melonclient.gui.*;
import java.util.function.*;
import me.kaimson.melonclient.*;
import net.minecraft.client.renderer.*;
import me.kaimson.melonclient.gui.utils.*;
import java.awt.*;
import net.minecraft.util.*;

public class ElementTextfield extends Element
{
    private boolean focused;
    private final String placeholder;
    private String text;
    private int cursorPos;
    private final ResourceLocation SEARCH;
    
    public ElementTextfield(final int x, final int y, final int width, final int height, final String placeholder, final GuiScreen parent) {
        super(x, y, width, height, false, null, parent);
        this.text = "";
        this.SEARCH = new ResourceLocation("melonclient/icons/search.png");
        this.placeholder = placeholder;
    }
    
    @Override
    public void renderElement(final float partialTicks) {
        if (this.text != null) {
            Client.titleRenderer2.drawString(this.text, this.getX() + 2, this.getY(), 16777215);
        }
        if (this.focused && this.cursorPos != 0) {
            final float width = Client.titleRenderer2.getWidth(this.text.substring(0, this.cursorPos));
            GLRectUtils.drawRect(this.getX() + width + 2.0f, this.getY(), this.getX() + width + 3.0f, this.getY() + this.height, Integer.MAX_VALUE);
        }
        if (this.placeholder != null && !this.placeholder.isEmpty() && (this.text == null || this.text.isEmpty())) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.5f, 0.5f, 0.5f, 1.0f);
            this.mc.getTextureManager().bindTexture(this.SEARCH);
            final int b = 9;
            GuiUtils.drawModalRectWithCustomSizedTexture(this.getX() + 1, this.getY() + 1, 0.0f, 0.0f, b, b, (float)b, (float)b);
            GuiUtils.drawRect(this.getX() + 11, this.getY() + 1, this.getX() + 12, this.getY() + this.height - 1, new Color(150, 150, 150, 100).getRGB());
            Client.titleRenderer2.drawString(this.placeholder, this.getX() + 14, this.getY(), this.hovered ? new Color(175, 175, 175, 110).getRGB() : new Color(150, 150, 150, 100).getRGB());
            GlStateManager.popMatrix();
        }
    }
    
    @Override
    public void renderBackground(final float partialTicks) {
        GLRectUtils.drawRoundedRect(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 2.0f, this.hovered ? new Color(0, 0, 0, 150).getRGB() : new Color(0, 0, 0, 100).getRGB());
        if (this.focused) {
            GLRectUtils.drawRoundedOutline(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 2.0f, 2.0f, Client.getMainColor(125));
        }
    }
    
    @Override
    public void keyTyped(final char typedChar, final int keyCode) {
        if (this.focused) {
            if (keyCode == 14) {
                if (!this.text.isEmpty()) {
                    if (this.text.length() == 1) {
                        this.text = "";
                    }
                    else {
                        this.text = this.text.substring(0, this.text.length() - 1);
                    }
                    --this.cursorPos;
                }
            }
            else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                ++this.cursorPos;
                this.text += typedChar;
            }
        }
    }
    
    @Override
    public boolean mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        this.focused = (this.enabled && this.hovered);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean isFocused() {
        return this.focused;
    }
    
    public String getText() {
        return this.text;
    }
}
