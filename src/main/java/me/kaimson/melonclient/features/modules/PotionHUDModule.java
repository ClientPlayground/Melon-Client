package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.modules.utils.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.utils.*;
import me.kaimson.melonclient.features.*;
import me.kaimson.melonclient.gui.settings.*;
import net.minecraft.potion.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.*;
import me.kaimson.melonclient.gui.utils.*;
import net.minecraft.client.*;
import java.util.*;

public class PotionHUDModule extends IModuleRenderer
{
    private final ResourceLocation inventoryBackground;
    private int width;
    private int height;
    private final Setting shadow;
    private final Setting blink;
    
    public PotionHUDModule() {
        super("Potion HUD", 20);
        this.inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");
        new Setting(this, "General Options");
        this.shadow = new Setting(this, "Text Shadow").setDefault(true);
        new Setting(this, "Blink Options");
        this.blink = new Setting(this, "Blink").setDefault(true);
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public void render(final float x, final float y) {
        if (this.mc.thePlayer.getActivePotionEffects() != null && this.mc.thePlayer.getActivePotionEffects().size() > 0) {
            this.render(this.mc.thePlayer.getActivePotionEffects(), x, y, false);
        }
        else if (this.mc.currentScreen instanceof GuiHUDEditor) {
            this.renderDummy(x, y);
        }
    }
    
    @Override
    public void renderDummy(final float x, final float y) {
        final Collection<PotionEffect> potionEffects = new ArrayList<>(Collections.emptySet());
        potionEffects.add(new PotionEffect(Potion.absorption.id, 9));
        potionEffects.add(new PotionEffect(Potion.moveSpeed.id, 9));
        this.width = 90;
        this.height = 44;
        this.render(potionEffects, x, y, true);
    }
    
    private void render(final Collection<PotionEffect> potionEffects, final float x, final float y, final boolean isDummy) {
        if (potionEffects == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        int j = (int)y;
        final int tileHeight = 22;
        if (!isDummy) {
            this.width = 0;
            this.height = 22 * potionEffects.size();
        }
        for (final PotionEffect potionEffect : potionEffects) {
            final Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
            if (potion.hasStatusIcon()) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                this.mc.getTextureManager().bindTexture(this.inventoryBackground);
                GuiUtils.INSTANCE.drawTexturedModalRect(x + 6.0f, (float)(j + 2), potion.getStatusIconIndex() % 8 * 18, 198 + potion.getStatusIconIndex() / 8 * 18, 18, 18);
            }
            FontUtils.drawString(I18n.format(potion.getName(), new Object[0]), x + 10.0f + 18.0f, j + 3, 16777215, this.shadow.getBoolean());
            Label_0284: {
                if (isDummy) {
                    if (this.blink.getBoolean()) {
                        if (Minecraft.getSystemTime() / 50L % 20L >= 10L) {
                            break Label_0284;
                        }
                    }
                }
                else if (!this.shouldRender(potionEffect.getDuration(), 10)) {
                    break Label_0284;
                }
                FontUtils.drawString(Potion.getDurationString(potionEffect), x + 10.0f + 18.0f, j + 3 + 10, 8355711, this.shadow.getBoolean());
            }
            if (!isDummy) {
                this.width = Math.max(this.width, 30 + this.mc.fontRendererObj.getStringWidth(I18n.format(potion.getName(), new Object[0])));
            }
            j += tileHeight;
        }
        GlStateManager.popMatrix();
    }
    
    private boolean shouldRender(final int ticksLeft, final int thresholdSeconds) {
        return !this.blink.getBoolean() || ticksLeft / 20 > thresholdSeconds || ticksLeft % 20 < 10;
    }
}
