package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.SettingsChangedEvent;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsRegistry {
  private static Logger LOGGER = LogManager.getLogger();
  
  private Map<SettingKey<?>, Object> settings = Collections.synchronizedMap(new LinkedHashMap<>());
  
  private final Path configFile = (Minecraft.getMinecraft()).mcDataDir.toPath().resolve("config/replaymod.json");
  
  public void register() {
    String config;
    if (Files.exists(this.configFile, new java.nio.file.LinkOption[0])) {
      try {
        config = new String(Files.readAllBytes(this.configFile), StandardCharsets.UTF_8);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      } 
    } else {
      save();
      return;
    } 
    Gson gson = new Gson();
    JsonObject root = (JsonObject)gson.fromJson(config, JsonObject.class);
    if (root == null) {
      LOGGER.error("Config file {} appears corrupted: {}", new Object[] { this.configFile, config });
      save();
      return;
    } 
    for (Map.Entry<SettingKey<?>, Object> entry : this.settings.entrySet()) {
      SettingKey<?> key = entry.getKey();
      JsonElement category = root.get(key.getCategory());
      if (category != null && category.isJsonObject()) {
        JsonElement valueElem = category.getAsJsonObject().get(key.getKey());
        if (!valueElem.isJsonPrimitive())
          continue; 
        JsonPrimitive value = valueElem.getAsJsonPrimitive();
        if (key.getDefault() instanceof Boolean && value.isBoolean())
          entry.setValue(Boolean.valueOf(value.getAsBoolean())); 
        if (key.getDefault() instanceof Integer && value.isNumber())
          entry.setValue(Integer.valueOf(value.getAsNumber().intValue())); 
        if (key.getDefault() instanceof Double && value.isNumber())
          entry.setValue(Double.valueOf(value.getAsNumber().doubleValue())); 
        if (key.getDefault() instanceof String && value.isString())
          entry.setValue(value.getAsString()); 
      } 
    } 
  }
  
  public void register(Class<?> settingsClass) {
    for (Field field : settingsClass.getDeclaredFields()) {
      if ((field.getModifiers() & 0x9) != 0 && SettingKey.class
        .isAssignableFrom(field.getType()))
        try {
          register((SettingKey)field.get(null));
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }  
    } 
  }
  
  public void register(SettingKey<?> key) {
    this.settings.put(key, key.getDefault());
  }
  
  public Set<SettingKey<?>> getSettings() {
    return this.settings.keySet();
  }
  
  public <T> T get(SettingKey<T> key) {
    if (!this.settings.containsKey(key))
      throw new IllegalArgumentException("Setting " + key + " unknown."); 
    return (T)this.settings.get(key);
  }
  
  public <T> void set(SettingKey<T> key, T value) {
    this.settings.put(key, value);
    EventHandler.call((Event)new SettingsChangedEvent(this, key));
  }
  
  public void save() {
    JsonObject root = new JsonObject();
    for (Map.Entry<SettingKey<?>, Object> entry : this.settings.entrySet()) {
      SettingKey<?> key = entry.getKey();
      JsonObject category = root.getAsJsonObject(key.getCategory());
      if (category == null) {
        category = new JsonObject();
        root.add(key.getCategory(), (JsonElement)category);
      } 
      Object value = entry.getValue();
      if (value instanceof Boolean)
        category.addProperty(key.getKey(), (Boolean)value); 
      if (value instanceof Number)
        category.addProperty(key.getKey(), (Number)value); 
      if (value instanceof String)
        category.addProperty(key.getKey(), (String)value); 
    } 
    Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    String config = gson.toJson((JsonElement)root);
    try {
      Files.createDirectories(this.configFile.getParent(), (FileAttribute<?>[])new FileAttribute[0]);
      Files.write(this.configFile, config.getBytes(StandardCharsets.UTF_8), new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public static interface SettingKey<T> {
    String getCategory();
    
    String getKey();
    
    String getDisplayString();
    
    T getDefault();
  }
  
  public static interface MultipleChoiceSettingKey<T> extends SettingKey<T> {
    List<T> getChoices();
  }
  
  public static class SettingKeys<T> implements SettingKey<T> {
    private final String category;
    
    private final String key;
    
    private final String displayString;
    
    private final T defaultValue;
    
    public SettingKeys(String category, String key, String displayString, T defaultValue) {
      this.category = category;
      this.key = key;
      this.displayString = displayString;
      this.defaultValue = defaultValue;
    }
    
    public String getCategory() {
      return this.category;
    }
    
    public String getKey() {
      return this.key;
    }
    
    public String getDisplayString() {
      return this.displayString;
    }
    
    public T getDefault() {
      return this.defaultValue;
    }
  }
  
  public static class MultipleChoiceSettingKeys<T> extends SettingKeys<T> implements MultipleChoiceSettingKey<T> {
    private List<T> choices = Collections.emptyList();
    
    public MultipleChoiceSettingKeys(String category, String key, String displayString, T defaultValue) {
      super(category, key, displayString, defaultValue);
    }
    
    public void setChoices(List<T> choices) {
      this.choices = Collections.unmodifiableList(choices);
    }
    
    public List<T> getChoices() {
      return this.choices;
    }
  }
}
