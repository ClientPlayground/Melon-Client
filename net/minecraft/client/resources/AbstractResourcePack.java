package net.minecraft.client.resources;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractResourcePack implements IResourcePack {
  private static final Logger resourceLog = LogManager.getLogger();
  
  public final File resourcePackFile;
  
  public AbstractResourcePack(File resourcePackFileIn) {
    this.resourcePackFile = resourcePackFileIn;
  }
  
  private static String locationToName(ResourceLocation location) {
    return String.format("%s/%s/%s", new Object[] { "assets", location.getResourceDomain(), location.getResourcePath() });
  }
  
  protected static String getRelativeName(File p_110595_0_, File p_110595_1_) {
    return p_110595_0_.toURI().relativize(p_110595_1_.toURI()).getPath();
  }
  
  public InputStream getInputStream(ResourceLocation location) throws IOException {
    return getInputStreamByName(locationToName(location));
  }
  
  public boolean resourceExists(ResourceLocation location) {
    return hasResourceName(locationToName(location));
  }
  
  protected abstract InputStream getInputStreamByName(String paramString) throws IOException;
  
  protected abstract boolean hasResourceName(String paramString);
  
  protected void logNameNotLowercase(String name) {
    resourceLog.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", new Object[] { name, this.resourcePackFile });
  }
  
  public <T extends net.minecraft.client.resources.data.IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
    return readMetadata(metadataSerializer, getInputStreamByName("pack.mcmeta"), metadataSectionName);
  }
  
  static <T extends net.minecraft.client.resources.data.IMetadataSection> T readMetadata(IMetadataSerializer p_110596_0_, InputStream p_110596_1_, String p_110596_2_) {
    JsonObject jsonobject = null;
    BufferedReader bufferedreader = null;
    try {
      bufferedreader = new BufferedReader(new InputStreamReader(p_110596_1_, Charsets.UTF_8));
      jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
    } catch (RuntimeException runtimeexception) {
      throw new JsonParseException(runtimeexception);
    } finally {
      IOUtils.closeQuietly(bufferedreader);
    } 
    return (T)p_110596_0_.parseMetadataSection(p_110596_2_, jsonobject);
  }
  
  public BufferedImage getPackImage() throws IOException {
    return TextureUtil.readBufferedImage(getInputStreamByName("pack.png"));
  }
  
  public String getPackName() {
    return this.resourcePackFile.getName();
  }
}
