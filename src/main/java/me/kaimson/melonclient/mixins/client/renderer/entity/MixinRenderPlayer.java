package me.kaimson.melonclient.mixins.client.renderer.entity;

import me.kaimson.melonclient.cosmetics.LayerCape;
import net.minecraft.client.entity.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.cosmetics.*;
import net.minecraft.client.renderer.entity.layers.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ RenderPlayer.class })
public abstract class MixinRenderPlayer extends RendererLivingEntity<AbstractClientPlayer>
{
    public MixinRenderPlayer(final RenderManager renderManagerIn, final ModelBase modelBaseIn, final float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }
    
    @Inject(method = { "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V" }, at = { @At("RETURN") })
    private void constructor(final RenderManager renderManager, final boolean useSmallArms, final CallbackInfo ci) {
        this.addLayer(new LayerCape());
    }
}
