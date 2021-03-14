package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class StructureVillagePieces {
  public static void registerVillagePieces() {
    MapGenStructureIO.registerStructureComponent((Class)House1.class, "ViBH");
    MapGenStructureIO.registerStructureComponent((Class)Field1.class, "ViDF");
    MapGenStructureIO.registerStructureComponent((Class)Field2.class, "ViF");
    MapGenStructureIO.registerStructureComponent((Class)Torch.class, "ViL");
    MapGenStructureIO.registerStructureComponent((Class)Hall.class, "ViPH");
    MapGenStructureIO.registerStructureComponent((Class)House4Garden.class, "ViSH");
    MapGenStructureIO.registerStructureComponent((Class)WoodHut.class, "ViSmH");
    MapGenStructureIO.registerStructureComponent((Class)Church.class, "ViST");
    MapGenStructureIO.registerStructureComponent((Class)House2.class, "ViS");
    MapGenStructureIO.registerStructureComponent((Class)Start.class, "ViStart");
    MapGenStructureIO.registerStructureComponent((Class)Path.class, "ViSR");
    MapGenStructureIO.registerStructureComponent((Class)House3.class, "ViTRH");
    MapGenStructureIO.registerStructureComponent((Class)Well.class, "ViW");
  }
  
  public static List<PieceWeight> getStructureVillageWeightedPieceList(Random random, int size) {
    List<PieceWeight> list = Lists.newArrayList();
    list.add(new PieceWeight((Class)House4Garden.class, 4, MathHelper.getRandomIntegerInRange(random, 2 + size, 4 + size * 2)));
    list.add(new PieceWeight((Class)Church.class, 20, MathHelper.getRandomIntegerInRange(random, 0 + size, 1 + size)));
    list.add(new PieceWeight((Class)House1.class, 20, MathHelper.getRandomIntegerInRange(random, 0 + size, 2 + size)));
    list.add(new PieceWeight((Class)WoodHut.class, 3, MathHelper.getRandomIntegerInRange(random, 2 + size, 5 + size * 3)));
    list.add(new PieceWeight((Class)Hall.class, 15, MathHelper.getRandomIntegerInRange(random, 0 + size, 2 + size)));
    list.add(new PieceWeight((Class)Field1.class, 3, MathHelper.getRandomIntegerInRange(random, 1 + size, 4 + size)));
    list.add(new PieceWeight((Class)Field2.class, 3, MathHelper.getRandomIntegerInRange(random, 2 + size, 4 + size * 2)));
    list.add(new PieceWeight((Class)House2.class, 15, MathHelper.getRandomIntegerInRange(random, 0, 1 + size)));
    list.add(new PieceWeight((Class)House3.class, 8, MathHelper.getRandomIntegerInRange(random, 0 + size, 3 + size * 2)));
    Iterator<PieceWeight> iterator = list.iterator();
    while (iterator.hasNext()) {
      if (((PieceWeight)iterator.next()).villagePiecesLimit == 0)
        iterator.remove(); 
    } 
    return list;
  }
  
  private static int func_75079_a(List<PieceWeight> p_75079_0_) {
    boolean flag = false;
    int i = 0;
    for (PieceWeight structurevillagepieces$pieceweight : p_75079_0_) {
      if (structurevillagepieces$pieceweight.villagePiecesLimit > 0 && structurevillagepieces$pieceweight.villagePiecesSpawned < structurevillagepieces$pieceweight.villagePiecesLimit)
        flag = true; 
      i += structurevillagepieces$pieceweight.villagePieceWeight;
    } 
    return flag ? i : -1;
  }
  
  private static Village func_176065_a(Start start, PieceWeight weight, List<StructureComponent> p_176065_2_, Random rand, int p_176065_4_, int p_176065_5_, int p_176065_6_, EnumFacing facing, int p_176065_8_) {
    Class<? extends Village> oclass = weight.villagePieceClass;
    Village structurevillagepieces$village = null;
    if (oclass == House4Garden.class) {
      structurevillagepieces$village = House4Garden.func_175858_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == Church.class) {
      structurevillagepieces$village = Church.func_175854_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == House1.class) {
      structurevillagepieces$village = House1.func_175850_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == WoodHut.class) {
      structurevillagepieces$village = WoodHut.func_175853_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == Hall.class) {
      structurevillagepieces$village = Hall.func_175857_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == Field1.class) {
      structurevillagepieces$village = Field1.func_175851_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == Field2.class) {
      structurevillagepieces$village = Field2.func_175852_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == House2.class) {
      structurevillagepieces$village = House2.func_175855_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } else if (oclass == House3.class) {
      structurevillagepieces$village = House3.func_175849_a(start, p_176065_2_, rand, p_176065_4_, p_176065_5_, p_176065_6_, facing, p_176065_8_);
    } 
    return structurevillagepieces$village;
  }
  
  private static Village func_176067_c(Start start, List<StructureComponent> p_176067_1_, Random rand, int p_176067_3_, int p_176067_4_, int p_176067_5_, EnumFacing facing, int p_176067_7_) {
    int i = func_75079_a(start.structureVillageWeightedPieceList);
    if (i <= 0)
      return null; 
    int j = 0;
    while (j < 5) {
      j++;
      int k = rand.nextInt(i);
      for (PieceWeight structurevillagepieces$pieceweight : start.structureVillageWeightedPieceList) {
        k -= structurevillagepieces$pieceweight.villagePieceWeight;
        if (k < 0) {
          if (!structurevillagepieces$pieceweight.canSpawnMoreVillagePiecesOfType(p_176067_7_) || (structurevillagepieces$pieceweight == start.structVillagePieceWeight && start.structureVillageWeightedPieceList.size() > 1))
            break; 
          Village structurevillagepieces$village = func_176065_a(start, structurevillagepieces$pieceweight, p_176067_1_, rand, p_176067_3_, p_176067_4_, p_176067_5_, facing, p_176067_7_);
          if (structurevillagepieces$village != null) {
            structurevillagepieces$pieceweight.villagePiecesSpawned++;
            start.structVillagePieceWeight = structurevillagepieces$pieceweight;
            if (!structurevillagepieces$pieceweight.canSpawnMoreVillagePieces())
              start.structureVillageWeightedPieceList.remove(structurevillagepieces$pieceweight); 
            return structurevillagepieces$village;
          } 
        } 
      } 
    } 
    StructureBoundingBox structureboundingbox = Torch.func_175856_a(start, p_176067_1_, rand, p_176067_3_, p_176067_4_, p_176067_5_, facing);
    if (structureboundingbox != null)
      return new Torch(start, p_176067_7_, rand, structureboundingbox, facing); 
    return null;
  }
  
  private static StructureComponent func_176066_d(Start start, List<StructureComponent> p_176066_1_, Random rand, int p_176066_3_, int p_176066_4_, int p_176066_5_, EnumFacing facing, int p_176066_7_) {
    if (p_176066_7_ > 50)
      return null; 
    if (Math.abs(p_176066_3_ - (start.getBoundingBox()).minX) <= 112 && Math.abs(p_176066_5_ - (start.getBoundingBox()).minZ) <= 112) {
      StructureComponent structurecomponent = func_176067_c(start, p_176066_1_, rand, p_176066_3_, p_176066_4_, p_176066_5_, facing, p_176066_7_ + 1);
      if (structurecomponent != null) {
        int i = (structurecomponent.boundingBox.minX + structurecomponent.boundingBox.maxX) / 2;
        int j = (structurecomponent.boundingBox.minZ + structurecomponent.boundingBox.maxZ) / 2;
        int k = structurecomponent.boundingBox.maxX - structurecomponent.boundingBox.minX;
        int l = structurecomponent.boundingBox.maxZ - structurecomponent.boundingBox.minZ;
        int i1 = (k > l) ? k : l;
        if (start.getWorldChunkManager().areBiomesViable(i, j, i1 / 2 + 4, MapGenVillage.villageSpawnBiomes)) {
          p_176066_1_.add(structurecomponent);
          start.field_74932_i.add(structurecomponent);
          return structurecomponent;
        } 
      } 
      return null;
    } 
    return null;
  }
  
  private static StructureComponent func_176069_e(Start start, List<StructureComponent> p_176069_1_, Random rand, int p_176069_3_, int p_176069_4_, int p_176069_5_, EnumFacing facing, int p_176069_7_) {
    if (p_176069_7_ > 3 + start.terrainType)
      return null; 
    if (Math.abs(p_176069_3_ - (start.getBoundingBox()).minX) <= 112 && Math.abs(p_176069_5_ - (start.getBoundingBox()).minZ) <= 112) {
      StructureBoundingBox structureboundingbox = Path.func_175848_a(start, p_176069_1_, rand, p_176069_3_, p_176069_4_, p_176069_5_, facing);
      if (structureboundingbox != null && structureboundingbox.minY > 10) {
        StructureComponent structurecomponent = new Path(start, p_176069_7_, rand, structureboundingbox, facing);
        int i = (structurecomponent.boundingBox.minX + structurecomponent.boundingBox.maxX) / 2;
        int j = (structurecomponent.boundingBox.minZ + structurecomponent.boundingBox.maxZ) / 2;
        int k = structurecomponent.boundingBox.maxX - structurecomponent.boundingBox.minX;
        int l = structurecomponent.boundingBox.maxZ - structurecomponent.boundingBox.minZ;
        int i1 = (k > l) ? k : l;
        if (start.getWorldChunkManager().areBiomesViable(i, j, i1 / 2 + 4, MapGenVillage.villageSpawnBiomes)) {
          p_176069_1_.add(structurecomponent);
          start.field_74930_j.add(structurecomponent);
          return structurecomponent;
        } 
      } 
      return null;
    } 
    return null;
  }
  
  public static class Church extends Village {
    public Church() {}
    
    public Church(StructureVillagePieces.Start start, int p_i45564_2_, Random rand, StructureBoundingBox p_i45564_4_, EnumFacing facing) {
      super(start, p_i45564_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45564_4_;
    }
    
    public static Church func_175854_a(StructureVillagePieces.Start start, List<StructureComponent> p_175854_1_, Random rand, int p_175854_3_, int p_175854_4_, int p_175854_5_, EnumFacing facing, int p_175854_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175854_3_, p_175854_4_, p_175854_5_, 0, 0, 0, 5, 12, 9, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175854_1_, structureboundingbox) == null) ? new Church(start, p_175854_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 12 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 3, 7, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 3, 9, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 3, 0, 8, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 3, 10, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 10, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 10, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 4, 0, 4, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 4, 4, 4, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 8, 3, 4, 8, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 4, 3, 10, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 5, 3, 5, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 9, 0, 4, 9, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 4, 4, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 11, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 11, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 11, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 11, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 1, 1, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 1, 1, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 1, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 3, 1, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 3, 1, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 1, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 3, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 1)), 1, 2, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 0)), 3, 2, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 6, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 7, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 6, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 7, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 6, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 7, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 6, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 7, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 3, 8, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.getOpposite()), 2, 4, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.rotateY()), 1, 4, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.rotateYCCW()), 3, 4, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode), 2, 4, 5, structureBoundingBoxIn);
      int i = getMetadataWithOffset(Blocks.ladder, 4);
      for (int j = 1; j <= 9; j++)
        setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, j, 3, structureBoundingBoxIn); 
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      if (getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn); 
      for (int l = 0; l < 9; l++) {
        for (int k = 0; k < 5; k++) {
          clearCurrentPositionBlocksUpwards(worldIn, k, 12, l, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), k, -1, l, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 2, 1, 2, 1);
      return true;
    }
    
    protected int func_180779_c(int p_180779_1_, int p_180779_2_) {
      return 2;
    }
  }
  
  public static class Field1 extends Village {
    private Block cropTypeA;
    
    private Block cropTypeB;
    
    private Block cropTypeC;
    
    private Block cropTypeD;
    
    public Field1() {}
    
    public Field1(StructureVillagePieces.Start start, int p_i45570_2_, Random rand, StructureBoundingBox p_i45570_4_, EnumFacing facing) {
      super(start, p_i45570_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45570_4_;
      this.cropTypeA = func_151559_a(rand);
      this.cropTypeB = func_151559_a(rand);
      this.cropTypeC = func_151559_a(rand);
      this.cropTypeD = func_151559_a(rand);
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setInteger("CA", Block.blockRegistry.getIDForObject(this.cropTypeA));
      tagCompound.setInteger("CB", Block.blockRegistry.getIDForObject(this.cropTypeB));
      tagCompound.setInteger("CC", Block.blockRegistry.getIDForObject(this.cropTypeC));
      tagCompound.setInteger("CD", Block.blockRegistry.getIDForObject(this.cropTypeD));
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.cropTypeA = Block.getBlockById(tagCompound.getInteger("CA"));
      this.cropTypeB = Block.getBlockById(tagCompound.getInteger("CB"));
      this.cropTypeC = Block.getBlockById(tagCompound.getInteger("CC"));
      this.cropTypeD = Block.getBlockById(tagCompound.getInteger("CD"));
    }
    
    private Block func_151559_a(Random rand) {
      switch (rand.nextInt(5)) {
        case 0:
          return Blocks.carrots;
        case 1:
          return Blocks.potatoes;
      } 
      return Blocks.wheat;
    }
    
    public static Field1 func_175851_a(StructureVillagePieces.Start start, List<StructureComponent> p_175851_1_, Random rand, int p_175851_3_, int p_175851_4_, int p_175851_5_, EnumFacing facing, int p_175851_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175851_3_, p_175851_4_, p_175851_5_, 0, 0, 0, 13, 4, 9, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175851_1_, structureboundingbox) == null) ? new Field1(start, p_175851_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 4 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 12, 4, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 1, 8, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 0, 1, 11, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 0, 0, 12, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 11, 0, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 8, 11, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 1, 9, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);
      for (int i = 1; i <= 7; i++) {
        setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 1, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 2, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 4, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 5, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 7, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 8, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 10, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 11, 1, i, structureBoundingBoxIn);
      } 
      for (int k = 0; k < 9; k++) {
        for (int j = 0; j < 13; j++) {
          clearCurrentPositionBlocksUpwards(worldIn, j, 4, k, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.dirt.getDefaultState(), j, -1, k, structureBoundingBoxIn);
        } 
      } 
      return true;
    }
  }
  
  public static class Field2 extends Village {
    private Block cropTypeA;
    
    private Block cropTypeB;
    
    public Field2() {}
    
    public Field2(StructureVillagePieces.Start start, int p_i45569_2_, Random rand, StructureBoundingBox p_i45569_4_, EnumFacing facing) {
      super(start, p_i45569_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45569_4_;
      this.cropTypeA = func_151560_a(rand);
      this.cropTypeB = func_151560_a(rand);
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setInteger("CA", Block.blockRegistry.getIDForObject(this.cropTypeA));
      tagCompound.setInteger("CB", Block.blockRegistry.getIDForObject(this.cropTypeB));
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.cropTypeA = Block.getBlockById(tagCompound.getInteger("CA"));
      this.cropTypeB = Block.getBlockById(tagCompound.getInteger("CB"));
    }
    
    private Block func_151560_a(Random rand) {
      switch (rand.nextInt(5)) {
        case 0:
          return Blocks.carrots;
        case 1:
          return Blocks.potatoes;
      } 
      return Blocks.wheat;
    }
    
    public static Field2 func_175852_a(StructureVillagePieces.Start start, List<StructureComponent> p_175852_1_, Random rand, int p_175852_3_, int p_175852_4_, int p_175852_5_, EnumFacing facing, int p_175852_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175852_3_, p_175852_4_, p_175852_5_, 0, 0, 0, 7, 4, 9, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175852_1_, structureboundingbox) == null) ? new Field2(start, p_175852_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 4 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 6, 4, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 5, 0, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 8, 5, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);
      for (int i = 1; i <= 7; i++) {
        setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 1, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 2, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 4, 1, i, structureBoundingBoxIn);
        setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 5, 1, i, structureBoundingBoxIn);
      } 
      for (int k = 0; k < 9; k++) {
        for (int j = 0; j < 7; j++) {
          clearCurrentPositionBlocksUpwards(worldIn, j, 4, k, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.dirt.getDefaultState(), j, -1, k, structureBoundingBoxIn);
        } 
      } 
      return true;
    }
  }
  
  public static class Hall extends Village {
    public Hall() {}
    
    public Hall(StructureVillagePieces.Start start, int p_i45567_2_, Random rand, StructureBoundingBox p_i45567_4_, EnumFacing facing) {
      super(start, p_i45567_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45567_4_;
    }
    
    public static Hall func_175857_a(StructureVillagePieces.Start start, List<StructureComponent> p_175857_1_, Random rand, int p_175857_3_, int p_175857_4_, int p_175857_5_, EnumFacing facing, int p_175857_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175857_3_, p_175857_4_, p_175857_5_, 0, 0, 0, 9, 7, 11, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175857_1_, structureboundingbox) == null) ? new Hall(start, p_175857_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 7 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 4, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 8, 4, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 6, 8, 0, 10, Blocks.dirt.getDefaultState(), Blocks.dirt.getDefaultState(), false);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 0, 6, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 2, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 6, 8, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 10, 7, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 7, 0, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 0, 8, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 7, 1, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 5, 7, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 7, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 1, 8, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 4, 8, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 2, 8, 5, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 3, structureBoundingBoxIn);
      int i = getMetadataWithOffset(Blocks.oak_stairs, 3);
      int j = getMetadataWithOffset(Blocks.oak_stairs, 2);
      for (int k = -1; k <= 2; k++) {
        for (int l = 0; l <= 8; l++) {
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 4 + k, k, structureBoundingBoxIn);
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 4 + k, 5 - k, structureBoundingBoxIn);
        } 
      } 
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 1, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 2, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.oak_stairs, 3)), 2, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.oak_stairs, 1)), 1, 1, 3, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 0, 1, 7, 0, 3, Blocks.double_stone_slab.getDefaultState(), Blocks.double_stone_slab.getDefaultState(), false);
      setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 6, 1, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 6, 1, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      if (getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn); 
      setBlockState(worldIn, Blocks.air.getDefaultState(), 6, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.getOpposite()), 6, 3, 4, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 6, 1, 5, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      for (int i1 = 0; i1 < 5; i1++) {
        for (int j1 = 0; j1 < 9; j1++) {
          clearCurrentPositionBlocksUpwards(worldIn, j1, 7, i1, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j1, -1, i1, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 4, 1, 2, 2);
      return true;
    }
    
    protected int func_180779_c(int p_180779_1_, int p_180779_2_) {
      return (p_180779_1_ == 0) ? 4 : super.func_180779_c(p_180779_1_, p_180779_2_);
    }
  }
  
  public static class House1 extends Village {
    public House1() {}
    
    public House1(StructureVillagePieces.Start start, int p_i45571_2_, Random rand, StructureBoundingBox p_i45571_4_, EnumFacing facing) {
      super(start, p_i45571_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45571_4_;
    }
    
    public static House1 func_175850_a(StructureVillagePieces.Start start, List<StructureComponent> p_175850_1_, Random rand, int p_175850_3_, int p_175850_4_, int p_175850_5_, EnumFacing facing, int p_175850_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175850_3_, p_175850_4_, p_175850_5_, 0, 0, 0, 9, 9, 6, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175850_1_, structureboundingbox) == null) ? new House1(start, p_175850_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 9 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 8, 0, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 8, 5, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 1, 8, 6, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 7, 2, 8, 7, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      int i = getMetadataWithOffset(Blocks.oak_stairs, 3);
      int j = getMetadataWithOffset(Blocks.oak_stairs, 2);
      for (int k = -1; k <= 2; k++) {
        for (int l = 0; l <= 8; l++) {
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 6 + k, k, structureBoundingBoxIn);
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 6 + k, 5 - k, structureBoundingBoxIn);
        } 
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 5, 8, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 0, 8, 1, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 0, 7, 1, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 4, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 5, 0, 4, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 5, 8, 4, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 0, 8, 4, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 1, 0, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 7, 4, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 1, 8, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 4, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 3, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 3, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 7, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 4, 7, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 4, 7, 3, 4, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 7, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.oak_stairs, 0)), 7, 1, 3, structureBoundingBoxIn);
      int j1 = getMetadataWithOffset(Blocks.oak_stairs, 3);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j1), 6, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j1), 5, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j1), 4, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j1), 3, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 6, 1, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 6, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 1, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 4, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.crafting_table.getDefaultState(), 7, 1, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 1, 1, 0, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      if (getBlockStateFromPos(worldIn, 1, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 1, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 0, -1, structureBoundingBoxIn); 
      for (int k1 = 0; k1 < 6; k1++) {
        for (int i1 = 0; i1 < 9; i1++) {
          clearCurrentPositionBlocksUpwards(worldIn, i1, 9, k1, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), i1, -1, k1, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 2, 1, 2, 1);
      return true;
    }
    
    protected int func_180779_c(int p_180779_1_, int p_180779_2_) {
      return 1;
    }
  }
  
  public static class House2 extends Village {
    private static final List<WeightedRandomChestContent> villageBlacksmithChestContents = Lists.newArrayList((Object[])new WeightedRandomChestContent[] { 
          new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 3), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new WeightedRandomChestContent(Items.apple, 0, 1, 3, 15), new WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_sword, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.iron_chestplate, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.iron_helmet, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.iron_leggings, 0, 1, 1, 5), 
          new WeightedRandomChestContent((Item)Items.iron_boots, 0, 1, 1, 5), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.obsidian), 0, 3, 7, 5), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.sapling), 0, 3, 7, 5), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1) });
    
    private boolean hasMadeChest;
    
    public House2() {}
    
    public House2(StructureVillagePieces.Start start, int p_i45563_2_, Random rand, StructureBoundingBox p_i45563_4_, EnumFacing facing) {
      super(start, p_i45563_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45563_4_;
    }
    
    public static House2 func_175855_a(StructureVillagePieces.Start start, List<StructureComponent> p_175855_1_, Random rand, int p_175855_3_, int p_175855_4_, int p_175855_5_, EnumFacing facing, int p_175855_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175855_3_, p_175855_4_, p_175855_5_, 0, 0, 0, 10, 6, 7, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175855_1_, structureboundingbox) == null) ? new House2(start, p_175855_7_, rand, structureboundingbox, facing) : null;
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setBoolean("Chest", this.hasMadeChest);
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.hasMadeChest = tagCompound.getBoolean("Chest");
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 6 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 9, 4, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 9, 0, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 9, 4, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 9, 5, 6, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 8, 5, 5, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 4, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 3, 4, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 6, 0, 4, 6, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 3, 1, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 2, 3, 3, 2, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 3, 5, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 6, 5, 3, 6, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 0, 5, 3, 0, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 0, 9, 3, 0, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 4, 9, 4, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      setBlockState(worldIn, Blocks.flowing_lava.getDefaultState(), 7, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.flowing_lava.getDefaultState(), 8, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 9, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 9, 2, 4, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 4, 8, 2, 5, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 1, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.furnace.getDefaultState(), 6, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.furnace.getDefaultState(), 6, 3, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 8, 1, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 2, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.oak_stairs, 3)), 2, 1, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.oak_stairs, 1)), 1, 1, 4, structureBoundingBoxIn);
      if (!this.hasMadeChest && structureBoundingBoxIn.isVecInside((Vec3i)new BlockPos(getXWithOffset(5, 5), getYWithOffset(1), getZWithOffset(5, 5)))) {
        this.hasMadeChest = true;
        generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 5, 1, 5, villageBlacksmithChestContents, 3 + randomIn.nextInt(6));
      } 
      for (int i = 6; i <= 8; i++) {
        if (getBlockStateFromPos(worldIn, i, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, i, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
          setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), i, 0, -1, structureBoundingBoxIn); 
      } 
      for (int k = 0; k < 7; k++) {
        for (int j = 0; j < 10; j++) {
          clearCurrentPositionBlocksUpwards(worldIn, j, 6, k, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, k, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 7, 1, 1, 1);
      return true;
    }
    
    protected int func_180779_c(int p_180779_1_, int p_180779_2_) {
      return 3;
    }
  }
  
  public static class House3 extends Village {
    public House3() {}
    
    public House3(StructureVillagePieces.Start start, int p_i45561_2_, Random rand, StructureBoundingBox p_i45561_4_, EnumFacing facing) {
      super(start, p_i45561_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45561_4_;
    }
    
    public static House3 func_175849_a(StructureVillagePieces.Start start, List<StructureComponent> p_175849_1_, Random rand, int p_175849_3_, int p_175849_4_, int p_175849_5_, EnumFacing facing, int p_175849_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175849_3_, p_175849_4_, p_175849_5_, 0, 0, 0, 9, 7, 12, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175849_1_, structureboundingbox) == null) ? new House3(start, p_175849_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 7 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 4, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 8, 4, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 5, 8, 0, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 7, 0, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 0, 8, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 7, 2, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 5, 2, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 6, 2, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 10, 7, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 2, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 1, 8, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 4, 3, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 2, 8, 5, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 4, structureBoundingBoxIn);
      int i = getMetadataWithOffset(Blocks.oak_stairs, 3);
      int j = getMetadataWithOffset(Blocks.oak_stairs, 2);
      for (int k = -1; k <= 2; k++) {
        for (int l = 0; l <= 8; l++) {
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 4 + k, k, structureBoundingBoxIn);
          if ((k > -1 || l <= 1) && (k > 0 || l <= 3) && (k > 1 || l <= 4 || l >= 6))
            setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 4 + k, 5 - k, structureBoundingBoxIn); 
        } 
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 4, 5, 3, 4, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 4, 2, 7, 4, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 4, 4, 5, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 5, 4, 6, 5, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 6, 3, 5, 6, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      int k1 = getMetadataWithOffset(Blocks.oak_stairs, 0);
      for (int l1 = 4; l1 >= 1; l1--) {
        setBlockState(worldIn, Blocks.planks.getDefaultState(), l1, 2 + l1, 7 - l1, structureBoundingBoxIn);
        for (int i1 = 8 - l1; i1 <= 10; i1++)
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(k1), l1, 2 + l1, i1, structureBoundingBoxIn); 
      } 
      int i2 = getMetadataWithOffset(Blocks.oak_stairs, 1);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 6, 6, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 7, 5, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i2), 6, 6, 4, structureBoundingBoxIn);
      for (int j2 = 6; j2 <= 8; j2++) {
        for (int j1 = 5; j1 <= 10; j1++)
          setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i2), j2, 12 - j2, j1, structureBoundingBoxIn); 
      } 
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 6, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 2, 5, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 8, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 9, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 2, 6, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 7, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 8, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 2, 9, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 4, 4, 10, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 4, 10, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 6, 4, 10, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 5, 5, 10, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, -1, 3, 2, -1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      if (getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn); 
      for (int k2 = 0; k2 < 5; k2++) {
        for (int i3 = 0; i3 < 9; i3++) {
          clearCurrentPositionBlocksUpwards(worldIn, i3, 7, k2, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), i3, -1, k2, structureBoundingBoxIn);
        } 
      } 
      for (int l2 = 5; l2 < 11; l2++) {
        for (int j3 = 2; j3 < 9; j3++) {
          clearCurrentPositionBlocksUpwards(worldIn, j3, 7, l2, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j3, -1, l2, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 4, 1, 2, 2);
      return true;
    }
  }
  
  public static class House4Garden extends Village {
    private boolean isRoofAccessible;
    
    public House4Garden() {}
    
    public House4Garden(StructureVillagePieces.Start start, int p_i45566_2_, Random rand, StructureBoundingBox p_i45566_4_, EnumFacing facing) {
      super(start, p_i45566_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45566_4_;
      this.isRoofAccessible = rand.nextBoolean();
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setBoolean("Terrace", this.isRoofAccessible);
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.isRoofAccessible = tagCompound.getBoolean("Terrace");
    }
    
    public static House4Garden func_175858_a(StructureVillagePieces.Start start, List<StructureComponent> p_175858_1_, Random rand, int p_175858_3_, int p_175858_4_, int p_175858_5_, EnumFacing facing, int p_175858_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175858_3_, p_175858_4_, p_175858_5_, 0, 0, 0, 5, 6, 5, facing);
      return (StructureComponent.findIntersecting(p_175858_1_, structureboundingbox) != null) ? null : new House4Garden(start, p_175858_7_, rand, structureboundingbox, facing);
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 6 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 0, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 4, 4, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 3, 4, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 3, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 1, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 3, 4, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 3, 3, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 2, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 1, 0, structureBoundingBoxIn);
      if (getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn); 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      if (this.isRoofAccessible) {
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 0, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 5, 0, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 5, 0, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 3, 5, 0, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 0, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 4, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 5, 4, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 5, 4, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 3, 5, 4, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 4, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 1, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 2, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 3, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 1, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 2, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 3, structureBoundingBoxIn);
      } 
      if (this.isRoofAccessible) {
        int i = getMetadataWithOffset(Blocks.ladder, 3);
        setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 1, 3, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 2, 3, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 3, 3, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 4, 3, structureBoundingBoxIn);
      } 
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);
      for (int k = 0; k < 5; k++) {
        for (int j = 0; j < 5; j++) {
          clearCurrentPositionBlocksUpwards(worldIn, j, 6, k, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, k, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 1, 1, 2, 1);
      return true;
    }
  }
  
  public static class Path extends Road {
    private int length;
    
    public Path() {}
    
    public Path(StructureVillagePieces.Start start, int p_i45562_2_, Random rand, StructureBoundingBox p_i45562_4_, EnumFacing facing) {
      super(start, p_i45562_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45562_4_;
      this.length = Math.max(p_i45562_4_.getXSize(), p_i45562_4_.getZSize());
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setInteger("Length", this.length);
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.length = tagCompound.getInteger("Length");
    }
    
    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
      boolean flag = false;
      int i;
      for (i = rand.nextInt(5); i < this.length - 8; i += 2 + rand.nextInt(5)) {
        StructureComponent structurecomponent = getNextComponentNN((StructureVillagePieces.Start)componentIn, listIn, rand, 0, i);
        if (structurecomponent != null) {
          i += Math.max(structurecomponent.boundingBox.getXSize(), structurecomponent.boundingBox.getZSize());
          flag = true;
        } 
      } 
      int j;
      for (j = rand.nextInt(5); j < this.length - 8; j += 2 + rand.nextInt(5)) {
        StructureComponent structurecomponent1 = getNextComponentPP((StructureVillagePieces.Start)componentIn, listIn, rand, 0, j);
        if (structurecomponent1 != null) {
          j += Math.max(structurecomponent1.boundingBox.getXSize(), structurecomponent1.boundingBox.getZSize());
          flag = true;
        } 
      } 
      if (flag && rand.nextInt(3) > 0 && this.coordBaseMode != null)
        switch (this.coordBaseMode) {
          case NORTH:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, getComponentType());
            break;
          case SOUTH:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.WEST, getComponentType());
            break;
          case WEST:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, getComponentType());
            break;
          case EAST:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, getComponentType());
            break;
        }  
      if (flag && rand.nextInt(3) > 0 && this.coordBaseMode != null)
        switch (this.coordBaseMode) {
          case NORTH:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, getComponentType());
            break;
          case SOUTH:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.EAST, getComponentType());
            break;
          case WEST:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, getComponentType());
            break;
          case EAST:
            StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, getComponentType());
            break;
        }  
    }
    
    public static StructureBoundingBox func_175848_a(StructureVillagePieces.Start start, List<StructureComponent> p_175848_1_, Random rand, int p_175848_3_, int p_175848_4_, int p_175848_5_, EnumFacing facing) {
      for (int i = 7 * MathHelper.getRandomIntegerInRange(rand, 3, 5); i >= 7; i -= 7) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175848_3_, p_175848_4_, p_175848_5_, 0, 0, 0, 3, 3, i, facing);
        if (StructureComponent.findIntersecting(p_175848_1_, structureboundingbox) == null)
          return structureboundingbox; 
      } 
      return null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      IBlockState iblockstate = func_175847_a(Blocks.gravel.getDefaultState());
      IBlockState iblockstate1 = func_175847_a(Blocks.cobblestone.getDefaultState());
      for (int i = this.boundingBox.minX; i <= this.boundingBox.maxX; i++) {
        for (int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; j++) {
          BlockPos blockpos = new BlockPos(i, 64, j);
          if (structureBoundingBoxIn.isVecInside((Vec3i)blockpos)) {
            blockpos = worldIn.getTopSolidOrLiquidBlock(blockpos).down();
            worldIn.setBlockState(blockpos, iblockstate, 2);
            worldIn.setBlockState(blockpos.down(), iblockstate1, 2);
          } 
        } 
      } 
      return true;
    }
  }
  
  public static class PieceWeight {
    public Class<? extends StructureVillagePieces.Village> villagePieceClass;
    
    public final int villagePieceWeight;
    
    public int villagePiecesSpawned;
    
    public int villagePiecesLimit;
    
    public PieceWeight(Class<? extends StructureVillagePieces.Village> p_i2098_1_, int p_i2098_2_, int p_i2098_3_) {
      this.villagePieceClass = p_i2098_1_;
      this.villagePieceWeight = p_i2098_2_;
      this.villagePiecesLimit = p_i2098_3_;
    }
    
    public boolean canSpawnMoreVillagePiecesOfType(int p_75085_1_) {
      return (this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit);
    }
    
    public boolean canSpawnMoreVillagePieces() {
      return (this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit);
    }
  }
  
  public static abstract class Road extends Village {
    public Road() {}
    
    protected Road(StructureVillagePieces.Start start, int type) {
      super(start, type);
    }
  }
  
  public static class Start extends Well {
    public WorldChunkManager worldChunkMngr;
    
    public boolean inDesert;
    
    public int terrainType;
    
    public StructureVillagePieces.PieceWeight structVillagePieceWeight;
    
    public List<StructureVillagePieces.PieceWeight> structureVillageWeightedPieceList;
    
    public List<StructureComponent> field_74932_i = Lists.newArrayList();
    
    public List<StructureComponent> field_74930_j = Lists.newArrayList();
    
    public Start() {}
    
    public Start(WorldChunkManager chunkManagerIn, int p_i2104_2_, Random rand, int p_i2104_4_, int p_i2104_5_, List<StructureVillagePieces.PieceWeight> p_i2104_6_, int p_i2104_7_) {
      super((Start)null, 0, rand, p_i2104_4_, p_i2104_5_);
      this.worldChunkMngr = chunkManagerIn;
      this.structureVillageWeightedPieceList = p_i2104_6_;
      this.terrainType = p_i2104_7_;
      BiomeGenBase biomegenbase = chunkManagerIn.getBiomeGenerator(new BlockPos(p_i2104_4_, 0, p_i2104_5_), BiomeGenBase.field_180279_ad);
      this.inDesert = (biomegenbase == BiomeGenBase.desert || biomegenbase == BiomeGenBase.desertHills);
      func_175846_a(this.inDesert);
    }
    
    public WorldChunkManager getWorldChunkManager() {
      return this.worldChunkMngr;
    }
  }
  
  public static class Torch extends Village {
    public Torch() {}
    
    public Torch(StructureVillagePieces.Start start, int p_i45568_2_, Random rand, StructureBoundingBox p_i45568_4_, EnumFacing facing) {
      super(start, p_i45568_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45568_4_;
    }
    
    public static StructureBoundingBox func_175856_a(StructureVillagePieces.Start start, List<StructureComponent> p_175856_1_, Random rand, int p_175856_3_, int p_175856_4_, int p_175856_5_, EnumFacing facing) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175856_3_, p_175856_4_, p_175856_5_, 0, 0, 0, 3, 4, 2, facing);
      return (StructureComponent.findIntersecting(p_175856_1_, structureboundingbox) != null) ? null : structureboundingbox;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 4 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 2, 3, 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 0, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.wool.getStateFromMeta(EnumDyeColor.WHITE.getDyeDamage()), 1, 3, 0, structureBoundingBoxIn);
      boolean flag = (this.coordBaseMode == EnumFacing.EAST || this.coordBaseMode == EnumFacing.NORTH);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.rotateY()), flag ? 2 : 0, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode), 1, 3, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.rotateYCCW()), flag ? 0 : 2, 3, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)this.coordBaseMode.getOpposite()), 1, 3, -1, structureBoundingBoxIn);
      return true;
    }
  }
  
  static abstract class Village extends StructureComponent {
    protected int field_143015_k = -1;
    
    private int villagersSpawned;
    
    private boolean isDesertVillage;
    
    public Village() {}
    
    protected Village(StructureVillagePieces.Start start, int type) {
      super(type);
      if (start != null)
        this.isDesertVillage = start.inDesert; 
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      tagCompound.setInteger("HPos", this.field_143015_k);
      tagCompound.setInteger("VCount", this.villagersSpawned);
      tagCompound.setBoolean("Desert", this.isDesertVillage);
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      this.field_143015_k = tagCompound.getInteger("HPos");
      this.villagersSpawned = tagCompound.getInteger("VCount");
      this.isDesertVillage = tagCompound.getBoolean("Desert");
    }
    
    protected StructureComponent getNextComponentNN(StructureVillagePieces.Start start, List<StructureComponent> p_74891_2_, Random rand, int p_74891_4_, int p_74891_5_) {
      if (this.coordBaseMode != null)
        switch (this.coordBaseMode) {
          case NORTH:
            return StructureVillagePieces.func_176066_d(start, p_74891_2_, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ + p_74891_5_, EnumFacing.WEST, getComponentType());
          case SOUTH:
            return StructureVillagePieces.func_176066_d(start, p_74891_2_, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ + p_74891_5_, EnumFacing.WEST, getComponentType());
          case WEST:
            return StructureVillagePieces.func_176066_d(start, p_74891_2_, rand, this.boundingBox.minX + p_74891_5_, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, getComponentType());
          case EAST:
            return StructureVillagePieces.func_176066_d(start, p_74891_2_, rand, this.boundingBox.minX + p_74891_5_, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, getComponentType());
        }  
      return null;
    }
    
    protected StructureComponent getNextComponentPP(StructureVillagePieces.Start start, List<StructureComponent> p_74894_2_, Random rand, int p_74894_4_, int p_74894_5_) {
      if (this.coordBaseMode != null)
        switch (this.coordBaseMode) {
          case NORTH:
            return StructureVillagePieces.func_176066_d(start, p_74894_2_, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74894_4_, this.boundingBox.minZ + p_74894_5_, EnumFacing.EAST, getComponentType());
          case SOUTH:
            return StructureVillagePieces.func_176066_d(start, p_74894_2_, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74894_4_, this.boundingBox.minZ + p_74894_5_, EnumFacing.EAST, getComponentType());
          case WEST:
            return StructureVillagePieces.func_176066_d(start, p_74894_2_, rand, this.boundingBox.minX + p_74894_5_, this.boundingBox.minY + p_74894_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, getComponentType());
          case EAST:
            return StructureVillagePieces.func_176066_d(start, p_74894_2_, rand, this.boundingBox.minX + p_74894_5_, this.boundingBox.minY + p_74894_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, getComponentType());
        }  
      return null;
    }
    
    protected int getAverageGroundLevel(World worldIn, StructureBoundingBox p_74889_2_) {
      int i = 0;
      int j = 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; k++) {
        for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; l++) {
          blockpos$mutableblockpos.set(l, 64, k);
          if (p_74889_2_.isVecInside((Vec3i)blockpos$mutableblockpos)) {
            i += Math.max(worldIn.getTopSolidOrLiquidBlock((BlockPos)blockpos$mutableblockpos).getY(), worldIn.provider.getAverageGroundLevel());
            j++;
          } 
        } 
      } 
      if (j == 0)
        return -1; 
      return i / j;
    }
    
    protected static boolean canVillageGoDeeper(StructureBoundingBox p_74895_0_) {
      return (p_74895_0_ != null && p_74895_0_.minY > 10);
    }
    
    protected void spawnVillagers(World worldIn, StructureBoundingBox p_74893_2_, int p_74893_3_, int p_74893_4_, int p_74893_5_, int p_74893_6_) {
      if (this.villagersSpawned < p_74893_6_)
        for (int i = this.villagersSpawned; i < p_74893_6_; i++) {
          int j = getXWithOffset(p_74893_3_ + i, p_74893_5_);
          int k = getYWithOffset(p_74893_4_);
          int l = getZWithOffset(p_74893_3_ + i, p_74893_5_);
          if (!p_74893_2_.isVecInside((Vec3i)new BlockPos(j, k, l)))
            break; 
          this.villagersSpawned++;
          EntityVillager entityvillager = new EntityVillager(worldIn);
          entityvillager.setLocationAndAngles(j + 0.5D, k, l + 0.5D, 0.0F, 0.0F);
          entityvillager.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos((Entity)entityvillager)), (IEntityLivingData)null);
          entityvillager.setProfession(func_180779_c(i, entityvillager.getProfession()));
          worldIn.spawnEntityInWorld((Entity)entityvillager);
        }  
    }
    
    protected int func_180779_c(int p_180779_1_, int p_180779_2_) {
      return p_180779_2_;
    }
    
    protected IBlockState func_175847_a(IBlockState p_175847_1_) {
      if (this.isDesertVillage) {
        if (p_175847_1_.getBlock() == Blocks.log || p_175847_1_.getBlock() == Blocks.log2)
          return Blocks.sandstone.getDefaultState(); 
        if (p_175847_1_.getBlock() == Blocks.cobblestone)
          return Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata()); 
        if (p_175847_1_.getBlock() == Blocks.planks)
          return Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()); 
        if (p_175847_1_.getBlock() == Blocks.oak_stairs)
          return Blocks.sandstone_stairs.getDefaultState().withProperty((IProperty)BlockStairs.FACING, p_175847_1_.getValue((IProperty)BlockStairs.FACING)); 
        if (p_175847_1_.getBlock() == Blocks.stone_stairs)
          return Blocks.sandstone_stairs.getDefaultState().withProperty((IProperty)BlockStairs.FACING, p_175847_1_.getValue((IProperty)BlockStairs.FACING)); 
        if (p_175847_1_.getBlock() == Blocks.gravel)
          return Blocks.sandstone.getDefaultState(); 
      } 
      return p_175847_1_;
    }
    
    protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
      IBlockState iblockstate = func_175847_a(blockstateIn);
      super.setBlockState(worldIn, iblockstate, x, y, z, boundingboxIn);
    }
    
    protected void fillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean existingOnly) {
      IBlockState iblockstate = func_175847_a(boundaryBlockState);
      IBlockState iblockstate1 = func_175847_a(insideBlockState);
      super.fillWithBlocks(worldIn, boundingboxIn, xMin, yMin, zMin, xMax, yMax, zMax, iblockstate, iblockstate1, existingOnly);
    }
    
    protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
      IBlockState iblockstate = func_175847_a(blockstateIn);
      super.replaceAirAndLiquidDownwards(worldIn, iblockstate, x, y, z, boundingboxIn);
    }
    
    protected void func_175846_a(boolean p_175846_1_) {
      this.isDesertVillage = p_175846_1_;
    }
  }
  
  public static class Well extends Village {
    public Well() {}
    
    public Well(StructureVillagePieces.Start start, int p_i2109_2_, Random rand, int p_i2109_4_, int p_i2109_5_) {
      super(start, p_i2109_2_);
      this.coordBaseMode = EnumFacing.Plane.HORIZONTAL.random(rand);
      switch (this.coordBaseMode) {
        case NORTH:
        case SOUTH:
          this.boundingBox = new StructureBoundingBox(p_i2109_4_, 64, p_i2109_5_, p_i2109_4_ + 6 - 1, 78, p_i2109_5_ + 6 - 1);
          return;
      } 
      this.boundingBox = new StructureBoundingBox(p_i2109_4_, 64, p_i2109_5_, p_i2109_4_ + 6 - 1, 78, p_i2109_5_ + 6 - 1);
    }
    
    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
      StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.WEST, getComponentType());
      StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.EAST, getComponentType());
      StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ - 1, EnumFacing.NORTH, getComponentType());
      StructureVillagePieces.func_176069_e((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, getComponentType());
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 3, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 4, 12, 4, Blocks.cobblestone.getDefaultState(), Blocks.flowing_water.getDefaultState(), false);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 12, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 3, 12, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 12, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 3, 12, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 13, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 14, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 13, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 14, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 13, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 14, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 13, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 14, 4, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 15, 1, 4, 15, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      for (int i = 0; i <= 5; i++) {
        for (int j = 0; j <= 5; j++) {
          if (j == 0 || j == 5 || i == 0 || i == 5) {
            setBlockState(worldIn, Blocks.gravel.getDefaultState(), j, 11, i, structureBoundingBoxIn);
            clearCurrentPositionBlocksUpwards(worldIn, j, 12, i, structureBoundingBoxIn);
          } 
        } 
      } 
      return true;
    }
  }
  
  public static class WoodHut extends Village {
    private boolean isTallHouse;
    
    private int tablePosition;
    
    public WoodHut() {}
    
    public WoodHut(StructureVillagePieces.Start start, int p_i45565_2_, Random rand, StructureBoundingBox p_i45565_4_, EnumFacing facing) {
      super(start, p_i45565_2_);
      this.coordBaseMode = facing;
      this.boundingBox = p_i45565_4_;
      this.isTallHouse = rand.nextBoolean();
      this.tablePosition = rand.nextInt(3);
    }
    
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setInteger("T", this.tablePosition);
      tagCompound.setBoolean("C", this.isTallHouse);
    }
    
    protected void readStructureFromNBT(NBTTagCompound tagCompound) {
      super.readStructureFromNBT(tagCompound);
      this.tablePosition = tagCompound.getInteger("T");
      this.isTallHouse = tagCompound.getBoolean("C");
    }
    
    public static WoodHut func_175853_a(StructureVillagePieces.Start start, List<StructureComponent> p_175853_1_, Random rand, int p_175853_3_, int p_175853_4_, int p_175853_5_, EnumFacing facing, int p_175853_7_) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175853_3_, p_175853_4_, p_175853_5_, 0, 0, 0, 4, 6, 5, facing);
      return (canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175853_1_, structureboundingbox) == null) ? new WoodHut(start, p_175853_7_, rand, structureboundingbox, facing) : null;
    }
    
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.field_143015_k < 0) {
        this.field_143015_k = getAverageGroundLevel(worldIn, structureBoundingBoxIn);
        if (this.field_143015_k < 0)
          return true; 
        this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 6 - 1, 0);
      } 
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 3, 0, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 3, Blocks.dirt.getDefaultState(), Blocks.dirt.getDefaultState(), false);
      if (this.isTallHouse) {
        fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 2, 4, 3, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      } else {
        fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 2, 5, 3, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      } 
      setBlockState(worldIn, Blocks.log.getDefaultState(), 1, 4, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 4, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 1, 4, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 4, 4, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 1, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 3, structureBoundingBoxIn);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 3, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 3, 3, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 4, 0, 3, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 4, 3, 3, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 1, 3, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 2, 3, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 2, structureBoundingBoxIn);
      if (this.tablePosition > 0) {
        setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), this.tablePosition, 1, 3, structureBoundingBoxIn);
        setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), this.tablePosition, 2, 3, structureBoundingBoxIn);
      } 
      setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
      setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
      placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 1, 1, 0, EnumFacing.getHorizontal(getMetadataWithOffset(Blocks.oak_door, 1)));
      if (getBlockStateFromPos(worldIn, 1, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && getBlockStateFromPos(worldIn, 1, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
        setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 0, -1, structureBoundingBoxIn); 
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 4; j++) {
          clearCurrentPositionBlocksUpwards(worldIn, j, 6, i, structureBoundingBoxIn);
          replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, i, structureBoundingBoxIn);
        } 
      } 
      spawnVillagers(worldIn, structureBoundingBoxIn, 1, 1, 2, 1);
      return true;
    }
  }
}
