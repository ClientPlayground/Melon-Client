package me.kaimson.melonclient.gui.settings;

import me.kaimson.melonclient.gui.*;
import me.kaimson.melonclient.gui.GuiScreen;
import net.minecraft.client.gui.*;
import me.kaimson.melonclient.gui.components.*;
import me.kaimson.melonclient.gui.utils.blur.*;
import java.awt.*;
import me.kaimson.melonclient.gui.utils.*;
import me.kaimson.melonclient.gui.elements.settings.color.*;
import java.util.concurrent.atomic.*;
import me.kaimson.melonclient.gui.elements.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.gui.elements.settings.*;
import me.kaimson.melonclient.utils.*;
import me.kaimson.melonclient.gui.elements.settings.mode.*;

public abstract class SettingsBase extends GuiScreen
{
    private static int totalOffset;
    protected final net.minecraft.client.gui.GuiScreen parentScreen;
    protected ScaledResolution sr;
    private net.minecraft.client.gui.GuiScreen nextScreen;
    public static GuiScreen lastWindow;
    
    @Override
    public void initGui() {
        super.initGui();
        SettingsBase.totalOffset = 0;
        this.sr = new ScaledResolution(this.mc);
        this.components.add(new ComponentToolbar(this.width / 2 - this.getWidth() / 2, this.height / 2 - this.getHeight() / 2, this.width / 2 - this.getWidth() / 2 + 16, this.height / 2 + this.getHeight() / 2, 3, 14, ComponentToolbar.Layout.VERTICAL).setOffset(1, 1).required(new ElementButtonIcon(14, 14, "icons/close.png", element -> this.nextGui(null)), ComponentToolbar.Position.POSITIVE).optional(new ElementButtonIcon(14, 14, "icons/home.png", element -> this.nextGui(this.parentScreen)), ComponentToolbar.Position.POSITIVE, this.parentScreen != null).optional(new ElementButtonIcon(14, 14, "icons/info.png", element -> {}), ComponentToolbar.Position.NEGATIVE, false).optional(new ElementButtonIcon(14, 14, "icons/settings.png", element -> this.nextGui(new GuiSettings(this))), ComponentToolbar.Position.NEGATIVE, !(this instanceof GuiSettings)).required(new ElementButtonIcon(14, 14, "icons/edit.png", element -> this.nextGui(new GuiHUDEditor(this))), ComponentToolbar.Position.NEGATIVE));
        BlurShader.INSTANCE.onGuiOpen(5.0f);
    }
    
    protected void drawBackground() {
        GuiUtils.drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 125).getRGB());
        GLRectUtils.drawRoundedRect(this.width / 2 - this.getWidth() / 2 + 18, this.height / 2 - this.getHeight() / 2, this.width / 2 + this.getWidth() / 2, this.height / 2 + this.getHeight() / 2, 5.0f, new Color(0, 0, 0, 140).getRGB());
        WatermarkRenderer.render(this.width, this.height);
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.enableInput = this.elements.stream().filter(element -> element instanceof SettingElementColor).noneMatch(element -> {
            final SettingElementColor color = (SettingElementColor) element;
            return color.colorPane.hovered || color.huePane.hovered || color.alphaPane.hovered;
        });
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.nextScreen == null || !(this.nextScreen instanceof SettingsBase) || this.nextScreen == this.parentScreen || this.parentScreen == null) {
            if (this.nextScreen != this.parentScreen) {
                GuiModules.scrollState = 0;
            }
            BlurShader.INSTANCE.onGuiClose();
        }
        SettingsBase.lastWindow = null;
        if (this instanceof GuiModuleSettings && this.nextScreen == null) {
            SettingsBase.lastWindow = this;
        }
        SettingsBase.totalOffset = 0;
    }
    
    protected void nextGui(final net.minecraft.client.gui.GuiScreen screen) {
        this.nextScreen = screen;
        this.mc.displayGuiScreen(screen);
    }
    
    protected int getWidth() {
        return Math.max(356, this.width - this.width / 3);
    }
    
    protected int getHeight() {
        return this.height - this.height / 3;
    }
    
    protected void addSetting(final Setting setting, final int x, final int y) {
        if (setting.hasValue()) {
            Element element = null;
            switch (setting.getType()) {
                case COLOR: {
                    final AtomicBoolean found = new AtomicBoolean(false);
                    element = new SettingElementColor(x + 1, y + 1, 10, 10, this.getWidth() - 100, 0, setting, (mainElement, expanded) -> {
                        if (expanded) {
                            SettingsBase.totalOffset += 34;
                            this.expandScroll(37);
                        }
                        else {
                            SettingsBase.totalOffset -= 34;
                            this.expandScroll(-37);
                        }
                        this.elements.forEach(settingElement -> {
                            if ((settingElement instanceof SettingElement || settingElement instanceof ElementCategory) && (settingElement == mainElement || found.get())) {
                                found.set(true);
                                if (settingElement != mainElement) {
                                    settingElement.addOffsetToY(expanded ? 34 : -34);
                                }
                            }
                        });
                    }, (module, settingElement) -> module.setValue(new ColorObject(GuiUtils.getRGB(((SettingElementColor)settingElement).color, ((SettingElementColor)settingElement).alpha), ((SettingElementColor)settingElement).chroma.active, (int)((SettingElementColor)settingElement).chromaSpeed.getDenormalized())), this);
                    break;
                }
                case CHECKBOX: {
                    element = new SettingElementCheckbox(x + 1, y + 1, 10, 10, setting, (module, settingElement) -> setting.setValue(!setting.getBoolean()), this);
                    break;
                }
                case INT_SLIDER: {
                    element = new SettingElementSlider(x + 1, y + 4, 75, 5, setting.getRange(0), setting.getRange(1), setting.getRange(2), setting.getInt(), setting, (module, settingElement) -> setting.setValue((int)MathUtil.denormalizeValue(((SettingElementSlider) settingElement).sliderValue, setting.getRange(0), setting.getRange(1), setting.getRange(2))), this);
                    element.setXOffset((int)Client.titleRenderer.getWidth(setting.getDescription()) + 6);
                    break;
                }
                case FLOAT_SLIDER: {
                    element = new SettingElementSlider(x + 1, y + 4, 75, 5, setting.getRange(0), setting.getRange(1), setting.getRange(2), setting.getFloat(), setting, (module, settingElement) -> setting.setValue(MathUtil.denormalizeValue(((SettingElementSlider) settingElement).sliderValue, setting.getRange(0), setting.getRange(1), setting.getRange(2))), this);
                    element.setXOffset((int)Client.titleRenderer.getWidth(setting.getDescription()) + 6);
                    break;
                }
                case KEYBIND: {
                    element = new SettingElementKeybind(x + 1, y + 1, 10, 10, setting, (module, settingElement) -> setting.setValue(new KeyBinding(((SettingElementKeybind) settingElement).keycode)), this);
                    element.setXOffset((int)(Client.titleRenderer.getWidth(setting.getDescription()) + 4.0f));
                    break;
                }
                case MODE: {
                    element = new SettingElementMode(x + 1, y + 1, this.getWidth() - 145, 100, 10, setting.getInt(), setting, (module, settingElement) -> setting.setValue(((SettingElementMode) settingElement).mode), this);
                    break;
                }
            }
            if (element == null) {
                return;
            }
            this.elements.add(element);
        }
        else {
            this.elements.add(new ElementCategory(x - 7, y + 3, 0, 10, setting.getDescription(), true));
        }
    }
    
    public SettingsBase(final net.minecraft.client.gui.GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
}
