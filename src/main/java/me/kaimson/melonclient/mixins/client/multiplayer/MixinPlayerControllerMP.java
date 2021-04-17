package me.kaimson.melonclient.mixins.client.multiplayer;

import org.spongepowered.asm.mixin.*;
import net.minecraft.client.multiplayer.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin({ PlayerControllerMP.class })
public interface MixinPlayerControllerMP
{
    @Accessor
    void setIsHittingBlock(final boolean p0);
}
