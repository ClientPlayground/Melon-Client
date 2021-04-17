package me.kaimson.melonclient.mixins.world.chunk.storage;

import org.spongepowered.asm.mixin.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import java.io.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin({ AnvilChunkLoader.class })
public class MixinAnvilChunkLoader
{
    @Inject(method = { "loadChunk" }, at = { @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;read(Ljava/io/DataInputStream;)Lnet/minecraft/nbt/NBTTagCompound;", shift = At.Shift.AFTER) }, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void closeInputstream(final World worldIn, final int x, final int z, final CallbackInfoReturnable<Chunk> cir, final ChunkCoordIntPair pair, final NBTTagCompound nbt, final DataInputStream inputStream) throws IOException {
        inputStream.close();
    }
}
