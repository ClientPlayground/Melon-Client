package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBanner extends BlockContainer {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", (Predicate)EnumFacing.Plane.HORIZONTAL);
  
  public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);
  
  protected BlockBanner() {
    super(Material.wood);
    float f = 0.25F;
    float f1 = 1.0F;
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
  }
  
  public String getLocalizedName() {
    return StatCollector.translateToLocal("item.banner.white.name");
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
    setBlockBoundsBasedOnState((IBlockAccess)worldIn, pos);
    return super.getSelectedBoundingBox(worldIn, pos);
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    return true;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean canSpawnInBlock() {
    return true;
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return (TileEntity)new TileEntityBanner();
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.banner;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return Items.banner;
  }
  
  public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityBanner) {
      ItemStack itemstack = new ItemStack(Items.banner, 1, ((TileEntityBanner)tileentity).getBaseColor());
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      tileentity.writeToNBT(nbttagcompound);
      nbttagcompound.removeTag("x");
      nbttagcompound.removeTag("y");
      nbttagcompound.removeTag("z");
      nbttagcompound.removeTag("id");
      itemstack.setTagInfo("BlockEntityTag", (NBTBase)nbttagcompound);
      spawnAsEntity(worldIn, pos, itemstack);
    } else {
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    } 
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return (!hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos));
  }
  
  public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
    if (te instanceof TileEntityBanner) {
      TileEntityBanner tileentitybanner = (TileEntityBanner)te;
      ItemStack itemstack = new ItemStack(Items.banner, 1, ((TileEntityBanner)te).getBaseColor());
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      TileEntityBanner.setBaseColorAndPatterns(nbttagcompound, tileentitybanner.getBaseColor(), tileentitybanner.getPatterns());
      itemstack.setTagInfo("BlockEntityTag", (NBTBase)nbttagcompound);
      spawnAsEntity(worldIn, pos, itemstack);
    } else {
      super.harvestBlock(worldIn, player, pos, state, (TileEntity)null);
    } 
  }
  
  public static class BlockBannerHanging extends BlockBanner {
    public BlockBannerHanging() {
      setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH));
    }
    
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
      EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue((IProperty)FACING);
      float f = 0.0F;
      float f1 = 0.78125F;
      float f2 = 0.0F;
      float f3 = 1.0F;
      float f4 = 0.125F;
      setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      switch (enumfacing) {
        default:
          setBlockBounds(f2, f, 1.0F - f4, f3, f1, 1.0F);
          return;
        case SOUTH:
          setBlockBounds(f2, f, 0.0F, f3, f1, f4);
          return;
        case WEST:
          setBlockBounds(1.0F - f4, f, f2, 1.0F, f1, f3);
          return;
        case EAST:
          break;
      } 
      setBlockBounds(0.0F, f, f2, f4, f1, f3);
    }
    
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
      EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
      if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock().getMaterial().isSolid()) {
        dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
      } 
      super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    }
    
    public IBlockState getStateFromMeta(int meta) {
      EnumFacing enumfacing = EnumFacing.getFront(meta);
      if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        enumfacing = EnumFacing.NORTH; 
      return getDefaultState().withProperty((IProperty)FACING, (Comparable)enumfacing);
    }
    
    public int getMetaFromState(IBlockState state) {
      return ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
    }
    
    protected BlockState createBlockState() {
      return new BlockState(this, new IProperty[] { (IProperty)FACING });
    }
  }
  
  public static class BlockBannerStanding extends BlockBanner {
    public BlockBannerStanding() {
      setDefaultState(this.blockState.getBaseState().withProperty((IProperty)ROTATION, Integer.valueOf(0)));
    }
    
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
      if (!worldIn.getBlockState(pos.down()).getBlock().getMaterial().isSolid()) {
        dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
      } 
      super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    }
    
    public IBlockState getStateFromMeta(int meta) {
      return getDefaultState().withProperty((IProperty)ROTATION, Integer.valueOf(meta));
    }
    
    public int getMetaFromState(IBlockState state) {
      return ((Integer)state.getValue((IProperty)ROTATION)).intValue();
    }
    
    protected BlockState createBlockState() {
      return new BlockState(this, new IProperty[] { (IProperty)ROTATION });
    }
  }
}
