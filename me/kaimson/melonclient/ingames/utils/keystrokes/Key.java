package me.kaimson.melonclient.ingames.utils.keystrokes;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.GLColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Key {
  private final KeyBinding keyBinding;
  
  private final Keystrokes keystrokes;
  
  private long pressTime;
  
  private float faded = 0.0F;
  
  private float fadeTime = 100.0F;
  
  private boolean wasPressed;
  
  private double height = 18.0D;
  
  public double getHeight() {
    return this.height;
  }
  
  private Type type = Type.NORMAL;
  
  public Key(KeyBinding keyBinding, Keystrokes keystrokes) {
    this.keyBinding = keyBinding;
    this.keystrokes = keystrokes;
  }
  
  public static Key create(KeyBinding keyBinding, Keystrokes keystrokes) {
    return new Key(keyBinding, keystrokes);
  }
  
  public void render(double width, double height) {
    boolean pressed = isPressed();
    float pressModifier = Math.min(1.0F, (float)(System.currentTimeMillis() - this.pressTime) / ((Float)IngameDisplay.KEYSTROKES_FADE_TIME.getOrDefault(Float.valueOf(100.0F))).floatValue());
    float brightness = (pressed ? pressModifier : (1.0F - pressModifier)) * 0.8F;
    GlStateManager.pushMatrix();
    GL11.glPushMatrix();
    GlStateManager.enableBlend();
    GL11.glDisable(3553);
    setBackgroundColor(brightness);
    GL11.glBegin(7);
    GL11.glVertex2d(0.0D, height);
    GL11.glVertex2d(width, height);
    GL11.glVertex2d(width, 0.0D);
    GL11.glVertex2d(0.0D, 0.0D);
    GL11.glEnd();
    GL11.glEnable(3553);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.disableBlend();
    GL11.glPopMatrix();
    GlStateManager.popMatrix();
    if (IngameDisplay.KEYSTROKES_OUTLINE.isEnabled()) {
      drawColoredRect(0.0D, 0.0D, width, 1.0D, pressed);
      drawColoredRect(width - 1.0D, 0.0D, width, height, pressed);
      drawColoredRect(width, height, 0.0D, height - 1.0D, pressed);
      drawColoredRect(1.0D, height, 0.0D, 0.0D, pressed);
    } 
    switch (this.type) {
      case NORMAL:
        KeyUtils.instance.drawKeyText(Keyboard.getKeyName(this.keyBinding.getKeyCode()), width, height, pressed);
        return;
      case SPACE_BAR:
        KeyUtils.instance.drawSpacebar(width, height, pressed);
        return;
      case RIGHT_MOUSE:
      case LEFT_MOUSE:
        KeyUtils.instance.drawKeyText((this.type == Type.LEFT_MOUSE) ? "LMB" : "RMB", width, height, pressed);
        break;
    } 
  }
  
  private void setBackgroundColor(float brightness) {
    boolean pressed = isPressed();
    int bgColor = 0;
    int bgPressed = 0;
    int converted = GLColor.convertPercentToValue(brightness);
    if (((Integer)Client.config.getCustoms().getOrDefault(IngameDisplay.KEYSTROKES_BACKGROUND_COLOR, Integer.valueOf(0))).intValue() != 0)
      bgColor = ((Integer)Client.config.getCustoms().get(IngameDisplay.KEYSTROKES_BACKGROUND_COLOR)).intValue(); 
    if (((Integer)Client.config.getCustoms().getOrDefault(IngameDisplay.KEYSTROKES_PRESSED_BACKGROUND_COLOR, Integer.valueOf(0))).intValue() != 0)
      bgPressed = ((Integer)Client.config.getCustoms().get(IngameDisplay.KEYSTROKES_PRESSED_BACKGROUND_COLOR)).intValue(); 
    if (bgColor == 0)
      bgColor = (new Color(converted, converted, converted, 102)).getRGB(); 
    if (bgPressed == 0)
      bgPressed = (new Color(converted, converted, converted, 102)).getRGB(); 
    int bg = pressed ? bgPressed : bgColor;
    if (this.faded < 1.0F) {
      int lastColor = !pressed ? bgPressed : bgColor;
      GLColor.setGlColor(GLColor.getIntermediateColor(bg, lastColor, this.faded));
      return;
    } 
    GLColor.setGlColor(bg);
  }
  
  private void drawColoredRect(double x1, double y1, double x2, double y2, boolean invertColor) {
    GlStateManager.pushMatrix();
    GL11.glPushMatrix();
    GlStateManager.enableBlend();
    GL11.glDisable(3553);
    GL11.glShadeModel(7425);
    GL11.glBegin(7);
    GLColor.setGlColor(this.keystrokes.getColor(x1, invertColor));
    GL11.glVertex3d(x1, y2, 0.0D);
    GLColor.setGlColor(this.keystrokes.getColor(x2, invertColor));
    GL11.glVertex3d(x2, y2, 0.0D);
    GL11.glVertex3d(x2, y1, 0.0D);
    GLColor.setGlColor(this.keystrokes.getColor(x1, invertColor));
    GL11.glVertex3d(x1, y1, 0.0D);
    GL11.glEnd();
    GL11.glEnable(3553);
    GlStateManager.disableBlend();
    GL11.glPopMatrix();
    GlStateManager.popMatrix();
  }
  
  public Key setSpacebar() {
    this.height = 14.0D;
    this.type = Type.SPACE_BAR;
    return this;
  }
  
  public Key setLeftMouse() {
    this.height = 20.0D;
    this.type = Type.LEFT_MOUSE;
    return this;
  }
  
  public Key setRightMouse() {
    this.height = 20.0D;
    this.type = Type.RIGHT_MOUSE;
    return this;
  }
  
  private boolean isPressed() {
    int keycode = this.keyBinding.getKeyCode();
    boolean pressed = (keycode < 0) ? Mouse.isButtonDown(keycode + 100) : Keyboard.isKeyDown(keycode);
    if (this.wasPressed != pressed)
      this.pressTime = System.currentTimeMillis(); 
    this.fadeTime = ((Float)IngameDisplay.KEYSTROKES_FADE_TIME.getOrDefault(Float.valueOf(100.0F))).floatValue();
    this.faded = (float)(System.currentTimeMillis() - this.pressTime) / this.fadeTime;
    this.wasPressed = pressed;
    return pressed;
  }
  
  public static class FillerKey extends Key {
    public FillerKey() {
      super(null, null);
    }
    
    public void render(double width, double height) {}
  }
  
  private enum Type {
    NORMAL, LEFT_MOUSE, RIGHT_MOUSE, SPACE_BAR;
  }
}
