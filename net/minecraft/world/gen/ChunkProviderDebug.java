package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderDebug implements IChunkProvider {
  private static final List<IBlockState> field_177464_a = Lists.newArrayList();
  
  public ChunkProviderDebug(World worldIn) {
    this.world = worldIn;
  }
  
  public Chunk provideChunk(int x, int z) {
    ChunkPrimer chunkprimer = new ChunkPrimer();
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        int k = x * 16 + i;
        int l = z * 16 + j;
        chunkprimer.setBlockState(i, 60, j, Blocks.barrier.getDefaultState());
        IBlockState iblockstate = func_177461_b(k, l);
        if (iblockstate != null)
          chunkprimer.setBlockState(i, 70, j, iblockstate); 
      } 
    } 
    Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
    chunk.generateSkylightMap();
    BiomeGenBase[] abiomegenbase = this.world.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, x * 16, z * 16, 16, 16);
    byte[] abyte = chunk.getBiomeArray();
    for (int i1 = 0; i1 < abyte.length; i1++)
      abyte[i1] = (byte)(abiomegenbase[i1]).biomeID; 
    chunk.generateSkylightMap();
    return chunk;
  }
  
  public static IBlockState func_177461_b(int p_177461_0_, int p_177461_1_) {
    IBlockState iblockstate = null;
    if (p_177461_0_ > 0 && p_177461_1_ > 0 && p_177461_0_ % 2 != 0 && p_177461_1_ % 2 != 0) {
      p_177461_0_ /= 2;
      p_177461_1_ /= 2;
      if (p_177461_0_ <= field_177462_b && p_177461_1_ <= field_181039_c) {
        int i = MathHelper.abs_int(p_177461_0_ * field_177462_b + p_177461_1_);
        if (i < field_177464_a.size())
          iblockstate = field_177464_a.get(i); 
      } 
    } 
    return iblockstate;
  }
  
  public boolean chunkExists(int x, int z) {
    return true;
  }
  
  public void populate(IChunkProvider chunkProvider, int x, int z) {}
  
  public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z) {
    return false;
  }
  
  public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback) {
    return true;
  }
  
  public void saveExtraData() {}
  
  public boolean unloadQueuedChunks() {
    return false;
  }
  
  public boolean canSave() {
    return true;
  }
  
  public String makeString() {
    return "DebugLevelSource";
  }
  
  public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
    BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(pos);
    return biomegenbase.getSpawnableList(creatureType);
  }
  
  public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
    return null;
  }
  
  public int getLoadedChunkCount() {
    return 0;
  }
  
  public void recreateStructures(Chunk chunkIn, int x, int z) {}
  
  public Chunk provideChunk(BlockPos blockPosIn) {
    return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
  }
  
  static {
    for (Block block : Block.blockRegistry)
      field_177464_a.addAll((Collection<? extends IBlockState>)block.getBlockState().getValidStates()); 
  }
  
  private static final int field_177462_b = MathHelper.ceiling_float_int(MathHelper.sqrt_float(field_177464_a.size()));
  
  private static final int field_181039_c = MathHelper.ceiling_float_int(field_177464_a.size() / field_177462_b);
  
  private final World world;
}
