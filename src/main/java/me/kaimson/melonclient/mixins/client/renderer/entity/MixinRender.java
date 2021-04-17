package me.kaimson.melonclient.mixins.client.renderer.entity;

import net.minecraft.entity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.*;
import me.kaimson.melonclient.features.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.client.entity.*;
import me.kaimson.melonclient.utils.*;
import me.kaimson.melonclient.features.modules.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ Render.class })
public abstract class MixinRender<T extends Entity>
{
    @Shadow
    @Final
    protected RenderManager renderManager;
    
    @Shadow
    public abstract FontRenderer getFontRendererFromRenderManager();
    
    /**
     * @author Kaimson the Clown
     */
    @Overwrite
    protected void renderLivingLabel(final T entityIn, final String str, final double x, final double y, final double z, final int maxDistance) {
        final double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);
        if (d0 <= maxDistance * maxDistance) {
            final FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
            final float f = 1.6f;
            final float f2 = 0.016666668f * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x + 0.0f, (float)y + entityIn.height + 0.5f, (float)z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-f2, -f2, f2);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            final Tessellator tessellator = Tessellator.getInstance();
            final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 0;
            if (str.equals("deadmau5")) {
                i = -10;
            }
            if (!SettingsManager.INSTANCE.transparentNametags.getBoolean()) {
                final int j = fontrenderer.getStringWidth(str) / 2;
                GlStateManager.disableTexture2D();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos((double)(-j - 1), (double)(-1 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(-j - 1), (double)(8 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(j + 1), (double)(8 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(j + 1), (double)(-1 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }
    
    @Inject(method = { "doRender" }, at = { @At("TAIL") })
    private void doRender(final T entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks, final CallbackInfo ci) {
        if (SettingsManager.INSTANCE.showName.getBoolean() && entity instanceof EntityPlayerSP) {
            NametagRenderer.render(0.0, 0.0, 0.0);
        }
    }
    
    @ModifyArg(method = { "renderLivingLabel" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 1), index = 0)
    private float getViewX(final float viewXIn) {
        final float viewX = PerspectiveModule.INSTANCE.isHeld() ? PerspectiveModule.getCameraPitch() : viewXIn;
        return (SettingsManager.INSTANCE.fixNametagRot.getBoolean() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) ? (-viewX) : viewX;
    }
    
    @ModifyArg(method = { "renderLivingLabel" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 0), index = 0)
    private float getViewY(final float viewY) {
        return PerspectiveModule.INSTANCE.isHeld() ? (-PerspectiveModule.getCameraYaw()) : viewY;
    }
}
