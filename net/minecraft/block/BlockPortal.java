package net.minecraft.block;

import com.google.common.cache.LoadingCache;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPortal extends BlockBreakable {
  public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, (Enum[])new EnumFacing.Axis[] { EnumFacing.Axis.X, EnumFacing.Axis.Z });
  
  public BlockPortal() {
    super(Material.portal, false);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)AXIS, (Comparable)EnumFacing.Axis.X));
    setTickRandomly(true);
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    super.updateTick(worldIn, pos, state, rand);
    if (worldIn.provider.isSurfaceWorld() && worldIn.getGameRules().getGameRuleBooleanValue("doMobSpawning") && rand.nextInt(2000) < worldIn.getDifficulty().getDifficultyId()) {
      int i = pos.getY();
      BlockPos blockpos;
      for (blockpos = pos; !World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, blockpos) && blockpos.getY() > 0; blockpos = blockpos.down());
      if (i > 0 && !worldIn.getBlockState(blockpos.up()).getBlock().isNormalCube()) {
        Entity entity = ItemMonsterPlacer.spawnCreature(worldIn, 57, blockpos.getX() + 0.5D, blockpos.getY() + 1.1D, blockpos.getZ() + 0.5D);
        if (entity != null)
          entity.timeUntilPortal = entity.getPortalCooldown(); 
      } 
    } 
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis)worldIn.getBlockState(pos).getValue((IProperty)AXIS);
    float f = 0.125F;
    float f1 = 0.125F;
    if (enumfacing$axis == EnumFacing.Axis.X)
      f = 0.5F; 
    if (enumfacing$axis == EnumFacing.Axis.Z)
      f1 = 0.5F; 
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1);
  }
  
  public static int getMetaForAxis(EnumFacing.Axis axis) {
    return (axis == EnumFacing.Axis.X) ? 1 : ((axis == EnumFacing.Axis.Z) ? 2 : 0);
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean func_176548_d(World worldIn, BlockPos p_176548_2_) {
    Size blockportal$size = new Size(worldIn, p_176548_2_, EnumFacing.Axis.X);
    if (blockportal$size.func_150860_b() && blockportal$size.field_150864_e == 0) {
      blockportal$size.func_150859_c();
      return true;
    } 
    Size blockportal$size1 = new Size(worldIn, p_176548_2_, EnumFacing.Axis.Z);
    if (blockportal$size1.func_150860_b() && blockportal$size1.field_150864_e == 0) {
      blockportal$size1.func_150859_c();
      return true;
    } 
    return false;
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis)state.getValue((IProperty)AXIS);
    if (enumfacing$axis == EnumFacing.Axis.X) {
      Size blockportal$size = new Size(worldIn, pos, EnumFacing.Axis.X);
      if (!blockportal$size.func_150860_b() || blockportal$size.field_150864_e < blockportal$size.field_150868_h * blockportal$size.field_150862_g)
        worldIn.setBlockState(pos, Blocks.air.getDefaultState()); 
    } else if (enumfacing$axis == EnumFacing.Axis.Z) {
      Size blockportal$size1 = new Size(worldIn, pos, EnumFacing.Axis.Z);
      if (!blockportal$size1.func_150860_b() || blockportal$size1.field_150864_e < blockportal$size1.field_150868_h * blockportal$size1.field_150862_g)
        worldIn.setBlockState(pos, Blocks.air.getDefaultState()); 
    } 
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    EnumFacing.Axis enumfacing$axis = null;
    IBlockState iblockstate = worldIn.getBlockState(pos);
    if (worldIn.getBlockState(pos).getBlock() == this) {
      enumfacing$axis = (EnumFacing.Axis)iblockstate.getValue((IProperty)AXIS);
      if (enumfacing$axis == null)
        return false; 
      if (enumfacing$axis == EnumFacing.Axis.Z && side != EnumFacing.EAST && side != EnumFacing.WEST)
        return false; 
      if (enumfacing$axis == EnumFacing.Axis.X && side != EnumFacing.SOUTH && side != EnumFacing.NORTH)
        return false; 
    } 
    boolean flag = (worldIn.getBlockState(pos.west()).getBlock() == this && worldIn.getBlockState(pos.west(2)).getBlock() != this);
    boolean flag1 = (worldIn.getBlockState(pos.east()).getBlock() == this && worldIn.getBlockState(pos.east(2)).getBlock() != this);
    boolean flag2 = (worldIn.getBlockState(pos.north()).getBlock() == this && worldIn.getBlockState(pos.north(2)).getBlock() != this);
    boolean flag3 = (worldIn.getBlockState(pos.south()).getBlock() == this && worldIn.getBlockState(pos.south(2)).getBlock() != this);
    boolean flag4 = (flag || flag1 || enumfacing$axis == EnumFacing.Axis.X);
    boolean flag5 = (flag2 || flag3 || enumfacing$axis == EnumFacing.Axis.Z);
    return (flag4 && side == EnumFacing.WEST) ? true : ((flag4 && side == EnumFacing.EAST) ? true : ((flag5 && side == EnumFacing.NORTH) ? true : ((flag5 && side == EnumFacing.SOUTH))));
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.TRANSLUCENT;
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    if (entityIn.ridingEntity == null && entityIn.riddenByEntity == null)
      entityIn.setPortal(pos); 
  }
  
  public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (rand.nextInt(100) == 0)
      worldIn.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "portal.portal", 0.5F, rand.nextFloat() * 0.4F + 0.8F, false); 
    for (int i = 0; i < 4; i++) {
      double d0 = (pos.getX() + rand.nextFloat());
      double d1 = (pos.getY() + rand.nextFloat());
      double d2 = (pos.getZ() + rand.nextFloat());
      double d3 = (rand.nextFloat() - 0.5D) * 0.5D;
      double d4 = (rand.nextFloat() - 0.5D) * 0.5D;
      double d5 = (rand.nextFloat() - 0.5D) * 0.5D;
      int j = rand.nextInt(2) * 2 - 1;
      if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this) {
        d0 = pos.getX() + 0.5D + 0.25D * j;
        d3 = (rand.nextFloat() * 2.0F * j);
      } else {
        d2 = pos.getZ() + 0.5D + 0.25D * j;
        d5 = (rand.nextFloat() * 2.0F * j);
      } 
      worldIn.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5, new int[0]);
    } 
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return null;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)AXIS, ((meta & 0x3) == 2) ? (Comparable)EnumFacing.Axis.Z : (Comparable)EnumFacing.Axis.X);
  }
  
  public int getMetaFromState(IBlockState state) {
    return getMetaForAxis((EnumFacing.Axis)state.getValue((IProperty)AXIS));
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)AXIS });
  }
  
  public BlockPattern.PatternHelper func_181089_f(World p_181089_1_, BlockPos p_181089_2_) {
    EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Z;
    Size blockportal$size = new Size(p_181089_1_, p_181089_2_, EnumFacing.Axis.X);
    LoadingCache<BlockPos, BlockWorldState> loadingcache = BlockPattern.func_181627_a(p_181089_1_, true);
    if (!blockportal$size.func_150860_b()) {
      enumfacing$axis = EnumFacing.Axis.X;
      blockportal$size = new Size(p_181089_1_, p_181089_2_, EnumFacing.Axis.Z);
    } 
    if (!blockportal$size.func_150860_b())
      return new BlockPattern.PatternHelper(p_181089_2_, EnumFacing.NORTH, EnumFacing.UP, loadingcache, 1, 1, 1); 
    int[] aint = new int[(EnumFacing.AxisDirection.values()).length];
    EnumFacing enumfacing = blockportal$size.field_150866_c.rotateYCCW();
    BlockPos blockpos = blockportal$size.field_150861_f.up(blockportal$size.func_181100_a() - 1);
    for (EnumFacing.AxisDirection enumfacing$axisdirection : EnumFacing.AxisDirection.values()) {
      BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper((enumfacing.getAxisDirection() == enumfacing$axisdirection) ? blockpos : blockpos.offset(blockportal$size.field_150866_c, blockportal$size.func_181101_b() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.func_181101_b(), blockportal$size.func_181100_a(), 1);
      for (int i = 0; i < blockportal$size.func_181101_b(); i++) {
        for (int j = 0; j < blockportal$size.func_181100_a(); j++) {
          BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);
          if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getBlock().getMaterial() != Material.air)
            aint[enumfacing$axisdirection.ordinal()] = aint[enumfacing$axisdirection.ordinal()] + 1; 
        } 
      } 
    } 
    EnumFacing.AxisDirection enumfacing$axisdirection1 = EnumFacing.AxisDirection.POSITIVE;
    for (EnumFacing.AxisDirection enumfacing$axisdirection2 : EnumFacing.AxisDirection.values()) {
      if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()])
        enumfacing$axisdirection1 = enumfacing$axisdirection2; 
    } 
    return new BlockPattern.PatternHelper((enumfacing.getAxisDirection() == enumfacing$axisdirection1) ? blockpos : blockpos.offset(blockportal$size.field_150866_c, blockportal$size.func_181101_b() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection1, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.func_181101_b(), blockportal$size.func_181100_a(), 1);
  }
  
  public static class Size {
    private final World world;
    
    private final EnumFacing.Axis axis;
    
    private final EnumFacing field_150866_c;
    
    private final EnumFacing field_150863_d;
    
    private int field_150864_e = 0;
    
    private BlockPos field_150861_f;
    
    private int field_150862_g;
    
    private int field_150868_h;
    
    public Size(World worldIn, BlockPos p_i45694_2_, EnumFacing.Axis p_i45694_3_) {
      this.world = worldIn;
      this.axis = p_i45694_3_;
      if (p_i45694_3_ == EnumFacing.Axis.X) {
        this.field_150863_d = EnumFacing.EAST;
        this.field_150866_c = EnumFacing.WEST;
      } else {
        this.field_150863_d = EnumFacing.NORTH;
        this.field_150866_c = EnumFacing.SOUTH;
      } 
      for (BlockPos blockpos = p_i45694_2_; p_i45694_2_.getY() > blockpos.getY() - 21 && p_i45694_2_.getY() > 0 && func_150857_a(worldIn.getBlockState(p_i45694_2_.down()).getBlock()); p_i45694_2_ = p_i45694_2_.down());
      int i = func_180120_a(p_i45694_2_, this.field_150863_d) - 1;
      if (i >= 0) {
        this.field_150861_f = p_i45694_2_.offset(this.field_150863_d, i);
        this.field_150868_h = func_180120_a(this.field_150861_f, this.field_150866_c);
        if (this.field_150868_h < 2 || this.field_150868_h > 21) {
          this.field_150861_f = null;
          this.field_150868_h = 0;
        } 
      } 
      if (this.field_150861_f != null)
        this.field_150862_g = func_150858_a(); 
    }
    
    protected int func_180120_a(BlockPos p_180120_1_, EnumFacing p_180120_2_) {
      int i;
      for (i = 0; i < 22; i++) {
        BlockPos blockpos = p_180120_1_.offset(p_180120_2_, i);
        if (!func_150857_a(this.world.getBlockState(blockpos).getBlock()) || this.world.getBlockState(blockpos.down()).getBlock() != Blocks.obsidian)
          break; 
      } 
      Block block = this.world.getBlockState(p_180120_1_.offset(p_180120_2_, i)).getBlock();
      return (block == Blocks.obsidian) ? i : 0;
    }
    
    public int func_181100_a() {
      return this.field_150862_g;
    }
    
    public int func_181101_b() {
      return this.field_150868_h;
    }
    
    protected int func_150858_a() {
      label38: for (this.field_150862_g = 0; this.field_150862_g < 21; this.field_150862_g++) {
        for (int i = 0; i < this.field_150868_h; i++) {
          BlockPos blockpos = this.field_150861_f.offset(this.field_150866_c, i).up(this.field_150862_g);
          Block block = this.world.getBlockState(blockpos).getBlock();
          if (!func_150857_a(block))
            break label38; 
          if (block == Blocks.portal)
            this.field_150864_e++; 
          if (i == 0) {
            block = this.world.getBlockState(blockpos.offset(this.field_150863_d)).getBlock();
            if (block != Blocks.obsidian)
              break label38; 
          } else if (i == this.field_150868_h - 1) {
            block = this.world.getBlockState(blockpos.offset(this.field_150866_c)).getBlock();
            if (block != Blocks.obsidian)
              break label38; 
          } 
        } 
      } 
      for (int j = 0; j < this.field_150868_h; j++) {
        if (this.world.getBlockState(this.field_150861_f.offset(this.field_150866_c, j).up(this.field_150862_g)).getBlock() != Blocks.obsidian) {
          this.field_150862_g = 0;
          break;
        } 
      } 
      if (this.field_150862_g <= 21 && this.field_150862_g >= 3)
        return this.field_150862_g; 
      this.field_150861_f = null;
      this.field_150868_h = 0;
      this.field_150862_g = 0;
      return 0;
    }
    
    protected boolean func_150857_a(Block p_150857_1_) {
      return (p_150857_1_.blockMaterial == Material.air || p_150857_1_ == Blocks.fire || p_150857_1_ == Blocks.portal);
    }
    
    public boolean func_150860_b() {
      return (this.field_150861_f != null && this.field_150868_h >= 2 && this.field_150868_h <= 21 && this.field_150862_g >= 3 && this.field_150862_g <= 21);
    }
    
    public void func_150859_c() {
      for (int i = 0; i < this.field_150868_h; i++) {
        BlockPos blockpos = this.field_150861_f.offset(this.field_150866_c, i);
        for (int j = 0; j < this.field_150862_g; j++)
          this.world.setBlockState(blockpos.up(j), Blocks.portal.getDefaultState().withProperty((IProperty)BlockPortal.AXIS, (Comparable)this.axis), 2); 
      } 
    }
  }
}
