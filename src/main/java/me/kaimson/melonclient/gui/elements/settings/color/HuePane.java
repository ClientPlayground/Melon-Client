package me.kaimson.melonclient.gui.elements.settings.color;

import me.kaimson.melonclient.gui.utils.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.renderer.vertex.*;
import java.awt.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;

public class HuePane extends SettingElementPane
{
    public int hue;
    
    public HuePane(final int x, final int y, final int width, final int height) {
        super(x, y, width, height, 0, 360);
    }
    
    @Override
    public void renderPane(final int mouseX, final int mouseY) {
        super.renderPane(mouseX, mouseY);
        for (int i = 0; i < 64; i += 8) {
            final int c1 = GuiUtils.hsvToRgb(i * 360 / 64, 100, 100) | 0xFF000000;
            final int c2 = GuiUtils.hsvToRgb((i + 8) * 360 / 64, 100, 100) | 0xFF000000;
            GLRectUtils.drawGradientRect(this.getX(), this.getY() + i / 8 * 6, this.getX() + this.width, this.getY() + i / 8 * 6 + 6, c1, c1, c2, c2, 0);
        }
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glLineWidth(1.0f);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double)this.x, (double)(this.y + this.hue * this.height / this.max), 0.0).endVertex();
        worldRenderer.pos((double)(this.x + this.width), (double)(this.y + this.hue * this.height / this.max), 0.0).endVertex();
        tessellator.draw();
        GLRectUtils.drawRoundedOutline(this.getX() - 0.5f, this.getY() - 0.2f, this.getX() + this.width + 0.4f, this.getY() + this.height + 0.4f, 2.0f, 1.5f, new Color(200, 200, 200, 255).getRGB());
    }
    
    @Override
    public void dragging(final int mouseX, final int mouseY) {
        this.hue = MathHelper.clamp_int((mouseY - this.y) * this.max / this.height, this.min, this.max);
    }
}
