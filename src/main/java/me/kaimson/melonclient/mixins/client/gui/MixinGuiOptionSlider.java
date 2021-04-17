package me.kaimson.melonclient.mixins.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.*;
import net.minecraft.client.settings.GameSettings;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.gui.utils.*;

@Mixin(GuiOptionSlider.class)
public class MixinGuiOptionSlider extends GuiButton
{
    @Shadow
    private float sliderValue;
    @Shadow
    public boolean dragging;

    @Shadow
    private GameSettings.Options options;

    public MixinGuiOptionSlider() {
        super(0, 0, 0, "");
    }
    
    protected void mouseDragged(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (mouseX - (this.xPosition + 4f)) / (this.width - 8f);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0f, 1.0f);
                final float f = this.options.denormalizeValue(this.sliderValue);
                mc.gameSettings.setOptionFloatValue(this.options, f);
                this.sliderValue = this.options.normalizeValue(f);
                this.displayString = mc.gameSettings.getKeyBinding(this.options);
            }
            GLRectUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + 20, 2.0f, Client.getMainColor(40));
            GLRectUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + 20, 2.0f, 2.0f, Client.getMainColor(255));
            GLRectUtils.drawRoundedOutline(this.xPosition + (int)(this.sliderValue * (this.width - 8)), this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + 20, 2.0f, 2.0f, Client.getMainColor(255));
        }
    }
}
