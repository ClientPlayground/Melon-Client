package me.kaimson.melonclient.features.modules.keystrokes.keys;

import me.kaimson.melonclient.features.modules.keystrokes.*;
import net.minecraft.client.settings.*;

public class FillerKey extends Key
{
    public FillerKey(final int gapSize, final KeystrokesModule keystrokesModule) {
        super(gapSize, null, keystrokesModule);
    }
    
    @Override
    public void render() {
    }
}
