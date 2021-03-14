package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.resources.IResourceManager;

public class DynamicTexture extends AbstractTexture {
  private final int[] dynamicTextureData;
  
  private final int width;
  
  private final int height;
  
  public DynamicTexture(BufferedImage bufferedImage) {
    this(bufferedImage.getWidth(), bufferedImage.getHeight());
    bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
    updateDynamicTexture();
  }
  
  public DynamicTexture(int textureWidth, int textureHeight) {
    this.width = textureWidth;
    this.height = textureHeight;
    this.dynamicTextureData = new int[textureWidth * textureHeight];
    TextureUtil.allocateTexture(getGlTextureId(), textureWidth, textureHeight);
  }
  
  public void loadTexture(IResourceManager resourceManager) throws IOException {}
  
  public void updateDynamicTexture() {
    TextureUtil.uploadTexture(getGlTextureId(), this.dynamicTextureData, this.width, this.height);
  }
  
  public int[] getTextureData() {
    return this.dynamicTextureData;
  }
}
