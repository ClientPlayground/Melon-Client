package me.kaimson.melonclient.mixins.client.renderer;

import net.minecraft.client.multiplayer.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.entity.player.*;
import me.kaimson.melonclient.config.*;
import me.kaimson.melonclient.features.modules.*;
import me.kaimson.melonclient.gui.utils.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import net.minecraft.block.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Shadow
    private WorldClient theWorld;

    @Inject(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER))
    private void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        if (ModuleConfig.INSTANCE.isEnabled(BlockOverlayModule.INSTANCE)) {
            GuiUtils.setGlColor(BlockOverlayModule.INSTANCE.outlineColor.getColor());
        }
    }

    @ModifyArg(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glLineWidth(F)V", remap = false))
    private float getLineWidth(float lineWidth) {
        return ModuleConfig.INSTANCE.isEnabled(BlockOverlayModule.INSTANCE) ? BlockOverlayModule.INSTANCE.outlineWidth.getFloat() : lineWidth;
    }

    @Inject(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;depthMask(Z)V", ordinal = 0, shift = At.Shift.AFTER))
    private void disableDepth(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        if (ModuleConfig.INSTANCE.isEnabled(BlockOverlayModule.INSTANCE) && BlockOverlayModule.INSTANCE.ignoreDepth.getBoolean()) {
            GlStateManager.disableDepth();
        }
    }

    @Inject(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;depthMask(Z)V", ordinal = 1))
    private void enableDepth(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        if (ModuleConfig.INSTANCE.isEnabled(BlockOverlayModule.INSTANCE) && BlockOverlayModule.INSTANCE.ignoreDepth.getBoolean()) {
            GlStateManager.enableDepth();
        }
    }

    @Inject(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawSelectionBoundingBox(Lnet/minecraft/util/AxisAlignedBB;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void drawSelectionBoxFill(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci, float f, BlockPos blockPos, Block block) {
        if (ModuleConfig.INSTANCE.isEnabled(BlockOverlayModule.INSTANCE) && BlockOverlayModule.INSTANCE.fill.getBoolean()) {
            GuiUtils.setGlColor(BlockOverlayModule.INSTANCE.fillColor.getColor());
            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double d2 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double d3 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
            BlockOverlayModule.INSTANCE.drawFilledWithGL(block.getSelectedBoundingBox(this.theWorld, blockPos).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026).offset(-d0, -d2, -d3));
        }
    }
}
