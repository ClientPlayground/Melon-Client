package net.optifine;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.util.EntityUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CustomColors {
  private static String paletteFormatDefault = "vanilla";
  
  private static CustomColormap waterColors = null;
  
  private static CustomColormap foliagePineColors = null;
  
  private static CustomColormap foliageBirchColors = null;
  
  private static CustomColormap swampFoliageColors = null;
  
  private static CustomColormap swampGrassColors = null;
  
  private static CustomColormap[] colorsBlockColormaps = null;
  
  private static CustomColormap[][] blockColormaps = (CustomColormap[][])null;
  
  private static CustomColormap skyColors = null;
  
  private static CustomColorFader skyColorFader = new CustomColorFader();
  
  private static CustomColormap fogColors = null;
  
  private static CustomColorFader fogColorFader = new CustomColorFader();
  
  private static CustomColormap underwaterColors = null;
  
  private static CustomColorFader underwaterColorFader = new CustomColorFader();
  
  private static CustomColormap underlavaColors = null;
  
  private static CustomColorFader underlavaColorFader = new CustomColorFader();
  
  private static LightMapPack[] lightMapPacks = null;
  
  private static int lightmapMinDimensionId = 0;
  
  private static CustomColormap redstoneColors = null;
  
  private static CustomColormap xpOrbColors = null;
  
  private static int xpOrbTime = -1;
  
  private static CustomColormap durabilityColors = null;
  
  private static CustomColormap stemColors = null;
  
  private static CustomColormap stemMelonColors = null;
  
  private static CustomColormap stemPumpkinColors = null;
  
  private static CustomColormap myceliumParticleColors = null;
  
  private static boolean useDefaultGrassFoliageColors = true;
  
  private static int particleWaterColor = -1;
  
  private static int particlePortalColor = -1;
  
  private static int lilyPadColor = -1;
  
  private static int expBarTextColor = -1;
  
  private static int bossTextColor = -1;
  
  private static int signTextColor = -1;
  
  private static Vec3 fogColorNether = null;
  
  private static Vec3 fogColorEnd = null;
  
  private static Vec3 skyColorEnd = null;
  
  private static int[] spawnEggPrimaryColors = null;
  
  private static int[] spawnEggSecondaryColors = null;
  
  private static float[][] wolfCollarColors = (float[][])null;
  
  private static float[][] sheepColors = (float[][])null;
  
  private static int[] textColors = null;
  
  private static int[] mapColorsOriginal = null;
  
  private static int[] potionColors = null;
  
  private static final IBlockState BLOCK_STATE_DIRT = Blocks.dirt.getDefaultState();
  
  private static final IBlockState BLOCK_STATE_WATER = Blocks.water.getDefaultState();
  
  public static Random random = new Random();
  
  private static final IColorizer COLORIZER_GRASS = new IColorizer() {
      public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        BiomeGenBase biomegenbase = CustomColors.getColorBiome(blockAccess, blockPos);
        return (CustomColors.swampGrassColors != null && biomegenbase == BiomeGenBase.swampland) ? CustomColors.swampGrassColors.getColor(biomegenbase, blockPos) : biomegenbase.getGrassColorAtPos(blockPos);
      }
      
      public boolean isColorConstant() {
        return false;
      }
    };
  
  private static final IColorizer COLORIZER_FOLIAGE = new IColorizer() {
      public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        BiomeGenBase biomegenbase = CustomColors.getColorBiome(blockAccess, blockPos);
        return (CustomColors.swampFoliageColors != null && biomegenbase == BiomeGenBase.swampland) ? CustomColors.swampFoliageColors.getColor(biomegenbase, blockPos) : biomegenbase.getFoliageColorAtPos(blockPos);
      }
      
      public boolean isColorConstant() {
        return false;
      }
    };
  
  private static final IColorizer COLORIZER_FOLIAGE_PINE = new IColorizer() {
      public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        return (CustomColors.foliagePineColors != null) ? CustomColors.foliagePineColors.getColor(blockAccess, blockPos) : ColorizerFoliage.getFoliageColorPine();
      }
      
      public boolean isColorConstant() {
        return (CustomColors.foliagePineColors == null);
      }
    };
  
  private static final IColorizer COLORIZER_FOLIAGE_BIRCH = new IColorizer() {
      public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        return (CustomColors.foliageBirchColors != null) ? CustomColors.foliageBirchColors.getColor(blockAccess, blockPos) : ColorizerFoliage.getFoliageColorBirch();
      }
      
      public boolean isColorConstant() {
        return (CustomColors.foliageBirchColors == null);
      }
    };
  
  private static final IColorizer COLORIZER_WATER = new IColorizer() {
      public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
        BiomeGenBase biomegenbase = CustomColors.getColorBiome(blockAccess, blockPos);
        return (CustomColors.waterColors != null) ? CustomColors.waterColors.getColor(biomegenbase, blockPos) : (Reflector.ForgeBiome_getWaterColorMultiplier.exists() ? Reflector.callInt(biomegenbase, Reflector.ForgeBiome_getWaterColorMultiplier, new Object[0]) : biomegenbase.waterColorMultiplier);
      }
      
      public boolean isColorConstant() {
        return false;
      }
    };
  
  public static void update() {
    paletteFormatDefault = "vanilla";
    waterColors = null;
    foliageBirchColors = null;
    foliagePineColors = null;
    swampGrassColors = null;
    swampFoliageColors = null;
    skyColors = null;
    fogColors = null;
    underwaterColors = null;
    underlavaColors = null;
    redstoneColors = null;
    xpOrbColors = null;
    xpOrbTime = -1;
    durabilityColors = null;
    stemColors = null;
    myceliumParticleColors = null;
    lightMapPacks = null;
    particleWaterColor = -1;
    particlePortalColor = -1;
    lilyPadColor = -1;
    expBarTextColor = -1;
    bossTextColor = -1;
    signTextColor = -1;
    fogColorNether = null;
    fogColorEnd = null;
    skyColorEnd = null;
    colorsBlockColormaps = null;
    blockColormaps = (CustomColormap[][])null;
    useDefaultGrassFoliageColors = true;
    spawnEggPrimaryColors = null;
    spawnEggSecondaryColors = null;
    wolfCollarColors = (float[][])null;
    sheepColors = (float[][])null;
    textColors = null;
    setMapColors(mapColorsOriginal);
    potionColors = null;
    paletteFormatDefault = getValidProperty("mcpatcher/color.properties", "palette.format", CustomColormap.FORMAT_STRINGS, "vanilla");
    String s = "mcpatcher/colormap/";
    String[] astring = { "water.png", "watercolorX.png" };
    waterColors = getCustomColors(s, astring, 256, 256);
    updateUseDefaultGrassFoliageColors();
    if (Config.isCustomColors()) {
      String[] astring1 = { "pine.png", "pinecolor.png" };
      foliagePineColors = getCustomColors(s, astring1, 256, 256);
      String[] astring2 = { "birch.png", "birchcolor.png" };
      foliageBirchColors = getCustomColors(s, astring2, 256, 256);
      String[] astring3 = { "swampgrass.png", "swampgrasscolor.png" };
      swampGrassColors = getCustomColors(s, astring3, 256, 256);
      String[] astring4 = { "swampfoliage.png", "swampfoliagecolor.png" };
      swampFoliageColors = getCustomColors(s, astring4, 256, 256);
      String[] astring5 = { "sky0.png", "skycolor0.png" };
      skyColors = getCustomColors(s, astring5, 256, 256);
      String[] astring6 = { "fog0.png", "fogcolor0.png" };
      fogColors = getCustomColors(s, astring6, 256, 256);
      String[] astring7 = { "underwater.png", "underwatercolor.png" };
      underwaterColors = getCustomColors(s, astring7, 256, 256);
      String[] astring8 = { "underlava.png", "underlavacolor.png" };
      underlavaColors = getCustomColors(s, astring8, 256, 256);
      String[] astring9 = { "redstone.png", "redstonecolor.png" };
      redstoneColors = getCustomColors(s, astring9, 16, 1);
      xpOrbColors = getCustomColors(s + "xporb.png", -1, -1);
      durabilityColors = getCustomColors(s + "durability.png", -1, -1);
      String[] astring10 = { "stem.png", "stemcolor.png" };
      stemColors = getCustomColors(s, astring10, 8, 1);
      stemPumpkinColors = getCustomColors(s + "pumpkinstem.png", 8, 1);
      stemMelonColors = getCustomColors(s + "melonstem.png", 8, 1);
      String[] astring11 = { "myceliumparticle.png", "myceliumparticlecolor.png" };
      myceliumParticleColors = getCustomColors(s, astring11, -1, -1);
      Pair<LightMapPack[], Integer> pair = parseLightMapPacks();
      lightMapPacks = (LightMapPack[])pair.getLeft();
      lightmapMinDimensionId = ((Integer)pair.getRight()).intValue();
      readColorProperties("mcpatcher/color.properties");
      blockColormaps = readBlockColormaps(new String[] { s + "custom/", s + "blocks/" }, colorsBlockColormaps, 256, 256);
      updateUseDefaultGrassFoliageColors();
    } 
  }
  
  private static String getValidProperty(String fileName, String key, String[] validValues, String valDef) {
    try {
      ResourceLocation resourcelocation = new ResourceLocation(fileName);
      InputStream inputstream = Config.getResourceStream(resourcelocation);
      if (inputstream == null)
        return valDef; 
      PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
      propertiesOrdered.load(inputstream);
      inputstream.close();
      String s = propertiesOrdered.getProperty(key);
      if (s == null)
        return valDef; 
      List<String> list = Arrays.asList(validValues);
      if (!list.contains(s)) {
        warn("Invalid value: " + key + "=" + s);
        warn("Expected values: " + Config.arrayToString((Object[])validValues));
        return valDef;
      } 
      dbg("" + key + "=" + s);
      return s;
    } catch (FileNotFoundException var9) {
      return valDef;
    } catch (IOException ioexception) {
      ioexception.printStackTrace();
      return valDef;
    } 
  }
  
  private static Pair<LightMapPack[], Integer> parseLightMapPacks() {
    String s = "mcpatcher/lightmap/world";
    String s1 = ".png";
    String[] astring = ResUtils.collectFiles(s, s1);
    Map<Integer, String> map = new HashMap<>();
    for (int i = 0; i < astring.length; i++) {
      String s2 = astring[i];
      String s3 = StrUtils.removePrefixSuffix(s2, s, s1);
      int j = Config.parseInt(s3, -2147483648);
      if (j == Integer.MIN_VALUE) {
        warn("Invalid dimension ID: " + s3 + ", path: " + s2);
      } else {
        map.put(Integer.valueOf(j), s2);
      } 
    } 
    Set<Integer> set = map.keySet();
    Integer[] ainteger = set.<Integer>toArray(new Integer[set.size()]);
    Arrays.sort((Object[])ainteger);
    if (ainteger.length <= 0)
      return (Pair<LightMapPack[], Integer>)new ImmutablePair(null, Integer.valueOf(0)); 
    int j1 = ainteger[0].intValue();
    int k1 = ainteger[ainteger.length - 1].intValue();
    int k = k1 - j1 + 1;
    CustomColormap[] acustomcolormap = new CustomColormap[k];
    for (int l = 0; l < ainteger.length; l++) {
      Integer integer = ainteger[l];
      String s4 = map.get(integer);
      CustomColormap customcolormap = getCustomColors(s4, -1, -1);
      if (customcolormap != null)
        if (customcolormap.getWidth() < 16) {
          warn("Invalid lightmap width: " + customcolormap.getWidth() + ", path: " + s4);
        } else {
          int i1 = integer.intValue() - j1;
          acustomcolormap[i1] = customcolormap;
        }  
    } 
    LightMapPack[] alightmappack = new LightMapPack[acustomcolormap.length];
    for (int l1 = 0; l1 < acustomcolormap.length; l1++) {
      CustomColormap customcolormap3 = acustomcolormap[l1];
      if (customcolormap3 != null) {
        String s5 = customcolormap3.name;
        String s6 = customcolormap3.basePath;
        CustomColormap customcolormap1 = getCustomColors(s6 + "/" + s5 + "_rain.png", -1, -1);
        CustomColormap customcolormap2 = getCustomColors(s6 + "/" + s5 + "_thunder.png", -1, -1);
        LightMap lightmap = new LightMap(customcolormap3);
        LightMap lightmap1 = (customcolormap1 != null) ? new LightMap(customcolormap1) : null;
        LightMap lightmap2 = (customcolormap2 != null) ? new LightMap(customcolormap2) : null;
        LightMapPack lightmappack = new LightMapPack(lightmap, lightmap1, lightmap2);
        alightmappack[l1] = lightmappack;
      } 
    } 
    return (Pair<LightMapPack[], Integer>)new ImmutablePair(alightmappack, Integer.valueOf(j1));
  }
  
  private static int getTextureHeight(String path, int defHeight) {
    try {
      InputStream inputstream = Config.getResourceStream(new ResourceLocation(path));
      if (inputstream == null)
        return defHeight; 
      BufferedImage bufferedimage = ImageIO.read(inputstream);
      inputstream.close();
      return (bufferedimage == null) ? defHeight : bufferedimage.getHeight();
    } catch (IOException var4) {
      return defHeight;
    } 
  }
  
  private static void readColorProperties(String fileName) {
    try {
      ResourceLocation resourcelocation = new ResourceLocation(fileName);
      InputStream inputstream = Config.getResourceStream(resourcelocation);
      if (inputstream == null)
        return; 
      dbg("Loading " + fileName);
      PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
      propertiesOrdered.load(inputstream);
      inputstream.close();
      particleWaterColor = readColor((Properties)propertiesOrdered, new String[] { "particle.water", "drop.water" });
      particlePortalColor = readColor((Properties)propertiesOrdered, "particle.portal");
      lilyPadColor = readColor((Properties)propertiesOrdered, "lilypad");
      expBarTextColor = readColor((Properties)propertiesOrdered, "text.xpbar");
      bossTextColor = readColor((Properties)propertiesOrdered, "text.boss");
      signTextColor = readColor((Properties)propertiesOrdered, "text.sign");
      fogColorNether = readColorVec3((Properties)propertiesOrdered, "fog.nether");
      fogColorEnd = readColorVec3((Properties)propertiesOrdered, "fog.end");
      skyColorEnd = readColorVec3((Properties)propertiesOrdered, "sky.end");
      colorsBlockColormaps = readCustomColormaps((Properties)propertiesOrdered, fileName);
      spawnEggPrimaryColors = readSpawnEggColors((Properties)propertiesOrdered, fileName, "egg.shell.", "Spawn egg shell");
      spawnEggSecondaryColors = readSpawnEggColors((Properties)propertiesOrdered, fileName, "egg.spots.", "Spawn egg spot");
      wolfCollarColors = readDyeColors((Properties)propertiesOrdered, fileName, "collar.", "Wolf collar");
      sheepColors = readDyeColors((Properties)propertiesOrdered, fileName, "sheep.", "Sheep");
      textColors = readTextColors((Properties)propertiesOrdered, fileName, "text.code.", "Text");
      int[] aint = readMapColors((Properties)propertiesOrdered, fileName, "map.", "Map");
      if (aint != null) {
        if (mapColorsOriginal == null)
          mapColorsOriginal = getMapColors(); 
        setMapColors(aint);
      } 
      potionColors = readPotionColors((Properties)propertiesOrdered, fileName, "potion.", "Potion");
      xpOrbTime = Config.parseInt(propertiesOrdered.getProperty("xporb.time"), -1);
    } catch (FileNotFoundException var5) {
      return;
    } catch (IOException ioexception) {
      ioexception.printStackTrace();
    } 
  }
  
  private static CustomColormap[] readCustomColormaps(Properties props, String fileName) {
    List<CustomColormap> list = new ArrayList();
    String s = "palette.block.";
    Map<Object, Object> map = new HashMap<>();
    for (Object e : props.keySet()) {
      String s1 = (String)e;
      String s2 = props.getProperty(s1);
      if (s1.startsWith(s))
        map.put(s1, s2); 
    } 
    String[] astring = (String[])map.keySet().toArray((Object[])new String[map.size()]);
    for (int j = 0; j < astring.length; j++) {
      String s6 = astring[j];
      String s3 = props.getProperty(s6);
      dbg("Block palette: " + s6 + " = " + s3);
      String s4 = s6.substring(s.length());
      String s5 = TextureUtils.getBasePath(fileName);
      s4 = TextureUtils.fixResourcePath(s4, s5);
      CustomColormap customcolormap = getCustomColors(s4, 256, 256);
      if (customcolormap == null) {
        warn("Colormap not found: " + s4);
      } else {
        ConnectedParser connectedparser = new ConnectedParser("CustomColors");
        MatchBlock[] amatchblock = connectedparser.parseMatchBlocks(s3);
        if (amatchblock != null && amatchblock.length > 0) {
          for (int i = 0; i < amatchblock.length; i++) {
            MatchBlock matchblock = amatchblock[i];
            customcolormap.addMatchBlock(matchblock);
          } 
          list.add(customcolormap);
        } else {
          warn("Invalid match blocks: " + s3);
        } 
      } 
    } 
    if (list.size() <= 0)
      return null; 
    CustomColormap[] acustomcolormap = list.<CustomColormap>toArray(new CustomColormap[list.size()]);
    return acustomcolormap;
  }
  
  private static CustomColormap[][] readBlockColormaps(String[] basePaths, CustomColormap[] basePalettes, int width, int height) {
    String[] astring = ResUtils.collectFiles(basePaths, new String[] { ".properties" });
    Arrays.sort((Object[])astring);
    List list = new ArrayList();
    for (int i = 0; i < astring.length; i++) {
      String s = astring[i];
      dbg("Block colormap: " + s);
      try {
        ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);
        InputStream inputstream = Config.getResourceStream(resourcelocation);
        if (inputstream == null) {
          warn("File not found: " + s);
        } else {
          PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
          propertiesOrdered.load(inputstream);
          CustomColormap customcolormap = new CustomColormap((Properties)propertiesOrdered, s, width, height, paletteFormatDefault);
          if (customcolormap.isValid(s) && customcolormap.isValidMatchBlocks(s))
            addToBlockList(customcolormap, list); 
        } 
      } catch (FileNotFoundException var12) {
        warn("File not found: " + s);
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
    } 
    if (basePalettes != null)
      for (int j = 0; j < basePalettes.length; j++) {
        CustomColormap customcolormap1 = basePalettes[j];
        addToBlockList(customcolormap1, list);
      }  
    if (list.size() <= 0)
      return (CustomColormap[][])null; 
    CustomColormap[][] acustomcolormap = blockListToArray(list);
    return acustomcolormap;
  }
  
  private static void addToBlockList(CustomColormap cm, List blockList) {
    int[] aint = cm.getMatchBlockIds();
    if (aint != null && aint.length > 0) {
      for (int i = 0; i < aint.length; i++) {
        int j = aint[i];
        if (j < 0) {
          warn("Invalid block ID: " + j);
        } else {
          addToList(cm, blockList, j);
        } 
      } 
    } else {
      warn("No match blocks: " + Config.arrayToString(aint));
    } 
  }
  
  private static void addToList(CustomColormap cm, List<List> lists, int id) {
    while (id >= lists.size())
      lists.add(null); 
    List<List> list = lists.get(id);
    if (list == null) {
      list = new ArrayList();
      list.set(id, list);
    } 
    list.add(cm);
  }
  
  private static CustomColormap[][] blockListToArray(List<List> lists) {
    CustomColormap[][] acustomcolormap = new CustomColormap[lists.size()][];
    for (int i = 0; i < lists.size(); i++) {
      List list = lists.get(i);
      if (list != null) {
        CustomColormap[] acustomcolormap1 = (CustomColormap[])list.toArray((Object[])new CustomColormap[list.size()]);
        acustomcolormap[i] = acustomcolormap1;
      } 
    } 
    return acustomcolormap;
  }
  
  private static int readColor(Properties props, String[] names) {
    for (int i = 0; i < names.length; i++) {
      String s = names[i];
      int j = readColor(props, s);
      if (j >= 0)
        return j; 
    } 
    return -1;
  }
  
  private static int readColor(Properties props, String name) {
    String s = props.getProperty(name);
    if (s == null)
      return -1; 
    s = s.trim();
    int i = parseColor(s);
    if (i < 0) {
      warn("Invalid color: " + name + " = " + s);
      return i;
    } 
    dbg(name + " = " + s);
    return i;
  }
  
  private static int parseColor(String str) {
    if (str == null)
      return -1; 
    str = str.trim();
    try {
      int i = Integer.parseInt(str, 16) & 0xFFFFFF;
      return i;
    } catch (NumberFormatException var2) {
      return -1;
    } 
  }
  
  private static Vec3 readColorVec3(Properties props, String name) {
    int i = readColor(props, name);
    if (i < 0)
      return null; 
    int j = i >> 16 & 0xFF;
    int k = i >> 8 & 0xFF;
    int l = i & 0xFF;
    float f = j / 255.0F;
    float f1 = k / 255.0F;
    float f2 = l / 255.0F;
    return new Vec3(f, f1, f2);
  }
  
  private static CustomColormap getCustomColors(String basePath, String[] paths, int width, int height) {
    for (int i = 0; i < paths.length; i++) {
      String s = paths[i];
      s = basePath + s;
      CustomColormap customcolormap = getCustomColors(s, width, height);
      if (customcolormap != null)
        return customcolormap; 
    } 
    return null;
  }
  
  public static CustomColormap getCustomColors(String pathImage, int width, int height) {
    try {
      ResourceLocation resourcelocation = new ResourceLocation(pathImage);
      if (!Config.hasResource(resourcelocation))
        return null; 
      dbg("Colormap " + pathImage);
      PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
      String s = StrUtils.replaceSuffix(pathImage, ".png", ".properties");
      ResourceLocation resourcelocation1 = new ResourceLocation(s);
      if (Config.hasResource(resourcelocation1)) {
        InputStream inputstream = Config.getResourceStream(resourcelocation1);
        propertiesOrdered.load(inputstream);
        inputstream.close();
        dbg("Colormap properties: " + s);
      } else {
        propertiesOrdered.put("format", paletteFormatDefault);
        propertiesOrdered.put("source", pathImage);
        s = pathImage;
      } 
      CustomColormap customcolormap = new CustomColormap((Properties)propertiesOrdered, s, width, height, paletteFormatDefault);
      return !customcolormap.isValid(s) ? null : customcolormap;
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    } 
  }
  
  public static void updateUseDefaultGrassFoliageColors() {
    useDefaultGrassFoliageColors = (foliageBirchColors == null && foliagePineColors == null && swampGrassColors == null && swampFoliageColors == null && Config.isSwampColors() && Config.isSmoothBiomes());
  }
  
  public static int getColorMultiplier(BakedQuad quad, IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
    IColorizer customcolors$icolorizer;
    Block block = blockState.getBlock();
    IBlockState iblockstate = renderEnv.getBlockState();
    if (blockColormaps != null) {
      if (!quad.hasTintIndex()) {
        if (block == Blocks.grass)
          iblockstate = BLOCK_STATE_DIRT; 
        if (block == Blocks.redstone_wire)
          return -1; 
      } 
      if (block == Blocks.double_plant && renderEnv.getMetadata() >= 8) {
        blockPos = blockPos.down();
        iblockstate = blockAccess.getBlockState(blockPos);
      } 
      CustomColormap customcolormap = getBlockColormap(iblockstate);
      if (customcolormap != null) {
        if (Config.isSmoothBiomes() && !customcolormap.isColorConstant())
          return getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolormap, renderEnv.getColorizerBlockPosM()); 
        return customcolormap.getColor(blockAccess, blockPos);
      } 
    } 
    if (!quad.hasTintIndex())
      return -1; 
    if (block == Blocks.waterlily)
      return getLilypadColorMultiplier(blockAccess, blockPos); 
    if (block == Blocks.redstone_wire)
      return getRedstoneColor(renderEnv.getBlockState()); 
    if (block instanceof net.minecraft.block.BlockStem)
      return getStemColorMultiplier(block, blockAccess, blockPos, renderEnv); 
    if (useDefaultGrassFoliageColors)
      return -1; 
    int i = renderEnv.getMetadata();
    if (block != Blocks.grass && block != Blocks.tallgrass && block != Blocks.double_plant) {
      if (block == Blocks.double_plant) {
        customcolors$icolorizer = COLORIZER_GRASS;
        if (i >= 8)
          blockPos = blockPos.down(); 
      } else if (block == Blocks.leaves) {
        switch (i & 0x3) {
          case 0:
            customcolors$icolorizer = COLORIZER_FOLIAGE;
            break;
          case 1:
            customcolors$icolorizer = COLORIZER_FOLIAGE_PINE;
            break;
          case 2:
            customcolors$icolorizer = COLORIZER_FOLIAGE_BIRCH;
            break;
          default:
            customcolors$icolorizer = COLORIZER_FOLIAGE;
            break;
        } 
      } else if (block == Blocks.leaves2) {
        customcolors$icolorizer = COLORIZER_FOLIAGE;
      } else {
        if (block != Blocks.vine)
          return -1; 
        customcolors$icolorizer = COLORIZER_FOLIAGE;
      } 
    } else {
      customcolors$icolorizer = COLORIZER_GRASS;
    } 
    return (Config.isSmoothBiomes() && !customcolors$icolorizer.isColorConstant()) ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolors$icolorizer, renderEnv.getColorizerBlockPosM()) : customcolors$icolorizer.getColor(iblockstate, blockAccess, blockPos);
  }
  
  protected static BiomeGenBase getColorBiome(IBlockAccess blockAccess, BlockPos blockPos) {
    BiomeGenBase biomegenbase = blockAccess.getBiomeGenForCoords(blockPos);
    if (biomegenbase == BiomeGenBase.swampland && !Config.isSwampColors())
      biomegenbase = BiomeGenBase.plains; 
    return biomegenbase;
  }
  
  private static CustomColormap getBlockColormap(IBlockState blockState) {
    if (blockColormaps == null)
      return null; 
    if (!(blockState instanceof BlockStateBase))
      return null; 
    BlockStateBase blockstatebase = (BlockStateBase)blockState;
    int i = blockstatebase.getBlockId();
    if (i >= 0 && i < blockColormaps.length) {
      CustomColormap[] acustomcolormap = blockColormaps[i];
      if (acustomcolormap == null)
        return null; 
      for (int j = 0; j < acustomcolormap.length; j++) {
        CustomColormap customcolormap = acustomcolormap[j];
        if (customcolormap.matchesBlock(blockstatebase))
          return customcolormap; 
      } 
      return null;
    } 
    return null;
  }
  
  private static int getSmoothColorMultiplier(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, IColorizer colorizer, BlockPosM blockPosM) {
    int i = 0;
    int j = 0;
    int k = 0;
    int l = blockPos.getX();
    int i1 = blockPos.getY();
    int j1 = blockPos.getZ();
    BlockPosM blockposm = blockPosM;
    for (int k1 = l - 1; k1 <= l + 1; k1++) {
      for (int l1 = j1 - 1; l1 <= j1 + 1; l1++) {
        blockposm.setXyz(k1, i1, l1);
        int i2 = colorizer.getColor(blockState, blockAccess, blockposm);
        i += i2 >> 16 & 0xFF;
        j += i2 >> 8 & 0xFF;
        k += i2 & 0xFF;
      } 
    } 
    int j2 = i / 9;
    int k2 = j / 9;
    int l2 = k / 9;
    return j2 << 16 | k2 << 8 | l2;
  }
  
  public static int getFluidColor(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, RenderEnv renderEnv) {
    Block block = blockState.getBlock();
    IColorizer customcolors$icolorizer = getBlockColormap(blockState);
    if (customcolors$icolorizer == null && blockState.getBlock().getMaterial() == Material.water)
      customcolors$icolorizer = COLORIZER_WATER; 
    return (customcolors$icolorizer == null) ? block.colorMultiplier(blockAccess, blockPos, 0) : ((Config.isSmoothBiomes() && !customcolors$icolorizer.isColorConstant()) ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolors$icolorizer, renderEnv.getColorizerBlockPosM()) : customcolors$icolorizer.getColor(blockState, blockAccess, blockPos));
  }
  
  public static void updatePortalFX(EntityFX fx) {
    if (particlePortalColor >= 0) {
      int i = particlePortalColor;
      int j = i >> 16 & 0xFF;
      int k = i >> 8 & 0xFF;
      int l = i & 0xFF;
      float f = j / 255.0F;
      float f1 = k / 255.0F;
      float f2 = l / 255.0F;
      fx.setRBGColorF(f, f1, f2);
    } 
  }
  
  public static void updateMyceliumFX(EntityFX fx) {
    if (myceliumParticleColors != null) {
      int i = myceliumParticleColors.getColorRandom();
      int j = i >> 16 & 0xFF;
      int k = i >> 8 & 0xFF;
      int l = i & 0xFF;
      float f = j / 255.0F;
      float f1 = k / 255.0F;
      float f2 = l / 255.0F;
      fx.setRBGColorF(f, f1, f2);
    } 
  }
  
  private static int getRedstoneColor(IBlockState blockState) {
    if (redstoneColors == null)
      return -1; 
    int i = getRedstoneLevel(blockState, 15);
    int j = redstoneColors.getColor(i);
    return j;
  }
  
  public static void updateReddustFX(EntityFX fx, IBlockAccess blockAccess, double x, double y, double z) {
    if (redstoneColors != null) {
      IBlockState iblockstate = blockAccess.getBlockState(new BlockPos(x, y, z));
      int i = getRedstoneLevel(iblockstate, 15);
      int j = redstoneColors.getColor(i);
      int k = j >> 16 & 0xFF;
      int l = j >> 8 & 0xFF;
      int i1 = j & 0xFF;
      float f = k / 255.0F;
      float f1 = l / 255.0F;
      float f2 = i1 / 255.0F;
      fx.setRBGColorF(f, f1, f2);
    } 
  }
  
  private static int getRedstoneLevel(IBlockState state, int def) {
    Block block = state.getBlock();
    if (!(block instanceof BlockRedstoneWire))
      return def; 
    Object object = state.getValue((IProperty)BlockRedstoneWire.POWER);
    if (!(object instanceof Integer))
      return def; 
    Integer integer = (Integer)object;
    return integer.intValue();
  }
  
  public static float getXpOrbTimer(float timer) {
    if (xpOrbTime <= 0)
      return timer; 
    float f = 628.0F / xpOrbTime;
    return timer * f;
  }
  
  public static int getXpOrbColor(float timer) {
    if (xpOrbColors == null)
      return -1; 
    int i = (int)Math.round(((MathHelper.sin(timer) + 1.0F) * (xpOrbColors.getLength() - 1)) / 2.0D);
    int j = xpOrbColors.getColor(i);
    return j;
  }
  
  public static int getDurabilityColor(int dur255) {
    if (durabilityColors == null)
      return -1; 
    int i = dur255 * durabilityColors.getLength() / 255;
    int j = durabilityColors.getColor(i);
    return j;
  }
  
  public static void updateWaterFX(EntityFX fx, IBlockAccess blockAccess, double x, double y, double z, RenderEnv renderEnv) {
    if (waterColors != null || blockColormaps != null || particleWaterColor >= 0) {
      BlockPos blockpos = new BlockPos(x, y, z);
      renderEnv.reset(BLOCK_STATE_WATER, blockpos);
      int i = getFluidColor(blockAccess, BLOCK_STATE_WATER, blockpos, renderEnv);
      int j = i >> 16 & 0xFF;
      int k = i >> 8 & 0xFF;
      int l = i & 0xFF;
      float f = j / 255.0F;
      float f1 = k / 255.0F;
      float f2 = l / 255.0F;
      if (particleWaterColor >= 0) {
        int i1 = particleWaterColor >> 16 & 0xFF;
        int j1 = particleWaterColor >> 8 & 0xFF;
        int k1 = particleWaterColor & 0xFF;
        f *= i1 / 255.0F;
        f1 *= j1 / 255.0F;
        f2 *= k1 / 255.0F;
      } 
      fx.setRBGColorF(f, f1, f2);
    } 
  }
  
  private static int getLilypadColorMultiplier(IBlockAccess blockAccess, BlockPos blockPos) {
    return (lilyPadColor < 0) ? Blocks.waterlily.colorMultiplier(blockAccess, blockPos) : lilyPadColor;
  }
  
  private static Vec3 getFogColorNether(Vec3 col) {
    return (fogColorNether == null) ? col : fogColorNether;
  }
  
  private static Vec3 getFogColorEnd(Vec3 col) {
    return (fogColorEnd == null) ? col : fogColorEnd;
  }
  
  private static Vec3 getSkyColorEnd(Vec3 col) {
    return (skyColorEnd == null) ? col : skyColorEnd;
  }
  
  public static Vec3 getSkyColor(Vec3 skyColor3d, IBlockAccess blockAccess, double x, double y, double z) {
    if (skyColors == null)
      return skyColor3d; 
    int i = skyColors.getColorSmooth(blockAccess, x, y, z, 3);
    int j = i >> 16 & 0xFF;
    int k = i >> 8 & 0xFF;
    int l = i & 0xFF;
    float f = j / 255.0F;
    float f1 = k / 255.0F;
    float f2 = l / 255.0F;
    float f3 = (float)skyColor3d.xCoord / 0.5F;
    float f4 = (float)skyColor3d.yCoord / 0.66275F;
    float f5 = (float)skyColor3d.zCoord;
    f *= f3;
    f1 *= f4;
    f2 *= f5;
    Vec3 vec3 = skyColorFader.getColor(f, f1, f2);
    return vec3;
  }
  
  private static Vec3 getFogColor(Vec3 fogColor3d, IBlockAccess blockAccess, double x, double y, double z) {
    if (fogColors == null)
      return fogColor3d; 
    int i = fogColors.getColorSmooth(blockAccess, x, y, z, 3);
    int j = i >> 16 & 0xFF;
    int k = i >> 8 & 0xFF;
    int l = i & 0xFF;
    float f = j / 255.0F;
    float f1 = k / 255.0F;
    float f2 = l / 255.0F;
    float f3 = (float)fogColor3d.xCoord / 0.753F;
    float f4 = (float)fogColor3d.yCoord / 0.8471F;
    float f5 = (float)fogColor3d.zCoord;
    f *= f3;
    f1 *= f4;
    f2 *= f5;
    Vec3 vec3 = fogColorFader.getColor(f, f1, f2);
    return vec3;
  }
  
  public static Vec3 getUnderwaterColor(IBlockAccess blockAccess, double x, double y, double z) {
    return getUnderFluidColor(blockAccess, x, y, z, underwaterColors, underwaterColorFader);
  }
  
  public static Vec3 getUnderlavaColor(IBlockAccess blockAccess, double x, double y, double z) {
    return getUnderFluidColor(blockAccess, x, y, z, underlavaColors, underlavaColorFader);
  }
  
  public static Vec3 getUnderFluidColor(IBlockAccess blockAccess, double x, double y, double z, CustomColormap underFluidColors, CustomColorFader underFluidColorFader) {
    if (underFluidColors == null)
      return null; 
    int i = underFluidColors.getColorSmooth(blockAccess, x, y, z, 3);
    int j = i >> 16 & 0xFF;
    int k = i >> 8 & 0xFF;
    int l = i & 0xFF;
    float f = j / 255.0F;
    float f1 = k / 255.0F;
    float f2 = l / 255.0F;
    Vec3 vec3 = underFluidColorFader.getColor(f, f1, f2);
    return vec3;
  }
  
  private static int getStemColorMultiplier(Block blockStem, IBlockAccess blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
    CustomColormap customcolormap = stemColors;
    if (blockStem == Blocks.pumpkin_stem && stemPumpkinColors != null)
      customcolormap = stemPumpkinColors; 
    if (blockStem == Blocks.melon_stem && stemMelonColors != null)
      customcolormap = stemMelonColors; 
    if (customcolormap == null)
      return -1; 
    int i = renderEnv.getMetadata();
    return customcolormap.getColor(i);
  }
  
  public static boolean updateLightmap(World world, float torchFlickerX, int[] lmColors, boolean nightvision, float partialTicks) {
    if (world == null)
      return false; 
    if (lightMapPacks == null)
      return false; 
    int i = world.provider.getDimensionId();
    int j = i - lightmapMinDimensionId;
    if (j >= 0 && j < lightMapPacks.length) {
      LightMapPack lightmappack = lightMapPacks[j];
      return (lightmappack == null) ? false : lightmappack.updateLightmap(world, torchFlickerX, lmColors, nightvision, partialTicks);
    } 
    return false;
  }
  
  public static Vec3 getWorldFogColor(Vec3 fogVec, World world, Entity renderViewEntity, float partialTicks) {
    Minecraft minecraft;
    int i = world.provider.getDimensionId();
    switch (i) {
      case -1:
        fogVec = getFogColorNether(fogVec);
        break;
      case 0:
        minecraft = Minecraft.getMinecraft();
        fogVec = getFogColor(fogVec, (IBlockAccess)minecraft.theWorld, renderViewEntity.posX, renderViewEntity.posY + 1.0D, renderViewEntity.posZ);
        break;
      case 1:
        fogVec = getFogColorEnd(fogVec);
        break;
    } 
    return fogVec;
  }
  
  public static Vec3 getWorldSkyColor(Vec3 skyVec, World world, Entity renderViewEntity, float partialTicks) {
    Minecraft minecraft;
    int i = world.provider.getDimensionId();
    switch (i) {
      case 0:
        minecraft = Minecraft.getMinecraft();
        skyVec = getSkyColor(skyVec, (IBlockAccess)minecraft.theWorld, renderViewEntity.posX, renderViewEntity.posY + 1.0D, renderViewEntity.posZ);
        break;
      case 1:
        skyVec = getSkyColorEnd(skyVec);
        break;
    } 
    return skyVec;
  }
  
  private static int[] readSpawnEggColors(Properties props, String fileName, String prefix, String logName) {
    List<Integer> list = new ArrayList<>();
    Set set = props.keySet();
    int i = 0;
    for (Object e : set) {
      String s = (String)e;
      String s1 = props.getProperty(s);
      if (s.startsWith(prefix)) {
        String s2 = StrUtils.removePrefix(s, prefix);
        int j = EntityUtils.getEntityIdByName(s2);
        if (j < 0) {
          warn("Invalid spawn egg name: " + s);
          continue;
        } 
        int k = parseColor(s1);
        if (k < 0) {
          warn("Invalid spawn egg color: " + s + " = " + s1);
          continue;
        } 
        while (list.size() <= j)
          list.add(Integer.valueOf(-1)); 
        list.set(j, Integer.valueOf(k));
        i++;
      } 
    } 
    if (i <= 0)
      return null; 
    dbg(logName + " colors: " + i);
    int[] aint = new int[list.size()];
    for (int l = 0; l < aint.length; l++)
      aint[l] = ((Integer)list.get(l)).intValue(); 
    return aint;
  }
  
  private static int getSpawnEggColor(ItemMonsterPlacer item, ItemStack itemStack, int layer, int color) {
    int i = itemStack.getMetadata();
    int[] aint = (layer == 0) ? spawnEggPrimaryColors : spawnEggSecondaryColors;
    if (aint == null)
      return color; 
    if (i >= 0 && i < aint.length) {
      int j = aint[i];
      return (j < 0) ? color : j;
    } 
    return color;
  }
  
  public static int getColorFromItemStack(ItemStack itemStack, int layer, int color) {
    if (itemStack == null)
      return color; 
    Item item = itemStack.getItem();
    return (item == null) ? color : ((item instanceof ItemMonsterPlacer) ? getSpawnEggColor((ItemMonsterPlacer)item, itemStack, layer, color) : color);
  }
  
  private static float[][] readDyeColors(Properties props, String fileName, String prefix, String logName) {
    EnumDyeColor[] aenumdyecolor = EnumDyeColor.values();
    Map<String, EnumDyeColor> map = new HashMap<>();
    for (int i = 0; i < aenumdyecolor.length; i++) {
      EnumDyeColor enumdyecolor = aenumdyecolor[i];
      map.put(enumdyecolor.getName(), enumdyecolor);
    } 
    float[][] afloat1 = new float[aenumdyecolor.length][];
    int k = 0;
    for (Object e : props.keySet()) {
      String s = (String)e;
      String s1 = props.getProperty(s);
      if (s.startsWith(prefix)) {
        String s2 = StrUtils.removePrefix(s, prefix);
        if (s2.equals("lightBlue"))
          s2 = "light_blue"; 
        EnumDyeColor enumdyecolor1 = map.get(s2);
        int j = parseColor(s1);
        if (enumdyecolor1 != null && j >= 0) {
          float[] afloat = { (j >> 16 & 0xFF) / 255.0F, (j >> 8 & 0xFF) / 255.0F, (j & 0xFF) / 255.0F };
          afloat1[enumdyecolor1.ordinal()] = afloat;
          k++;
          continue;
        } 
        warn("Invalid color: " + s + " = " + s1);
      } 
    } 
    if (k <= 0)
      return (float[][])null; 
    dbg(logName + " colors: " + k);
    return afloat1;
  }
  
  private static float[] getDyeColors(EnumDyeColor dye, float[][] dyeColors, float[] colors) {
    if (dyeColors == null)
      return colors; 
    if (dye == null)
      return colors; 
    float[] afloat = dyeColors[dye.ordinal()];
    return (afloat == null) ? colors : afloat;
  }
  
  public static float[] getWolfCollarColors(EnumDyeColor dye, float[] colors) {
    return getDyeColors(dye, wolfCollarColors, colors);
  }
  
  public static float[] getSheepColors(EnumDyeColor dye, float[] colors) {
    return getDyeColors(dye, sheepColors, colors);
  }
  
  private static int[] readTextColors(Properties props, String fileName, String prefix, String logName) {
    int[] aint = new int[32];
    Arrays.fill(aint, -1);
    int i = 0;
    for (Object e : props.keySet()) {
      String s = (String)e;
      String s1 = props.getProperty(s);
      if (s.startsWith(prefix)) {
        String s2 = StrUtils.removePrefix(s, prefix);
        int j = Config.parseInt(s2, -1);
        int k = parseColor(s1);
        if (j >= 0 && j < aint.length && k >= 0) {
          aint[j] = k;
          i++;
          continue;
        } 
        warn("Invalid color: " + s + " = " + s1);
      } 
    } 
    if (i <= 0)
      return null; 
    dbg(logName + " colors: " + i);
    return aint;
  }
  
  public static int getTextColor(int index, int color) {
    if (textColors == null)
      return color; 
    if (index >= 0 && index < textColors.length) {
      int i = textColors[index];
      return (i < 0) ? color : i;
    } 
    return color;
  }
  
  private static int[] readMapColors(Properties props, String fileName, String prefix, String logName) {
    int[] aint = new int[MapColor.mapColorArray.length];
    Arrays.fill(aint, -1);
    int i = 0;
    for (Object o : props.keySet()) {
      String s = (String)o;
      String s1 = props.getProperty(s);
      if (s.startsWith(prefix)) {
        String s2 = StrUtils.removePrefix(s, prefix);
        int j = getMapColorIndex(s2);
        int k = parseColor(s1);
        if (j >= 0 && j < aint.length && k >= 0) {
          aint[j] = k;
          i++;
          continue;
        } 
        warn("Invalid color: " + s + " = " + s1);
      } 
    } 
    if (i <= 0)
      return null; 
    dbg(logName + " colors: " + i);
    return aint;
  }
  
  private static int[] readPotionColors(Properties props, String fileName, String prefix, String logName) {
    int[] aint = new int[Potion.potionTypes.length];
    Arrays.fill(aint, -1);
    int i = 0;
    for (Object e : props.keySet()) {
      String s = (String)e;
      String s1 = props.getProperty(s);
      if (s.startsWith(prefix)) {
        int j = getPotionId(s);
        int k = parseColor(s1);
        if (j >= 0 && j < aint.length && k >= 0) {
          aint[j] = k;
          i++;
          continue;
        } 
        warn("Invalid color: " + s + " = " + s1);
      } 
    } 
    if (i <= 0)
      return null; 
    dbg(logName + " colors: " + i);
    return aint;
  }
  
  private static int getPotionId(String name) {
    if (name.equals("potion.water"))
      return 0; 
    Potion[] apotion = Potion.potionTypes;
    for (int i = 0; i < apotion.length; i++) {
      Potion potion = apotion[i];
      if (potion != null && potion.getName().equals(name))
        return potion.getId(); 
    } 
    return -1;
  }
  
  public static int getPotionColor(int potionId, int color) {
    if (potionColors == null)
      return color; 
    if (potionId >= 0 && potionId < potionColors.length) {
      int i = potionColors[potionId];
      return (i < 0) ? color : i;
    } 
    return color;
  }
  
  private static int getMapColorIndex(String name) {
    return (name == null) ? -1 : (name.equals("air") ? MapColor.airColor.colorIndex : (name.equals("grass") ? MapColor.grassColor.colorIndex : (name.equals("sand") ? MapColor.sandColor.colorIndex : (name.equals("cloth") ? MapColor.clothColor.colorIndex : (name.equals("tnt") ? MapColor.tntColor.colorIndex : (name.equals("ice") ? MapColor.iceColor.colorIndex : (name.equals("iron") ? MapColor.ironColor.colorIndex : (name.equals("foliage") ? MapColor.foliageColor.colorIndex : (name.equals("clay") ? MapColor.clayColor.colorIndex : (name.equals("dirt") ? MapColor.dirtColor.colorIndex : (name.equals("stone") ? MapColor.stoneColor.colorIndex : (name.equals("water") ? MapColor.waterColor.colorIndex : (name.equals("wood") ? MapColor.woodColor.colorIndex : (name.equals("quartz") ? MapColor.quartzColor.colorIndex : (name.equals("gold") ? MapColor.goldColor.colorIndex : (name.equals("diamond") ? MapColor.diamondColor.colorIndex : (name.equals("lapis") ? MapColor.lapisColor.colorIndex : (name.equals("emerald") ? MapColor.emeraldColor.colorIndex : (name.equals("podzol") ? MapColor.obsidianColor.colorIndex : (name.equals("netherrack") ? MapColor.netherrackColor.colorIndex : ((!name.equals("snow") && !name.equals("white")) ? ((!name.equals("adobe") && !name.equals("orange")) ? (name.equals("magenta") ? MapColor.magentaColor.colorIndex : ((!name.equals("light_blue") && !name.equals("lightBlue")) ? (name.equals("yellow") ? MapColor.yellowColor.colorIndex : (name.equals("lime") ? MapColor.limeColor.colorIndex : (name.equals("pink") ? MapColor.pinkColor.colorIndex : (name.equals("gray") ? MapColor.grayColor.colorIndex : (name.equals("silver") ? MapColor.silverColor.colorIndex : (name.equals("cyan") ? MapColor.cyanColor.colorIndex : (name.equals("purple") ? MapColor.purpleColor.colorIndex : (name.equals("blue") ? MapColor.blueColor.colorIndex : (name.equals("brown") ? MapColor.brownColor.colorIndex : (name.equals("green") ? MapColor.greenColor.colorIndex : (name.equals("red") ? MapColor.redColor.colorIndex : (name.equals("black") ? MapColor.blackColor.colorIndex : -1)))))))))))) : MapColor.lightBlueColor.colorIndex)) : MapColor.adobeColor.colorIndex) : MapColor.snowColor.colorIndex)))))))))))))))))))));
  }
  
  private static int[] getMapColors() {
    MapColor[] amapcolor = MapColor.mapColorArray;
    int[] aint = new int[amapcolor.length];
    Arrays.fill(aint, -1);
    for (int i = 0; i < amapcolor.length && i < aint.length; i++) {
      MapColor mapcolor = amapcolor[i];
      if (mapcolor != null)
        aint[i] = mapcolor.colorValue; 
    } 
    return aint;
  }
  
  private static void setMapColors(int[] colors) {
    if (colors != null) {
      MapColor[] amapcolor = MapColor.mapColorArray;
      boolean flag = false;
      for (int i = 0; i < amapcolor.length && i < colors.length; i++) {
        MapColor mapcolor = amapcolor[i];
        if (mapcolor != null) {
          int j = colors[i];
          if (j >= 0 && mapcolor.colorValue != j) {
            mapcolor.colorValue = j;
            flag = true;
          } 
        } 
      } 
      if (flag)
        Minecraft.getMinecraft().getTextureManager().reloadBannerTextures(); 
    } 
  }
  
  private static void dbg(String str) {
    Config.dbg("CustomColors: " + str);
  }
  
  private static void warn(String str) {
    Config.warn("CustomColors: " + str);
  }
  
  public static int getExpBarTextColor(int color) {
    return (expBarTextColor < 0) ? color : expBarTextColor;
  }
  
  public static int getBossTextColor(int color) {
    return (bossTextColor < 0) ? color : bossTextColor;
  }
  
  public static int getSignTextColor(int color) {
    return (signTextColor < 0) ? color : signTextColor;
  }
  
  public static interface IColorizer {
    int getColor(IBlockState param1IBlockState, IBlockAccess param1IBlockAccess, BlockPos param1BlockPos);
    
    boolean isColorConstant();
  }
}
