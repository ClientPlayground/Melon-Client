package me.kaimson.melonclient.mixins.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import me.kaimson.melonclient.features.modules.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.init.*;
import net.minecraft.client.*;
import net.minecraft.item.*;
import net.minecraft.block.*;
import net.minecraft.client.renderer.block.model.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin({ LayerHeldItem.class })
public class MixinLayerHeldItem
{
    @Shadow
    @Final
    private RendererLivingEntity<?> livingEntityRenderer;
    
    @Inject(method = { "doRenderLayer" }, at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBiped;postRenderArm(F)V", ordinal = 0) }, cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void doRenderLayer(final EntityLivingBase entitylivingbaseIn, final float p_177141_2_, final float p_177141_3_, final float partialTicks, final float p_177141_5_, final float p_177141_6_, final float p_177141_7_, final float scale, final CallbackInfo ci, ItemStack itemstack) {
        if (entitylivingbaseIn instanceof EntityPlayer) {
            if (OldAnimationsModule.INSTANCE.block.getBoolean()) {
                if (((EntityPlayer)entitylivingbaseIn).isBlocking()) {
                    if (entitylivingbaseIn.isSneaking()) {
                        ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325f);
                        GlStateManager.scale(1.05f, 1.05f, 1.05f);
                        GlStateManager.translate(-0.58f, 0.32f, -0.07f);
                        GlStateManager.rotate(-24405.0f, 137290.0f, -2009900.0f, -2654900.0f);
                    }
                    else {
                        ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325f);
                        GlStateManager.scale(1.05f, 1.05f, 1.05f);
                        GlStateManager.translate(-0.45f, 0.25f, -0.07f);
                        GlStateManager.rotate(-24405.0f, 137290.0f, -2009900.0f, -2654900.0f);
                    }
                }
                else {
                    ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
                }
            }
            else {
                ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
            }
            GlStateManager.translate(-0.0625f, 0.4375f, 0.0625f);
            if (((EntityPlayer)entitylivingbaseIn).fishEntity != null) {
                itemstack = new ItemStack((Item)Items.fishing_rod, 0);
            }
        }
        else {
            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
            GlStateManager.translate(-0.0625f, 0.4375f, 0.0625f);
        }
        final Item item = itemstack.getItem();
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
            GlStateManager.translate(0.0f, 0.1875f, -0.3125f);
            GlStateManager.rotate(20.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
            final float f1 = 0.375f;
            GlStateManager.scale(-f1, -f1, f1);
        }
        if (entitylivingbaseIn.isSneaking()) {
            GlStateManager.translate(0.0f, 0.203125f, 0.0f);
        }
        minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
        GlStateManager.popMatrix();
        ci.cancel();
    }
}
