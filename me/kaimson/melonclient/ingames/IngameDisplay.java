package me.kaimson.melonclient.ingames;

import java.lang.annotation.Annotation;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.config.AnchorPoint;
import me.kaimson.melonclient.ingames.annotations.DisplayItem;
import me.kaimson.melonclient.ingames.annotations.Dummy;
import me.kaimson.melonclient.ingames.annotations.EventItem;
import me.kaimson.melonclient.ingames.annotations.Setting;
import me.kaimson.melonclient.ingames.annotations.SettingAll;
import me.kaimson.melonclient.ingames.annotations.SettingMode;
import me.kaimson.melonclient.ingames.annotations.SettingSlider;
import me.kaimson.melonclient.ingames.render.RenderType;

public enum IngameDisplay {
  SCALE,
  TEXT_COLOR,
  CHROMA,
  SHADOW,
  SHOW_BACKGROUND,
  BACKGROUND_COLOR,
  FPS(Message.FPS, RenderType.TEXT),
  COORDINATES(Message.COORDINATES, RenderType.TEXT),
  PING(Message.PING, RenderType.TEXT),
  MEMORY_USAGE(Message.MEMORY_USAGE, RenderType.TEXT),
  CPS(Message.CPS, RenderType.TEXT),
  KEYSTROKES(RenderType.OTHER, true),
  POTION_EFFECTS(RenderType.OTHER, true),
  SCOREBOARD(RenderType.OTHER, true),
  ARMOR_STATUS(RenderType.OTHER, true),
  CLOCK(RenderType.OTHER, true),
  REACH(RenderType.OTHER, false),
  COMBO(RenderType.OTHER, false),
  DISCORD_INTEGRATION_SHOW_SERVER,
  REACH_COLOR,
  REACH_CHROMA,
  COMBO_COLOR,
  COMBO_CHROMA,
  TEXT_TEXT,
  TOGGLE_SPRINT(RenderType.OTHER, false),
  ITEM_PHYSICS,
  OLD_ANIMATIONS,
  BLOCK_OVERLAY,
  FASTER_WORLD_LOADING,
  AUTO_GG,
  AUTO_GLHF,
  DISCORD_INTEGRATION,
  CHAT,
  WINDOWED_FULLSCREEN,
  CUSTOM_CROSSHAIR,
  GUI_BLUR,
  MOTION_BLUR,
  MORE_PARTICLES,
  MORE_PARTICLES_MULTIPLIER,
  POTION_EFFECTS_SHADOW,
  CUSTOM_CROSSHAIR_MODE_CIRCLE,
  CUSTOM_CROSSHAIR_MODE_SQUARE,
  CUSTOM_CROSSHAIR_MODE_CROSS,
  CUSTOM_CROSSHAIR_MODE,
  CUSTOM_CROSSHAIR_COLOR,
  CUSTOM_CROSSHAIR_RENDERGAP,
  CUSTOM_CROSSHAIR_THICKNESS,
  CUSTOM_CROSSHAIR_WIDTH,
  CUSTOM_CROSSHAIR_HEIGHT,
  CUSTOM_CROSSHAIR_RAINBOW,
  ITEM_PHYSICS_ROTATION_SPEED,
  ARMOR_STATUS_HORIZONTAL,
  ARMOR_STATUS_DURABILITY,
  TOGGLE_SPRINT_DEFAULT_KEYBIND,
  TOGGLE_SPRINT_SHOW_TEXT,
  TOGGLE_SNEAK_DEFAULT_KEYBIND,
  TOGGLE_SNEAK_SHOW_TEXT,
  SCOREBOARD_RED_NUMBERS,
  SCOREBOARD_BACKGROUND_COLOR,
  SCOREBOARD_TOP_BACKGROUND_COLOR,
  CHAT_COMPACT,
  CHAT_SMOOTH,
  CHAT_SHADOW,
  BLOCK_OVERLAY_LINE_WIDTH,
  BLOCK_OVERLAY_COLOR,
  BLOCK_OVERLAY_FILL,
  BLOCK_OVERLAY_FILL_COLOR,
  BLOCK_OVERLAY_IGNORE_DEPTH,
  OLD_ANIMATIONS_BUILD,
  OLD_ANIMATIONS_EAT,
  OLD_ANIMATIONS_BLOCKHIT,
  OLD_ANIMATIONS_ROD,
  OLD_ANIMATIONS_SWORD,
  OLD_ANIMATIONS_DAMAGE,
  OLD_ANIMATIONS_BOW,
  OLD_ANIMATIONS_SWING,
  KEYSTROKES_CHROMA,
  KEYSTROKES_BACKGROUND_COLOR,
  KEYSTROKES_PRESSED_BACKGROUND_COLOR,
  KEYSTROKES_TEXT_COLOR,
  KEYSTROKES_PRESSED_TEXT_COLOR,
  KEYSTROKES_OUTLINE,
  KEYSTROKES_FADE_TIME;
  
