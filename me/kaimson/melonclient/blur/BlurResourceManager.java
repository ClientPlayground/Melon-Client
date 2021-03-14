package me.kaimson.melonclient.blur;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class BlurResourceManager implements IResourceManager {
  public Set<String> getResourceDomains() {
    return null;
  }
  
  public IResource getResource(ResourceLocation location) throws IOException {
    return new BlurResource(location);
  }
  
  public List<IResource> getAllResources(ResourceLocation location) throws IOException {
    return null;
  }
}
