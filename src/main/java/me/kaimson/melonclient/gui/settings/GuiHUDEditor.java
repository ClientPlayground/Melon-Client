package me.kaimson.melonclient.gui.settings;

import me.kaimson.melonclient.gui.*;
import me.kaimson.melonclient.gui.GuiScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.features.*;
import me.kaimson.melonclient.config.*;
import org.lwjgl.input.*;
import me.kaimson.melonclient.features.modules.utils.*;
import me.kaimson.melonclient.utils.*;
import me.kaimson.melonclient.gui.components.*;
import me.kaimson.melonclient.gui.elements.*;
import java.util.*;
import java.awt.*;
import me.kaimson.melonclient.gui.utils.*;
import java.io.*;

public class GuiHUDEditor extends GuiScreen
{
    private Module dragging;
    public static Module lastDragging;
    private int dragClickX;
    private int dragClickY;
    private int offsetX;
    private int offsetY;
    private ScaledResolution sr;
    private final net.minecraft.client.gui.GuiScreen parentScreen;
    
    public GuiHUDEditor(final net.minecraft.client.gui.GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        this.sr = new ScaledResolution(Minecraft.getMinecraft());
        this.elements.clear();
        for (final Module module : ModuleManager.INSTANCE.modules) {
            if (module.isRender()) {
                if (!ModuleConfig.INSTANCE.isEnabled(module)) {
                    continue;
                }
                this.elements.add(new ElementLocation(module, element -> {
                    this.dragging = module;
                    final float mouseX = Mouse.getX() / this.sr.getScaleFactor();
                    final float mouseY = (this.mc.displayHeight - Mouse.getY()) / this.sr.getScaleFactor();
                    this.offsetX = (int)(-(mouseX - ModuleConfig.INSTANCE.getActualX(module)) - BoxUtils.getOffsetX(module, ((IModuleRenderer)module).getWidth()));
                    this.offsetY = (int)(-(mouseY - ModuleConfig.INSTANCE.getActualY(module)) - BoxUtils.getOffsetY(module, ((IModuleRenderer)module).getHeight()));
                }));
            }
        }
        this.components.clear();
        this.components.add(new ComponentToolbar(this.width / 2 - 36, this.height - 25, this.width / 2 + 36, this.height - 7, 13, 14, ComponentToolbar.Layout.HORIZONTAL).setOffset(2, 2).required(new ElementButtonIcon(14, 14, "icons/home.png", element -> this.mc.displayGuiScreen(this.parentScreen)), ComponentToolbar.Position.POSITIVE).optional(new ElementButtonIcon(14, 14, "icons/edit.png", element -> {}), ComponentToolbar.Position.POSITIVE, false).optional(new ElementButtonIcon(14, 14, "icons/settings.png", element -> {}), ComponentToolbar.Position.POSITIVE, false));
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GuiUtils.drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB());
        if (this.dragging != null && Mouse.isButtonDown(0)) {
            this.updateModulePosition(mouseX, mouseY);
        }
        if (Mouse.isButtonDown(0) && this.dragClickX != 0 && this.dragClickY != 0 && this.dragClickX != mouseX && this.dragClickY != mouseY) {
            GuiUtils.drawRect(this.dragClickX, this.dragClickY, mouseX, mouseY, new Color(150, 150, 150, 100).getRGB());
            GLRectUtils.drawRectOutline(Math.min(this.dragClickX, mouseX), Math.min(this.dragClickY, mouseY), Math.max(this.dragClickX, mouseX), Math.max(this.dragClickY, mouseY), new Color(150, 150, 150, 150).getRGB());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (GuiHUDEditor.lastDragging != null) {
            float xOffset = 0.0f;
            float yOffset = 0.0f;
            switch (keyCode) {
                case 203: {
                    --xOffset;
                    break;
                }
                case 205: {
                    ++xOffset;
                    break;
                }
                case 200: {
                    --yOffset;
                    break;
                }
                case 208: {
                    ++yOffset;
                    break;
                }
            }
            final float x = ModuleConfig.INSTANCE.getPosition(GuiHUDEditor.lastDragging).getX() + xOffset;
            final float y = ModuleConfig.INSTANCE.getPosition(GuiHUDEditor.lastDragging).getY() + yOffset;
            ModuleConfig.INSTANCE.setPosition(GuiHUDEditor.lastDragging, ModuleConfig.INSTANCE.getPosition(GuiHUDEditor.lastDragging).getAnchorPoint(), x, y);
        }
    }
    
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.elements.stream().filter(element -> element instanceof ElementLocation).noneMatch(element -> element.hovered)) {
            this.dragClickX = mouseX;
            this.dragClickY = mouseY;
        }
    }
    
    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.dragClickX = 0;
        this.dragClickY = 0;
        if (this.dragging != null) {
            ModuleConfig.INSTANCE.setClosestAnchorPoint(this.dragging);
            this.updateModulePosition(mouseX, mouseY);
            this.dragging = null;
        }
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GuiHUDEditor.lastDragging = null;
        ModuleConfig.INSTANCE.saveConfig();
    }
    
    private void updateModulePosition(final int mouseX, final int mouseY) {
        final float x = mouseX - BoxUtils.getBoxOffX(this.dragging, ModuleConfig.INSTANCE.getPosition(this.dragging).getAnchorPoint().getX(this.sr.getScaledWidth()), ((IModuleRenderer)this.dragging).getWidth()) + this.offsetX;
        final float y = mouseY - BoxUtils.getBoxOffY(this.dragging, ModuleConfig.INSTANCE.getPosition(this.dragging).getAnchorPoint().getY(this.sr.getScaledHeight()), ((IModuleRenderer)this.dragging).getHeight()) + this.offsetY;
        ModuleConfig.INSTANCE.setPosition(this.dragging, ModuleConfig.INSTANCE.getPosition(this.dragging).getAnchorPoint(), x, y);
    }
}
