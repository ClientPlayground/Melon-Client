package me.kaimson.melonclient.features.modules.utils;

import me.kaimson.melonclient.features.*;
import net.minecraft.client.*;

public abstract class IModuleRenderer extends Module
{
    protected final Minecraft mc;
    
    public IModuleRenderer(final String displayName) {
        this(displayName, -1);
    }
    
    public IModuleRenderer(final String displayName, final int textureIndex) {
        super(displayName, textureIndex);
        this.mc = Minecraft.getMinecraft();
    }
    
    public abstract int getWidth();
    
    public abstract int getHeight();
    
    public abstract void render(final float p0, final float p1);
    
    public void renderDummy(final float x, final float y) {
    }
}
