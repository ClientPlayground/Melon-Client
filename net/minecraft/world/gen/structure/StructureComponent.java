package net.minecraft.world.gen.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDoor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

public abstract class StructureComponent {
  protected StructureBoundingBox boundingBox;
  
  protected EnumFacing coordBaseMode;
  
  protected int componentType;
  
  public StructureComponent() {}
  
  protected StructureComponent(int type) {
    this.componentType = type;
  }
  
  public NBTTagCompound createStructureBaseNBT() {
    NBTTagCompound nbttagcompound = new NBTTagCompound();
    nbttagcompound.setString("id", MapGenStructureIO.getStructureComponentName(this));
    nbttagcompound.setTag("BB", (NBTBase)this.boundingBox.toNBTTagIntArray());
    nbttagcompound.setInteger("O", (this.coordBaseMode == null) ? -1 : this.coordBaseMode.getHorizontalIndex());
    nbttagcompound.setInteger("GD", this.componentType);
    writeStructureToNBT(nbttagcompound);
    return nbttagcompound;
  }
  
  protected abstract void writeStructureToNBT(NBTTagCompound paramNBTTagCompound);
  
  public void readStructureBaseNBT(World worldIn, NBTTagCompound tagCompound) {
    if (tagCompound.hasKey("BB"))
      this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB")); 
    int i = tagCompound.getInteger("O");
    this.coordBaseMode = (i == -1) ? null : EnumFacing.getHorizontal(i);
    this.componentType = tagCompound.getInteger("GD");
    readStructureFromNBT(tagCompound);
  }
  
  protected abstract void readStructureFromNBT(NBTTagCompound paramNBTTagCompound);
  
  public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {}
  
  public abstract boolean addComponentParts(World paramWorld, Random paramRandom, StructureBoundingBox paramStructureBoundingBox);
  
  public StructureBoundingBox getBoundingBox() {
    return this.boundingBox;
  }
  
  public int getComponentType() {
    return this.componentType;
  }
  
  public static StructureComponent findIntersecting(List<StructureComponent> listIn, StructureBoundingBox boundingboxIn) {
    for (StructureComponent structurecomponent : listIn) {
      if (structurecomponent.getBoundingBox() != null && structurecomponent.getBoundingBox().intersectsWith(boundingboxIn))
        return structurecomponent; 
    } 
    return null;
  }
  
  public BlockPos getBoundingBoxCenter() {
    return new BlockPos(this.boundingBox.getCenter());
  }
  
