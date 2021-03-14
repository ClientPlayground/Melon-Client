package me.kaimson.melonclient.blur;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class BlurResource implements IResource {
  private ResourceLocation location;
  
  public BlurResource(ResourceLocation location) {
    this.location = location;
  }
  
  public InputStream getInputStream() {
    StringBuilder data = new StringBuilder();
    Scanner scan = new Scanner(BlurResource.class.getResourceAsStream("/assets/minecraft/shaders/post/fade_in_blur.json"));
    try {
      while (scan.hasNextLine())
        data.append(scan.nextLine().replaceAll("@radius@", Integer.toString(5))).append("\n"); 
    } finally {
      scan.close();
    } 
    return new ByteArrayInputStream(data.toString().getBytes());
  }
  
  private InputStream getResourceStream(ResourceLocation location) {
    String s = "/assets/minecraft/" + location.getResourceDomain() + "/" + location.getResourcePath();
    InputStream inputstream = null;
    try {
      inputstream = (Minecraft.getMinecraft()).mcDefaultResourcePack.getInputStream(location);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return (inputstream != null) ? inputstream : DefaultResourcePack.class.getResourceAsStream(s);
  }
  
  public boolean hasMetadata() {
    return false;
  }
  
  public <T extends net.minecraft.client.resources.data.IMetadataSection> T getMetadata(String p_110526_1_) {
    return null;
  }
  
  public String getResourcePackName() {
    return null;
  }
  
  public ResourceLocation getResourceLocation() {
    return null;
  }
}
