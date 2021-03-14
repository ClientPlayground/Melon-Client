package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.PropertiesOrdered;

public class EmissiveTextures {
  private static String suffixEmissive = null;
  
  private static String suffixEmissivePng = null;
  
  private static boolean active = false;
  
  private static boolean render = false;
  
  private static boolean hasEmissive = false;
  
  private static boolean renderEmissive = false;
  
  private static float lightMapX;
  
  private static float lightMapY;
  
  private static final String SUFFIX_PNG = ".png";
  
  private static final ResourceLocation LOCATION_EMPTY = new ResourceLocation("mcpatcher/ctm/default/empty.png");
  
  public static boolean isActive() {
    return active;
  }
  
  public static String getSuffixEmissive() {
    return suffixEmissive;
  }
  
  public static void beginRender() {
    render = true;
    hasEmissive = false;
  }
  
  public static ITextureObject getEmissiveTexture(ITextureObject texture, Map<ResourceLocation, ITextureObject> mapTextures) {
    SimpleTexture simpleTexture1;
    if (!render)
      return texture; 
    if (!(texture instanceof SimpleTexture))
      return texture; 
    SimpleTexture simpletexture = (SimpleTexture)texture;
    ResourceLocation resourcelocation = simpletexture.locationEmissive;
    if (!renderEmissive) {
      if (resourcelocation != null)
        hasEmissive = true; 
      return texture;
    } 
    if (resourcelocation == null)
      resourcelocation = LOCATION_EMPTY; 
    ITextureObject itextureobject = mapTextures.get(resourcelocation);
    if (itextureobject == null) {
      simpleTexture1 = new SimpleTexture(resourcelocation);
      TextureManager texturemanager = Config.getTextureManager();
      texturemanager.loadTexture(resourcelocation, (ITextureObject)simpleTexture1);
    } 
    return (ITextureObject)simpleTexture1;
  }
  
  public static boolean hasEmissive() {
    return hasEmissive;
  }
  
  public static void beginRenderEmissive() {
    lightMapX = OpenGlHelper.lastBrightnessX;
    lightMapY = OpenGlHelper.lastBrightnessY;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, lightMapY);
    renderEmissive = true;
  }
  
  public static void endRenderEmissive() {
    renderEmissive = false;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightMapX, lightMapY);
  }
  
  public static void endRender() {
    render = false;
    hasEmissive = false;
  }
  
  public static void update() {
    active = false;
    suffixEmissive = null;
    suffixEmissivePng = null;
    if (Config.isEmissiveTextures())
      try {
        String s = "optifine/emissive.properties";
        ResourceLocation resourcelocation = new ResourceLocation(s);
        InputStream inputstream = Config.getResourceStream(resourcelocation);
        if (inputstream == null)
          return; 
        dbg("Loading " + s);
        PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
        propertiesOrdered.load(inputstream);
        inputstream.close();
        suffixEmissive = propertiesOrdered.getProperty("suffix.emissive");
        if (suffixEmissive != null)
          suffixEmissivePng = suffixEmissive + ".png"; 
        active = (suffixEmissive != null);
      } catch (FileNotFoundException var4) {
        return;
      } catch (IOException ioexception) {
        ioexception.printStackTrace();
      }  
  }
  
  private static void dbg(String str) {
    Config.dbg("EmissiveTextures: " + str);
  }
  
  private static void warn(String str) {
    Config.warn("EmissiveTextures: " + str);
  }
  
  public static boolean isEmissive(ResourceLocation loc) {
    return (suffixEmissivePng == null) ? false : loc.getResourcePath().endsWith(suffixEmissivePng);
  }
  
  public static void loadTexture(ResourceLocation loc, SimpleTexture tex) {
    if (loc != null && tex != null) {
      tex.isEmissive = false;
      tex.locationEmissive = null;
      if (suffixEmissivePng != null) {
        String s = loc.getResourcePath();
        if (s.endsWith(".png"))
          if (s.endsWith(suffixEmissivePng)) {
            tex.isEmissive = true;
          } else {
            String s1 = s.substring(0, s.length() - ".png".length()) + suffixEmissivePng;
            ResourceLocation resourcelocation = new ResourceLocation(loc.getResourceDomain(), s1);
            if (Config.hasResource(resourcelocation))
              tex.locationEmissive = resourcelocation; 
          }  
      } 
    } 
  }
}