  protected boolean isLiquidInStructureBoundingBox(World worldIn, StructureBoundingBox boundingboxIn) {
    int i = Math.max(this.boundingBox.minX - 1, boundingboxIn.minX);
    int j = Math.max(this.boundingBox.minY - 1, boundingboxIn.minY);
    int k = Math.max(this.boundingBox.minZ - 1, boundingboxIn.minZ);
    int l = Math.min(this.boundingBox.maxX + 1, boundingboxIn.maxX);
    int i1 = Math.min(this.boundingBox.maxY + 1, boundingboxIn.maxY);
    int j1 = Math.min(this.boundingBox.maxZ + 1, boundingboxIn.maxZ);
    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
    for (int k1 = i; k1 <= l; k1++) {
      for (int l1 = k; l1 <= j1; l1++) {
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(k1, j, l1)).getBlock().getMaterial().isLiquid())
          return true; 
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(k1, i1, l1)).getBlock().getMaterial().isLiquid())
          return true; 
      } 
    } 
    for (int i2 = i; i2 <= l; i2++) {
      for (int k2 = j; k2 <= i1; k2++) {
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(i2, k2, k)).getBlock().getMaterial().isLiquid())
          return true; 
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(i2, k2, j1)).getBlock().getMaterial().isLiquid())
          return true; 
      } 
    } 
    for (int j2 = k; j2 <= j1; j2++) {
      for (int l2 = j; l2 <= i1; l2++) {
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(i, l2, j2)).getBlock().getMaterial().isLiquid())
          return true; 
        if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos.set(l, l2, j2)).getBlock().getMaterial().isLiquid())
          return true; 
      } 
    } 
    return false;
  }
  
  protected int getXWithOffset(int x, int z) {
    if (this.coordBaseMode == null)
      return x; 
    switch (this.coordBaseMode) {
      case NORTH:
      case SOUTH:
        return this.boundingBox.minX + x;
      case WEST:
        return this.boundingBox.maxX - z;
      case EAST:
        return this.boundingBox.minX + z;
    } 
    return x;
  }
  
  protected int getYWithOffset(int y) {
    return (this.coordBaseMode == null) ? y : (y + this.boundingBox.minY);
  }
  
  protected int getZWithOffset(int x, int z) {
    if (this.coordBaseMode == null)
      return z; 
    switch (this.coordBaseMode) {
      case NORTH:
        return this.boundingBox.maxZ - z;
      case SOUTH:
        return this.boundingBox.minZ + z;
      case WEST:
      case EAST:
        return this.boundingBox.minZ + x;
    } 
    return z;
  }
  
  protected int getMetadataWithOffset(Block blockIn, int meta) {
    if (blockIn == Blocks.rail) {
      if (this.coordBaseMode == EnumFacing.WEST || this.coordBaseMode == EnumFacing.EAST) {
        if (meta == 1)
          return 0; 
        return 1;
      } 
    } else if (blockIn instanceof net.minecraft.block.BlockDoor) {
      if (this.coordBaseMode == EnumFacing.SOUTH) {
        if (meta == 0)
          return 2; 
        if (meta == 2)
          return 0; 
      } else {
        if (this.coordBaseMode == EnumFacing.WEST)
          return meta + 1 & 0x3; 
        if (this.coordBaseMode == EnumFacing.EAST)
          return meta + 3 & 0x3; 
      } 
    } else if (blockIn != Blocks.stone_stairs && blockIn != Blocks.oak_stairs && blockIn != Blocks.nether_brick_stairs && blockIn != Blocks.stone_brick_stairs && blockIn != Blocks.sandstone_stairs) {
      if (blockIn == Blocks.ladder) {
        if (this.coordBaseMode == EnumFacing.SOUTH) {
          if (meta == EnumFacing.NORTH.getIndex())
            return EnumFacing.SOUTH.getIndex(); 
          if (meta == EnumFacing.SOUTH.getIndex())
            return EnumFacing.NORTH.getIndex(); 
        } else if (this.coordBaseMode == EnumFacing.WEST) {
          if (meta == EnumFacing.NORTH.getIndex())
            return EnumFacing.WEST.getIndex(); 
          if (meta == EnumFacing.SOUTH.getIndex())
            return EnumFacing.EAST.getIndex(); 
          if (meta == EnumFacing.WEST.getIndex())
            return EnumFacing.NORTH.getIndex(); 
          if (meta == EnumFacing.EAST.getIndex())
            return EnumFacing.SOUTH.getIndex(); 
        } else if (this.coordBaseMode == EnumFacing.EAST) {
          if (meta == EnumFacing.NORTH.getIndex())
            return EnumFacing.EAST.getIndex(); 
          if (meta == EnumFacing.SOUTH.getIndex())
            return EnumFacing.WEST.getIndex(); 
          if (meta == EnumFacing.WEST.getIndex())
            return EnumFacing.NORTH.getIndex(); 
          if (meta == EnumFacing.EAST.getIndex())
            return EnumFacing.SOUTH.getIndex(); 
        } 
      } else if (blockIn == Blocks.stone_button) {
        if (this.coordBaseMode == EnumFacing.SOUTH) {
          if (meta == 3)
            return 4; 
          if (meta == 4)
            return 3; 
        } else if (this.coordBaseMode == EnumFacing.WEST) {
          if (meta == 3)
            return 1; 
          if (meta == 4)
            return 2; 
          if (meta == 2)
            return 3; 
          if (meta == 1)
            return 4; 
        } else if (this.coordBaseMode == EnumFacing.EAST) {
          if (meta == 3)
            return 2; 
          if (meta == 4)
            return 1; 
          if (meta == 2)
            return 3; 
          if (meta == 1)
            return 4; 
        } 
      } else if (blockIn != Blocks.tripwire_hook && !(blockIn instanceof net.minecraft.block.BlockDirectional)) {
        if (blockIn == Blocks.piston || blockIn == Blocks.sticky_piston || blockIn == Blocks.lever || blockIn == Blocks.dispenser)
          if (this.coordBaseMode == EnumFacing.SOUTH) {
            if (meta == EnumFacing.NORTH.getIndex() || meta == EnumFacing.SOUTH.getIndex())
              return EnumFacing.getFront(meta).getOpposite().getIndex(); 
          } else if (this.coordBaseMode == EnumFacing.WEST) {
            if (meta == EnumFacing.NORTH.getIndex())
              return EnumFacing.WEST.getIndex(); 
            if (meta == EnumFacing.SOUTH.getIndex())
              return EnumFacing.EAST.getIndex(); 
            if (meta == EnumFacing.WEST.getIndex())
              return EnumFacing.NORTH.getIndex(); 
            if (meta == EnumFacing.EAST.getIndex())
              return EnumFacing.SOUTH.getIndex(); 
          } else if (this.coordBaseMode == EnumFacing.EAST) {
            if (meta == EnumFacing.NORTH.getIndex())
              return EnumFacing.EAST.getIndex(); 
            if (meta == EnumFacing.SOUTH.getIndex())
              return EnumFacing.WEST.getIndex(); 
            if (meta == EnumFacing.WEST.getIndex())
              return EnumFacing.NORTH.getIndex(); 
            if (meta == EnumFacing.EAST.getIndex())
              return EnumFacing.SOUTH.getIndex(); 
          }  
      } else {
        EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
        if (this.coordBaseMode == EnumFacing.SOUTH) {
          if (enumfacing == EnumFacing.SOUTH || enumfacing == EnumFacing.NORTH)
            return enumfacing.getOpposite().getHorizontalIndex(); 
        } else if (this.coordBaseMode == EnumFacing.WEST) {
          if (enumfacing == EnumFacing.NORTH)
            return EnumFacing.WEST.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.SOUTH)
            return EnumFacing.EAST.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.WEST)
            return EnumFacing.NORTH.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.EAST)
            return EnumFacing.SOUTH.getHorizontalIndex(); 
        } else if (this.coordBaseMode == EnumFacing.EAST) {
          if (enumfacing == EnumFacing.NORTH)
            return EnumFacing.EAST.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.SOUTH)
            return EnumFacing.WEST.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.WEST)
            return EnumFacing.NORTH.getHorizontalIndex(); 
          if (enumfacing == EnumFacing.EAST)
            return EnumFacing.SOUTH.getHorizontalIndex(); 
        } 
      } 
    } else if (this.coordBaseMode == EnumFacing.SOUTH) {
      if (meta == 2)
        return 3; 
      if (meta == 3)
        return 2; 
    } else if (this.coordBaseMode == EnumFacing.WEST) {
      if (meta == 0)
        return 2; 
      if (meta == 1)
        return 3; 
      if (meta == 2)
        return 0; 
      if (meta == 3)
        return 1; 
    } else if (this.coordBaseMode == EnumFacing.EAST) {
      if (meta == 0)
        return 2; 
      if (meta == 1)
        return 3; 
      if (meta == 2)
        return 1; 
      if (meta == 3)
        return 0; 
    } 
    return meta;
  }
  
  protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
    BlockPos blockpos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    if (boundingboxIn.isVecInside((Vec3i)blockpos))
      worldIn.setBlockState(blockpos, blockstateIn, 2); 
  }
  
  protected IBlockState getBlockStateFromPos(World worldIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
    int i = getXWithOffset(x, z);
    int j = getYWithOffset(y);
    int k = getZWithOffset(x, z);
    BlockPos blockpos = new BlockPos(i, j, k);
    return !boundingboxIn.isVecInside((Vec3i)blockpos) ? Blocks.air.getDefaultState() : worldIn.getBlockState(blockpos);
  }
  
  protected void fillWithAir(World worldIn, StructureBoundingBox structurebb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    for (int i = minY; i <= maxY; i++) {
      for (int j = minX; j <= maxX; j++) {
        for (int k = minZ; k <= maxZ; k++)
          setBlockState(worldIn, Blocks.air.getDefaultState(), j, i, k, structurebb); 
      } 
    } 
  }
  
  protected void fillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean existingOnly) {
    for (int i = yMin; i <= yMax; i++) {
      for (int j = xMin; j <= xMax; j++) {
        for (int k = zMin; k <= zMax; k++) {
          if (!existingOnly || getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getBlock().getMaterial() != Material.air)
            if (i != yMin && i != yMax && j != xMin && j != xMax && k != zMin && k != zMax) {
              setBlockState(worldIn, insideBlockState, j, i, k, boundingboxIn);
            } else {
              setBlockState(worldIn, boundaryBlockState, j, i, k, boundingboxIn);
            }  
        } 
      } 
    } 
  }
  
  protected void fillWithRandomizedBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean alwaysReplace, Random rand, BlockSelector blockselector) {
    for (int i = minY; i <= maxY; i++) {
      for (int j = minX; j <= maxX; j++) {
        for (int k = minZ; k <= maxZ; k++) {
          if (!alwaysReplace || getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getBlock().getMaterial() != Material.air) {
            blockselector.selectBlocks(rand, j, i, k, (i == minY || i == maxY || j == minX || j == maxX || k == minZ || k == maxZ));
            setBlockState(worldIn, blockselector.getBlockState(), j, i, k, boundingboxIn);
          } 
        } 
      } 
    } 
  }
  
  protected void func_175805_a(World worldIn, StructureBoundingBox boundingboxIn, Random rand, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState blockstate1, IBlockState blockstate2, boolean p_175805_13_) {
    for (int i = minY; i <= maxY; i++) {
      for (int j = minX; j <= maxX; j++) {
        for (int k = minZ; k <= maxZ; k++) {
          if (rand.nextFloat() <= chance && (!p_175805_13_ || getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getBlock().getMaterial() != Material.air))
            if (i != minY && i != maxY && j != minX && j != maxX && k != minZ && k != maxZ) {
              setBlockState(worldIn, blockstate2, j, i, k, boundingboxIn);
            } else {
              setBlockState(worldIn, blockstate1, j, i, k, boundingboxIn);
            }  
        } 
      } 
    } 
  }
  
  protected void randomlyPlaceBlock(World worldIn, StructureBoundingBox boundingboxIn, Random rand, float chance, int x, int y, int z, IBlockState blockstateIn) {
    if (rand.nextFloat() < chance)
      setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn); 
  }
  
  protected void randomlyRareFillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState blockstateIn, boolean p_180777_10_) {
    float f = (maxX - minX + 1);
    float f1 = (maxY - minY + 1);
    float f2 = (maxZ - minZ + 1);
    float f3 = minX + f / 2.0F;
    float f4 = minZ + f2 / 2.0F;
    for (int i = minY; i <= maxY; i++) {
      float f5 = (i - minY) / f1;
      for (int j = minX; j <= maxX; j++) {
        float f6 = (j - f3) / f * 0.5F;
        for (int k = minZ; k <= maxZ; k++) {
          float f7 = (k - f4) / f2 * 0.5F;
          if (!p_180777_10_ || getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getBlock().getMaterial() != Material.air) {
            float f8 = f6 * f6 + f5 * f5 + f7 * f7;
            if (f8 <= 1.05F)
              setBlockState(worldIn, blockstateIn, j, i, k, boundingboxIn); 
          } 
        } 
      } 
    } 
  }
  
  protected void clearCurrentPositionBlocksUpwards(World worldIn, int x, int y, int z, StructureBoundingBox structurebb) {
    BlockPos blockpos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    if (structurebb.isVecInside((Vec3i)blockpos))
      while (!worldIn.isAirBlock(blockpos) && blockpos.getY() < 255) {
        worldIn.setBlockState(blockpos, Blocks.air.getDefaultState(), 2);
        blockpos = blockpos.up();
      }  
  }
  
  protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
    int i = getXWithOffset(x, z);
    int j = getYWithOffset(y);
    int k = getZWithOffset(x, z);
    if (boundingboxIn.isVecInside((Vec3i)new BlockPos(i, j, k)))
      while ((worldIn.isAirBlock(new BlockPos(i, j, k)) || worldIn.getBlockState(new BlockPos(i, j, k)).getBlock().getMaterial().isLiquid()) && j > 1) {
        worldIn.setBlockState(new BlockPos(i, j, k), blockstateIn, 2);
        j--;
      }  
  }
  
  protected boolean generateChestContents(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, List<WeightedRandomChestContent> listIn, int max) {
    BlockPos blockpos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    if (boundingBoxIn.isVecInside((Vec3i)blockpos) && worldIn.getBlockState(blockpos).getBlock() != Blocks.chest) {
      IBlockState iblockstate = Blocks.chest.getDefaultState();
      worldIn.setBlockState(blockpos, Blocks.chest.correctFacing(worldIn, blockpos, iblockstate), 2);
      TileEntity tileentity = worldIn.getTileEntity(blockpos);
      if (tileentity instanceof net.minecraft.tileentity.TileEntityChest)
        WeightedRandomChestContent.generateChestContents(rand, listIn, (IInventory)tileentity, max); 
      return true;
    } 
    return false;
  }
  
  protected boolean generateDispenserContents(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, int meta, List<WeightedRandomChestContent> listIn, int max) {
    BlockPos blockpos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    if (boundingBoxIn.isVecInside((Vec3i)blockpos) && worldIn.getBlockState(blockpos).getBlock() != Blocks.dispenser) {
      worldIn.setBlockState(blockpos, Blocks.dispenser.getStateFromMeta(getMetadataWithOffset(Blocks.dispenser, meta)), 2);
      TileEntity tileentity = worldIn.getTileEntity(blockpos);
      if (tileentity instanceof TileEntityDispenser)
        WeightedRandomChestContent.generateDispenserContents(rand, listIn, (TileEntityDispenser)tileentity, max); 
      return true;
    } 
    return false;
  }
  
  protected void placeDoorCurrentPosition(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, EnumFacing facing) {
    BlockPos blockpos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    if (boundingBoxIn.isVecInside((Vec3i)blockpos))
      ItemDoor.placeDoor(worldIn, blockpos, facing.rotateYCCW(), Blocks.oak_door); 
  }
  
  public void func_181138_a(int p_181138_1_, int p_181138_2_, int p_181138_3_) {
    this.boundingBox.offset(p_181138_1_, p_181138_2_, p_181138_3_);
  }
  
  public static abstract class BlockSelector {
    protected IBlockState blockstate = Blocks.air.getDefaultState();
    
    public abstract void selectBlocks(Random param1Random, int param1Int1, int param1Int2, int param1Int3, boolean param1Boolean);
    
    public IBlockState getBlockState() {
      return this.blockstate;
    }
  }
}
