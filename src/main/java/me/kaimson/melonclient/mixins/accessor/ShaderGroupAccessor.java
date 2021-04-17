package me.kaimson.melonclient.mixins.accessor;

import org.spongepowered.asm.mixin.*;
import java.util.*;
import net.minecraft.client.shader.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin({ ShaderGroup.class })
public interface ShaderGroupAccessor
{
    @Accessor
    List<Shader> getListShaders();
}
