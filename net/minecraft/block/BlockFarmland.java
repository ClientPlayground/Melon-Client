package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFarmland extends Block {
  public static final PropertyInteger MOISTURE = PropertyInteger.create("moisture", 0, 7);
  
  protected BlockFarmland() {
    super(Material.ground);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)MOISTURE, Integer.valueOf(0)));
    setTickRandomly(true);
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
    setLightOpacity(255);
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    int i = ((Integer)state.getValue((IProperty)MOISTURE)).intValue();
    if (!hasWater(worldIn, pos) && !worldIn.canLightningStrike(pos.up())) {
      if (i > 0) {
        worldIn.setBlockState(pos, state.withProperty((IProperty)MOISTURE, Integer.valueOf(i - 1)), 2);
      } else if (!hasCrops(worldIn, pos)) {
        worldIn.setBlockState(pos, Blocks.dirt.getDefaultState());
      } 
    } else if (i < 7) {
      worldIn.setBlockState(pos, state.withProperty((IProperty)MOISTURE, Integer.valueOf(7)), 2);
    } 
  }
  
  public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
    if (entityIn instanceof net.minecraft.entity.EntityLivingBase) {
      if (!worldIn.isRemote && worldIn.rand.nextFloat() < fallDistance - 0.5F) {
        if (!(entityIn instanceof net.minecraft.entity.player.EntityPlayer) && !worldIn.getGameRules().getGameRuleBooleanValue("mobGriefing"))
          return; 
        worldIn.setBlockState(pos, Blocks.dirt.getDefaultState());
      } 
      super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    } 
  }
  
  private boolean hasCrops(World worldIn, BlockPos pos) {
    Block block = worldIn.getBlockState(pos.up()).getBlock();
    return (block instanceof BlockCrops || block instanceof BlockStem);
  }
  
  private boolean hasWater(World worldIn, BlockPos pos) {
    for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
      if (worldIn.getBlockState((BlockPos)blockpos$mutableblockpos).getBlock().getMaterial() == Material.water)
        return true; 
    } 
    return false;
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    if (worldIn.getBlockState(pos.up()).getBlock().getMaterial().isSolid())
      worldIn.setBlockState(pos, Blocks.dirt.getDefaultState()); 
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    Block block;
    switch (side) {
      case UP:
        return true;
      case NORTH:
      case SOUTH:
      case WEST:
      case EAST:
        block = worldIn.getBlockState(pos).getBlock();
        return (!block.isOpaqueCube() && block != Blocks.farmland);
    } 
    return super.shouldSideBeRendered(worldIn, pos, side);
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Blocks.dirt.getItemDropped(Blocks.dirt.getDefaultState().withProperty((IProperty)BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return Item.getItemFromBlock(Blocks.dirt);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)MOISTURE, Integer.valueOf(meta & 0x7));
  }
  
  public int getMetaFromState(IBlockState state) {
    return ((Integer)state.getValue((IProperty)MOISTURE)).intValue();
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)MOISTURE });
  }
}
