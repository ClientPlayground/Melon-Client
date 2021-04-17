package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.gui.*;
import me.kaimson.melonclient.gui.utils.*;
import net.minecraft.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.enchantment.*;
import java.util.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.utils.*;

public class HotbarModule extends Module
{
    public static HotbarModule INSTANCE;
    private final Setting showAttackDamage;
    private final Setting showEnchantments;
    
    public HotbarModule() {
        super("Hotbar");
        new Setting(this, "General Options");
        this.showAttackDamage = new Setting(this, "Show held item attack damage").setDefault(false);
        this.showEnchantments = new Setting(this, "Show held item enchantments").setDefault(false);
        HotbarModule.INSTANCE = this;
    }
    
    public void onTick() {
        this.renderAttackDamage();
        this.renderEnchantments();
    }
    
    private void renderAttackDamage() {
        if (!this.showAttackDamage.getBoolean()) {
            return;
        }
        final ItemStack heldItemStack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
        if (heldItemStack != null) {
            GlStateManager.pushMatrix();
            final float scale = 0.5f;
            GlStateManager.scale(scale, scale, 1.0f);
            final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            FontUtils.drawCenteredString(this.getAttackDamageString(heldItemStack), sr.getScaledWidth() / 2.0f / scale, (sr.getScaledHeight() - 59 + (Minecraft.getMinecraft().playerController.shouldDrawHUD() ? -1 : 14) + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT) * 2 + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, 13421772);
            GlStateManager.popMatrix();
        }
    }
    
    private void renderEnchantments() {
        if (!this.showEnchantments.getBoolean()) {
            return;
        }
        final ItemStack heldItemStack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
        if (heldItemStack != null) {
            String toDraw = "";
            toDraw = this.getEnchantmentString(heldItemStack);
            GlStateManager.pushMatrix();
            final float scale = 0.5f;
            GlStateManager.scale(scale, scale, 1.0f);
            final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            FontUtils.drawString(toDraw, sr.getScaledWidth() - Minecraft.getMinecraft().fontRendererObj.getStringWidth(toDraw) / 2 + 0.1f, (sr.getScaledHeight() - 59 + (Minecraft.getMinecraft().playerController.shouldDrawHUD() ? -2 : 14) + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT) * 2, 13421772);
            GlStateManager.popMatrix();
        }
    }
    
    private String getAttackDamageString(final ItemStack stack) {
        for (final String entry : stack.getTooltip((EntityPlayer)Minecraft.getMinecraft().thePlayer, true)) {
            if (entry.endsWith("Attack Damage")) {
                return entry.split(" ", 2)[0].substring(2);
            }
        }
        return "";
    }
    
    private String getEnchantmentString(final ItemStack heldItemStack) {
        final StringBuilder enchantBuilder = new StringBuilder();
        final Map<Integer, Integer> en = (Map<Integer, Integer>)EnchantmentHelper.getEnchantments(heldItemStack);
        for (final Map.Entry<Integer, Integer> entry : en.entrySet()) {
            enchantBuilder.append(EnumChatFormatting.BOLD.toString());
            enchantBuilder.append(Maps.ENCHANTMENT_SHORT_NAME.get(entry.getKey()));
            enchantBuilder.append(" ");
            enchantBuilder.append(entry.getValue());
            enchantBuilder.append(" ");
        }
        return enchantBuilder.toString().trim();
    }
}
