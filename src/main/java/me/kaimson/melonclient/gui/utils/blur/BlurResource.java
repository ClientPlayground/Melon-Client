package me.kaimson.melonclient.gui.utils.blur;

import net.minecraft.client.resources.*;
import java.util.*;
import java.io.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.util.*;

public class BlurResource implements IResource
{
    private final float BLUR_RADIUS;
    
    public InputStream getInputStream() {
        final StringBuilder data = new StringBuilder();
        final Scanner scan = new Scanner(BlurResource.class.getResourceAsStream("/assets/minecraft/melonclient/shaders/post/fade_in_blur.json"));
        try {
            while (scan.hasNextLine()) {
                data.append(scan.nextLine().replaceAll("@radius@", Integer.toString((int)this.BLUR_RADIUS))).append("\n");
            }
        }
        finally {
            scan.close();
        }
        return new ByteArrayInputStream(data.toString().getBytes());
    }
    
    public boolean hasMetadata() {
        return false;
    }
    
    public <T extends IMetadataSection> T getMetadata(final String p_110526_1_) {
        return null;
    }
    
    public String getResourcePackName() {
        return null;
    }
    
    public ResourceLocation getResourceLocation() {
        return null;
    }
    
    public BlurResource(final float BLUR_RADIUS) {
        this.BLUR_RADIUS = BLUR_RADIUS;
    }
}
