package me.kaimson.melonclient.config;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.annotations.Setting;
import me.kaimson.melonclient.ingames.annotations.SettingAll;
import me.kaimson.melonclient.ingames.render.RenderType;
import me.kaimson.melonclient.util.ProcessHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Config {
  private final File configFile;
  
  private final Language language = Language.ENGLISH;
  
  private JsonObject languageConfig = new JsonObject();
  
  public JsonObject getLanguageConfig() {
    return this.languageConfig;
  }
  
  public Set<IngameDisplay> getEnabledIngame() {
    return this.enabledIngame;
  }
  
  private final Set<IngameDisplay> enabledIngame = EnumSet.noneOf(IngameDisplay.class);
  
  private final Map<IngameDisplay, Float> scales = Maps.newEnumMap(IngameDisplay.class);
  
  private final Map<IngameDisplay, Position> positions = Maps.newEnumMap(IngameDisplay.class);
  
  private final Map<IngameDisplay, AnchorPoint> anchorPoints = Maps.newEnumMap(IngameDisplay.class);
  
  public Map<IngameDisplay, Object> getCustoms() {
    return this.customs;
  }
  
  private final Map<IngameDisplay, Object> customs = Maps.newEnumMap(IngameDisplay.class);
  
  private final float defaultScale = 1.0F;
  
  private final ConfigJson json = new ConfigJson();
  
  public Config(File configFile) {
    this.configFile = configFile;
  }
  
  public void createFolderStructure() {
    try {
      if (!Client.clientDirectory.exists()) {
        boolean files = Client.clientDirectory.mkdirs();
        Client.log(files ? "Config-files successfully created!" : "Could not create config-files!");
      } 
      if (!this.configFile.exists()) {
        boolean file = this.configFile.createNewFile();
        Client.log(file ? "Config-file was successfully created!" : ("Could not create config-file " + this.configFile.getName() + "!"));
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void saveConfig() {
    try {
      long time = ProcessHandler.instantiate().time(() -> {
            Client.log("Saving config...");
            createFolderStructure();
            try {
              JsonObject configFileJson = new JsonObject();
              BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile));
              try {
                for (IngameDisplay display : IngameDisplay.values()) {
                  if (display.isDisplayItem() || display.isEventItem()) {
                    JsonObject displayObject = new JsonObject();
                    displayObject.addProperty("enabled", Boolean.valueOf(display.isEnabled()));
                    if (display.isDisplayItem()) {
                      displayObject.addProperty("posX", Float.valueOf(getRelativePosition(display).getX()));
                      displayObject.addProperty("posY", Float.valueOf(getRelativePosition(display).getY()));
                      displayObject.addProperty("scale", this.scales.getOrDefault(display, Float.valueOf(1.0F)));
                      displayObject.addProperty("anchorPoint", Integer.valueOf(display.getAnchorPoint().getId()));
                    } 
                    saveSettings(displayObject, display);
                    configFileJson.add(display.getID(), (JsonElement)displayObject);
                  } 
                } 
                writer.write((new GsonBuilder()).setPrettyPrinting().create().toJson((new JsonParser()).parse(configFileJson.toString())));
              } finally {
                if (Collections.<BufferedWriter>singletonList(writer).get(0) != null)
                  writer.close(); 
              } 
            } catch (IOException e) {
              Client.error("An unkown error occured, while trying to save the config!", new Object[0]);
              e.printStackTrace();
            } 
          });
      Client.log("Config saved successfully in " + time + "ms!");
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  private void saveSettings(JsonObject displayObject, IngameDisplay display) {
    try {
      JsonObject settingObject = new JsonObject();
      for (IngameDisplay setting : IngameDisplay.values()) {
        if (setting.isAllSetting()) {
          SettingAll settingAll = (SettingAll)setting.getAnnotation();
          if (settingAll.wrap() && ((
            settingAll.target() == SettingAll.Target.DISPLAY_ITEM && display.isDisplayItem()) || (settingAll
            .target() == SettingAll.Target.DISPLAY_ITEM_RENDERTYPE_TEXT && display.isDisplayItem() && display.getRenderType() == RenderType.TEXT)))
            saveReflectedObject(settingObject, setting.name(), setting.name().toLowerCase(), display); 
        } 
        if (setting.isSetting() && setting.name().startsWith(display.name())) {
          String key = setting.name().replace(display.name(), "").substring(1);
          if (!this.customs.containsKey(setting)) {
            if (setting.getType() == Setting.Type.CHECKBOX)
              settingObject.addProperty(key, Boolean.valueOf(setting.isEnabled())); 
          } else {
            Object obj = this.customs.get(setting);
            if (obj instanceof Number) {
              settingObject.addProperty(key, (Number)obj);
            } else if (obj instanceof String) {
              settingObject.addProperty(key, (String)obj);
            } else {
              settingObject.add(key, (JsonElement)obj);
            } 
          } 
        } 
      } 
      displayObject.add("settings", (JsonElement)settingObject);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  private Field getReflectedField(String fieldName) {
    try {
      Field field = IngameDisplay.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public Object getReflectedObject(String fieldName, Object instance) {
    try {
      return getReflectedField(fieldName).get(instance);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public void setReflectedObject(String fieldName, Object value, Object instance) {
    try {
      getReflectedField(fieldName).set(instance, value);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  private void saveReflectedObject(JsonObject jsonObject, String key, String reflectedObjectName, Object instance) {
    try {
      Field reflectedField = getReflectedField(reflectedObjectName);
      Object reflectedObject = reflectedField.get(instance);
      if (reflectedObject instanceof Integer) {
        jsonObject.addProperty(key, Integer.valueOf(reflectedField.getInt(instance)));
      } else if (reflectedObject instanceof Boolean) {
        jsonObject.addProperty(key, Boolean.valueOf(reflectedField.getBoolean(instance)));
      } 
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  private void loadReflectedObject(String reflectedFieldName, Object newValue, Object instance) {
    try {
      Field reflectedField = getReflectedField(reflectedFieldName);
      reflectedField.setAccessible(true);
      reflectedField.set(instance, newValue);
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public void loadConfig() {
    long time = ProcessHandler.instantiate().time(() -> {
          JsonObject configFileJson;
          Client.log("Loading config...");
          try {
            configFileJson = this.json.loadJsonFile(this.configFile);
          } catch (JsonParseException e) {
            putDefaults();
            return;
          } 
          if (this.json.getFileContents(this.configFile) == null || Objects.equals(this.json.getFileContents(this.configFile), "{}")) {
            putDefaults();
            return;
          } 
          for (IngameDisplay display : IngameDisplay.values()) {
            if (display.getAnnotation() == null)
              Client.error("Invalid number of annotations! Error on " + display + ".", new Object[0]); 
            if (configFileJson.get(display.getID()) != null) {
              JsonObject displayObject = configFileJson.get(display.getID()).getAsJsonObject();
              if (display.isDisplayItem() || display.isEventItem()) {
                if (displayObject.get("enabled").getAsBoolean())
                  this.enabledIngame.add(display); 
                loadSettings(displayObject, display);
                if (display.isDisplayItem()) {
                  if (displayObject.get("posX") != null && displayObject.get("posY") != null)
                    setPosition(display, displayObject.get("posX").getAsFloat(), displayObject.get("posY").getAsFloat()); 
                  if (displayObject.get("scale") != null)
                    setScale(display, displayObject.get("scale").getAsFloat()); 
                  if (displayObject.get("anchorPoint") != null)
                    setAnchorPoint(display, AnchorPoint.fromId(displayObject.get("anchorPoint").getAsInt())); 
                  if (displayObject.get("format") != null) {
                    JsonObject formatObject = displayObject.get("format").getAsJsonObject();
                    formatObject.get("prefix_color").getAsInt();
                    formatObject.get("value_color").getAsInt();
                    formatObject.get("separator_color").getAsInt();
                    formatObject.get("shadow").getAsBoolean();
                  } 
                } 
              } 
            } 
          } 
        });
    Client.log("Config loaded successfully in " + time + "ms!");
  }
  
  private void loadSettings(JsonObject displayObject, IngameDisplay display) {
    try {
      if (displayObject.get("settings") == null)
        return; 
      JsonObject settingObject = displayObject.get("settings").getAsJsonObject();
      for (IngameDisplay setting : IngameDisplay.values()) {
        if (setting.isAllSetting()) {
          SettingAll settingAll = (SettingAll)setting.getAnnotation();
          if (settingObject.get(setting.name()) != null) {
            Object value = null;
            if (settingAll.type() == Setting.Type.COLOR) {
              value = Integer.valueOf(settingObject.get(setting.name()).getAsInt());
            } else if (settingAll.type() == Setting.Type.CHECKBOX) {
              value = Boolean.valueOf(settingObject.get(setting.name()).getAsBoolean());
            } 
            loadReflectedObject(setting.name().toLowerCase(), value, display);
          } 
        } 
        if (setting.isSetting() && setting.name().startsWith(display.name())) {
          String name = setting.name().replace(display.name(), "").substring(1);
          JsonElement element = settingObject.get(name);
          if (element != null) {
            Object value = null;
            if (setting.getType() == Setting.Type.SLIDER) {
              value = Float.valueOf(element.getAsFloat());
            } else if (setting.getType() == Setting.Type.COLOR) {
              value = Integer.valueOf(element.getAsInt());
            } else if (setting.getType() == Setting.Type.TEXT) {
              value = element.getAsString();
            } else if (setting.getType() == Setting.Type.ARRAY) {
              value = element.getAsJsonArray();
            } 
            if (value != null)
              this.customs.put(setting, value); 
            if (setting.getType() == Setting.Type.CHECKBOX)
              setEnabled(setting, settingObject.get(name).getAsBoolean()); 
          } 
        } 
      } 
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public void putDefaults() {
    Client.log("Putting config defaults...");
    for (IngameDisplay display : IngameDisplay.values())
      putDefault(display); 
    saveConfig();
  }
  
  public void putDefault(IngameDisplay display) {
    if (display.isDisplayItem()) {
      setPosition(display, 0.0F, 0.0F);
      setScale(display, 1.0F);
      setAnchorPoint(display, AnchorPoint.TOP_CENTER);
    } 
    generateDefaults(display);
  }
  
  private void generateDefaults(IngameDisplay display) {
    for (IngameDisplay setting : IngameDisplay.values()) {
      if (setting.isAllSetting()) {
        SettingAll settingAll = (SettingAll)setting.getAnnotation();
        if (settingAll.wrap()) {
          Object value = null;
          if (settingAll.type() == Setting.Type.COLOR) {
            value = Integer.valueOf(0);
          } else if (settingAll.type() == Setting.Type.CHECKBOX) {
            value = Boolean.valueOf(false);
          } 
          if (value != null)
            setReflectedObject(setting.name().toLowerCase(), value, display); 
        } 
      } 
      if (setting.isSetting() && setting.name().startsWith(display.name()))
        this.customs.remove(setting); 
    } 
    saveConfig();
  }
  
  public void loadLanguageFile() {
    long time = ProcessHandler.instantiate().time(() -> {
          Client.log("Loading language file: " + this.language.getPath() + ".json");
          try {
            InputStream fileStream = getClass().getResourceAsStream(this.language.getPath() + ".json");
            try {
              if (fileStream != null) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) != -1)
                  result.write(buffer, 0, length); 
                String dataString = result.toString("UTF-8");
                this.languageConfig = (new JsonParser()).parse(dataString).getAsJsonObject();
              } else {
                Client.error("An error occured, while trying to load the language file!", new Object[0]);
                throw new FileNotFoundException("Language file " + this.language.getPath() + ".json not found!");
              } 
            } finally {
              if (Collections.<InputStream>singletonList(fileStream).get(0) != null)
                fileStream.close(); 
            } 
          } catch (JsonParseException|IllegalStateException|IOException e) {
            e.printStackTrace();
          } 
        });
    Client.log("Language file successfully loaded in " + time + "ms!");
  }
  
  public void setEnabled(IngameDisplay display) {
    if (this.enabledIngame.contains(display)) {
      this.enabledIngame.remove(display);
    } else {
      this.enabledIngame.add(display);
    } 
  }
  
  public void setEnabled(IngameDisplay display, boolean enabled) {
    if (enabled) {
      this.enabledIngame.add(display);
    } else {
      this.enabledIngame.remove(display);
    } 
  }
  
  public boolean isEnabled(IngameDisplay display) {
    return this.enabledIngame.contains(display);
  }
  
  public float getActualX(IngameDisplay display) {
    return ((AnchorPoint)this.anchorPoints.get(display)).getX((new ScaledResolution(Minecraft.getMinecraft())).getScaledWidth()) + getRelativePosition(display).getX();
  }
  
  public float getActualY(IngameDisplay display) {
    return ((AnchorPoint)this.anchorPoints.get(display)).getY((new ScaledResolution(Minecraft.getMinecraft())).getScaledHeight()) + getRelativePosition(display).getY();
  }
  
  public Position getRelativePosition(IngameDisplay display) {
    if (this.positions.containsKey(display))
      return this.positions.get(display); 
    this.positions.put(display, new Position(0.0F, 0.0F));
    this.anchorPoints.put(display, AnchorPoint.TOP_CENTER);
    return getRelativePosition(display);
  }
  
  public void setClosestAnchorPoint(IngameDisplay display) {
    float actualX = getActualX(display);
    float actualY = getActualY(display);
    ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    int maxX = sr.getScaledWidth();
    int maxY = sr.getScaledHeight();
    double shortestDistance = -1.0D;
    AnchorPoint closestAnchorPoint = AnchorPoint.CENTER;
    for (AnchorPoint point : AnchorPoint.values()) {
      double distance = Point2D.distance(actualX, actualY, point.getX(maxX), point.getY(maxY));
      if (shortestDistance == -1.0D || distance < shortestDistance) {
        closestAnchorPoint = point;
        shortestDistance = distance;
      } 
    } 
    float x = actualX - closestAnchorPoint.getX(sr.getScaledWidth());
    float y = actualY - closestAnchorPoint.getY(sr.getScaledHeight());
    setAnchorPoint(display, closestAnchorPoint);
    setPosition(display, x, y);
  }
  
  public void setPosition(IngameDisplay display, float x, float y) {
    if (this.positions.containsKey(display)) {
      ((Position)this.positions.get(display)).setX(x);
      ((Position)this.positions.get(display)).setY(y);
    } else {
      this.positions.put(display, new Position(x, y));
    } 
  }
  
  public void setAnchorPoint(IngameDisplay display, AnchorPoint anchorPoint) {
    this.anchorPoints.put(display, anchorPoint);
  }
  
  public AnchorPoint getAnchorPoint(IngameDisplay display) {
    return this.anchorPoints.get(display);
  }
  
  public void setScale(IngameDisplay display, float scale) {
    this.scales.put(display, Float.valueOf(scale));
  }
  
  public float getScale(IngameDisplay display) {
    return ((Float)this.scales.getOrDefault(display, Float.valueOf(1.0F))).floatValue();
  }
}
