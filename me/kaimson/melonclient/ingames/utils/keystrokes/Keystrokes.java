package me.kaimson.melonclient.ingames.utils.keystrokes;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class Keystrokes {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final KeyUtils keyUtils = new KeyUtils(this);
  
  private Builder builder;
  
  private float keyOffset;
  
  private Builder createLayout() {
    Key none = new Key.FillerKey();
    Key keyW = Key.create(this.mc.gameSettings.keyBindForward, this);
    Key keyA = Key.create(this.mc.gameSettings.keyBindLeft, this);
    Key keyS = Key.create(this.mc.gameSettings.keyBindBack, this);
    Key keyD = Key.create(this.mc.gameSettings.keyBindRight, this);
    Key keyLM = Key.create(this.mc.gameSettings.keyBindAttack, this).setLeftMouse();
    Key keyRM = Key.create(this.mc.gameSettings.keyBindUseItem, this).setRightMouse();
    Key keySpace = Key.create(this.mc.gameSettings.keyBindJump, this).setSpacebar();
    return (new Builder())
      .setWidth(60)
      .setGapSize(2)
      .addRow(new Key[] { none, keyW, none }).addRow(new Key[] { keyA, keyS, keyD }).addRow(new Key[] { keyLM, keyRM }).addRow(new Key[] { keySpace }).build();
  }
  
  public void render(IngameDisplay display, int x, int y) {
    if (this.builder == null)
      this.builder = createLayout(); 
    display.setWidth(this.builder.width);
    display.setHeight(this.builder.height);
    float scale = display.getScale();
    GL11.glPushMatrix();
    GL11.glScaled(scale, scale, 0.0D);
    GL11.glTranslated(x, y, 0.0D);
    for (Rows row : this.builder.rows) {
      GL11.glPushMatrix();
      for (Key key : row.getKeys()) {
        key.render(row.keyWidth, row.getHeight());
        double offset = row.keyWidth + this.builder.gapSize;
        GL11.glTranslated(offset, 0.0D, 0.0D);
        this.keyOffset = (float)(this.keyOffset + offset / this.builder.width);
      } 
      GL11.glPopMatrix();
      GL11.glTranslated(0.0D, row.getHeight() + this.builder.gapSize, 0.0D);
      this.keyOffset = 0.0F;
    } 
    GL11.glScaled(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 0.0D);
    GL11.glPopMatrix();
  }
  
  public int getColor(double offset, boolean pressed) {
    int color;
    if (IngameDisplay.KEYSTROKES_CHROMA.isEnabled()) {
      if (pressed) {
        color = -16777216;
      } else {
        long systemTime = 2000L;
        float speed = 2000.0F;
        float hue = (float)(System.currentTimeMillis() % systemTime) / speed;
        hue = (float)(hue - (this.keyOffset + offset / this.builder.width) * 0.3D);
        color = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      } 
    } else {
      color = (new Color(255, 255, 255, 255)).getRGB();
      if (pressed)
        color ^= 0xFFFFFF; 
    } 
    return color;
  }
  
  public class Builder {
    private final List<Keystrokes.Rows> rows = Lists.newArrayList();
    
    private int width;
    
    private int height;
    
    private int gapSize;
    
    public Builder setWidth(int width) {
      this.width = width;
      return this;
    }
    
    public int getHeight() {
      return this.height;
    }
    
    public Builder setGapSize(int gapSize) {
      this.gapSize = gapSize;
      return this;
    }
    
    public Builder addRow(Key... keys) {
      double keyWidth = ((this.width - this.gapSize * (keys.length - 1)) / keys.length);
      double height = Arrays.<Key>stream(keys).mapToDouble(Key::getHeight).max().orElse(0.0D);
      this.rows.add(new Keystrokes.Rows(keys, keyWidth, height));
      return this;
    }
    
    public Builder build() {
      this.height = (int)(this.rows.stream().mapToDouble(Keystrokes.Rows::getHeight).sum() + (this.gapSize * (this.rows.size() - 1)));
      return this;
    }
  }
  
  public class Rows {
    private final Key[] keys;
    
    private final double keyWidth;
    
    private final double height;
    
    public Rows(Key[] keys, double keyWidth, double height) {
      this.keys = keys;
      this.keyWidth = keyWidth;
      this.height = height;
    }
    
    public Key[] getKeys() {
      return this.keys;
    }
    
    public double getKeyWidth() {
      return this.keyWidth;
    }
    
    public double getHeight() {
      return this.height;
    }
  }
}
