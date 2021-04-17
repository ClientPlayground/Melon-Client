package me.kaimson.melonclient.mixins.client.network;

import net.minecraft.network.play.*;
import net.minecraft.client.network.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import net.minecraft.network.play.server.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.config.*;
import me.kaimson.melonclient.features.modules.*;
import net.minecraft.network.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin({ NetHandlerPlayClient.class })
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient
{
    @Shadow
    private Minecraft gameController;
    
    @Inject(method = { "handleTimeUpdate" }, at = { @At("HEAD") }, cancellable = true)
    private void handleTimeUpdate(final S03PacketTimeUpdate packetIn, final CallbackInfo ci) {
        S03PacketTimeUpdate packet = packetIn;
        if (ModuleConfig.INSTANCE.isEnabled(TimeChangerModule.INSTANCE)) {
            switch (TimeChangerModule.INSTANCE.time.getInt()) {
                case 1: {
                    packet = new S03PacketTimeUpdate(packet.getWorldTime(), -6000L, true);
                    break;
                }
                case 2: {
                    packet = new S03PacketTimeUpdate(packet.getWorldTime(), -22880L, true);
                    break;
                }
                case 3: {
                    packet = new S03PacketTimeUpdate(packet.getWorldTime(), -18000L, true);
                    break;
                }
            }
            PacketThreadUtil.checkThreadAndEnqueue(packet, this, this.gameController);
            this.gameController.theWorld.setTotalWorldTime(packetIn.getTotalWorldTime());
            this.gameController.theWorld.setWorldTime(packetIn.getWorldTime());
            ci.cancel();
        }
    }
}
