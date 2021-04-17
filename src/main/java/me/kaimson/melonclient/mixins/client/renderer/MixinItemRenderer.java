package me.kaimson.melonclient.mixins.client.renderer;

import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.client.entity.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.features.modules.*;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.item.*;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Shadow
    @Final
    private Minecraft mc;
    private float swingProgress;

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemUseAction()Lnet/minecraft/item/EnumAction;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void modifySwingProgress(float partialTicks, CallbackInfo ci, float f, AbstractClientPlayer player, float f1, float f2, float f3) {
        this.swingProgress = f1;
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;doBlockTransformations()V", shift = At.Shift.AFTER))
    private void modifySwing(float partialTicks, CallbackInfo ci) {
        if (OldAnimationsModule.INSTANCE.blockHit.getBoolean()) {
            GlStateManager.scale(0.83f, 0.88f, 0.85f);
            GlStateManager.translate(-0.3f, 0.1f, 0.0f);
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void modifyEat(float partialTicks, CallbackInfo ci) {
        if (OldAnimationsModule.INSTANCE.eating.getBoolean()) {
            GlStateManager.scale(0.8f, 1.0f, 1.0f);
            GlStateManager.translate(-0.2f, -0.1f, 0.0f);
        }
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1), index = 1)
    private float drinkSwingProgress(float swingProgress) {
        return OldAnimationsModule.INSTANCE.eating.getBoolean() ? this.swingProgress : 0.0f;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 2), index = 1)
    private float blockSwingProgress(float swingProgress) {
        return OldAnimationsModule.INSTANCE.blockHit.getBoolean() ? this.swingProgress : 0.0f;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 3), index = 1)
    private float bowSwingProgress(float swingProgress) {
        return OldAnimationsModule.INSTANCE.bow.getBoolean() ? this.swingProgress : 0.0f;
    }

    @Inject(method = "transformFirstPersonItem", at = {@At("HEAD")})
    private void transformFirstPersonItem(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (OldAnimationsModule.INSTANCE.bow.getBoolean() && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getItemInUse() != null && this.mc.thePlayer.getItemInUse().getItem() != null && Item.getIdFromItem(this.mc.thePlayer.getItemInUse().getItem()) == 261) {
            GlStateManager.translate(-0.01f, 0.05f, -0.06f);
        }
        if (OldAnimationsModule.INSTANCE.rod.getBoolean() && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.getCurrentEquippedItem().getItem() != null && (Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 346 || Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 398)) {
            GlStateManager.translate(0.08f, -0.027f, -0.33f);
            GlStateManager.scale(0.93f, 1.0f, 1.0f);
        }
        if (OldAnimationsModule.INSTANCE.swing.getBoolean() && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.isSwingInProgress  && this.mc.thePlayer.getCurrentEquippedItem() != null && !this.mc.thePlayer.isEating() && !this.mc.thePlayer.isBlocking()) {
            GlStateManager.scale(0.85f, 0.85f, 0.85f);
            GlStateManager.translate(-0.078f, 0.003f, 0.05f);
        }
    }
}
