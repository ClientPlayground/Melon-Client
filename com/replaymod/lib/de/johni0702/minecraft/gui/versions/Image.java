package com.replaymod.lib.de.johni0702.minecraft.gui.versions;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class Image implements AutoCloseable {
  private BufferedImage inner;
  
  public Image(int width, int height) {
    this(new BufferedImage(width, height, 2));
  }
  
  public Image(BufferedImage inner) {
    this.inner = inner;
  }
  
  public BufferedImage getInner() {
    return this.inner;
  }
  
  public void close() {
    if (this.inner != null)
      this.inner = null; 
  }
  
  public int getWidth() {
    return this.inner.getWidth();
  }
  
  public int getHeight() {
    return this.inner.getHeight();
  }
  
  public void setRGBA(int x, int y, int r, int g, int b, int a) {
    this.inner.setRGB(x, y, (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
  }
  
  public static Image read(Path path) throws IOException {
    return read(Files.newInputStream(path, new java.nio.file.OpenOption[0]));
  }
  
  public static Image read(InputStream in) throws IOException {
    return new Image(
        
        ImageIO.read(in));
  }
  
  public void writePNG(File file) throws IOException {
    ImageIO.write(this.inner, "PNG", file);
  }
  
  public void writePNG(OutputStream outputStream) throws IOException {
    ImageIO.write(this.inner, "PNG", outputStream);
  }
  
  public Image scaledSubRect(int x, int y, int width, int height, int scaledWidth, int scaledHeight) {
    BufferedImage dst = new BufferedImage(scaledWidth, scaledHeight, this.inner.getType());
    Graphics2D graphics = dst.createGraphics();
    graphics.drawImage(this.inner, 0, 0, scaledWidth, scaledHeight, x, y, x + width, y + height, null);
    graphics.dispose();
    return new Image(dst);
  }
  
  @Deprecated
  public BufferedImage toBufferedImage() {
    return this.inner;
  }
  
  public DynamicTexture toTexture() {
    return new DynamicTexture(this.inner);
  }
}
