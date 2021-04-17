package me.kaimson.melonclient.mixins.client.gui;

import org.spongepowered.asm.mixin.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ GuiMultiplayer.class })
public class MixinGuiMultiplayer
{
    @Inject(method = { "connectToServer" }, at = { @At("HEAD") })
    private void connectToServer(final ServerData server, final CallbackInfo ci) {
        if (Minecraft.getMinecraft().theWorld != null) {
            Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
        }
    }
}
