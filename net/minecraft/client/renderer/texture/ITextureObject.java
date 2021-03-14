package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.client.resources.IResourceManager;
import net.optifine.shaders.MultiTexID;

public interface ITextureObject {
  void setBlurMipmap(boolean paramBoolean1, boolean paramBoolean2);
  
  void restoreLastBlurMipmap();
  
  void loadTexture(IResourceManager paramIResourceManager) throws IOException;
  
  int getGlTextureId();
  
  MultiTexID getMultiTexID();
}
