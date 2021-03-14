package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockBed extends BlockDirectional {
  public static final PropertyEnum<EnumPartType> PART = PropertyEnum.create("part", EnumPartType.class);
  
  public static final PropertyBool OCCUPIED = PropertyBool.create("occupied");
  
  public BlockBed() {
    super(Material.cloth);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)PART, EnumPartType.FOOT).withProperty((IProperty)OCCUPIED, Boolean.valueOf(false)));
    setBedBounds();
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote)
      return true; 
    if (state.getValue((IProperty)PART) != EnumPartType.HEAD) {
      pos = pos.offset((EnumFacing)state.getValue((IProperty)FACING));
      state = worldIn.getBlockState(pos);
      if (state.getBlock() != this)
        return true; 
    } 
    if (worldIn.provider.canRespawnHere() && worldIn.getBiomeGenForCoords(pos) != BiomeGenBase.hell) {
      if (((Boolean)state.getValue((IProperty)OCCUPIED)).booleanValue()) {
        EntityPlayer entityplayer = getPlayerInBed(worldIn, pos);
        if (entityplayer != null) {
          playerIn.addChatComponentMessage((IChatComponent)new ChatComponentTranslation("tile.bed.occupied", new Object[0]));
          return true;
        } 
        state = state.withProperty((IProperty)OCCUPIED, Boolean.valueOf(false));
        worldIn.setBlockState(pos, state, 4);
      } 
      EntityPlayer.EnumStatus entityplayer$enumstatus = playerIn.trySleep(pos);
      if (entityplayer$enumstatus == EntityPlayer.EnumStatus.OK) {
        state = state.withProperty((IProperty)OCCUPIED, Boolean.valueOf(true));
        worldIn.setBlockState(pos, state, 4);
        return true;
      } 
      if (entityplayer$enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
        playerIn.addChatComponentMessage((IChatComponent)new ChatComponentTranslation("tile.bed.noSleep", new Object[0]));
      } else if (entityplayer$enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
        playerIn.addChatComponentMessage((IChatComponent)new ChatComponentTranslation("tile.bed.notSafe", new Object[0]));
      } 
      return true;
    } 
    worldIn.setBlockToAir(pos);
    BlockPos blockpos = pos.offset(((EnumFacing)state.getValue((IProperty)FACING)).getOpposite());
    if (worldIn.getBlockState(blockpos).getBlock() == this)
      worldIn.setBlockToAir(blockpos); 
    worldIn.newExplosion((Entity)null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, true);
    return true;
  }
  
  private EntityPlayer getPlayerInBed(World worldIn, BlockPos pos) {
    for (EntityPlayer entityplayer : worldIn.playerEntities) {
      if (entityplayer.isPlayerSleeping() && entityplayer.playerLocation.equals(pos))
        return entityplayer; 
    } 
    return null;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    setBedBounds();
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    if (state.getValue((IProperty)PART) == EnumPartType.HEAD) {
      if (worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock() != this)
        worldIn.setBlockToAir(pos); 
    } else if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() != this) {
      worldIn.setBlockToAir(pos);
      if (!worldIn.isRemote)
        dropBlockAsItem(worldIn, pos, state, 0); 
    } 
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return (state.getValue((IProperty)PART) == EnumPartType.HEAD) ? null : Items.bed;
  }
  
  private void setBedBounds() {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
  }
  
  public static BlockPos getSafeExitLocation(World worldIn, BlockPos pos, int tries) {
    EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue((IProperty)FACING);
    int i = pos.getX();
    int j = pos.getY();
    int k = pos.getZ();
    for (int l = 0; l <= 1; l++) {
      int i1 = i - enumfacing.getFrontOffsetX() * l - 1;
      int j1 = k - enumfacing.getFrontOffsetZ() * l - 1;
      int k1 = i1 + 2;
      int l1 = j1 + 2;
      for (int i2 = i1; i2 <= k1; i2++) {
        for (int j2 = j1; j2 <= l1; j2++) {
          BlockPos blockpos = new BlockPos(i2, j, j2);
          if (hasRoomForPlayer(worldIn, blockpos)) {
            if (tries <= 0)
              return blockpos; 
            tries--;
          } 
        } 
      } 
    } 
    return null;
  }
  
  protected static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
    return (World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, pos.down()) && !worldIn.getBlockState(pos).getBlock().getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getBlock().getMaterial().isSolid());
  }
  
  public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    if (state.getValue((IProperty)PART) == EnumPartType.FOOT)
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0); 
  }
  
  public int getMobilityFlag() {
    return 1;
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return Items.bed;
  }
  
  public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
    if (player.capabilities.isCreativeMode && state.getValue((IProperty)PART) == EnumPartType.HEAD) {
      BlockPos blockpos = pos.offset(((EnumFacing)state.getValue((IProperty)FACING)).getOpposite());
      if (worldIn.getBlockState(blockpos).getBlock() == this)
        worldIn.setBlockToAir(blockpos); 
    } 
  }
  
  public IBlockState getStateFromMeta(int meta) {
    EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
    return ((meta & 0x8) > 0) ? getDefaultState().withProperty((IProperty)PART, EnumPartType.HEAD).withProperty((IProperty)FACING, (Comparable)enumfacing).withProperty((IProperty)OCCUPIED, Boolean.valueOf(((meta & 0x4) > 0))) : getDefaultState().withProperty((IProperty)PART, EnumPartType.FOOT).withProperty((IProperty)FACING, (Comparable)enumfacing);
  }
  
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    if (state.getValue((IProperty)PART) == EnumPartType.FOOT) {
      IBlockState iblockstate = worldIn.getBlockState(pos.offset((EnumFacing)state.getValue((IProperty)FACING)));
      if (iblockstate.getBlock() == this)
        state = state.withProperty((IProperty)OCCUPIED, iblockstate.getValue((IProperty)OCCUPIED)); 
    } 
    return state;
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getHorizontalIndex();
    if (state.getValue((IProperty)PART) == EnumPartType.HEAD) {
      i |= 0x8;
      if (((Boolean)state.getValue((IProperty)OCCUPIED)).booleanValue())
        i |= 0x4; 
    } 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)PART, (IProperty)OCCUPIED });
  }
  
  public enum EnumPartType implements IStringSerializable {
    HEAD("head"),
    FOOT("foot");
    
    private final String name;
    
    EnumPartType(String name) {
      this.name = name;
    }
    
    public String toString() {
      return this.name;
    }
    
    public String getName() {
      return this.name;
    }
  }
}