  public Message getMessage() {
    return this.message;
  }
  
  public RenderType getRenderType() {
    return this.renderType;
  }
  
  public int getWidth() {
    return this.width;
  }
  
  public void setWidth(int width) {
    this.width = width;
  }
  
  public int getHeight() {
    return this.height;
  }
  
  public void setHeight(int height) {
    this.height = height;
  }
  
  public int getText_color() {
    return this.text_color;
  }
  
  public int getBackground_color() {
    return this.background_color;
  }
  
  public boolean isShow_background() {
    return this.show_background;
  }
  
  public boolean isChroma() {
    return this.chroma;
  }
  
  public boolean isShadow() {
    return this.shadow;
  }
  
  private boolean directive = false;
  
  private final Message message;
  
  private final RenderType renderType;
  
  private int width;
  
  private int height;
  
  private int text_color;
  
  private int background_color;
  
  private boolean show_background;
  
  private boolean chroma;
  
  private boolean shadow;
  
  IngameDisplay(Message message) {
    this.message = message;
    this.renderType = RenderType.NONE;
  }
  
  IngameDisplay(RenderType renderType, boolean directive) {
    this.message = Message.NONE;
    this.renderType = renderType;
    this.directive = directive;
  }
  
  IngameDisplay(Message message, RenderType renderType) {
    this.message = message;
    this.renderType = renderType;
  }
  
  public void render(int x, int y) {
    if (getRenderType() == RenderType.TEXT) {
      Client.renderManager.renderBackground(this, x, y, false);
      Client.renderManager.renderIngame(this, x, y);
    } else if (getRenderType() == RenderType.OTHER) {
      Client.renderManager.renderOther(this, x, y, this.directive);
    } 
  }
  
  public Object getOrDefault(Object defaultValue) {
    return Client.config.getCustoms().getOrDefault(this, defaultValue);
  }
  
  public float getScale() {
    return Client.config.getScale(this);
  }
  
  public AnchorPoint getAnchorPoint() {
    return Client.config.getAnchorPoint(this);
  }
  
  public boolean isEnabled() {
    return Client.config.isEnabled(this);
  }
  
  public Setting.Type getType() {
    try {
      if (isSetting()) {
        if (getAnnotation() instanceof SettingSlider)
          return Setting.Type.SLIDER; 
        if (getAnnotation() instanceof SettingAll)
          return ((SettingAll)getAnnotation()).type(); 
        if (getAnnotation() instanceof SettingMode)
          return Setting.Type.MODE; 
        return ((Setting)getAnnotation()).type();
      } 
      return null;
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public boolean isDisplayItem() {
    try {
      return ((getClass().getField(name()).getAnnotations()).length > 0 && getClass().getField(name()).getAnnotations()[0] instanceof DisplayItem);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public boolean isEventItem() {
    try {
      return ((getClass().getField(name()).getAnnotations()).length > 0 && getClass().getField(name()).getAnnotations()[0] instanceof EventItem);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public boolean isSetting() {
    try {
      return (getAnnotation() instanceof Setting || getAnnotation() instanceof SettingSlider || getAnnotation() instanceof SettingMode);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public boolean isAllSetting() {
    try {
      return getAnnotation() instanceof SettingAll;
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public Annotation getAnnotation() {
    try {
      return ((getClass().getField(name()).getAnnotations()).length > 0) ? getClass().getField(name()).getAnnotations()[0] : null;
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public SettingMode getSettingMode() {
    return (SettingMode)getAnnotation();
  }
  
  public String getID() {
    return Client.utils.capitalize(name());
  }
}
