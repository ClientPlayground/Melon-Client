package me.kaimson.melonclient.gui.elements.settings.color;

import net.minecraft.client.*;
import me.kaimson.melonclient.*;
import net.minecraft.client.renderer.vertex.*;
import me.kaimson.melonclient.gui.utils.*;
import org.lwjgl.opengl.*;
import java.awt.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;

public class AlphaPane extends SettingElementPane
{
    public int alpha;
    
    public AlphaPane(final int x, final int y, final int width, final int height) {
        super(x, y, width, height, 0, 255);
        this.alpha = 255;
    }
    
    public void renderPane(final int rgb, final int mouseX, final int mouseY) {
        this.renderPane(mouseX, mouseY);
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().getTextureManager().bindTexture(Client.TRANSPARENT);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double)this.getX(), (double)(this.getY() + this.height), 0.0).tex(0.0, 4.0).endVertex();
        worldRenderer.pos((double)(this.getX() + this.width), (double)(this.getY() + this.height), 0.0).tex(1.25, 4.0).endVertex();
        worldRenderer.pos((double)(this.getX() + this.width), (double)this.getY(), 0.0).tex(1.25, 0.0).endVertex();
        worldRenderer.pos((double)this.getX(), (double)this.getY(), 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
        GLRectUtils.drawGradientRect(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, rgb | 0xFF000000, rgb | 0xFF000000, rgb, rgb, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glLineWidth(1.0f);
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double)this.getX(), (double)(this.getY() + this.height - this.alpha * this.height / this.max), 0.0).endVertex();
        worldRenderer.pos((double)(this.getX() + this.width), (double)(this.getY() + this.height - this.alpha * this.height / this.max), 0.0).endVertex();
        tessellator.draw();
        GLRectUtils.drawRoundedOutline(this.getX() - 0.5f, this.getY() - 0.2f, this.getX() + this.width + 0.4f, this.getY() + this.height + 0.4f, 2.0f, 1.5f, new Color(200, 200, 200, 255).getRGB());
    }
    
    @Override
    public void dragging(final int mouseX, final int mouseY) {
        this.alpha = MathHelper.clamp_int((this.height - (mouseY - this.getY())) * this.max / this.height, this.min, this.max);
    }
}
