package me.kaimson.melonclient.mixins.accessor;

import org.spongepowered.asm.mixin.*;
import java.util.*;
import net.minecraft.client.resources.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin({ SimpleReloadableResourceManager.class })
public interface SimpleReloadableResourceManagerAccessor
{
    @Accessor
    Map<String, FallbackResourceManager> getDomainResourceManagers();
}
