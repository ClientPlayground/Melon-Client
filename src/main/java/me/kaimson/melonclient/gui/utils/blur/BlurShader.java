package me.kaimson.melonclient.gui.utils.blur;

import net.minecraft.client.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.mixins.client.renderer.*;
import me.kaimson.melonclient.mixins.accessor.*;
import java.util.*;
import net.minecraft.client.shader.*;

public class BlurShader
{
    public static final BlurShader INSTANCE;
    private final Minecraft mc;
    private final ResourceLocation SHADER;
    private Map resourceManager;
    private long start;
    
    public BlurShader() {
        this.mc = Minecraft.getMinecraft();
        this.SHADER = new ResourceLocation("melonclient", "shaders/post/fade_in_blur.json");
    }
    
    public void onGuiOpen(final float BLUR_RADIUS) {
        if (this.resourceManager == null) {
            this.resourceManager = ((SimpleReloadableResourceManagerAccessor)Minecraft.getMinecraft().getResourceManager()).getDomainResourceManagers();
        }
        if (!this.resourceManager.containsKey("melonclient")) {
            this.resourceManager.put("melonclient", new BlurResourceManager(BLUR_RADIUS));
        }
        if (this.mc.theWorld != null && !this.mc.entityRenderer.isShaderActive()) {
            ((IMixinEntityRenderer)this.mc.entityRenderer).callLoadShader(this.SHADER);
            this.start = System.currentTimeMillis();
        }
    }
    
    public void onGuiClose() {
        if (this.mc.theWorld != null && this.mc.entityRenderer.isShaderActive()) {
            this.mc.entityRenderer.stopUseShader();
        }
    }
    
    public void onRenderTick() {
        if (this.mc.currentScreen != null && this.mc.entityRenderer.isShaderActive()) {
            for (final Shader s : ((ShaderGroupAccessor)this.mc.entityRenderer.getShaderGroup()).getListShaders()) {
                final ShaderUniform su = s.getShaderManager().getShaderUniform("Progress");
                if (su != null) {
                    su.set(this.getProgress());
                }
            }
        }
    }
    
    private float getProgress() {
        return Math.min((System.currentTimeMillis() - this.start) / 10.0f, 1.0f);
    }
    
    static {
        INSTANCE = new BlurShader();
    }
}
