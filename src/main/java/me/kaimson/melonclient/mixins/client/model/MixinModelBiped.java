package me.kaimson.melonclient.mixins.client.model;

import net.minecraft.client.model.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.entity.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.features.modules.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ ModelBiped.class })
public class MixinModelBiped extends ModelBase
{
    @Shadow
    public ModelRenderer bipedRightArm;
    
    @Inject(method = { "setRotationAngles" }, at = { @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelRenderer;rotateAngleY:F", ordinal = 6, shift = At.Shift.AFTER) })
    private void setRotationAngleY(final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scaleFactor, final Entity entityIn, final CallbackInfo ci) {
        this.bipedRightArm.rotateAngleY = (OldAnimationsModule.INSTANCE.block.getBoolean() ? 0.0f : -0.5235988f);
    }
}
