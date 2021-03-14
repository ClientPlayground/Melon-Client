package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockButton extends Block {
  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  
  public static final PropertyBool POWERED = PropertyBool.create("powered");
  
  private final boolean wooden;
  
  protected BlockButton(boolean wooden) {
    super(Material.circuits);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)POWERED, Boolean.valueOf(false)));
    setTickRandomly(true);
    setCreativeTab(CreativeTabs.tabRedstone);
    this.wooden = wooden;
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public int tickRate(World worldIn) {
    return this.wooden ? 30 : 20;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
    return func_181088_a(worldIn, pos, side.getOpposite());
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    for (EnumFacing enumfacing : EnumFacing.values()) {
      if (func_181088_a(worldIn, pos, enumfacing))
        return true; 
    } 
    return false;
  }
  
  protected static boolean func_181088_a(World p_181088_0_, BlockPos p_181088_1_, EnumFacing p_181088_2_) {
    BlockPos blockpos = p_181088_1_.offset(p_181088_2_);
    return (p_181088_2_ == EnumFacing.DOWN) ? World.doesBlockHaveSolidTopSurface((IBlockAccess)p_181088_0_, blockpos) : p_181088_0_.getBlockState(blockpos).getBlock().isNormalCube();
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    return func_181088_a(worldIn, pos, facing.getOpposite()) ? getDefaultState().withProperty((IProperty)FACING, (Comparable)facing).withProperty((IProperty)POWERED, Boolean.valueOf(false)) : getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.DOWN).withProperty((IProperty)POWERED, Boolean.valueOf(false));
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    if (checkForDrop(worldIn, pos, state) && !func_181088_a(worldIn, pos, ((EnumFacing)state.getValue((IProperty)FACING)).getOpposite())) {
      dropBlockAsItem(worldIn, pos, state, 0);
      worldIn.setBlockToAir(pos);
    } 
  }
  
  private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
    if (canPlaceBlockAt(worldIn, pos))
      return true; 
    dropBlockAsItem(worldIn, pos, state, 0);
    worldIn.setBlockToAir(pos);
    return false;
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    updateBlockBounds(worldIn.getBlockState(pos));
  }
  
  private void updateBlockBounds(IBlockState state) {
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    boolean flag = ((Boolean)state.getValue((IProperty)POWERED)).booleanValue();
    float f = 0.25F;
    float f1 = 0.375F;
    float f2 = (flag ? true : 2) / 16.0F;
    float f3 = 0.125F;
    float f4 = 0.1875F;
    switch (enumfacing) {
      case EAST:
        setBlockBounds(0.0F, 0.375F, 0.3125F, f2, 0.625F, 0.6875F);
        break;
      case WEST:
        setBlockBounds(1.0F - f2, 0.375F, 0.3125F, 1.0F, 0.625F, 0.6875F);
        break;
      case SOUTH:
        setBlockBounds(0.3125F, 0.375F, 0.0F, 0.6875F, 0.625F, f2);
        break;
      case NORTH:
        setBlockBounds(0.3125F, 0.375F, 1.0F - f2, 0.6875F, 0.625F, 1.0F);
        break;
      case UP:
        setBlockBounds(0.3125F, 0.0F, 0.375F, 0.6875F, 0.0F + f2, 0.625F);
        break;
      case DOWN:
        setBlockBounds(0.3125F, 1.0F - f2, 0.375F, 0.6875F, 1.0F, 0.625F);
        break;
    } 
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
      return true; 
    worldIn.setBlockState(pos, state.withProperty((IProperty)POWERED, Boolean.valueOf(true)), 3);
    worldIn.markBlockRangeForRenderUpdate(pos, pos);
    worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
    notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING));
    worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
    return true;
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
      notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING)); 
    super.breakBlock(worldIn, pos, state);
  }
  
  public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    return ((Boolean)state.getValue((IProperty)POWERED)).booleanValue() ? 15 : 0;
  }
  
  public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    return !((Boolean)state.getValue((IProperty)POWERED)).booleanValue() ? 0 : ((state.getValue((IProperty)FACING) == side) ? 15 : 0);
  }
  
  public boolean canProvidePower() {
    return true;
  }
  
  public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {}
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (!worldIn.isRemote)
      if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
        if (this.wooden) {
          checkForArrows(worldIn, pos, state);
        } else {
          worldIn.setBlockState(pos, state.withProperty((IProperty)POWERED, Boolean.valueOf(false)));
          notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING));
          worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.5F);
          worldIn.markBlockRangeForRenderUpdate(pos, pos);
        }   
  }
  
  public void setBlockBoundsForItemRender() {
    float f = 0.1875F;
    float f1 = 0.125F;
    float f2 = 0.125F;
    setBlockBounds(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1, 0.5F + f2);
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    if (!worldIn.isRemote)
      if (this.wooden)
        if (!((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
          checkForArrows(worldIn, pos, state);   
  }
  
  private void checkForArrows(World worldIn, BlockPos pos, IBlockState state) {
    updateBlockBounds(state);
    List<? extends Entity> list = worldIn.getEntitiesWithinAABB(EntityArrow.class, new AxisAlignedBB(pos.getX() + this.minX, pos.getY() + this.minY, pos.getZ() + this.minZ, pos.getX() + this.maxX, pos.getY() + this.maxY, pos.getZ() + this.maxZ));
    boolean flag = !list.isEmpty();
    boolean flag1 = ((Boolean)state.getValue((IProperty)POWERED)).booleanValue();
    if (flag && !flag1) {
      worldIn.setBlockState(pos, state.withProperty((IProperty)POWERED, Boolean.valueOf(true)));
      notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING));
      worldIn.markBlockRangeForRenderUpdate(pos, pos);
      worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
    } 
    if (!flag && flag1) {
      worldIn.setBlockState(pos, state.withProperty((IProperty)POWERED, Boolean.valueOf(false)));
      notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING));
      worldIn.markBlockRangeForRenderUpdate(pos, pos);
      worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.5F);
    } 
    if (flag)
      worldIn.scheduleUpdate(pos, this, tickRate(worldIn)); 
  }
  
  private void notifyNeighbors(World worldIn, BlockPos pos, EnumFacing facing) {
    worldIn.notifyNeighborsOfStateChange(pos, this);
    worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    EnumFacing enumfacing;
    switch (meta & 0x7) {
      case 0:
        enumfacing = EnumFacing.DOWN;
        break;
      case 1:
        enumfacing = EnumFacing.EAST;
        break;
      case 2:
        enumfacing = EnumFacing.WEST;
        break;
      case 3:
        enumfacing = EnumFacing.SOUTH;
        break;
      case 4:
        enumfacing = EnumFacing.NORTH;
        break;
      default:
        enumfacing = EnumFacing.UP;
        break;
    } 
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)enumfacing).withProperty((IProperty)POWERED, Boolean.valueOf(((meta & 0x8) > 0)));
  }
  
  public int getMetaFromState(IBlockState state) {
    int i;
    switch ((EnumFacing)state.getValue((IProperty)FACING)) {
      case EAST:
        i = 1;
        break;
      case WEST:
        i = 2;
        break;
      case SOUTH:
        i = 3;
        break;
      case NORTH:
        i = 4;
        break;
      default:
        i = 5;
        break;
      case DOWN:
        i = 0;
        break;
    } 
    if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
      i |= 0x8; 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)POWERED });
  }
}
