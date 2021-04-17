package me.kaimson.melonclient.gui.settings;

import me.kaimson.melonclient.features.*;
import java.util.*;
import me.kaimson.melonclient.gui.*;
import me.kaimson.melonclient.gui.utils.*;
import me.kaimson.melonclient.*;
import java.awt.*;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.*;
import me.kaimson.melonclient.config.*;
import me.kaimson.melonclient.utils.*;

public class GuiModuleSettings extends SettingsBase
{
    private final Module module;
    private int row;
    private int column;
    
    public GuiModuleSettings(final Module module, final net.minecraft.client.gui.GuiScreen parentScreen) {
        super(parentScreen);
        this.module = module;
    }
    
    @Override
    public void initGui() {
        this.row = 1;
        this.column = 1;
        this.elements.clear();
        this.components.clear();
        this.module.settings.forEach(setting -> {
            this.addSetting(setting, this.width / 2 - this.getWidth() / 2 + 35, (int)this.getRowHeight(this.row, 17));
            ++this.column;
            if (this.column > 1) {
                this.column = 1;
                ++this.row;
            }
            return;
        });
        super.initGui();
        this.registerScroll(new GuiModules.Scroll(this.module.settings, this.width, this.height, this.height / 2 - this.getHeight() / 2 + 20, this.height / 2 + this.getHeight() / 2, 17, this.width / 2 + this.getWidth() / 2 - 4, 1));
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawBackground();
        int offset = 0;
        if (this.module.hasIcon()) {
            this.mc.getTextureManager().bindTexture(this.module.getIcon());
            final int b = ((this.module.getTextureIndex() != -1) ? this.module.getTextureIndex() : 24) - 4;
            GuiUtils.drawModalRectWithCustomSizedTexture(this.width / 2 - this.getWidth() / 2 + 25 + (20 - b) / 2, this.height / 2 - this.getHeight() / 2 + (20 - b) / 2, 0.0f, 0.0f, b, b, (float)b, (float)b);
            offset = 16;
        }
        Client.titleRenderer2.drawString(this.module.displayName.toUpperCase(), this.width / 2.0f - this.getWidth() / 2.0f + 30.0f + offset, this.height / 2.0f - this.getHeight() / 2.0f + 5.0f, new Color(200, 200, 200, 200).getRGB());
        GuiUtils.drawRect(this.width / 2 - this.getWidth() / 2 + 18, this.height / 2 - this.getHeight() / 2 + 19, this.width / 2 + this.getWidth() / 2, this.height / 2 - this.getHeight() / 2 + 20, new Color(100, 100, 100, 100).getRGB());
        final ScaledResolution sr = new ScaledResolution(this.mc);
        final int x = (this.width / 2 - this.getWidth() / 2) * sr.getScaleFactor();
        final int y = (this.height / 2 - this.getHeight() / 2 + 1) * sr.getScaleFactor();
        final int xWidth = (this.width / 2 + this.getWidth() / 2) * sr.getScaleFactor() - x;
        final int yHeight = (this.height / 2 + this.getHeight() / 2 - 20) * sr.getScaleFactor() - y;
        this.scissorFunc(sr, x, y, xWidth, yHeight);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(3089);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ModuleConfig.INSTANCE.saveConfig();
    }
    
    private double getRowHeight(double row, final int buttonHeight) {
        --row;
        return this.height / 2.0f - this.getHeight() / 2.0f + 25.0f + row * buttonHeight;
    }
}
