package me.kaimson.melonclient.gui.utils.blur;

import net.minecraft.util.*;
import net.minecraft.client.resources.*;
import java.io.*;
import java.util.*;

public class BlurResourceManager implements IResourceManager
{
    private final float BLUR_RADIUS;
    
    public Set<String> getResourceDomains() {
        return null;
    }
    
    public IResource getResource(final ResourceLocation location) throws IOException {
        return (IResource)new BlurResource(this.BLUR_RADIUS);
    }
    
    public List<IResource> getAllResources(final ResourceLocation location) throws IOException {
        return null;
    }
    
    public BlurResourceManager(final float BLUR_RADIUS) {
        this.BLUR_RADIUS = BLUR_RADIUS;
    }
}
