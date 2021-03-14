package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IRenderChunkFactory {
  RenderChunk makeRenderChunk(World paramWorld, RenderGlobal paramRenderGlobal, BlockPos paramBlockPos, int paramInt);
}
