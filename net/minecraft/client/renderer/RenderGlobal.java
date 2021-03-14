package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.GLColor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.optifine.CustomColors;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.SmartAnimations;
import net.optifine.model.BlockModelUtils;
import net.optifine.reflect.Reflector;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.CloudRenderer;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.shaders.ShadowUtils;
import net.optifine.util.ChunkUtils;
import net.optifine.util.RenderChunkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener {
  private static final Logger logger = LogManager.getLogger();
  
  private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
  
  private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
  
  private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
  
  private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
  
  private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");
  
  public final Minecraft mc;
  
  private final TextureManager renderEngine;
  
  private final RenderManager renderManager;
  
  private WorldClient theWorld;
  
  private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
  
  private List<ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(69696);
  
  private final Set<TileEntity> setTileEntities = Sets.newHashSet();
  
  private ViewFrustum viewFrustum;
  
  private int starGLCallList = -1;
  
  private int glSkyList = -1;
  
  private int glSkyList2 = -1;
  
  private VertexFormat vertexBufferFormat;
  
  private VertexBuffer starVBO;
  
  private VertexBuffer skyVBO;
  
  private VertexBuffer sky2VBO;
  
  private int cloudTickCounter;
  
  public final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.newHashMap();
  
  private final Map<BlockPos, ISound> mapSoundPositions = Maps.newHashMap();
  
  private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
  
  private Framebuffer entityOutlineFramebuffer;
  
  private ShaderGroup entityOutlineShader;
  
  private double frustumUpdatePosX = Double.MIN_VALUE;
  
  private double frustumUpdatePosY = Double.MIN_VALUE;
  
  private double frustumUpdatePosZ = Double.MIN_VALUE;
  
  private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
  
  private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
  
  private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
  
  private double lastViewEntityX = Double.MIN_VALUE;
  
  private double lastViewEntityY = Double.MIN_VALUE;
  
  private double lastViewEntityZ = Double.MIN_VALUE;
  
  private double lastViewEntityPitch = Double.MIN_VALUE;
  
  private double lastViewEntityYaw = Double.MIN_VALUE;
  
  private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
  
  private ChunkRenderContainer renderContainer;
  
  private int renderDistanceChunks = -1;
  
  private int renderEntitiesStartupCounter = 2;
  
  private int countEntitiesTotal;
  
  private int countEntitiesRendered;
  
  private int countEntitiesHidden;
  
  private boolean debugFixTerrainFrustum = false;
  
  private ClippingHelper debugFixedClippingHelper;
  
  private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
  
  private final Vector3d debugTerrainFrustumPosition = new Vector3d();
  
  private boolean vboEnabled = false;
  
  IRenderChunkFactory renderChunkFactory;
  
  private double prevRenderSortX;
  
  private double prevRenderSortY;
  
  private double prevRenderSortZ;
  
  public boolean displayListEntitiesDirty = true;
  
  private CloudRenderer cloudRenderer;
  
  public Entity renderedEntity;
  
  public Set chunksToResortTransparency = new LinkedHashSet();
  
  public Set chunksToUpdateForced = new LinkedHashSet();
  
  private Deque visibilityDeque = new ArrayDeque();
  
  private List renderInfosEntities = new ArrayList(1024);
  
  private List renderInfosTileEntities = new ArrayList(1024);
  
  private List renderInfosNormal = new ArrayList(1024);
  
  private List renderInfosEntitiesNormal = new ArrayList(1024);
  
  private List renderInfosTileEntitiesNormal = new ArrayList(1024);
  
  private List renderInfosShadow = new ArrayList(1024);
  
  private List renderInfosEntitiesShadow = new ArrayList(1024);
  
  private List renderInfosTileEntitiesShadow = new ArrayList(1024);
  
  private int renderDistance = 0;
  
  private int renderDistanceSq = 0;
  
  private static final Set SET_ALL_FACINGS = Collections.unmodifiableSet(new HashSet(Arrays.asList((Object[])EnumFacing.VALUES)));
  
  private int countTileEntitiesRendered;
  
  private IChunkProvider worldChunkProvider = null;
  
  private LongHashMap worldChunkProviderMap = null;
  
  private int countLoadedChunksPrev = 0;
  
  private RenderEnv renderEnv = new RenderEnv(Blocks.air.getDefaultState(), new BlockPos(0, 0, 0));
  
  public boolean renderOverlayDamaged = false;
  
  public boolean renderOverlayEyes = false;
  
  private boolean firstWorldLoad = false;
  
  private static int renderEntitiesCounter = 0;
  
  public RenderGlobal(Minecraft mcIn) {
    this.cloudRenderer = new CloudRenderer(mcIn);
    this.mc = mcIn;
    this.renderManager = mcIn.getRenderManager();
    this.renderEngine = mcIn.getTextureManager();
    this.renderEngine.bindTexture(locationForcefieldPng);
    GL11.glTexParameteri(3553, 10242, 10497);
    GL11.glTexParameteri(3553, 10243, 10497);
    GlStateManager.bindTexture(0);
    updateDestroyBlockIcons();
    this.vboEnabled = OpenGlHelper.useVbo();
    if (this.vboEnabled) {
      this.renderContainer = new VboRenderList();
      this.renderChunkFactory = (IRenderChunkFactory)new VboChunkFactory();
    } else {
      this.renderContainer = new RenderList();
      this.renderChunkFactory = (IRenderChunkFactory)new ListChunkFactory();
    } 
    this.vertexBufferFormat = new VertexFormat();
    this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
    generateStars();
    generateSky();
    generateSky2();
  }
  
  public void onResourceManagerReload(IResourceManager resourceManager) {
    updateDestroyBlockIcons();
  }
  
  private void updateDestroyBlockIcons() {
    TextureMap texturemap = this.mc.getTextureMapBlocks();
    for (int i = 0; i < this.destroyBlockIcons.length; i++)
      this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i); 
  }
  
  public void makeEntityOutlineShader() {
    if (OpenGlHelper.shadersSupported) {
      if (ShaderLinkHelper.getStaticShaderLinkHelper() == null)
        ShaderLinkHelper.setNewStaticShaderLinkHelper(); 
      ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");
      try {
        this.entityOutlineShader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), resourcelocation);
        this.entityOutlineShader.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
        this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
      } catch (IOException ioexception) {
        logger.warn("Failed to load shader: " + resourcelocation, ioexception);
        this.entityOutlineShader = null;
        this.entityOutlineFramebuffer = null;
      } catch (JsonSyntaxException jsonsyntaxexception) {
        logger.warn("Failed to load shader: " + resourcelocation, (Throwable)jsonsyntaxexception);
        this.entityOutlineShader = null;
        this.entityOutlineFramebuffer = null;
      } 
    } else {
      this.entityOutlineShader = null;
      this.entityOutlineFramebuffer = null;
    } 
  }
  
  public void renderEntityOutlineFramebuffer() {
    if (isRenderEntityOutlines()) {
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      this.entityOutlineFramebuffer.framebufferRenderExt(this.mc.displayWidth, this.mc.displayHeight, false);
      GlStateManager.disableBlend();
    } 
  }
  
  protected boolean isRenderEntityOutlines() {
    return (!Config.isFastRender() && !Config.isShaders() && !Config.isAntialiasing()) ? ((this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && this.mc.thePlayer != null && this.mc.thePlayer.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown())) : false;
  }
  
  private void generateSky2() {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    if (this.sky2VBO != null)
      this.sky2VBO.deleteGlBuffers(); 
    if (this.glSkyList2 >= 0) {
      GLAllocation.deleteDisplayLists(this.glSkyList2);
      this.glSkyList2 = -1;
    } 
    if (this.vboEnabled) {
      this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
      renderSky(worldrenderer, -16.0F, true);
      worldrenderer.finishDrawing();
      worldrenderer.reset();
      this.sky2VBO.bufferData(worldrenderer.getByteBuffer());
    } else {
      this.glSkyList2 = GLAllocation.generateDisplayLists(1);
      GL11.glNewList(this.glSkyList2, 4864);
      renderSky(worldrenderer, -16.0F, true);
      tessellator.draw();
      GL11.glEndList();
    } 
  }
  
  private void generateSky() {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    if (this.skyVBO != null)
      this.skyVBO.deleteGlBuffers(); 
    if (this.glSkyList >= 0) {
      GLAllocation.deleteDisplayLists(this.glSkyList);
      this.glSkyList = -1;
    } 
    if (this.vboEnabled) {
      this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
      renderSky(worldrenderer, 16.0F, false);
      worldrenderer.finishDrawing();
      worldrenderer.reset();
      this.skyVBO.bufferData(worldrenderer.getByteBuffer());
    } else {
      this.glSkyList = GLAllocation.generateDisplayLists(1);
      GL11.glNewList(this.glSkyList, 4864);
      renderSky(worldrenderer, 16.0F, false);
      tessellator.draw();
      GL11.glEndList();
    } 
  }
  
  private void renderSky(WorldRenderer worldRendererIn, float posY, boolean reverseX) {
    int i = 64;
    int j = 6;
    worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
    int k = (this.renderDistance / 64 + 1) * 64 + 64;
    for (int l = -k; l <= k; l += 64) {
      for (int i1 = -k; i1 <= k; i1 += 64) {
        float f = l;
        float f1 = (l + 64);
        if (reverseX) {
          f1 = l;
          f = (l + 64);
        } 
        worldRendererIn.pos(f, posY, i1).endVertex();
        worldRendererIn.pos(f1, posY, i1).endVertex();
        worldRendererIn.pos(f1, posY, (i1 + 64)).endVertex();
        worldRendererIn.pos(f, posY, (i1 + 64)).endVertex();
      } 
    } 
  }
  
  private void generateStars() {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    if (this.starVBO != null)
      this.starVBO.deleteGlBuffers(); 
    if (this.starGLCallList >= 0) {
      GLAllocation.deleteDisplayLists(this.starGLCallList);
      this.starGLCallList = -1;
    } 
    if (this.vboEnabled) {
      this.starVBO = new VertexBuffer(this.vertexBufferFormat);
      renderStars(worldrenderer);
      worldrenderer.finishDrawing();
      worldrenderer.reset();
      this.starVBO.bufferData(worldrenderer.getByteBuffer());
    } else {
      this.starGLCallList = GLAllocation.generateDisplayLists(1);
      GlStateManager.pushMatrix();
      GL11.glNewList(this.starGLCallList, 4864);
      renderStars(worldrenderer);
      tessellator.draw();
      GL11.glEndList();
      GlStateManager.popMatrix();
    } 
  }
  
  private void renderStars(WorldRenderer worldRendererIn) {
    Random random = new Random(10842L);
    worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
    for (int i = 0; i < 1500; i++) {
      double d0 = (random.nextFloat() * 2.0F - 1.0F);
      double d1 = (random.nextFloat() * 2.0F - 1.0F);
      double d2 = (random.nextFloat() * 2.0F - 1.0F);
      double d3 = (0.15F + random.nextFloat() * 0.1F);
      double d4 = d0 * d0 + d1 * d1 + d2 * d2;
      if (d4 < 1.0D && d4 > 0.01D) {
        d4 = 1.0D / Math.sqrt(d4);
        d0 *= d4;
        d1 *= d4;
        d2 *= d4;
        double d5 = d0 * 100.0D;
        double d6 = d1 * 100.0D;
        double d7 = d2 * 100.0D;
        double d8 = Math.atan2(d0, d2);
        double d9 = Math.sin(d8);
        double d10 = Math.cos(d8);
        double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
        double d12 = Math.sin(d11);
        double d13 = Math.cos(d11);
        double d14 = random.nextDouble() * Math.PI * 2.0D;
        double d15 = Math.sin(d14);
        double d16 = Math.cos(d14);
        for (int j = 0; j < 4; j++) {
          double d17 = 0.0D;
          double d18 = ((j & 0x2) - 1) * d3;
          double d19 = ((j + 1 & 0x2) - 1) * d3;
          double d20 = 0.0D;
          double d21 = d18 * d16 - d19 * d15;
          double d22 = d19 * d16 + d18 * d15;
          double d23 = d21 * d12 + 0.0D * d13;
          double d24 = 0.0D * d12 - d21 * d13;
          double d25 = d24 * d9 - d22 * d10;
          double d26 = d22 * d9 + d24 * d10;
          worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
        } 
      } 
    } 
  }
  
  public void setWorldAndLoadRenderers(WorldClient worldClientIn) {
    if (this.theWorld != null)
      this.theWorld.removeWorldAccess(this); 
    this.frustumUpdatePosX = Double.MIN_VALUE;
    this.frustumUpdatePosY = Double.MIN_VALUE;
    this.frustumUpdatePosZ = Double.MIN_VALUE;
    this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
    this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
    this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    this.renderManager.set((World)worldClientIn);
    this.theWorld = worldClientIn;
    if (Config.isDynamicLights())
      DynamicLights.clear(); 
    ChunkVisibility.reset();
    this.worldChunkProvider = null;
    this.worldChunkProviderMap = null;
    this.renderEnv.reset((IBlockState)null, (BlockPos)null);
    Shaders.checkWorldChanged((World)this.theWorld);
    if (worldClientIn != null) {
      worldClientIn.addWorldAccess(this);
      loadRenderers();
    } else {
      this.chunksToUpdate.clear();
      clearRenderInfos();
      if (this.viewFrustum != null)
        this.viewFrustum.deleteGlResources(); 
      this.viewFrustum = null;
    } 
  }
  
  public void loadRenderers() {
    if (this.theWorld != null) {
      this.displayListEntitiesDirty = true;
      Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());
      Blocks.leaves2.setGraphicsLevel(Config.isTreesFancy());
      BlockModelRenderer.updateAoLightValue();
      if (Config.isDynamicLights())
        DynamicLights.clear(); 
      SmartAnimations.update();
      this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
      this.renderDistance = this.renderDistanceChunks * 16;
      this.renderDistanceSq = this.renderDistance * this.renderDistance;
      boolean flag = this.vboEnabled;
      this.vboEnabled = OpenGlHelper.useVbo();
      if (flag && !this.vboEnabled) {
        this.renderContainer = new RenderList();
        this.renderChunkFactory = (IRenderChunkFactory)new ListChunkFactory();
      } else if (!flag && this.vboEnabled) {
        this.renderContainer = new VboRenderList();
        this.renderChunkFactory = (IRenderChunkFactory)new VboChunkFactory();
      } 
      generateStars();
      generateSky();
      generateSky2();
      if (this.viewFrustum != null)
        this.viewFrustum.deleteGlResources(); 
      stopChunkUpdates();
      synchronized (this.setTileEntities) {
        this.setTileEntities.clear();
      } 
      this.viewFrustum = new ViewFrustum((World)this.theWorld, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);
      if (this.theWorld != null) {
        Entity entity = this.mc.getRenderViewEntity();
        if (entity != null)
          this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ); 
      } 
      this.renderEntitiesStartupCounter = 2;
    } 
    if (this.mc.thePlayer == null)
      this.firstWorldLoad = true; 
  }
  
  protected void stopChunkUpdates() {
    this.chunksToUpdate.clear();
    this.renderDispatcher.stopChunkUpdates();
  }
  
  public void createBindEntityOutlineFbs(int width, int height) {
    if (OpenGlHelper.shadersSupported && this.entityOutlineShader != null)
      this.entityOutlineShader.createBindFramebuffers(width, height); 
  }
  
  public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
    // Byte code:
    //   0: iconst_0
    //   1: istore #4
    //   3: getstatic net/optifine/reflect/Reflector.MinecraftForgeClient_getRenderPass : Lnet/optifine/reflect/ReflectorMethod;
    //   6: invokevirtual exists : ()Z
    //   9: ifeq -> 24
    //   12: getstatic net/optifine/reflect/Reflector.MinecraftForgeClient_getRenderPass : Lnet/optifine/reflect/ReflectorMethod;
    //   15: iconst_0
    //   16: anewarray java/lang/Object
    //   19: invokestatic callInt : (Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)I
    //   22: istore #4
    //   24: aload_0
    //   25: getfield renderEntitiesStartupCounter : I
    //   28: ifle -> 50
    //   31: iload #4
    //   33: ifle -> 37
    //   36: return
    //   37: aload_0
    //   38: dup
    //   39: getfield renderEntitiesStartupCounter : I
    //   42: iconst_1
    //   43: isub
    //   44: putfield renderEntitiesStartupCounter : I
    //   47: goto -> 2037
    //   50: aload_1
    //   51: getfield prevPosX : D
    //   54: aload_1
    //   55: getfield posX : D
    //   58: aload_1
    //   59: getfield prevPosX : D
    //   62: dsub
    //   63: fload_3
    //   64: f2d
    //   65: dmul
    //   66: dadd
    //   67: dstore #5
    //   69: aload_1
    //   70: getfield prevPosY : D
    //   73: aload_1
    //   74: getfield posY : D
    //   77: aload_1
    //   78: getfield prevPosY : D
    //   81: dsub
    //   82: fload_3
    //   83: f2d
    //   84: dmul
    //   85: dadd
    //   86: dstore #7
    //   88: aload_1
    //   89: getfield prevPosZ : D
    //   92: aload_1
    //   93: getfield posZ : D
    //   96: aload_1
    //   97: getfield prevPosZ : D
    //   100: dsub
    //   101: fload_3
    //   102: f2d
    //   103: dmul
    //   104: dadd
    //   105: dstore #9
    //   107: aload_0
    //   108: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   111: getfield theProfiler : Lnet/minecraft/profiler/Profiler;
    //   114: ldc 'prepare'
    //   116: invokevirtual startSection : (Ljava/lang/String;)V
    //   119: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   122: aload_0
    //   123: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   126: aload_0
    //   127: getfield mc : Lnet/minecraft/client/Minecraft;
    //   130: invokevirtual getTextureManager : ()Lnet/minecraft/client/renderer/texture/TextureManager;
    //   133: aload_0
    //   134: getfield mc : Lnet/minecraft/client/Minecraft;
    //   137: getfield fontRendererObj : Lnet/minecraft/client/gui/FontRenderer;
    //   140: aload_0
    //   141: getfield mc : Lnet/minecraft/client/Minecraft;
    //   144: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   147: fload_3
    //   148: invokevirtual cacheActiveRenderInfo : (Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/entity/Entity;F)V
    //   151: aload_0
    //   152: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   155: aload_0
    //   156: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   159: aload_0
    //   160: getfield mc : Lnet/minecraft/client/Minecraft;
    //   163: getfield fontRendererObj : Lnet/minecraft/client/gui/FontRenderer;
    //   166: aload_0
    //   167: getfield mc : Lnet/minecraft/client/Minecraft;
    //   170: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   173: aload_0
    //   174: getfield mc : Lnet/minecraft/client/Minecraft;
    //   177: getfield pointedEntity : Lnet/minecraft/entity/Entity;
    //   180: aload_0
    //   181: getfield mc : Lnet/minecraft/client/Minecraft;
    //   184: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   187: fload_3
    //   188: invokevirtual cacheActiveRenderInfo : (Lnet/minecraft/world/World;Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/client/settings/GameSettings;F)V
    //   191: getstatic net/minecraft/client/renderer/RenderGlobal.renderEntitiesCounter : I
    //   194: iconst_1
    //   195: iadd
    //   196: putstatic net/minecraft/client/renderer/RenderGlobal.renderEntitiesCounter : I
    //   199: iload #4
    //   201: ifne -> 224
    //   204: aload_0
    //   205: iconst_0
    //   206: putfield countEntitiesTotal : I
    //   209: aload_0
    //   210: iconst_0
    //   211: putfield countEntitiesRendered : I
    //   214: aload_0
    //   215: iconst_0
    //   216: putfield countEntitiesHidden : I
    //   219: aload_0
    //   220: iconst_0
    //   221: putfield countTileEntitiesRendered : I
    //   224: aload_0
    //   225: getfield mc : Lnet/minecraft/client/Minecraft;
    //   228: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   231: astore #11
    //   233: aload #11
    //   235: getfield lastTickPosX : D
    //   238: aload #11
    //   240: getfield posX : D
    //   243: aload #11
    //   245: getfield lastTickPosX : D
    //   248: dsub
    //   249: fload_3
    //   250: f2d
    //   251: dmul
    //   252: dadd
    //   253: dstore #12
    //   255: aload #11
    //   257: getfield lastTickPosY : D
    //   260: aload #11
    //   262: getfield posY : D
    //   265: aload #11
    //   267: getfield lastTickPosY : D
    //   270: dsub
    //   271: fload_3
    //   272: f2d
    //   273: dmul
    //   274: dadd
    //   275: dstore #14
    //   277: aload #11
    //   279: getfield lastTickPosZ : D
    //   282: aload #11
    //   284: getfield posZ : D
    //   287: aload #11
    //   289: getfield lastTickPosZ : D
    //   292: dsub
    //   293: fload_3
    //   294: f2d
    //   295: dmul
    //   296: dadd
    //   297: dstore #16
    //   299: dload #12
    //   301: putstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.staticPlayerX : D
    //   304: dload #14
    //   306: putstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.staticPlayerY : D
    //   309: dload #16
    //   311: putstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.staticPlayerZ : D
    //   314: aload_0
    //   315: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   318: dload #12
    //   320: dload #14
    //   322: dload #16
    //   324: invokevirtual setRenderPosition : (DDD)V
    //   327: aload_0
    //   328: getfield mc : Lnet/minecraft/client/Minecraft;
    //   331: getfield entityRenderer : Lnet/minecraft/client/renderer/EntityRenderer;
    //   334: invokevirtual enableLightmap : ()V
    //   337: aload_0
    //   338: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   341: getfield theProfiler : Lnet/minecraft/profiler/Profiler;
    //   344: ldc_w 'global'
    //   347: invokevirtual endStartSection : (Ljava/lang/String;)V
    //   350: aload_0
    //   351: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   354: invokevirtual getLoadedEntityList : ()Ljava/util/List;
    //   357: astore #18
    //   359: iload #4
    //   361: ifne -> 375
    //   364: aload_0
    //   365: aload #18
    //   367: invokeinterface size : ()I
    //   372: putfield countEntitiesTotal : I
    //   375: invokestatic isFogOff : ()Z
    //   378: ifeq -> 397
    //   381: aload_0
    //   382: getfield mc : Lnet/minecraft/client/Minecraft;
    //   385: getfield entityRenderer : Lnet/minecraft/client/renderer/EntityRenderer;
    //   388: getfield fogStandard : Z
    //   391: ifeq -> 397
    //   394: invokestatic disableFog : ()V
    //   397: getstatic net/optifine/reflect/Reflector.ForgeEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   400: invokevirtual exists : ()Z
    //   403: istore #19
    //   405: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   408: invokevirtual exists : ()Z
    //   411: istore #20
    //   413: iconst_0
    //   414: istore #21
    //   416: iload #21
    //   418: aload_0
    //   419: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   422: getfield weatherEffects : Ljava/util/List;
    //   425: invokeinterface size : ()I
    //   430: if_icmpge -> 521
    //   433: aload_0
    //   434: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   437: getfield weatherEffects : Ljava/util/List;
    //   440: iload #21
    //   442: invokeinterface get : (I)Ljava/lang/Object;
    //   447: checkcast net/minecraft/entity/Entity
    //   450: astore #22
    //   452: iload #19
    //   454: ifeq -> 480
    //   457: aload #22
    //   459: getstatic net/optifine/reflect/Reflector.ForgeEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   462: iconst_1
    //   463: anewarray java/lang/Object
    //   466: dup
    //   467: iconst_0
    //   468: iload #4
    //   470: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   473: aastore
    //   474: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   477: ifeq -> 515
    //   480: aload_0
    //   481: dup
    //   482: getfield countEntitiesRendered : I
    //   485: iconst_1
    //   486: iadd
    //   487: putfield countEntitiesRendered : I
    //   490: aload #22
    //   492: dload #5
    //   494: dload #7
    //   496: dload #9
    //   498: invokevirtual isInRangeToRender3d : (DDD)Z
    //   501: ifeq -> 515
    //   504: aload_0
    //   505: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   508: aload #22
    //   510: fload_3
    //   511: invokevirtual renderEntitySimple : (Lnet/minecraft/entity/Entity;F)Z
    //   514: pop
    //   515: iinc #21, 1
    //   518: goto -> 416
    //   521: aload_0
    //   522: invokevirtual isRenderEntityOutlines : ()Z
    //   525: ifeq -> 821
    //   528: sipush #519
    //   531: invokestatic depthFunc : (I)V
    //   534: invokestatic disableFog : ()V
    //   537: aload_0
    //   538: getfield entityOutlineFramebuffer : Lnet/minecraft/client/shader/Framebuffer;
    //   541: invokevirtual framebufferClear : ()V
    //   544: aload_0
    //   545: getfield entityOutlineFramebuffer : Lnet/minecraft/client/shader/Framebuffer;
    //   548: iconst_0
    //   549: invokevirtual bindFramebuffer : (Z)V
    //   552: aload_0
    //   553: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   556: getfield theProfiler : Lnet/minecraft/profiler/Profiler;
    //   559: ldc_w 'entityOutlines'
    //   562: invokevirtual endStartSection : (Ljava/lang/String;)V
    //   565: invokestatic disableStandardItemLighting : ()V
    //   568: aload_0
    //   569: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   572: iconst_1
    //   573: invokevirtual setRenderOutlines : (Z)V
    //   576: iconst_0
    //   577: istore #21
    //   579: iload #21
    //   581: aload #18
    //   583: invokeinterface size : ()I
    //   588: if_icmpge -> 759
    //   591: aload #18
    //   593: iload #21
    //   595: invokeinterface get : (I)Ljava/lang/Object;
    //   600: checkcast net/minecraft/entity/Entity
    //   603: astore #22
    //   605: aload_0
    //   606: getfield mc : Lnet/minecraft/client/Minecraft;
    //   609: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   612: instanceof net/minecraft/entity/EntityLivingBase
    //   615: ifeq -> 638
    //   618: aload_0
    //   619: getfield mc : Lnet/minecraft/client/Minecraft;
    //   622: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   625: checkcast net/minecraft/entity/EntityLivingBase
    //   628: invokevirtual isPlayerSleeping : ()Z
    //   631: ifeq -> 638
    //   634: iconst_1
    //   635: goto -> 639
    //   638: iconst_0
    //   639: istore #23
    //   641: aload #22
    //   643: dload #5
    //   645: dload #7
    //   647: dload #9
    //   649: invokevirtual isInRangeToRender3d : (DDD)Z
    //   652: ifeq -> 704
    //   655: aload #22
    //   657: getfield ignoreFrustumCheck : Z
    //   660: ifne -> 692
    //   663: aload_2
    //   664: aload #22
    //   666: invokevirtual getEntityBoundingBox : ()Lnet/minecraft/util/AxisAlignedBB;
    //   669: invokeinterface isBoundingBoxInFrustum : (Lnet/minecraft/util/AxisAlignedBB;)Z
    //   674: ifne -> 692
    //   677: aload #22
    //   679: getfield riddenByEntity : Lnet/minecraft/entity/Entity;
    //   682: aload_0
    //   683: getfield mc : Lnet/minecraft/client/Minecraft;
    //   686: getfield thePlayer : Lnet/minecraft/client/entity/EntityPlayerSP;
    //   689: if_acmpne -> 704
    //   692: aload #22
    //   694: instanceof net/minecraft/entity/player/EntityPlayer
    //   697: ifeq -> 704
    //   700: iconst_1
    //   701: goto -> 705
    //   704: iconst_0
    //   705: istore #24
    //   707: aload #22
    //   709: aload_0
    //   710: getfield mc : Lnet/minecraft/client/Minecraft;
    //   713: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   716: if_acmpne -> 737
    //   719: aload_0
    //   720: getfield mc : Lnet/minecraft/client/Minecraft;
    //   723: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   726: getfield thirdPersonView : I
    //   729: ifne -> 737
    //   732: iload #23
    //   734: ifeq -> 753
    //   737: iload #24
    //   739: ifeq -> 753
    //   742: aload_0
    //   743: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   746: aload #22
    //   748: fload_3
    //   749: invokevirtual renderEntitySimple : (Lnet/minecraft/entity/Entity;F)Z
    //   752: pop
    //   753: iinc #21, 1
    //   756: goto -> 579
    //   759: aload_0
    //   760: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   763: iconst_0
    //   764: invokevirtual setRenderOutlines : (Z)V
    //   767: invokestatic enableStandardItemLighting : ()V
    //   770: iconst_0
    //   771: invokestatic depthMask : (Z)V
    //   774: aload_0
    //   775: getfield entityOutlineShader : Lnet/minecraft/client/shader/ShaderGroup;
    //   778: fload_3
    //   779: invokevirtual loadShaderGroup : (F)V
    //   782: invokestatic enableLighting : ()V
    //   785: iconst_1
    //   786: invokestatic depthMask : (Z)V
    //   789: aload_0
    //   790: getfield mc : Lnet/minecraft/client/Minecraft;
    //   793: invokevirtual getFramebuffer : ()Lnet/minecraft/client/shader/Framebuffer;
    //   796: iconst_0
    //   797: invokevirtual bindFramebuffer : (Z)V
    //   800: invokestatic enableFog : ()V
    //   803: invokestatic enableBlend : ()V
    //   806: invokestatic enableColorMaterial : ()V
    //   809: sipush #515
    //   812: invokestatic depthFunc : (I)V
    //   815: invokestatic enableDepth : ()V
    //   818: invokestatic enableAlpha : ()V
    //   821: aload_0
    //   822: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   825: getfield theProfiler : Lnet/minecraft/profiler/Profiler;
    //   828: ldc_w 'entities'
    //   831: invokevirtual endStartSection : (Ljava/lang/String;)V
    //   834: invokestatic isShaders : ()Z
    //   837: istore #21
    //   839: iload #21
    //   841: ifeq -> 847
    //   844: invokestatic beginEntities : ()V
    //   847: invokestatic updateItemRenderDistance : ()V
    //   850: aload_0
    //   851: getfield mc : Lnet/minecraft/client/Minecraft;
    //   854: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   857: getfield fancyGraphics : Z
    //   860: istore #22
    //   862: aload_0
    //   863: getfield mc : Lnet/minecraft/client/Minecraft;
    //   866: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   869: invokestatic isDroppedItemsFancy : ()Z
    //   872: putfield fancyGraphics : Z
    //   875: aload_0
    //   876: getfield renderInfosEntities : Ljava/util/List;
    //   879: invokeinterface iterator : ()Ljava/util/Iterator;
    //   884: astore #23
    //   886: aload #23
    //   888: invokeinterface hasNext : ()Z
    //   893: ifeq -> 1293
    //   896: aload #23
    //   898: invokeinterface next : ()Ljava/lang/Object;
    //   903: astore #24
    //   905: aload #24
    //   907: checkcast net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation
    //   910: astore #25
    //   912: aload #25
    //   914: getfield renderChunk : Lnet/minecraft/client/renderer/chunk/RenderChunk;
    //   917: invokevirtual getChunk : ()Lnet/minecraft/world/chunk/Chunk;
    //   920: astore #26
    //   922: aload #26
    //   924: invokevirtual getEntityLists : ()[Lnet/minecraft/util/ClassInheritanceMultiMap;
    //   927: aload #25
    //   929: getfield renderChunk : Lnet/minecraft/client/renderer/chunk/RenderChunk;
    //   932: invokevirtual getPosition : ()Lnet/minecraft/util/BlockPos;
    //   935: invokevirtual getY : ()I
    //   938: bipush #16
    //   940: idiv
    //   941: aaload
    //   942: astore #27
    //   944: aload #27
    //   946: invokevirtual isEmpty : ()Z
    //   949: ifne -> 1290
    //   952: aload #27
    //   954: invokevirtual iterator : ()Ljava/util/Iterator;
    //   957: astore #28
    //   959: aload #28
    //   961: invokeinterface hasNext : ()Z
    //   966: ifne -> 972
    //   969: goto -> 886
    //   972: aload #28
    //   974: invokeinterface next : ()Ljava/lang/Object;
    //   979: checkcast net/minecraft/entity/Entity
    //   982: astore #29
    //   984: iload #19
    //   986: ifeq -> 1012
    //   989: aload #29
    //   991: getstatic net/optifine/reflect/Reflector.ForgeEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   994: iconst_1
    //   995: anewarray java/lang/Object
    //   998: dup
    //   999: iconst_0
    //   1000: iload #4
    //   1002: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   1005: aastore
    //   1006: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1009: ifeq -> 959
    //   1012: aload_0
    //   1013: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   1016: aload #29
    //   1018: aload_2
    //   1019: dload #5
    //   1021: dload #7
    //   1023: dload #9
    //   1025: invokevirtual shouldRender : (Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z
    //   1028: ifne -> 1046
    //   1031: aload #29
    //   1033: getfield riddenByEntity : Lnet/minecraft/entity/Entity;
    //   1036: aload_0
    //   1037: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1040: getfield thePlayer : Lnet/minecraft/client/entity/EntityPlayerSP;
    //   1043: if_acmpne -> 1050
    //   1046: iconst_1
    //   1047: goto -> 1051
    //   1050: iconst_0
    //   1051: istore #30
    //   1053: iload #30
    //   1055: ifne -> 1061
    //   1058: goto -> 1212
    //   1061: aload_0
    //   1062: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1065: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   1068: instanceof net/minecraft/entity/EntityLivingBase
    //   1071: ifeq -> 1090
    //   1074: aload_0
    //   1075: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1078: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   1081: checkcast net/minecraft/entity/EntityLivingBase
    //   1084: invokevirtual isPlayerSleeping : ()Z
    //   1087: goto -> 1091
    //   1090: iconst_0
    //   1091: istore #31
    //   1093: aload #29
    //   1095: aload_0
    //   1096: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1099: invokevirtual getRenderViewEntity : ()Lnet/minecraft/entity/Entity;
    //   1102: if_acmpne -> 1123
    //   1105: aload_0
    //   1106: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1109: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   1112: getfield thirdPersonView : I
    //   1115: ifne -> 1123
    //   1118: iload #31
    //   1120: ifeq -> 1209
    //   1123: aload #29
    //   1125: getfield posY : D
    //   1128: dconst_0
    //   1129: dcmpg
    //   1130: iflt -> 1164
    //   1133: aload #29
    //   1135: getfield posY : D
    //   1138: ldc2_w 256.0
    //   1141: dcmpl
    //   1142: ifge -> 1164
    //   1145: aload_0
    //   1146: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1149: new net/minecraft/util/BlockPos
    //   1152: dup
    //   1153: aload #29
    //   1155: invokespecial <init> : (Lnet/minecraft/entity/Entity;)V
    //   1158: invokevirtual isBlockLoaded : (Lnet/minecraft/util/BlockPos;)Z
    //   1161: ifeq -> 1209
    //   1164: aload_0
    //   1165: dup
    //   1166: getfield countEntitiesRendered : I
    //   1169: iconst_1
    //   1170: iadd
    //   1171: putfield countEntitiesRendered : I
    //   1174: aload_0
    //   1175: aload #29
    //   1177: putfield renderedEntity : Lnet/minecraft/entity/Entity;
    //   1180: iload #21
    //   1182: ifeq -> 1190
    //   1185: aload #29
    //   1187: invokestatic nextEntity : (Lnet/minecraft/entity/Entity;)V
    //   1190: aload_0
    //   1191: getfield renderManager : Lnet/minecraft/client/renderer/entity/RenderManager;
    //   1194: aload #29
    //   1196: fload_3
    //   1197: invokevirtual renderEntitySimple : (Lnet/minecraft/entity/Entity;F)Z
    //   1200: pop
    //   1201: aload_0
    //   1202: aconst_null
    //   1203: putfield renderedEntity : Lnet/minecraft/entity/Entity;
    //   1206: goto -> 1212
    //   1209: goto -> 959
    //   1212: iload #30
    //   1214: ifne -> 1287
    //   1217: aload #29
    //   1219: instanceof net/minecraft/entity/projectile/EntityWitherSkull
    //   1222: ifeq -> 1287
    //   1225: iload #19
    //   1227: ifeq -> 1253
    //   1230: aload #29
    //   1232: getstatic net/optifine/reflect/Reflector.ForgeEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   1235: iconst_1
    //   1236: anewarray java/lang/Object
    //   1239: dup
    //   1240: iconst_0
    //   1241: iload #4
    //   1243: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   1246: aastore
    //   1247: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1250: ifeq -> 1287
    //   1253: aload_0
    //   1254: aload #29
    //   1256: putfield renderedEntity : Lnet/minecraft/entity/Entity;
    //   1259: iload #21
    //   1261: ifeq -> 1269
    //   1264: aload #29
    //   1266: invokestatic nextEntity : (Lnet/minecraft/entity/Entity;)V
    //   1269: aload_0
    //   1270: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1273: invokevirtual getRenderManager : ()Lnet/minecraft/client/renderer/entity/RenderManager;
    //   1276: aload #29
    //   1278: fload_3
    //   1279: invokevirtual renderWitherSkull : (Lnet/minecraft/entity/Entity;F)V
    //   1282: aload_0
    //   1283: aconst_null
    //   1284: putfield renderedEntity : Lnet/minecraft/entity/Entity;
    //   1287: goto -> 959
    //   1290: goto -> 886
    //   1293: aload_0
    //   1294: getfield mc : Lnet/minecraft/client/Minecraft;
    //   1297: getfield gameSettings : Lnet/minecraft/client/settings/GameSettings;
    //   1300: iload #22
    //   1302: putfield fancyGraphics : Z
    //   1305: iload #21
    //   1307: ifeq -> 1316
    //   1310: invokestatic endEntities : ()V
    //   1313: invokestatic beginBlockEntities : ()V
    //   1316: aload_0
    //   1317: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1320: getfield theProfiler : Lnet/minecraft/profiler/Profiler;
    //   1323: ldc_w 'blockentities'
    //   1326: invokevirtual endStartSection : (Ljava/lang/String;)V
    //   1329: invokestatic enableStandardItemLighting : ()V
    //   1332: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_hasFastRenderer : Lnet/optifine/reflect/ReflectorMethod;
    //   1335: invokevirtual exists : ()Z
    //   1338: ifeq -> 1347
    //   1341: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   1344: invokevirtual preDrawBatch : ()V
    //   1347: invokestatic updateTextRenderDistance : ()V
    //   1350: aload_0
    //   1351: getfield renderInfosTileEntities : Ljava/util/List;
    //   1354: invokeinterface iterator : ()Ljava/util/Iterator;
    //   1359: astore #23
    //   1361: aload #23
    //   1363: invokeinterface hasNext : ()Z
    //   1368: ifeq -> 1550
    //   1371: aload #23
    //   1373: invokeinterface next : ()Ljava/lang/Object;
    //   1378: astore #24
    //   1380: aload #24
    //   1382: checkcast net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation
    //   1385: astore #25
    //   1387: aload #25
    //   1389: getfield renderChunk : Lnet/minecraft/client/renderer/chunk/RenderChunk;
    //   1392: invokevirtual getCompiledChunk : ()Lnet/minecraft/client/renderer/chunk/CompiledChunk;
    //   1395: invokevirtual getTileEntities : ()Ljava/util/List;
    //   1398: astore #26
    //   1400: aload #26
    //   1402: invokeinterface isEmpty : ()Z
    //   1407: ifne -> 1547
    //   1410: aload #26
    //   1412: invokeinterface iterator : ()Ljava/util/Iterator;
    //   1417: astore #27
    //   1419: aload #27
    //   1421: invokeinterface hasNext : ()Z
    //   1426: ifne -> 1432
    //   1429: goto -> 1361
    //   1432: aload #27
    //   1434: invokeinterface next : ()Ljava/lang/Object;
    //   1439: checkcast net/minecraft/tileentity/TileEntity
    //   1442: astore #28
    //   1444: iload #20
    //   1446: ifne -> 1452
    //   1449: goto -> 1514
    //   1452: aload #28
    //   1454: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   1457: iconst_1
    //   1458: anewarray java/lang/Object
    //   1461: dup
    //   1462: iconst_0
    //   1463: iload #4
    //   1465: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   1468: aastore
    //   1469: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1472: ifeq -> 1419
    //   1475: aload #28
    //   1477: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_getRenderBoundingBox : Lnet/optifine/reflect/ReflectorMethod;
    //   1480: iconst_0
    //   1481: anewarray java/lang/Object
    //   1484: invokestatic call : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;
    //   1487: checkcast net/minecraft/util/AxisAlignedBB
    //   1490: astore #29
    //   1492: aload #29
    //   1494: ifnull -> 1514
    //   1497: aload_2
    //   1498: aload #29
    //   1500: invokeinterface isBoundingBoxInFrustum : (Lnet/minecraft/util/AxisAlignedBB;)Z
    //   1505: ifeq -> 1511
    //   1508: goto -> 1514
    //   1511: goto -> 1419
    //   1514: iload #21
    //   1516: ifeq -> 1524
    //   1519: aload #28
    //   1521: invokestatic nextBlockEntity : (Lnet/minecraft/tileentity/TileEntity;)V
    //   1524: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   1527: aload #28
    //   1529: fload_3
    //   1530: iconst_m1
    //   1531: invokevirtual renderTileEntity : (Lnet/minecraft/tileentity/TileEntity;FI)V
    //   1534: aload_0
    //   1535: dup
    //   1536: getfield countTileEntitiesRendered : I
    //   1539: iconst_1
    //   1540: iadd
    //   1541: putfield countTileEntitiesRendered : I
    //   1544: goto -> 1419
    //   1547: goto -> 1361
    //   1550: aload_0
    //   1551: getfield setTileEntities : Ljava/util/Set;
    //   1554: dup
    //   1555: astore #23
    //   1557: monitorenter
    //   1558: aload_0
    //   1559: getfield setTileEntities : Ljava/util/Set;
    //   1562: invokeinterface iterator : ()Ljava/util/Iterator;
    //   1567: astore #24
    //   1569: aload #24
    //   1571: invokeinterface hasNext : ()Z
    //   1576: ifeq -> 1642
    //   1579: aload #24
    //   1581: invokeinterface next : ()Ljava/lang/Object;
    //   1586: checkcast net/minecraft/tileentity/TileEntity
    //   1589: astore #25
    //   1591: iload #20
    //   1593: ifeq -> 1619
    //   1596: aload #25
    //   1598: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   1601: iconst_1
    //   1602: anewarray java/lang/Object
    //   1605: dup
    //   1606: iconst_0
    //   1607: iload #4
    //   1609: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   1612: aastore
    //   1613: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1616: ifeq -> 1639
    //   1619: iload #21
    //   1621: ifeq -> 1629
    //   1624: aload #25
    //   1626: invokestatic nextBlockEntity : (Lnet/minecraft/tileentity/TileEntity;)V
    //   1629: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   1632: aload #25
    //   1634: fload_3
    //   1635: iconst_m1
    //   1636: invokevirtual renderTileEntity : (Lnet/minecraft/tileentity/TileEntity;FI)V
    //   1639: goto -> 1569
    //   1642: aload #23
    //   1644: monitorexit
    //   1645: goto -> 1656
    //   1648: astore #32
    //   1650: aload #23
    //   1652: monitorexit
    //   1653: aload #32
    //   1655: athrow
    //   1656: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_hasFastRenderer : Lnet/optifine/reflect/ReflectorMethod;
    //   1659: invokevirtual exists : ()Z
    //   1662: ifeq -> 1673
    //   1665: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   1668: iload #4
    //   1670: invokevirtual drawBatch : (I)V
    //   1673: aload_0
    //   1674: iconst_1
    //   1675: putfield renderOverlayDamaged : Z
    //   1678: aload_0
    //   1679: invokespecial preRenderDamagedBlocks : ()V
    //   1682: aload_0
    //   1683: getfield damagedBlocks : Ljava/util/Map;
    //   1686: invokeinterface values : ()Ljava/util/Collection;
    //   1691: invokeinterface iterator : ()Ljava/util/Iterator;
    //   1696: astore #23
    //   1698: aload #23
    //   1700: invokeinterface hasNext : ()Z
    //   1705: ifeq -> 1992
    //   1708: aload #23
    //   1710: invokeinterface next : ()Ljava/lang/Object;
    //   1715: checkcast net/minecraft/client/renderer/DestroyBlockProgress
    //   1718: astore #24
    //   1720: aload #24
    //   1722: invokevirtual getPosition : ()Lnet/minecraft/util/BlockPos;
    //   1725: astore #25
    //   1727: aload_0
    //   1728: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1731: aload #25
    //   1733: invokevirtual getTileEntity : (Lnet/minecraft/util/BlockPos;)Lnet/minecraft/tileentity/TileEntity;
    //   1736: astore #26
    //   1738: aload #26
    //   1740: instanceof net/minecraft/tileentity/TileEntityChest
    //   1743: ifeq -> 1814
    //   1746: aload #26
    //   1748: checkcast net/minecraft/tileentity/TileEntityChest
    //   1751: astore #27
    //   1753: aload #27
    //   1755: getfield adjacentChestXNeg : Lnet/minecraft/tileentity/TileEntityChest;
    //   1758: ifnull -> 1785
    //   1761: aload #25
    //   1763: getstatic net/minecraft/util/EnumFacing.WEST : Lnet/minecraft/util/EnumFacing;
    //   1766: invokevirtual offset : (Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/BlockPos;
    //   1769: astore #25
    //   1771: aload_0
    //   1772: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1775: aload #25
    //   1777: invokevirtual getTileEntity : (Lnet/minecraft/util/BlockPos;)Lnet/minecraft/tileentity/TileEntity;
    //   1780: astore #26
    //   1782: goto -> 1814
    //   1785: aload #27
    //   1787: getfield adjacentChestZNeg : Lnet/minecraft/tileentity/TileEntityChest;
    //   1790: ifnull -> 1814
    //   1793: aload #25
    //   1795: getstatic net/minecraft/util/EnumFacing.NORTH : Lnet/minecraft/util/EnumFacing;
    //   1798: invokevirtual offset : (Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/BlockPos;
    //   1801: astore #25
    //   1803: aload_0
    //   1804: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1807: aload #25
    //   1809: invokevirtual getTileEntity : (Lnet/minecraft/util/BlockPos;)Lnet/minecraft/tileentity/TileEntity;
    //   1812: astore #26
    //   1814: aload_0
    //   1815: getfield theWorld : Lnet/minecraft/client/multiplayer/WorldClient;
    //   1818: aload #25
    //   1820: invokevirtual getBlockState : (Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;
    //   1823: invokeinterface getBlock : ()Lnet/minecraft/block/Block;
    //   1828: astore #27
    //   1830: iload #20
    //   1832: ifeq -> 1916
    //   1835: iconst_0
    //   1836: istore #28
    //   1838: aload #26
    //   1840: ifnull -> 1960
    //   1843: aload #26
    //   1845: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_shouldRenderInPass : Lnet/optifine/reflect/ReflectorMethod;
    //   1848: iconst_1
    //   1849: anewarray java/lang/Object
    //   1852: dup
    //   1853: iconst_0
    //   1854: iload #4
    //   1856: invokestatic valueOf : (I)Ljava/lang/Integer;
    //   1859: aastore
    //   1860: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1863: ifeq -> 1960
    //   1866: aload #26
    //   1868: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_canRenderBreaking : Lnet/optifine/reflect/ReflectorMethod;
    //   1871: iconst_0
    //   1872: anewarray java/lang/Object
    //   1875: invokestatic callBoolean : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z
    //   1878: ifeq -> 1960
    //   1881: aload #26
    //   1883: getstatic net/optifine/reflect/Reflector.ForgeTileEntity_getRenderBoundingBox : Lnet/optifine/reflect/ReflectorMethod;
    //   1886: iconst_0
    //   1887: anewarray java/lang/Object
    //   1890: invokestatic call : (Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;
    //   1893: checkcast net/minecraft/util/AxisAlignedBB
    //   1896: astore #29
    //   1898: aload #29
    //   1900: ifnull -> 1913
    //   1903: aload_2
    //   1904: aload #29
    //   1906: invokeinterface isBoundingBoxInFrustum : (Lnet/minecraft/util/AxisAlignedBB;)Z
    //   1911: istore #28
    //   1913: goto -> 1960
    //   1916: aload #26
    //   1918: ifnull -> 1957
    //   1921: aload #27
    //   1923: instanceof net/minecraft/block/BlockChest
    //   1926: ifne -> 1953
    //   1929: aload #27
    //   1931: instanceof net/minecraft/block/BlockEnderChest
    //   1934: ifne -> 1953
    //   1937: aload #27
    //   1939: instanceof net/minecraft/block/BlockSign
    //   1942: ifne -> 1953
    //   1945: aload #27
    //   1947: instanceof net/minecraft/block/BlockSkull
    //   1950: ifeq -> 1957
    //   1953: iconst_1
    //   1954: goto -> 1958
    //   1957: iconst_0
    //   1958: istore #28
    //   1960: iload #28
    //   1962: ifeq -> 1989
    //   1965: iload #21
    //   1967: ifeq -> 1975
    //   1970: aload #26
    //   1972: invokestatic nextBlockEntity : (Lnet/minecraft/tileentity/TileEntity;)V
    //   1975: getstatic net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher.instance : Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;
    //   1978: aload #26
    //   1980: fload_3
    //   1981: aload #24
    //   1983: invokevirtual getPartialBlockDamage : ()I
    //   1986: invokevirtual renderTileEntity : (Lnet/minecraft/tileentity/TileEntity;FI)V
    //   1989: goto -> 1698
    //   1992: aload_0
    //   1993: invokespecial postRenderDamagedBlocks : ()V
    //   1996: aload_0
    //   1997: iconst_0
    //   1998: putfield renderOverlayDamaged : Z
    //   2001: iload #21
    //   2003: ifeq -> 2009
    //   2006: invokestatic endBlockEntities : ()V
    //   2009: getstatic net/minecraft/client/renderer/RenderGlobal.renderEntitiesCounter : I
    //   2012: iconst_1
    //   2013: isub
    //   2014: putstatic net/minecraft/client/renderer/RenderGlobal.renderEntitiesCounter : I
    //   2017: aload_0
    //   2018: getfield mc : Lnet/minecraft/client/Minecraft;
    //   2021: getfield entityRenderer : Lnet/minecraft/client/renderer/EntityRenderer;
    //   2024: invokevirtual disableLightmap : ()V
    //   2027: aload_0
    //   2028: getfield mc : Lnet/minecraft/client/Minecraft;
    //   2031: getfield mcProfiler : Lnet/minecraft/profiler/Profiler;
    //   2034: invokevirtual endSection : ()V
    //   2037: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #650	-> 0
    //   #652	-> 3
    //   #654	-> 12
    //   #657	-> 24
    //   #659	-> 31
    //   #661	-> 36
    //   #664	-> 37
    //   #668	-> 50
    //   #669	-> 69
    //   #670	-> 88
    //   #671	-> 107
    //   #672	-> 119
    //   #673	-> 151
    //   #674	-> 191
    //   #676	-> 199
    //   #678	-> 204
    //   #679	-> 209
    //   #680	-> 214
    //   #681	-> 219
    //   #684	-> 224
    //   #685	-> 233
    //   #686	-> 255
    //   #687	-> 277
    //   #688	-> 299
    //   #689	-> 304
    //   #690	-> 309
    //   #691	-> 314
    //   #692	-> 327
    //   #693	-> 337
    //   #694	-> 350
    //   #696	-> 359
    //   #698	-> 364
    //   #701	-> 375
    //   #703	-> 394
    //   #706	-> 397
    //   #707	-> 405
    //   #709	-> 413
    //   #711	-> 433
    //   #713	-> 452
    //   #715	-> 480
    //   #717	-> 490
    //   #719	-> 504
    //   #709	-> 515
    //   #724	-> 521
    //   #726	-> 528
    //   #727	-> 534
    //   #728	-> 537
    //   #729	-> 544
    //   #730	-> 552
    //   #731	-> 565
    //   #732	-> 568
    //   #734	-> 576
    //   #736	-> 591
    //   #737	-> 605
    //   #738	-> 641
    //   #740	-> 707
    //   #742	-> 742
    //   #734	-> 753
    //   #746	-> 759
    //   #747	-> 767
    //   #748	-> 770
    //   #749	-> 774
    //   #750	-> 782
    //   #751	-> 785
    //   #752	-> 789
    //   #753	-> 800
    //   #754	-> 803
    //   #755	-> 806
    //   #756	-> 809
    //   #757	-> 815
    //   #758	-> 818
    //   #761	-> 821
    //   #762	-> 834
    //   #764	-> 839
    //   #766	-> 844
    //   #769	-> 847
    //   #770	-> 850
    //   #771	-> 862
    //   #774	-> 875
    //   #776	-> 905
    //   #777	-> 912
    //   #778	-> 922
    //   #780	-> 944
    //   #782	-> 952
    //   #791	-> 959
    //   #793	-> 969
    //   #796	-> 972
    //   #798	-> 984
    //   #800	-> 1012
    //   #802	-> 1053
    //   #804	-> 1058
    //   #807	-> 1061
    //   #809	-> 1093
    //   #811	-> 1164
    //   #812	-> 1174
    //   #814	-> 1180
    //   #816	-> 1185
    //   #819	-> 1190
    //   #820	-> 1201
    //   #821	-> 1206
    //   #823	-> 1209
    //   #826	-> 1212
    //   #828	-> 1253
    //   #830	-> 1259
    //   #832	-> 1264
    //   #835	-> 1269
    //   #836	-> 1282
    //   #838	-> 1287
    //   #840	-> 1290
    //   #842	-> 1293
    //   #844	-> 1305
    //   #846	-> 1310
    //   #847	-> 1313
    //   #850	-> 1316
    //   #851	-> 1329
    //   #853	-> 1332
    //   #855	-> 1341
    //   #858	-> 1347
    //   #861	-> 1350
    //   #863	-> 1380
    //   #864	-> 1387
    //   #866	-> 1400
    //   #868	-> 1410
    //   #876	-> 1419
    //   #878	-> 1429
    //   #881	-> 1432
    //   #883	-> 1444
    //   #885	-> 1449
    //   #888	-> 1452
    //   #890	-> 1475
    //   #892	-> 1492
    //   #894	-> 1508
    //   #896	-> 1511
    //   #899	-> 1514
    //   #901	-> 1519
    //   #904	-> 1524
    //   #905	-> 1534
    //   #906	-> 1544
    //   #908	-> 1547
    //   #910	-> 1550
    //   #912	-> 1558
    //   #914	-> 1591
    //   #916	-> 1619
    //   #918	-> 1624
    //   #921	-> 1629
    //   #923	-> 1639
    //   #924	-> 1642
    //   #926	-> 1656
    //   #928	-> 1665
    //   #931	-> 1673
    //   #932	-> 1678
    //   #934	-> 1682
    //   #936	-> 1720
    //   #937	-> 1727
    //   #939	-> 1738
    //   #941	-> 1746
    //   #943	-> 1753
    //   #945	-> 1761
    //   #946	-> 1771
    //   #948	-> 1785
    //   #950	-> 1793
    //   #951	-> 1803
    //   #955	-> 1814
    //   #958	-> 1830
    //   #960	-> 1835
    //   #962	-> 1838
    //   #964	-> 1881
    //   #966	-> 1898
    //   #968	-> 1903
    //   #970	-> 1913
    //   #974	-> 1916
    //   #977	-> 1960
    //   #979	-> 1965
    //   #981	-> 1970
    //   #984	-> 1975
    //   #986	-> 1989
    //   #988	-> 1992
    //   #989	-> 1996
    //   #991	-> 2001
    //   #993	-> 2006
    //   #996	-> 2009
    //   #997	-> 2017
    //   #998	-> 2027
    //   #1000	-> 2037
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   452	63	22	entity1	Lnet/minecraft/entity/Entity;
    //   416	105	21	j	I
    //   605	148	22	entity3	Lnet/minecraft/entity/Entity;
    //   641	112	23	flag2	Z
    //   707	46	24	flag3	Z
    //   579	180	21	k	I
    //   1093	116	31	flag5	Z
    //   984	303	29	entity2	Lnet/minecraft/entity/Entity;
    //   1053	234	30	flag4	Z
    //   959	331	28	iterator	Ljava/util/Iterator;
    //   912	378	25	renderglobal$containerlocalrenderinformation	Lnet/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation;
    //   922	368	26	chunk	Lnet/minecraft/world/chunk/Chunk;
    //   944	346	27	classinheritancemultimap	Lnet/minecraft/util/ClassInheritanceMultiMap;
    //   905	385	24	e	Ljava/lang/Object;
    //   1492	19	29	axisalignedbb1	Lnet/minecraft/util/AxisAlignedBB;
    //   1444	100	28	tileentity1	Lnet/minecraft/tileentity/TileEntity;
    //   1419	128	27	iterator1	Ljava/util/Iterator;
    //   1387	160	25	renderglobal$containerlocalrenderinformation1	Lnet/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation;
    //   1400	147	26	list1	Ljava/util/List;
    //   1380	167	24	e	Ljava/lang/Object;
    //   1591	48	25	tileentity	Lnet/minecraft/tileentity/TileEntity;
    //   1753	61	27	tileentitychest	Lnet/minecraft/tileentity/TileEntityChest;
    //   1898	15	29	axisalignedbb	Lnet/minecraft/util/AxisAlignedBB;
    //   1838	78	28	flag8	Z
    //   1727	262	25	blockpos	Lnet/minecraft/util/BlockPos;
    //   1738	251	26	tileentity2	Lnet/minecraft/tileentity/TileEntity;
    //   1830	159	27	block	Lnet/minecraft/block/Block;
    //   1960	29	28	flag8	Z
    //   1720	269	24	destroyblockprogress	Lnet/minecraft/client/renderer/DestroyBlockProgress;
    //   69	1968	5	d0	D
    //   88	1949	7	d1	D
    //   107	1930	9	d2	D
    //   233	1804	11	entity	Lnet/minecraft/entity/Entity;
    //   255	1782	12	d3	D
    //   277	1760	14	d4	D
    //   299	1738	16	d5	D
    //   359	1678	18	list	Ljava/util/List;
    //   405	1632	19	flag	Z
    //   413	1624	20	flag1	Z
    //   839	1198	21	flag6	Z
    //   862	1175	22	flag7	Z
    //   0	2038	0	this	Lnet/minecraft/client/renderer/RenderGlobal;
    //   0	2038	1	renderViewEntity	Lnet/minecraft/entity/Entity;
    //   0	2038	2	camera	Lnet/minecraft/client/renderer/culling/ICamera;
    //   0	2038	3	partialTicks	F
    //   3	2035	4	i	I
    // Local variable type table:
    //   start	length	slot	name	signature
    //   944	346	27	classinheritancemultimap	Lnet/minecraft/util/ClassInheritanceMultiMap<Lnet/minecraft/entity/Entity;>;
    //   1400	147	26	list1	Ljava/util/List<Lnet/minecraft/tileentity/TileEntity;>;
    //   359	1678	18	list	Ljava/util/List<Lnet/minecraft/entity/Entity;>;
    // Exception table:
    //   from	to	target	type
    //   1558	1645	1648	finally
    //   1648	1653	1648	finally
  }
  
  public String getDebugInfoRenders() {
    int i = this.viewFrustum.renderChunks.length;
    int j = 0;
    for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.renderInfos) {
      CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;
      if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty())
        j++; 
    } 
    return String.format("C: %d/%d %sD: %d, %s", new Object[] { Integer.valueOf(j), Integer.valueOf(i), this.mc.renderChunksMany ? "(s) " : "", Integer.valueOf(this.renderDistanceChunks), this.renderDispatcher.getDebugInfo() });
  }
  
  public String getDebugInfoEntities() {
    return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered) + ", " + Config.getVersionDebug();
  }
  
  public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
    Frustum frustum;
    if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks)
      loadRenderers(); 
    this.theWorld.theProfiler.startSection("camera");
    double d0 = viewEntity.posX - this.frustumUpdatePosX;
    double d1 = viewEntity.posY - this.frustumUpdatePosY;
    double d2 = viewEntity.posZ - this.frustumUpdatePosZ;
    if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
      this.frustumUpdatePosX = viewEntity.posX;
      this.frustumUpdatePosY = viewEntity.posY;
      this.frustumUpdatePosZ = viewEntity.posZ;
      this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
      this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
      this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
      this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
    } 
    if (Config.isDynamicLights())
      DynamicLights.update(this); 
    this.theWorld.theProfiler.endStartSection("renderlistcamera");
    double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
    double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
    double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
    this.renderContainer.initialize(d3, d4, d5);
    this.theWorld.theProfiler.endStartSection("cull");
    if (this.debugFixedClippingHelper != null) {
      Frustum frustum1 = new Frustum(this.debugFixedClippingHelper);
      frustum1.setPosition(this.debugTerrainFrustumPosition.x, this.debugTerrainFrustumPosition.y, this.debugTerrainFrustumPosition.z);
      frustum = frustum1;
    } 
    this.mc.mcProfiler.endStartSection("culling");
    BlockPos blockpos = new BlockPos(d3, d4 + viewEntity.getEyeHeight(), d5);
    RenderChunk renderchunk = this.viewFrustum.getRenderChunk(blockpos);
    new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);
    this.displayListEntitiesDirty = (this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || viewEntity.posX != this.lastViewEntityX || viewEntity.posY != this.lastViewEntityY || viewEntity.posZ != this.lastViewEntityZ || viewEntity.rotationPitch != this.lastViewEntityPitch || viewEntity.rotationYaw != this.lastViewEntityYaw);
    this.lastViewEntityX = viewEntity.posX;
    this.lastViewEntityY = viewEntity.posY;
    this.lastViewEntityZ = viewEntity.posZ;
    this.lastViewEntityPitch = viewEntity.rotationPitch;
    this.lastViewEntityYaw = viewEntity.rotationYaw;
    boolean flag = (this.debugFixedClippingHelper != null);
    this.mc.mcProfiler.endStartSection("update");
    Lagometer.timerVisibility.start();
    int i = getCountLoadedChunks();
    if (i != this.countLoadedChunksPrev) {
      this.countLoadedChunksPrev = i;
      this.displayListEntitiesDirty = true;
    } 
    int j = 256;
    if (!ChunkVisibility.isFinished())
      this.displayListEntitiesDirty = true; 
    if (!flag && this.displayListEntitiesDirty && Config.isIntegratedServerRunning())
      j = ChunkVisibility.getMaxChunkY((World)this.theWorld, viewEntity, this.renderDistanceChunks); 
    RenderChunk renderchunk1 = this.viewFrustum.getRenderChunk(new BlockPos(viewEntity.posX, viewEntity.posY, viewEntity.posZ));
    if (Shaders.isShadowPass) {
      this.renderInfos = this.renderInfosShadow;
      this.renderInfosEntities = this.renderInfosEntitiesShadow;
      this.renderInfosTileEntities = this.renderInfosTileEntitiesShadow;
      if (!flag && this.displayListEntitiesDirty) {
        clearRenderInfos();
        if (renderchunk1 != null && renderchunk1.getPosition().getY() > j)
          this.renderInfosEntities.add(renderchunk1.getRenderInfo()); 
        Iterator<RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(this.theWorld, partialTicks, viewEntity, this.renderDistanceChunks, this.viewFrustum);
        while (iterator.hasNext()) {
          RenderChunk renderchunk2 = iterator.next();
          if (renderchunk2 != null && renderchunk2.getPosition().getY() <= j) {
            ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = renderchunk2.getRenderInfo();
            if (!renderchunk2.compiledChunk.isEmpty() || renderchunk2.isNeedsUpdate())
              this.renderInfos.add(renderglobal$containerlocalrenderinformation); 
            if (ChunkUtils.hasEntities(renderchunk2.getChunk()))
              this.renderInfosEntities.add(renderglobal$containerlocalrenderinformation); 
            if (renderchunk2.getCompiledChunk().getTileEntities().size() > 0)
              this.renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation); 
          } 
        } 
      } 
    } else {
      this.renderInfos = this.renderInfosNormal;
      this.renderInfosEntities = this.renderInfosEntitiesNormal;
      this.renderInfosTileEntities = this.renderInfosTileEntitiesNormal;
    } 
    if (!flag && this.displayListEntitiesDirty && !Shaders.isShadowPass) {
      this.displayListEntitiesDirty = false;
      clearRenderInfos();
      this.visibilityDeque.clear();
      Deque<ContainerLocalRenderInformation> deque = this.visibilityDeque;
      boolean flag1 = this.mc.renderChunksMany;
      if (renderchunk != null && renderchunk.getPosition().getY() <= j) {
        boolean flag2 = false;
        ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation4 = new ContainerLocalRenderInformation(renderchunk, (EnumFacing)null, 0);
        Set set1 = SET_ALL_FACINGS;
        if (set1.size() == 1) {
          Vector3f vector3f = getViewVector(viewEntity, partialTicks);
          EnumFacing enumfacing2 = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
          set1.remove(enumfacing2);
        } 
        if (set1.isEmpty())
          flag2 = true; 
        if (flag2 && !playerSpectator) {
          this.renderInfos.add(renderglobal$containerlocalrenderinformation4);
        } else {
          if (playerSpectator && this.theWorld.getBlockState(blockpos).getBlock().isOpaqueCube())
            flag1 = false; 
          renderchunk.setFrameIndex(frameCount);
          deque.add(renderglobal$containerlocalrenderinformation4);
        } 
      } else {
        int j1 = (blockpos.getY() > 0) ? Math.min(j, 248) : 8;
        if (renderchunk1 != null)
          this.renderInfosEntities.add(renderchunk1.getRenderInfo()); 
        for (int k = -this.renderDistanceChunks; k <= this.renderDistanceChunks; k++) {
          for (int l = -this.renderDistanceChunks; l <= this.renderDistanceChunks; l++) {
            RenderChunk renderchunk3 = this.viewFrustum.getRenderChunk(new BlockPos((k << 4) + 8, j1, (l << 4) + 8));
            if (renderchunk3 != null && renderchunk3.isBoundingBoxInFrustum((ICamera)frustum, frameCount)) {
              renderchunk3.setFrameIndex(frameCount);
              ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = renderchunk3.getRenderInfo();
              renderglobal$containerlocalrenderinformation1.initialize((EnumFacing)null, 0);
              deque.add(renderglobal$containerlocalrenderinformation1);
            } 
          } 
        } 
      } 
      this.mc.mcProfiler.startSection("iteration");
      boolean flag3 = Config.isFogOn();
      while (!deque.isEmpty()) {
        ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation5 = deque.poll();
        RenderChunk renderchunk6 = renderglobal$containerlocalrenderinformation5.renderChunk;
        EnumFacing enumfacing1 = renderglobal$containerlocalrenderinformation5.facing;
        CompiledChunk compiledchunk = renderchunk6.compiledChunk;
        if (!compiledchunk.isEmpty() || renderchunk6.isNeedsUpdate())
          this.renderInfos.add(renderglobal$containerlocalrenderinformation5); 
        if (ChunkUtils.hasEntities(renderchunk6.getChunk()))
          this.renderInfosEntities.add(renderglobal$containerlocalrenderinformation5); 
        if (compiledchunk.getTileEntities().size() > 0)
          this.renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation5); 
        for (EnumFacing enumfacing : flag1 ? ChunkVisibility.getFacingsNotOpposite(renderglobal$containerlocalrenderinformation5.setFacing) : EnumFacing.VALUES) {
          if (!flag1 || enumfacing1 == null || compiledchunk.isVisible(enumfacing1.getOpposite(), enumfacing)) {
            RenderChunk renderchunk4 = getRenderChunkOffset(blockpos, renderchunk6, enumfacing, flag3, j);
            if (renderchunk4 != null && renderchunk4.setFrameIndex(frameCount) && renderchunk4.isBoundingBoxInFrustum((ICamera)frustum, frameCount)) {
              int i1 = renderglobal$containerlocalrenderinformation5.setFacing | 1 << enumfacing.ordinal();
              ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 = renderchunk4.getRenderInfo();
              renderglobal$containerlocalrenderinformation2.initialize(enumfacing, i1);
              deque.add(renderglobal$containerlocalrenderinformation2);
            } 
          } 
        } 
      } 
      this.mc.mcProfiler.endSection();
    } 
    this.mc.mcProfiler.endStartSection("captureFrustum");
    if (this.debugFixTerrainFrustum) {
      fixTerrainFrustum(d3, d4, d5);
      this.debugFixTerrainFrustum = false;
    } 
    Lagometer.timerVisibility.end();
    if (Shaders.isShadowPass) {
      Shaders.mcProfilerEndSection();
    } else {
      this.mc.mcProfiler.endStartSection("rebuildNear");
      this.renderDispatcher.clearChunkUpdates();
      Set<RenderChunk> set = this.chunksToUpdate;
      this.chunksToUpdate = Sets.newLinkedHashSet();
      Lagometer.timerChunkUpdate.start();
      for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 : this.renderInfos) {
        RenderChunk renderchunk5 = renderglobal$containerlocalrenderinformation3.renderChunk;
        if (renderchunk5.isNeedsUpdate() || set.contains(renderchunk5)) {
          this.displayListEntitiesDirty = true;
          BlockPos blockpos1 = renderchunk5.getPosition();
          boolean flag4 = (blockpos.distanceSq((blockpos1.getX() + 8), (blockpos1.getY() + 8), (blockpos1.getZ() + 8)) < 768.0D);
          if (!flag4) {
            this.chunksToUpdate.add(renderchunk5);
            continue;
          } 
          if (!renderchunk5.isPlayerUpdate()) {
            this.chunksToUpdateForced.add(renderchunk5);
            continue;
          } 
          this.mc.mcProfiler.startSection("build near");
          this.renderDispatcher.updateChunkNow(renderchunk5);
          renderchunk5.setNeedsUpdate(false);
          this.mc.mcProfiler.endSection();
        } 
      } 
      Lagometer.timerChunkUpdate.end();
      this.chunksToUpdate.addAll(set);
      this.mc.mcProfiler.endSection();
    } 
  }
  
  private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn) {
    BlockPos blockpos = renderChunkIn.getPosition();
    return (MathHelper.abs_int(pos.getX() - blockpos.getX()) > 16) ? false : ((MathHelper.abs_int(pos.getY() - blockpos.getY()) > 16) ? false : ((MathHelper.abs_int(pos.getZ() - blockpos.getZ()) <= 16)));
  }
  
  private Set<EnumFacing> getVisibleFacings(BlockPos pos) {
    VisGraph visgraph = new VisGraph();
    BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
    Chunk chunk = this.theWorld.getChunkFromBlockCoords(blockpos);
    for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15))) {
      if (chunk.getBlock((BlockPos)blockpos$mutableblockpos).isOpaqueCube())
        visgraph.func_178606_a((BlockPos)blockpos$mutableblockpos); 
    } 
    return visgraph.func_178609_b(pos);
  }
  
  private RenderChunk getRenderChunkOffset(BlockPos p_getRenderChunkOffset_1_, RenderChunk p_getRenderChunkOffset_2_, EnumFacing p_getRenderChunkOffset_3_, boolean p_getRenderChunkOffset_4_, int p_getRenderChunkOffset_5_) {
    RenderChunk renderchunk = p_getRenderChunkOffset_2_.getRenderChunkNeighbour(p_getRenderChunkOffset_3_);
    if (renderchunk == null)
      return null; 
    if (renderchunk.getPosition().getY() > p_getRenderChunkOffset_5_)
      return null; 
    if (p_getRenderChunkOffset_4_) {
      BlockPos blockpos = renderchunk.getPosition();
      int i = p_getRenderChunkOffset_1_.getX() - blockpos.getX();
      int j = p_getRenderChunkOffset_1_.getZ() - blockpos.getZ();
      int k = i * i + j * j;
      if (k > this.renderDistanceSq)
        return null; 
    } 
    return renderchunk;
  }
  
  private void fixTerrainFrustum(double x, double y, double z) {
    this.debugFixedClippingHelper = (ClippingHelper)new ClippingHelperImpl();
    ((ClippingHelperImpl)this.debugFixedClippingHelper).init();
    Matrix4f matrix4f = new Matrix4f(this.debugFixedClippingHelper.modelviewMatrix);
    matrix4f.transpose();
    Matrix4f matrix4f1 = new Matrix4f(this.debugFixedClippingHelper.projectionMatrix);
    matrix4f1.transpose();
    Matrix4f matrix4f2 = new Matrix4f();
    Matrix4f.mul((Matrix4f)matrix4f1, (Matrix4f)matrix4f, (Matrix4f)matrix4f2);
    matrix4f2.invert();
    this.debugTerrainFrustumPosition.x = x;
    this.debugTerrainFrustumPosition.y = y;
    this.debugTerrainFrustumPosition.z = z;
    this.debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
    this.debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
    this.debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
    this.debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
    this.debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
    this.debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
    this.debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);
    for (int i = 0; i < 8; i++) {
      Matrix4f.transform((Matrix4f)matrix4f2, this.debugTerrainMatrix[i], this.debugTerrainMatrix[i]);
      (this.debugTerrainMatrix[i]).x /= (this.debugTerrainMatrix[i]).w;
      (this.debugTerrainMatrix[i]).y /= (this.debugTerrainMatrix[i]).w;
      (this.debugTerrainMatrix[i]).z /= (this.debugTerrainMatrix[i]).w;
      (this.debugTerrainMatrix[i]).w = 1.0F;
    } 
  }
  
  protected Vector3f getViewVector(Entity entityIn, double partialTicks) {
    float f = (float)(entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
    float f1 = (float)(entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);
    if ((Minecraft.getMinecraft()).gameSettings.thirdPersonView == 2)
      f += 180.0F; 
    float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
    float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
    float f4 = -MathHelper.cos(-f * 0.017453292F);
    float f5 = MathHelper.sin(-f * 0.017453292F);
    return new Vector3f(f3 * f4, f5, f2 * f4);
  }
  
  public int renderBlockLayer(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
    RenderHelper.disableStandardItemLighting();
    if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT && !Shaders.isShadowPass) {
      this.mc.mcProfiler.startSection("translucent_sort");
      double d0 = entityIn.posX - this.prevRenderSortX;
      double d1 = entityIn.posY - this.prevRenderSortY;
      double d2 = entityIn.posZ - this.prevRenderSortZ;
      if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
        this.prevRenderSortX = entityIn.posX;
        this.prevRenderSortY = entityIn.posY;
        this.prevRenderSortZ = entityIn.posZ;
        int k = 0;
        this.chunksToResortTransparency.clear();
        for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.renderInfos) {
          if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15)
            this.chunksToResortTransparency.add(renderglobal$containerlocalrenderinformation.renderChunk); 
        } 
      } 
      this.mc.mcProfiler.endSection();
    } 
    this.mc.mcProfiler.startSection("filterempty");
    int l = 0;
    boolean flag = (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT);
    int i1 = flag ? (this.renderInfos.size() - 1) : 0;
    int i = flag ? -1 : this.renderInfos.size();
    int j1 = flag ? -1 : 1;
    int j;
    for (j = i1; j != i; j += j1) {
      RenderChunk renderchunk = ((ContainerLocalRenderInformation)this.renderInfos.get(j)).renderChunk;
      if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn)) {
        l++;
        this.renderContainer.addRenderChunk(renderchunk, blockLayerIn);
      } 
    } 
    if (l == 0) {
      this.mc.mcProfiler.endSection();
      return l;
    } 
    if (Config.isFogOff() && this.mc.entityRenderer.fogStandard)
      GlStateManager.disableFog(); 
    this.mc.mcProfiler.endStartSection("render_" + blockLayerIn);
    renderBlockLayer(blockLayerIn);
    this.mc.mcProfiler.endSection();
    return l;
  }
  
  private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn) {
    this.mc.entityRenderer.enableLightmap();
    if (OpenGlHelper.useVbo()) {
      GL11.glEnableClientState(32884);
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glEnableClientState(32888);
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glEnableClientState(32888);
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glEnableClientState(32886);
    } 
    if (Config.isShaders())
      ShadersRender.preRenderChunkLayer(blockLayerIn); 
    this.renderContainer.renderChunkLayer(blockLayerIn);
    if (Config.isShaders())
      ShadersRender.postRenderChunkLayer(blockLayerIn); 
    if (OpenGlHelper.useVbo())
      for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
        VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
        int i = vertexformatelement.getIndex();
        switch (vertexformatelement$enumusage) {
          case POSITION:
            GL11.glDisableClientState(32884);
          case UV:
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
            GL11.glDisableClientState(32888);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
          case COLOR:
            GL11.glDisableClientState(32886);
            GlStateManager.resetColor();
        } 
      }  
    this.mc.entityRenderer.disableLightmap();
  }
  
  private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn) {
    while (iteratorIn.hasNext()) {
      DestroyBlockProgress destroyblockprogress = iteratorIn.next();
      int i = destroyblockprogress.getCreationCloudUpdateTick();
      if (this.cloudTickCounter - i > 400)
        iteratorIn.remove(); 
    } 
  }
  
  public void updateClouds() {
    if (Config.isShaders() && Keyboard.isKeyDown(61) && Keyboard.isKeyDown(19)) {
      Shaders.uninit();
      Shaders.loadShaderPack();
    } 
    this.cloudTickCounter++;
    if (this.cloudTickCounter % 20 == 0)
      cleanupDamagedBlocks(this.damagedBlocks.values().iterator()); 
  }
  
  private void renderSkyEnd() {
    if (Config.isSkyEnabled()) {
      GlStateManager.disableFog();
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.depthMask(false);
      this.renderEngine.bindTexture(locationEndSkyPng);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      for (int i = 0; i < 6; i++) {
        GlStateManager.pushMatrix();
        if (i == 1)
          GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F); 
        if (i == 2)
          GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F); 
        if (i == 3)
          GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F); 
        if (i == 4)
          GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F); 
        if (i == 5)
          GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F); 
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        int j = 40;
        int k = 40;
        int l = 40;
        if (Config.isCustomColors()) {
          Vec3 vec3 = new Vec3(j / 255.0D, k / 255.0D, l / 255.0D);
          vec3 = CustomColors.getWorldSkyColor(vec3, (World)this.theWorld, this.mc.getRenderViewEntity(), 0.0F);
          j = (int)(vec3.xCoord * 255.0D);
          k = (int)(vec3.yCoord * 255.0D);
          l = (int)(vec3.zCoord * 255.0D);
        } 
        worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(j, k, l, 255).endVertex();
        worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(j, k, l, 255).endVertex();
        worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(j, k, l, 255).endVertex();
        worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(j, k, l, 255).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
      } 
      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
    } 
  }
  
  public void renderSky(float partialTicks, int pass) {
    if (Reflector.ForgeWorldProvider_getSkyRenderer.exists()) {
      WorldProvider worldprovider = this.mc.theWorld.provider;
      Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getSkyRenderer, new Object[0]);
      if (object != null) {
        Reflector.callVoid(object, Reflector.IRenderHandler_render, new Object[] { Float.valueOf(partialTicks), this.theWorld, this.mc });
        return;
      } 
    } 
    if (this.mc.theWorld.provider.getDimensionId() == 1) {
      renderSkyEnd();
    } else if (this.mc.theWorld.provider.isSurfaceWorld()) {
      GlStateManager.disableTexture2D();
      boolean flag = Config.isShaders();
      if (flag)
        Shaders.disableTexture2D(); 
      Vec3 vec3 = this.theWorld.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
      vec3 = CustomColors.getSkyColor(vec3, (IBlockAccess)this.mc.theWorld, (this.mc.getRenderViewEntity()).posX, (this.mc.getRenderViewEntity()).posY + 1.0D, (this.mc.getRenderViewEntity()).posZ);
      if (flag)
        Shaders.setSkyColor(vec3); 
      float f = (float)vec3.xCoord;
      float f1 = (float)vec3.yCoord;
      float f2 = (float)vec3.zCoord;
      if (pass != 2) {
        float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
        float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
        float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
        f = f3;
        f1 = f4;
        f2 = f5;
      } 
      GlStateManager.color(f, f1, f2);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GlStateManager.depthMask(false);
      GlStateManager.enableFog();
      if (flag)
        Shaders.enableFog(); 
      GlStateManager.color(f, f1, f2);
      if (flag)
        Shaders.preSkyList(); 
      if (Config.isSkyEnabled())
        if (this.vboEnabled) {
          this.skyVBO.bindBuffer();
          GL11.glEnableClientState(32884);
          GL11.glVertexPointer(3, 5126, 12, 0L);
          this.skyVBO.drawArrays(7);
          this.skyVBO.unbindBuffer();
          GL11.glDisableClientState(32884);
        } else {
          GlStateManager.callList(this.glSkyList);
        }  
      GlStateManager.disableFog();
      if (flag)
        Shaders.disableFog(); 
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      RenderHelper.disableStandardItemLighting();
      float[] afloat = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(partialTicks), partialTicks);
      if (afloat != null && Config.isSunMoonEnabled()) {
        GlStateManager.disableTexture2D();
        if (flag)
          Shaders.disableTexture2D(); 
        GlStateManager.shadeModel(7425);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((MathHelper.sin(this.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F) ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        float f6 = afloat[0];
        float f7 = afloat[1];
        float f8 = afloat[2];
        if (pass != 2) {
          float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
          float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
          float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
          f6 = f9;
          f7 = f10;
          f8 = f11;
        } 
        worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
        int j = 16;
        for (int l = 0; l <= 16; l++) {
          float f18 = l * 3.1415927F * 2.0F / 16.0F;
          float f12 = MathHelper.sin(f18);
          float f13 = MathHelper.cos(f18);
          worldrenderer.pos((f12 * 120.0F), (f13 * 120.0F), (-f13 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
        } 
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.shadeModel(7424);
      } 
      GlStateManager.enableTexture2D();
      if (flag)
        Shaders.enableTexture2D(); 
      GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
      GlStateManager.pushMatrix();
      float f15 = 1.0F - this.theWorld.getRainStrength(partialTicks);
      GlStateManager.color(1.0F, 1.0F, 1.0F, f15);
      GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
      CustomSky.renderSky((World)this.theWorld, this.renderEngine, partialTicks);
      if (flag)
        Shaders.preCelestialRotate(); 
      GlStateManager.rotate(this.theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
      if (flag)
        Shaders.postCelestialRotate(); 
      float f16 = 30.0F;
      if (Config.isSunTexture()) {
        this.renderEngine.bindTexture(locationSunPng);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-f16, 100.0D, -f16).tex(0.0D, 0.0D).endVertex();
        worldrenderer.pos(f16, 100.0D, -f16).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(f16, 100.0D, f16).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(-f16, 100.0D, f16).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();
      } 
      f16 = 20.0F;
      if (Config.isMoonTexture()) {
        this.renderEngine.bindTexture(locationMoonPhasesPng);
        int i = this.theWorld.getMoonPhase();
        int k = i % 4;
        int i1 = i / 4 % 2;
        float f19 = (k + 0) / 4.0F;
        float f21 = (i1 + 0) / 2.0F;
        float f23 = (k + 1) / 4.0F;
        float f14 = (i1 + 1) / 2.0F;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-f16, -100.0D, f16).tex(f23, f14).endVertex();
        worldrenderer.pos(f16, -100.0D, f16).tex(f19, f14).endVertex();
        worldrenderer.pos(f16, -100.0D, -f16).tex(f19, f21).endVertex();
        worldrenderer.pos(-f16, -100.0D, -f16).tex(f23, f21).endVertex();
        tessellator.draw();
      } 
      GlStateManager.disableTexture2D();
      if (flag)
        Shaders.disableTexture2D(); 
      float f17 = this.theWorld.getStarBrightness(partialTicks) * f15;
      if (f17 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers((World)this.theWorld)) {
        GlStateManager.color(f17, f17, f17, f17);
        if (this.vboEnabled) {
          this.starVBO.bindBuffer();
          GL11.glEnableClientState(32884);
          GL11.glVertexPointer(3, 5126, 12, 0L);
          this.starVBO.drawArrays(7);
          this.starVBO.unbindBuffer();
          GL11.glDisableClientState(32884);
        } else {
          GlStateManager.callList(this.starGLCallList);
        } 
      } 
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableFog();
      if (flag)
        Shaders.enableFog(); 
      GlStateManager.popMatrix();
      GlStateManager.disableTexture2D();
      if (flag)
        Shaders.disableTexture2D(); 
      GlStateManager.color(0.0F, 0.0F, 0.0F);
      double d0 = (this.mc.thePlayer.getPositionEyes(partialTicks)).yCoord - this.theWorld.getHorizon();
      if (d0 < 0.0D) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 12.0F, 0.0F);
        if (this.vboEnabled) {
          this.sky2VBO.bindBuffer();
          GL11.glEnableClientState(32884);
          GL11.glVertexPointer(3, 5126, 12, 0L);
          this.sky2VBO.drawArrays(7);
          this.sky2VBO.unbindBuffer();
          GL11.glDisableClientState(32884);
        } else {
          GlStateManager.callList(this.glSkyList2);
        } 
        GlStateManager.popMatrix();
        float f20 = 1.0F;
        float f22 = -((float)(d0 + 65.0D));
        float f24 = -1.0F;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(-1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, f22, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, f22, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
      } 
      if (this.theWorld.provider.isSkyColored()) {
        GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
      } else {
        GlStateManager.color(f, f1, f2);
      } 
      if (this.mc.gameSettings.renderDistanceChunks <= 4)
        GlStateManager.color(this.mc.entityRenderer.fogColorRed, this.mc.entityRenderer.fogColorGreen, this.mc.entityRenderer.fogColorBlue); 
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, -((float)(d0 - 16.0D)), 0.0F);
      if (Config.isSkyEnabled())
        if (this.vboEnabled) {
          this.sky2VBO.bindBuffer();
          GlStateManager.glEnableClientState(32884);
          GlStateManager.glVertexPointer(3, 5126, 12, 0);
          this.sky2VBO.drawArrays(7);
          this.sky2VBO.unbindBuffer();
          GlStateManager.glDisableClientState(32884);
        } else {
          GlStateManager.callList(this.glSkyList2);
        }  
      GlStateManager.popMatrix();
      GlStateManager.enableTexture2D();
      if (flag)
        Shaders.enableTexture2D(); 
      GlStateManager.depthMask(true);
    } 
  }
  
  public void renderClouds(float partialTicks, int pass) {
    if (!Config.isCloudsOff()) {
      if (Reflector.ForgeWorldProvider_getCloudRenderer.exists()) {
        WorldProvider worldprovider = this.mc.theWorld.provider;
        Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getCloudRenderer, new Object[0]);
        if (object != null) {
          Reflector.callVoid(object, Reflector.IRenderHandler_render, new Object[] { Float.valueOf(partialTicks), this.theWorld, this.mc });
          return;
        } 
      } 
      if (this.mc.theWorld.provider.isSurfaceWorld()) {
        if (Config.isShaders())
          Shaders.beginClouds(); 
        if (Config.isCloudsFancy()) {
          renderCloudsFancy(partialTicks, pass);
        } else {
          float f9 = partialTicks;
          partialTicks = 0.0F;
          GlStateManager.disableCull();
          float f10 = (float)((this.mc.getRenderViewEntity()).lastTickPosY + ((this.mc.getRenderViewEntity()).posY - (this.mc.getRenderViewEntity()).lastTickPosY) * partialTicks);
          int i = 32;
          int j = 8;
          Tessellator tessellator = Tessellator.getInstance();
          WorldRenderer worldrenderer = tessellator.getWorldRenderer();
          this.renderEngine.bindTexture(locationCloudsPng);
          GlStateManager.enableBlend();
          GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
          Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
          float f = (float)vec3.xCoord;
          float f1 = (float)vec3.yCoord;
          float f2 = (float)vec3.zCoord;
          this.cloudRenderer.prepareToRender(false, this.cloudTickCounter, f9, vec3);
          if (this.cloudRenderer.shouldUpdateGlList()) {
            this.cloudRenderer.startUpdateGlList();
            if (pass != 2) {
              float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
              float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
              float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
              f = f3;
              f1 = f4;
              f2 = f5;
            } 
            float f11 = 4.8828125E-4F;
            double d2 = (this.cloudTickCounter + partialTicks);
            double d0 = (this.mc.getRenderViewEntity()).prevPosX + ((this.mc.getRenderViewEntity()).posX - (this.mc.getRenderViewEntity()).prevPosX) * partialTicks + d2 * 0.029999999329447746D;
            double d1 = (this.mc.getRenderViewEntity()).prevPosZ + ((this.mc.getRenderViewEntity()).posZ - (this.mc.getRenderViewEntity()).prevPosZ) * partialTicks;
            int k = MathHelper.floor_double(d0 / 2048.0D);
            int l = MathHelper.floor_double(d1 / 2048.0D);
            d0 -= (k * 2048);
            d1 -= (l * 2048);
            float f6 = this.theWorld.provider.getCloudHeight() - f10 + 0.33F;
            f6 += this.mc.gameSettings.ofCloudsHeight * 128.0F;
            float f7 = (float)(d0 * 4.8828125E-4D);
            float f8 = (float)(d1 * 4.8828125E-4D);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (int i1 = -256; i1 < 256; i1 += 32) {
              for (int j1 = -256; j1 < 256; j1 += 32) {
                worldrenderer.pos((i1 + 0), f6, (j1 + 32)).tex(((i1 + 0) * 4.8828125E-4F + f7), ((j1 + 32) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                worldrenderer.pos((i1 + 32), f6, (j1 + 32)).tex(((i1 + 32) * 4.8828125E-4F + f7), ((j1 + 32) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                worldrenderer.pos((i1 + 32), f6, (j1 + 0)).tex(((i1 + 32) * 4.8828125E-4F + f7), ((j1 + 0) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                worldrenderer.pos((i1 + 0), f6, (j1 + 0)).tex(((i1 + 0) * 4.8828125E-4F + f7), ((j1 + 0) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
              } 
            } 
            tessellator.draw();
            this.cloudRenderer.endUpdateGlList();
          } 
          this.cloudRenderer.renderGlList();
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          GlStateManager.disableBlend();
          GlStateManager.enableCull();
        } 
        if (Config.isShaders())
          Shaders.endClouds(); 
      } 
    } 
  }
  
  public boolean hasCloudFog(double x, double y, double z, float partialTicks) {
    return false;
  }
  
  private void renderCloudsFancy(float partialTicks, int pass) {
    partialTicks = 0.0F;
    GlStateManager.disableCull();
    float f = (float)((this.mc.getRenderViewEntity()).lastTickPosY + ((this.mc.getRenderViewEntity()).posY - (this.mc.getRenderViewEntity()).lastTickPosY) * partialTicks);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    float f1 = 12.0F;
    float f2 = 4.0F;
    double d0 = (this.cloudTickCounter + partialTicks);
    double d1 = ((this.mc.getRenderViewEntity()).prevPosX + ((this.mc.getRenderViewEntity()).posX - (this.mc.getRenderViewEntity()).prevPosX) * partialTicks + d0 * 0.029999999329447746D) / 12.0D;
    double d2 = ((this.mc.getRenderViewEntity()).prevPosZ + ((this.mc.getRenderViewEntity()).posZ - (this.mc.getRenderViewEntity()).prevPosZ) * partialTicks) / 12.0D + 0.33000001311302185D;
    float f3 = this.theWorld.provider.getCloudHeight() - f + 0.33F;
    f3 += this.mc.gameSettings.ofCloudsHeight * 128.0F;
    int i = MathHelper.floor_double(d1 / 2048.0D);
    int j = MathHelper.floor_double(d2 / 2048.0D);
    d1 -= (i * 2048);
    d2 -= (j * 2048);
    this.renderEngine.bindTexture(locationCloudsPng);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
    float f4 = (float)vec3.xCoord;
    float f5 = (float)vec3.yCoord;
    float f6 = (float)vec3.zCoord;
    this.cloudRenderer.prepareToRender(true, this.cloudTickCounter, partialTicks, vec3);
    if (pass != 2) {
      float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
      float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
      float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
      f4 = f7;
      f5 = f8;
      f6 = f9;
    } 
    float f26 = f4 * 0.9F;
    float f27 = f5 * 0.9F;
    float f28 = f6 * 0.9F;
    float f10 = f4 * 0.7F;
    float f11 = f5 * 0.7F;
    float f12 = f6 * 0.7F;
    float f13 = f4 * 0.8F;
    float f14 = f5 * 0.8F;
    float f15 = f6 * 0.8F;
    float f16 = 0.00390625F;
    float f17 = MathHelper.floor_double(d1) * 0.00390625F;
    float f18 = MathHelper.floor_double(d2) * 0.00390625F;
    float f19 = (float)(d1 - MathHelper.floor_double(d1));
    float f20 = (float)(d2 - MathHelper.floor_double(d2));
    int k = 8;
    int l = 4;
    float f21 = 9.765625E-4F;
    GlStateManager.scale(12.0F, 1.0F, 12.0F);
    for (int i1 = 0; i1 < 2; i1++) {
      if (i1 == 0) {
        GlStateManager.colorMask(false, false, false, false);
      } else {
        switch (pass) {
          case 0:
            GlStateManager.colorMask(false, true, true, true);
            break;
          case 1:
            GlStateManager.colorMask(true, false, false, true);
            break;
          case 2:
            GlStateManager.colorMask(true, true, true, true);
            break;
        } 
      } 
      this.cloudRenderer.renderGlList();
    } 
    if (this.cloudRenderer.shouldUpdateGlList()) {
      this.cloudRenderer.startUpdateGlList();
      for (int l1 = -3; l1 <= 4; l1++) {
        for (int j1 = -3; j1 <= 4; j1++) {
          worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
          float f22 = (l1 * 8);
          float f23 = (j1 * 8);
          float f24 = f22 - f19;
          float f25 = f23 - f20;
          if (f3 > -5.0F) {
            worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
          } 
          if (f3 <= 5.0F) {
            worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 8.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 8.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
          } 
          if (l1 > -1)
            for (int k1 = 0; k1 < 8; k1++) {
              worldrenderer.pos((f24 + k1 + 0.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + k1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + k1 + 0.0F), (f3 + 4.0F), (f25 + 8.0F)).tex(((f22 + k1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + k1 + 0.0F), (f3 + 4.0F), (f25 + 0.0F)).tex(((f22 + k1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + k1 + 0.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + k1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
            }  
          if (l1 <= 1)
            for (int i2 = 0; i2 < 8; i2++) {
              worldrenderer.pos((f24 + i2 + 1.0F - 9.765625E-4F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + i2 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + i2 + 1.0F - 9.765625E-4F), (f3 + 4.0F), (f25 + 8.0F)).tex(((f22 + i2 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + i2 + 1.0F - 9.765625E-4F), (f3 + 4.0F), (f25 + 0.0F)).tex(((f22 + i2 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
              worldrenderer.pos((f24 + i2 + 1.0F - 9.765625E-4F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + i2 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
            }  
          if (j1 > -1)
            for (int j2 = 0; j2 < 8; j2++) {
              worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F), (f25 + j2 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + j2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
              worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F), (f25 + j2 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + j2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
              worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + j2 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + j2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
              worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + j2 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + j2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
            }  
          if (j1 <= 1)
            for (int k2 = 0; k2 < 8; k2++) {
              worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F), (f25 + k2 + 1.0F - 9.765625E-4F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
              worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F), (f25 + k2 + 1.0F - 9.765625E-4F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
              worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + k2 + 1.0F - 9.765625E-4F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
              worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + k2 + 1.0F - 9.765625E-4F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
            }  
          tessellator.draw();
        } 
      } 
      this.cloudRenderer.endUpdateGlList();
    } 
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.disableBlend();
    GlStateManager.enableCull();
  }
  
  public void updateChunks(long finishTimeNano) {
    finishTimeNano = (long)(finishTimeNano + 1.0E8D);
    this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);
    if (this.chunksToUpdateForced.size() > 0) {
      Iterator<RenderChunk> iterator = this.chunksToUpdateForced.iterator();
      while (iterator.hasNext()) {
        RenderChunk renderchunk = iterator.next();
        if (!this.renderDispatcher.updateChunkLater(renderchunk))
          break; 
        renderchunk.setNeedsUpdate(false);
        iterator.remove();
        this.chunksToUpdate.remove(renderchunk);
        this.chunksToResortTransparency.remove(renderchunk);
      } 
    } 
    if (this.chunksToResortTransparency.size() > 0) {
      Iterator<RenderChunk> iterator2 = this.chunksToResortTransparency.iterator();
      if (iterator2.hasNext()) {
        RenderChunk renderchunk2 = iterator2.next();
        if (this.renderDispatcher.updateTransparencyLater(renderchunk2))
          iterator2.remove(); 
      } 
    } 
    double d1 = 0.0D;
    int i = Config.getUpdatesPerFrame();
    if (!this.chunksToUpdate.isEmpty()) {
      Iterator<RenderChunk> iterator1 = this.chunksToUpdate.iterator();
      while (iterator1.hasNext()) {
        boolean flag1;
        RenderChunk renderchunk1 = iterator1.next();
        boolean flag = renderchunk1.isChunkRegionEmpty();
        if (flag) {
          flag1 = this.renderDispatcher.updateChunkNow(renderchunk1);
        } else {
          flag1 = this.renderDispatcher.updateChunkLater(renderchunk1);
        } 
        if (!flag1)
          break; 
        renderchunk1.setNeedsUpdate(false);
        iterator1.remove();
        if (!flag) {
          double d0 = 2.0D * RenderChunkUtils.getRelativeBufferSize(renderchunk1);
          d1 += d0;
          if (d1 > i)
            break; 
        } 
      } 
    } 
  }
  
  public void renderWorldBorder(Entity entityIn, float partialTicks) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    WorldBorder worldborder = this.theWorld.getWorldBorder();
    double d0 = (this.mc.gameSettings.renderDistanceChunks * 16);
    if (entityIn.posX >= worldborder.maxX() - d0 || entityIn.posX <= worldborder.minX() + d0 || entityIn.posZ >= worldborder.maxZ() - d0 || entityIn.posZ <= worldborder.minZ() + d0) {
      double d1 = 1.0D - worldborder.getClosestDistance(entityIn) / d0;
      d1 = Math.pow(d1, 4.0D);
      double d2 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
      double d3 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
      double d4 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
      this.renderEngine.bindTexture(locationForcefieldPng);
      GlStateManager.depthMask(false);
      GlStateManager.pushMatrix();
      int i = worldborder.getStatus().getID();
      float f = (i >> 16 & 0xFF) / 255.0F;
      float f1 = (i >> 8 & 0xFF) / 255.0F;
      float f2 = (i & 0xFF) / 255.0F;
      GlStateManager.color(f, f1, f2, (float)d1);
      GlStateManager.doPolygonOffset(-3.0F, -3.0F);
      GlStateManager.enablePolygonOffset();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableAlpha();
      GlStateManager.disableCull();
      float f3 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
      float f4 = 0.0F;
      float f5 = 0.0F;
      float f6 = 128.0F;
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.setTranslation(-d2, -d3, -d4);
      double d5 = Math.max(MathHelper.floor_double(d4 - d0), worldborder.minZ());
      double d6 = Math.min(MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());
      if (d2 > worldborder.maxX() - d0) {
        float f7 = 0.0F;
        for (double d7 = d5; d7 < d6; f7 += 0.5F) {
          double d8 = Math.min(1.0D, d6 - d7);
          float f8 = (float)d8 * 0.5F;
          worldrenderer.pos(worldborder.maxX(), 256.0D, d7).tex((f3 + f7), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(worldborder.maxX(), 256.0D, d7 + d8).tex((f3 + f8 + f7), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(worldborder.maxX(), 0.0D, d7 + d8).tex((f3 + f8 + f7), (f3 + 128.0F)).endVertex();
          worldrenderer.pos(worldborder.maxX(), 0.0D, d7).tex((f3 + f7), (f3 + 128.0F)).endVertex();
          d7++;
        } 
      } 
      if (d2 < worldborder.minX() + d0) {
        float f9 = 0.0F;
        for (double d9 = d5; d9 < d6; f9 += 0.5F) {
          double d12 = Math.min(1.0D, d6 - d9);
          float f12 = (float)d12 * 0.5F;
          worldrenderer.pos(worldborder.minX(), 256.0D, d9).tex((f3 + f9), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(worldborder.minX(), 256.0D, d9 + d12).tex((f3 + f12 + f9), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(worldborder.minX(), 0.0D, d9 + d12).tex((f3 + f12 + f9), (f3 + 128.0F)).endVertex();
          worldrenderer.pos(worldborder.minX(), 0.0D, d9).tex((f3 + f9), (f3 + 128.0F)).endVertex();
          d9++;
        } 
      } 
      d5 = Math.max(MathHelper.floor_double(d2 - d0), worldborder.minX());
      d6 = Math.min(MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());
      if (d4 > worldborder.maxZ() - d0) {
        float f10 = 0.0F;
        for (double d10 = d5; d10 < d6; f10 += 0.5F) {
          double d13 = Math.min(1.0D, d6 - d10);
          float f13 = (float)d13 * 0.5F;
          worldrenderer.pos(d10, 256.0D, worldborder.maxZ()).tex((f3 + f10), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex((f3 + f13 + f10), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex((f3 + f13 + f10), (f3 + 128.0F)).endVertex();
          worldrenderer.pos(d10, 0.0D, worldborder.maxZ()).tex((f3 + f10), (f3 + 128.0F)).endVertex();
          d10++;
        } 
      } 
      if (d4 < worldborder.minZ() + d0) {
        float f11 = 0.0F;
        for (double d11 = d5; d11 < d6; f11 += 0.5F) {
          double d14 = Math.min(1.0D, d6 - d11);
          float f14 = (float)d14 * 0.5F;
          worldrenderer.pos(d11, 256.0D, worldborder.minZ()).tex((f3 + f11), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(d11 + d14, 256.0D, worldborder.minZ()).tex((f3 + f14 + f11), (f3 + 0.0F)).endVertex();
          worldrenderer.pos(d11 + d14, 0.0D, worldborder.minZ()).tex((f3 + f14 + f11), (f3 + 128.0F)).endVertex();
          worldrenderer.pos(d11, 0.0D, worldborder.minZ()).tex((f3 + f11), (f3 + 128.0F)).endVertex();
          d11++;
        } 
      } 
      tessellator.draw();
      worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
      GlStateManager.enableCull();
      GlStateManager.disableAlpha();
      GlStateManager.doPolygonOffset(0.0F, 0.0F);
      GlStateManager.disablePolygonOffset();
      GlStateManager.enableAlpha();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
    } 
  }
  
  private void preRenderDamagedBlocks() {
    GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
    GlStateManager.enableBlend();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
    GlStateManager.doPolygonOffset(-1.0F, -10.0F);
    GlStateManager.enablePolygonOffset();
    GlStateManager.alphaFunc(516, 0.1F);
    GlStateManager.enableAlpha();
    GlStateManager.pushMatrix();
    if (Config.isShaders())
      ShadersRender.beginBlockDamage(); 
  }
  
  private void postRenderDamagedBlocks() {
    GlStateManager.disableAlpha();
    GlStateManager.doPolygonOffset(0.0F, 0.0F);
    GlStateManager.disablePolygonOffset();
    GlStateManager.enableAlpha();
    GlStateManager.depthMask(true);
    GlStateManager.popMatrix();
    if (Config.isShaders())
      ShadersRender.endBlockDamage(); 
  }
  
  public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks) {
    double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
    double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
    double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;
    if (!this.damagedBlocks.isEmpty()) {
      this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
      preRenderDamagedBlocks();
      worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
      worldRendererIn.setTranslation(-d0, -d1, -d2);
      worldRendererIn.markDirty();
      Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();
      while (iterator.hasNext()) {
        boolean flag;
        DestroyBlockProgress destroyblockprogress = iterator.next();
        BlockPos blockpos = destroyblockprogress.getPosition();
        double d3 = blockpos.getX() - d0;
        double d4 = blockpos.getY() - d1;
        double d5 = blockpos.getZ() - d2;
        Block block = this.theWorld.getBlockState(blockpos).getBlock();
        if (Reflector.ForgeTileEntity_canRenderBreaking.exists()) {
          boolean flag1 = (block instanceof net.minecraft.block.BlockChest || block instanceof net.minecraft.block.BlockEnderChest || block instanceof net.minecraft.block.BlockSign || block instanceof net.minecraft.block.BlockSkull);
          if (!flag1) {
            TileEntity tileentity = this.theWorld.getTileEntity(blockpos);
            if (tileentity != null)
              flag1 = Reflector.callBoolean(tileentity, Reflector.ForgeTileEntity_canRenderBreaking, new Object[0]); 
          } 
          flag = !flag1;
        } else {
          flag = (!(block instanceof net.minecraft.block.BlockChest) && !(block instanceof net.minecraft.block.BlockEnderChest) && !(block instanceof net.minecraft.block.BlockSign) && !(block instanceof net.minecraft.block.BlockSkull));
        } 
        if (flag) {
          if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D) {
            iterator.remove();
            continue;
          } 
          IBlockState iblockstate = this.theWorld.getBlockState(blockpos);
          if (iblockstate.getBlock().getMaterial() != Material.air) {
            int i = destroyblockprogress.getPartialBlockDamage();
            TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
            BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, (IBlockAccess)this.theWorld);
          } 
        } 
      } 
      tessellatorIn.draw();
      worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
      postRenderDamagedBlocks();
    } 
  }
  
  public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks) {
    if (execute == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      if (IngameDisplay.BLOCK_OVERLAY.isEnabled()) {
        GLColor.setGlColor((new Color(((Integer)IngameDisplay.BLOCK_OVERLAY_COLOR.getOrDefault(Integer.valueOf((new Color(0, 0, 0, 102)).getRGB()))).intValue(), true)).getRGB());
        GL11.glLineWidth(((Float)IngameDisplay.BLOCK_OVERLAY_LINE_WIDTH.getOrDefault(Float.valueOf(2.0F))).floatValue());
      } else {
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
      } 
      GlStateManager.disableTexture2D();
      if (Config.isShaders())
        Shaders.disableTexture2D(); 
      GlStateManager.depthMask(false);
      float f = 0.002F;
      BlockPos blockpos = movingObjectPositionIn.getBlockPos();
      Block block = this.theWorld.getBlockState(blockpos).getBlock();
      if (IngameDisplay.BLOCK_OVERLAY.isEnabled() && IngameDisplay.BLOCK_OVERLAY_IGNORE_DEPTH.isEnabled())
        GlStateManager.disableDepth(); 
      if (block.getMaterial() != Material.air && this.theWorld.getWorldBorder().contains(blockpos)) {
        block.setBlockBoundsBasedOnState((IBlockAccess)this.theWorld, blockpos);
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        AxisAlignedBB axisalignedbb = block.getSelectedBoundingBox((World)this.theWorld, blockpos);
        Block.EnumOffsetType block$enumoffsettype = block.getOffsetType();
        if (block$enumoffsettype != Block.EnumOffsetType.NONE)
          axisalignedbb = BlockModelUtils.getOffsetBoundingBox(axisalignedbb, block$enumoffsettype, blockpos); 
        AxisAlignedBB axis = axisalignedbb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2);
        drawSelectionBoundingBox(axis);
        if (IngameDisplay.BLOCK_OVERLAY.isEnabled() && IngameDisplay.BLOCK_OVERLAY_FILL.isEnabled()) {
          GLColor.setGlColor((new Color(((Integer)IngameDisplay.BLOCK_OVERLAY_FILL_COLOR.getOrDefault(Integer.valueOf((new Color(0, 0, 0, 50)).getRGB()))).intValue(), true)).getRGB());
          drawFilledWithGL(axis);
        } 
      } 
      if (IngameDisplay.BLOCK_OVERLAY.isEnabled() && IngameDisplay.BLOCK_OVERLAY_IGNORE_DEPTH.isEnabled())
        GlStateManager.enableDepth(); 
      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      if (Config.isShaders())
        Shaders.enableTexture2D(); 
      GlStateManager.disableBlend();
    } 
  }
  
  private void drawFilledWithGL(AxisAlignedBB box) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    tessellator.draw();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    tessellator.draw();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    tessellator.draw();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    tessellator.draw();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    tessellator.draw();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
    worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
    worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
    worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
    tessellator.draw();
  }
  
  public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(3, DefaultVertexFormats.POSITION);
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    tessellator.draw();
    worldrenderer.begin(3, DefaultVertexFormats.POSITION);
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    tessellator.draw();
    worldrenderer.begin(1, DefaultVertexFormats.POSITION);
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    tessellator.draw();
  }
  
  public static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
  }
  
  private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    this.viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
  }
  
  public void markBlockForUpdate(BlockPos pos) {
    int i = pos.getX();
    int j = pos.getY();
    int k = pos.getZ();
    markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
  }
  
  public void notifyLightSet(BlockPos pos) {
    int i = pos.getX();
    int j = pos.getY();
    int k = pos.getZ();
    markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
  }
  
  public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
  }
  
  public void playRecord(String recordName, BlockPos blockPosIn) {
    ISound isound = this.mapSoundPositions.get(blockPosIn);
    if (isound != null) {
      this.mc.getSoundHandler().stopSound(isound);
      this.mapSoundPositions.remove(blockPosIn);
    } 
    if (recordName != null) {
      ItemRecord itemrecord = ItemRecord.getRecord(recordName);
      if (itemrecord != null)
        this.mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal()); 
      PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(new ResourceLocation(recordName), blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ());
      this.mapSoundPositions.put(blockPosIn, positionedsoundrecord);
      this.mc.getSoundHandler().playSound((ISound)positionedsoundrecord);
    } 
  }
  
  public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {}
  
  public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {}
  
  public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
    try {
      spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
    } catch (Throwable throwable) {
      CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
      CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
      crashreportcategory.addCrashSection("ID", Integer.valueOf(particleID));
      if (parameters != null)
        crashreportcategory.addCrashSection("Parameters", parameters); 
      crashreportcategory.addCrashSectionCallable("Position", new Callable<String>() {
            public String call() throws Exception {
              return CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord);
            }
          });
      throw new ReportedException(crashreport);
    } 
  }
  
  private void spawnParticle(EnumParticleTypes particleIn, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
    spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
  }
  
  private EntityFX spawnEntityFX(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {
    if (this.mc != null && this.mc.getRenderViewEntity() != null && this.mc.effectRenderer != null) {
      int i = this.mc.gameSettings.particleSetting;
      if (i == 1 && this.theWorld.rand.nextInt(3) == 0)
        i = 2; 
      double d0 = (this.mc.getRenderViewEntity()).posX - xCoord;
      double d1 = (this.mc.getRenderViewEntity()).posY - yCoord;
      double d2 = (this.mc.getRenderViewEntity()).posZ - zCoord;
      if (particleID == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() && !Config.isAnimatedExplosion())
        return null; 
      if (particleID == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() && !Config.isAnimatedExplosion())
        return null; 
      if (particleID == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID() && !Config.isAnimatedExplosion())
        return null; 
      if (particleID == EnumParticleTypes.SUSPENDED.getParticleID() && !Config.isWaterParticles())
        return null; 
      if (particleID == EnumParticleTypes.SUSPENDED_DEPTH.getParticleID() && !Config.isVoidParticles())
        return null; 
      if (particleID == EnumParticleTypes.SMOKE_NORMAL.getParticleID() && !Config.isAnimatedSmoke())
        return null; 
      if (particleID == EnumParticleTypes.SMOKE_LARGE.getParticleID() && !Config.isAnimatedSmoke())
        return null; 
      if (particleID == EnumParticleTypes.SPELL_MOB.getParticleID() && !Config.isPotionParticles())
        return null; 
      if (particleID == EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID() && !Config.isPotionParticles())
        return null; 
      if (particleID == EnumParticleTypes.SPELL.getParticleID() && !Config.isPotionParticles())
        return null; 
      if (particleID == EnumParticleTypes.SPELL_INSTANT.getParticleID() && !Config.isPotionParticles())
        return null; 
      if (particleID == EnumParticleTypes.SPELL_WITCH.getParticleID() && !Config.isPotionParticles())
        return null; 
      if (particleID == EnumParticleTypes.PORTAL.getParticleID() && !Config.isPortalParticles())
        return null; 
      if (particleID == EnumParticleTypes.FLAME.getParticleID() && !Config.isAnimatedFlame())
        return null; 
      if (particleID == EnumParticleTypes.REDSTONE.getParticleID() && !Config.isAnimatedRedstone())
        return null; 
      if (particleID == EnumParticleTypes.DRIP_WATER.getParticleID() && !Config.isDrippingWaterLava())
        return null; 
      if (particleID == EnumParticleTypes.DRIP_LAVA.getParticleID() && !Config.isDrippingWaterLava())
        return null; 
      if (particleID == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() && !Config.isFireworkParticles())
        return null; 
      if (!ignoreRange) {
        double d3 = 1024.0D;
        if (particleID == EnumParticleTypes.CRIT.getParticleID())
          d3 = 38416.0D; 
        if (d0 * d0 + d1 * d1 + d2 * d2 > d3)
          return null; 
        if (i > 1)
          return null; 
      } 
      EntityFX entityfx = this.mc.effectRenderer.spawnEffectParticle(particleID, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
      if (particleID == EnumParticleTypes.WATER_BUBBLE.getParticleID())
        CustomColors.updateWaterFX(entityfx, (IBlockAccess)this.theWorld, xCoord, yCoord, zCoord, this.renderEnv); 
      if (particleID == EnumParticleTypes.WATER_SPLASH.getParticleID())
        CustomColors.updateWaterFX(entityfx, (IBlockAccess)this.theWorld, xCoord, yCoord, zCoord, this.renderEnv); 
      if (particleID == EnumParticleTypes.WATER_DROP.getParticleID())
        CustomColors.updateWaterFX(entityfx, (IBlockAccess)this.theWorld, xCoord, yCoord, zCoord, this.renderEnv); 
      if (particleID == EnumParticleTypes.TOWN_AURA.getParticleID())
        CustomColors.updateMyceliumFX(entityfx); 
      if (particleID == EnumParticleTypes.PORTAL.getParticleID())
        CustomColors.updatePortalFX(entityfx); 
      if (particleID == EnumParticleTypes.REDSTONE.getParticleID())
        CustomColors.updateReddustFX(entityfx, (IBlockAccess)this.theWorld, xCoord, yCoord, zCoord); 
      return entityfx;
    } 
    return null;
  }
  
  public void onEntityAdded(Entity entityIn) {
    RandomEntities.entityLoaded(entityIn, (World)this.theWorld);
    if (Config.isDynamicLights())
      DynamicLights.entityAdded(entityIn, this); 
  }
  
  public void onEntityRemoved(Entity entityIn) {
    RandomEntities.entityUnloaded(entityIn, (World)this.theWorld);
    if (Config.isDynamicLights())
      DynamicLights.entityRemoved(entityIn, this); 
  }
  
  public void deleteAllDisplayLists() {}
  
  public void broadcastSound(int soundID, BlockPos pos, int data) {
    switch (soundID) {
      case 1013:
      case 1018:
        if (this.mc.getRenderViewEntity() != null) {
          double d0 = pos.getX() - (this.mc.getRenderViewEntity()).posX;
          double d1 = pos.getY() - (this.mc.getRenderViewEntity()).posY;
          double d2 = pos.getZ() - (this.mc.getRenderViewEntity()).posZ;
          double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
          double d4 = (this.mc.getRenderViewEntity()).posX;
          double d5 = (this.mc.getRenderViewEntity()).posY;
          double d6 = (this.mc.getRenderViewEntity()).posZ;
          if (d3 > 0.0D) {
            d4 += d0 / d3 * 2.0D;
            d5 += d1 / d3 * 2.0D;
            d6 += d2 / d3 * 2.0D;
          } 
          if (soundID == 1013) {
            this.theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
            break;
          } 
          this.theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
        } 
        break;
    } 
  }
  
  public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data) {
    int i, j;
    double d0, d1, d2;
    int i1;
    Block block;
    double d3, d4, d5;
    int k, j1;
    float f, f1, f2;
    EnumParticleTypes enumparticletypes;
    int k1;
    double d6, d8, d10;
    int l1;
    double d22;
    int l;
    Random random = this.theWorld.rand;
    switch (sfxType) {
      case 1000:
        this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
        break;
      case 1001:
        this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
        break;
      case 1002:
        this.theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
        break;
      case 1003:
        this.theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 1004:
        this.theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
        break;
      case 1005:
        if (Item.getItemById(data) instanceof ItemRecord) {
          this.theWorld.playRecord(blockPosIn, "records." + ((ItemRecord)Item.getItemById(data)).recordName);
          break;
        } 
        this.theWorld.playRecord(blockPosIn, (String)null);
        break;
      case 1006:
        this.theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 1007:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1008:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1009:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1010:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1011:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1012:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1014:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1015:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1016:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1017:
        this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        break;
      case 1020:
        this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 1021:
        this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 1022:
        this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 2000:
        i = data % 3 - 1;
        j = data / 3 % 3 - 1;
        d0 = blockPosIn.getX() + i * 0.6D + 0.5D;
        d1 = blockPosIn.getY() + 0.5D;
        d2 = blockPosIn.getZ() + j * 0.6D + 0.5D;
        for (i1 = 0; i1 < 10; i1++) {
          double d15 = random.nextDouble() * 0.2D + 0.01D;
          double d16 = d0 + i * 0.01D + (random.nextDouble() - 0.5D) * j * 0.5D;
          double d17 = d1 + (random.nextDouble() - 0.5D) * 0.5D;
          double d18 = d2 + j * 0.01D + (random.nextDouble() - 0.5D) * i * 0.5D;
          double d19 = i * d15 + random.nextGaussian() * 0.01D;
          double d20 = -0.03D + random.nextGaussian() * 0.01D;
          double d21 = j * d15 + random.nextGaussian() * 0.01D;
          spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d16, d17, d18, d19, d20, d21, new int[0]);
        } 
        return;
      case 2001:
        block = Block.getBlockById(data & 0xFFF);
        if (block.getMaterial() != Material.air)
          this.mc.getSoundHandler().playSound((ISound)new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, blockPosIn.getX() + 0.5F, blockPosIn.getY() + 0.5F, blockPosIn.getZ() + 0.5F)); 
        this.mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(data >> 12 & 0xFF));
        break;
      case 2002:
        d3 = blockPosIn.getX();
        d4 = blockPosIn.getY();
        d5 = blockPosIn.getZ();
        for (k = 0; k < 8; k++) {
          spawnParticle(EnumParticleTypes.ITEM_CRACK, d3, d4, d5, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, new int[] { Item.getIdFromItem((Item)Items.potionitem), data });
        } 
        j1 = Items.potionitem.getColorFromDamage(data);
        f = (j1 >> 16 & 0xFF) / 255.0F;
        f1 = (j1 >> 8 & 0xFF) / 255.0F;
        f2 = (j1 >> 0 & 0xFF) / 255.0F;
        enumparticletypes = EnumParticleTypes.SPELL;
        if (Items.potionitem.isEffectInstant(data))
          enumparticletypes = EnumParticleTypes.SPELL_INSTANT; 
        for (k1 = 0; k1 < 100; k1++) {
          double d7 = random.nextDouble() * 4.0D;
          double d9 = random.nextDouble() * Math.PI * 2.0D;
          double d11 = Math.cos(d9) * d7;
          double d23 = 0.01D + random.nextDouble() * 0.5D;
          double d24 = Math.sin(d9) * d7;
          EntityFX entityfx = spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d3 + d11 * 0.1D, d4 + 0.3D, d5 + d24 * 0.1D, d11, d23, d24, new int[0]);
          if (entityfx != null) {
            float f3 = 0.75F + random.nextFloat() * 0.25F;
            entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
            entityfx.multiplyVelocity((float)d7);
          } 
        } 
        this.theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
        break;
      case 2003:
        d6 = blockPosIn.getX() + 0.5D;
        d8 = blockPosIn.getY();
        d10 = blockPosIn.getZ() + 0.5D;
        for (l1 = 0; l1 < 8; l1++) {
          spawnParticle(EnumParticleTypes.ITEM_CRACK, d6, d8, d10, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, new int[] { Item.getIdFromItem(Items.ender_eye) });
        } 
        for (d22 = 0.0D; d22 < 6.283185307179586D; d22 += 0.15707963267948966D) {
          spawnParticle(EnumParticleTypes.PORTAL, d6 + Math.cos(d22) * 5.0D, d8 - 0.4D, d10 + Math.sin(d22) * 5.0D, Math.cos(d22) * -5.0D, 0.0D, Math.sin(d22) * -5.0D, new int[0]);
          spawnParticle(EnumParticleTypes.PORTAL, d6 + Math.cos(d22) * 5.0D, d8 - 0.4D, d10 + Math.sin(d22) * 5.0D, Math.cos(d22) * -7.0D, 0.0D, Math.sin(d22) * -7.0D, new int[0]);
        } 
        return;
      case 2004:
        for (l = 0; l < 20; l++) {
          double d12 = blockPosIn.getX() + 0.5D + (this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
          double d13 = blockPosIn.getY() + 0.5D + (this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
          double d14 = blockPosIn.getZ() + 0.5D + (this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
          this.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d12, d13, d14, 0.0D, 0.0D, 0.0D, new int[0]);
          this.theWorld.spawnParticle(EnumParticleTypes.FLAME, d12, d13, d14, 0.0D, 0.0D, 0.0D, new int[0]);
        } 
        return;
      case 2005:
        ItemDye.spawnBonemealParticles((World)this.theWorld, blockPosIn, data);
        break;
    } 
  }
  
  public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
    if (progress >= 0 && progress < 10) {
      DestroyBlockProgress destroyblockprogress = this.damagedBlocks.get(Integer.valueOf(breakerId));
      if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
        destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
        this.damagedBlocks.put(Integer.valueOf(breakerId), destroyblockprogress);
      } 
      destroyblockprogress.setPartialBlockDamage(progress);
      destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
    } else {
      this.damagedBlocks.remove(Integer.valueOf(breakerId));
    } 
  }
  
  public void setDisplayListEntitiesDirty() {
    this.displayListEntitiesDirty = true;
  }
  
  public boolean hasNoChunkUpdates() {
    return (this.chunksToUpdate.isEmpty() && this.renderDispatcher.hasChunkUpdates());
  }
  
  public void resetClouds() {
    this.cloudRenderer.reset();
  }
  
  public int getCountRenderers() {
    return this.viewFrustum.renderChunks.length;
  }
  
  public int getCountActiveRenderers() {
    return this.renderInfos.size();
  }
  
  public int getCountEntitiesRendered() {
    return this.countEntitiesRendered;
  }
  
  public int getCountTileEntitiesRendered() {
    return this.countTileEntitiesRendered;
  }
  
  public int getCountLoadedChunks() {
    if (this.theWorld == null)
      return 0; 
    IChunkProvider ichunkprovider = this.theWorld.getChunkProvider();
    if (ichunkprovider == null)
      return 0; 
    if (ichunkprovider != this.worldChunkProvider) {
      this.worldChunkProvider = ichunkprovider;
      this.worldChunkProviderMap = (LongHashMap)Reflector.getFieldValue(ichunkprovider, Reflector.ChunkProviderClient_chunkMapping);
    } 
    return (this.worldChunkProviderMap == null) ? 0 : this.worldChunkProviderMap.getNumHashElements();
  }
  
  public int getCountChunksToUpdate() {
    return this.chunksToUpdate.size();
  }
  
  public RenderChunk getRenderChunk(BlockPos p_getRenderChunk_1_) {
    return this.viewFrustum.getRenderChunk(p_getRenderChunk_1_);
  }
  
  public WorldClient getWorld() {
    return this.theWorld;
  }
  
  private void clearRenderInfos() {
    if (renderEntitiesCounter > 0) {
      this.renderInfos = new ArrayList<>(this.renderInfos.size() + 16);
      this.renderInfosEntities = new ArrayList(this.renderInfosEntities.size() + 16);
      this.renderInfosTileEntities = new ArrayList(this.renderInfosTileEntities.size() + 16);
    } else {
      this.renderInfos.clear();
      this.renderInfosEntities.clear();
      this.renderInfosTileEntities.clear();
    } 
  }
  
  public void onPlayerPositionSet() {
    if (this.firstWorldLoad) {
      loadRenderers();
      this.firstWorldLoad = false;
    } 
  }
  
  public void pauseChunkUpdates() {
    if (this.renderDispatcher != null)
      this.renderDispatcher.pauseChunkUpdates(); 
  }
  
  public void resumeChunkUpdates() {
    if (this.renderDispatcher != null)
      this.renderDispatcher.resumeChunkUpdates(); 
  }
  
  public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd) {
    synchronized (this.setTileEntities) {
      this.setTileEntities.removeAll(tileEntitiesToRemove);
      this.setTileEntities.addAll(tileEntitiesToAdd);
    } 
  }
  
  public static class ContainerLocalRenderInformation {
    final RenderChunk renderChunk;
    
    EnumFacing facing;
    
    int setFacing;
    
    public ContainerLocalRenderInformation(RenderChunk p_i2_1_, EnumFacing p_i2_2_, int p_i2_3_) {
      this.renderChunk = p_i2_1_;
      this.facing = p_i2_2_;
      this.setFacing = p_i2_3_;
    }
    
    public void setFacingBit(byte p_setFacingBit_1_, EnumFacing p_setFacingBit_2_) {
      this.setFacing = this.setFacing | p_setFacingBit_1_ | 1 << p_setFacingBit_2_.ordinal();
    }
    
    public boolean isFacingBit(EnumFacing p_isFacingBit_1_) {
      return ((this.setFacing & 1 << p_isFacingBit_1_.ordinal()) > 0);
    }
    
    private void initialize(EnumFacing p_initialize_1_, int p_initialize_2_) {
      this.facing = p_initialize_1_;
      this.setFacing = p_initialize_2_;
    }
  }
}
