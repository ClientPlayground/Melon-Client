package me.kaimson.melonclient.utils;

import net.minecraft.client.renderer.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.gui.utils.*;

public class WatermarkRenderer
{
    public static void render(final int x, final int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Client.LOGO);
        GuiUtils.drawModalRectWithCustomSizedTexture(x - 158 - 6, y - 24 - 1, 0.0f, 0.0f, 77, 24, 163.0f, 24.0f);
        GuiUtils.setGlColor(Client.getMainColor(255));
        GuiUtils.drawModalRectWithCustomSizedTexture(x - 79 - 5, y - 24 - 2, 76.0f, 24.0f, 82, 24, 158.0f, 24.0f);
        GlStateManager.popMatrix();
    }
}
