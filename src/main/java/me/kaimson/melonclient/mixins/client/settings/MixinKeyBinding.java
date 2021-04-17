package me.kaimson.melonclient.mixins.client.settings;

import org.spongepowered.asm.mixin.*;
import net.minecraft.client.settings.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ KeyBinding.class })
public class MixinKeyBinding
{
    @Inject(method = { "onTick" }, at = { @At("HEAD") })
    private static void onTick(final int keyCode, final CallbackInfo ci) {
        if (keyCode != 0) {
            me.kaimson.melonclient.utils.KeyBinding.keyBindings.forEach(me.kaimson.melonclient.utils.KeyBinding::onTick);
        }
    }
    
    @Inject(method = { "setKeyBindState" }, at = { @At("HEAD") })
    private static void setKeyBindState(final int keyCode, final boolean pressed, final CallbackInfo ci) {
        if (keyCode != 0) {
            me.kaimson.melonclient.utils.KeyBinding.keyBindings.stream().filter(bind -> bind.getKeyCode() == keyCode).forEach(bind -> bind.setKeyBindState(keyCode, pressed));
        }
    }
}
