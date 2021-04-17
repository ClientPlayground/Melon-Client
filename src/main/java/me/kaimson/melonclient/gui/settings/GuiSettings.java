package me.kaimson.melonclient.gui.settings;

import me.kaimson.melonclient.features.*;
import java.util.*;
import me.kaimson.melonclient.gui.*;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.*;
import me.kaimson.melonclient.config.*;
import me.kaimson.melonclient.utils.*;

public class GuiSettings extends SettingsBase
{
    private int row;
    private int gap;
    
    public GuiSettings(final net.minecraft.client.gui.GuiScreen parentScreen) {
        super(parentScreen);
    }
    
    @Override
    public void initGui() {
        this.row = 1;
        this.gap = this.getLayoutWidth(this.getMainWidth() / 6) / 8;
        this.elements.clear();
        this.components.clear();
        SettingsManager.INSTANCE.settings.forEach(setting -> {
            this.addSetting(setting, this.width / 2 - this.getWidth() / 2 + 35, (int)this.getRowHeight(this.row, 17));
            ++this.row;
            return;
        });
        super.initGui();
        this.registerScroll(new GuiModules.Scroll(SettingsManager.INSTANCE.settings, this.width, this.height, this.height / 2 - this.getHeight() / 2, this.height / 2 + this.getHeight() / 2, 17, this.width / 2 + this.getWidth() / 2 - 4, 1));
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawBackground();
        final ScaledResolution sr = new ScaledResolution(this.mc);
        final int x = (this.width / 2 - this.getWidth() / 2) * sr.getScaleFactor();
        final int y = (this.height / 2 - this.getHeight() / 2 + 1) * sr.getScaleFactor();
        final int xWidth = (this.width / 2 + this.getWidth() / 2) * sr.getScaleFactor() - x;
        final int yHeight = (this.height / 2 + this.getHeight() / 2) * sr.getScaleFactor() - y;
        this.scissorFunc(sr, x, y, xWidth, yHeight);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(3089);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GeneralConfig.INSTANCE.saveConfig();
    }
    
    private double getRowHeight(double row, final int buttonHeight) {
        --row;
        return this.height / 2 - this.getHeight() / 2 + 5 + row * buttonHeight;
    }
    
    private int getLayoutWidth(final int margin) {
        return this.width / 2 + this.getWidth() / 2 - margin - (this.width / 2 - this.getWidth() / 2 + 18 + margin);
    }
    
    private int getMainWidth() {
        return this.width / 2 + this.getWidth() / 2 - (this.width / 2 - this.getWidth() / 2 + 16);
    }
}
