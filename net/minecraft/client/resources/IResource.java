package net.minecraft.client.resources;

import java.io.InputStream;
import net.minecraft.util.ResourceLocation;

public interface IResource {
  ResourceLocation getResourceLocation();
  
  InputStream getInputStream();
  
  boolean hasMetadata();
  
  <T extends net.minecraft.client.resources.data.IMetadataSection> T getMetadata(String paramString);
  
  String getResourcePackName();
}
