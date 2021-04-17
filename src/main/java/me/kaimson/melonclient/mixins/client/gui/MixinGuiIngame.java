package me.kaimson.melonclient.mixins.client.gui;

import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.features.modules.*;
import net.minecraft.inventory.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.injection.*;
import me.kaimson.melonclient.config.*;
import net.minecraft.client.gui.*;

@Mixin(GuiIngame.class)
public class MixinGuiIngame extends Gui {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "renderGameOverlay", at = @At("RETURN"))
    private void renderGameOverlay(float partialTicks, CallbackInfo ci) {
        Client.INSTANCE.onRenderOverlay();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;showCrosshair()Z"))
    private boolean showCrosshair(GuiIngame guiIngame) {
        if (this.mc.gameSettings.showDebugInfo && !this.mc.thePlayer.hasReducedDebug() && !this.mc.gameSettings.reducedDebugInfo) {
            return false;
        }
        if (!this.mc.playerController.isSpectator()) {
            return CrosshairModule.INSTANCE.showInThird.getBoolean() || this.mc.gameSettings.thirdPersonView == 0;
        }
        if (this.mc.pointedEntity != null) {
            return true;
        }
        if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            final BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
            return this.mc.theWorld.getTileEntity(blockpos) instanceof IInventory;
        }
        return false;
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    private void renderCrosshair(GuiIngame guiIngame, int x, int y, int textureX, int textureY, int width, int height) {
        if (ModuleConfig.INSTANCE.isEnabled(CrosshairModule.INSTANCE)) {
            final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            CrosshairModule.INSTANCE.render(guiIngame, sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, x, y);
        } else {
            guiIngame.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        }
    }
}
