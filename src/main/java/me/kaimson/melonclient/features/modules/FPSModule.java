package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.modules.utils.*;
import net.minecraft.client.*;

public class FPSModule extends DefaultModuleRenderer
{
    public FPSModule() {
        super("FPS");
    }
    
    @Override
    public Object getValue() {
        return Minecraft.getDebugFPS();
    }
}
