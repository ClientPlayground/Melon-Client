package me.kaimson.melonclient.config;

import com.google.common.collect.*;
import com.google.common.collect.Maps;
import me.kaimson.melonclient.features.*;
import me.kaimson.melonclient.utils.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.config.utils.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import java.awt.geom.*;

public class ModuleConfig extends Config
{
    public static final ModuleConfig INSTANCE;
    private final Set<Module> enabled;
    private final Map<Module, Position> positions;
    private final Map<Module, Float> scales;
    
    public ModuleConfig() {
        super("modules", "json", 0.1);
        this.enabled = Sets.newLinkedHashSet();
        this.positions =  Maps.newHashMap();
        this.scales = Maps.newHashMap();
    }
    
    @Override
    public void saveConfig() {
        this.createStructure();
        final JsonObject configFileJson = new JsonObject();
        configFileJson.addProperty("version", (Number)this.version);
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile));
            try {
                for (final Module module : ModuleManager.INSTANCE.modules) {
                    final JsonObject moduleObject = new JsonObject();
                    moduleObject.addProperty("enabled", this.isEnabled(module));
                    if (module.isRender()) {
                        moduleObject.addProperty("posX", (Number)this.getPosition(module).getX());
                        moduleObject.addProperty("posY", (Number)this.getPosition(module).getY());
                        moduleObject.addProperty("anchorPoint", this.getPosition(module).getAnchorPoint().name());
                    }
                    if (module.settings.size() > 0) {
                        final JsonObject propertiesObject = new JsonObject();
                        for (final Setting setting : module.settings) {
                            if (!setting.hasValue()) {
                                continue;
                            }
                            SettingWrapper.addSettingKey(propertiesObject, setting, setting.getObject());
                        }
                        moduleObject.add("properties", (JsonElement)propertiesObject);
                    }
                    configFileJson.add(module.getKey(), (JsonElement)moduleObject);
                }
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(configFileJson.toString())));
            }
            finally {
                if (Collections.singletonList(writer).get(0) != null) {
                    writer.close();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void loadConfig() {
        this.createStructure();
        JsonObject configFileJson = this.loadJsonFile(this.configFile);
        this.getNonNull(configFileJson, "version", (jsonElement) -> {
            Client.info("Detected " + this.name + " version: " + this.version + " => " + jsonElement.getAsDouble());
        });
        Iterator var2 = ModuleManager.INSTANCE.modules.iterator();

        while(var2.hasNext()) {
            Module module = (Module)var2.next();
            this.getNonNull(configFileJson, module.getKey(), (jsonElement) -> {
                JsonObject moduleObject = jsonElement.getAsJsonObject();
                this.getNonNull(moduleObject, "enabled", (element) -> {
                    this.setEnabled(module, element.getAsBoolean());
                });
                if (module.isRender()) {
                    this.getNonNull(moduleObject, "posX", (element) -> {
                        this.setPosition(module, this.getPosition(module).getAnchorPoint(), (float)element.getAsInt(), this.getPosition(module).getY());
                    });
                    this.getNonNull(moduleObject, "posY", (element) -> {
                        this.setPosition(module, this.getPosition(module).getAnchorPoint(), this.getPosition(module).getX(), (float)element.getAsInt());
                    });
                    this.getNonNull(moduleObject, "anchorPoint", (element) -> {
                        this.setPosition(module, AnchorPoint.valueOf(element.getAsString()), this.getPosition(module).getX(), this.getPosition(module).getY());
                    });
                }

                if (module.settings.size() > 0) {
                    JsonObject propertiesObject = moduleObject.getAsJsonObject("properties");
                    Iterator var5 = module.settings.iterator();

                    while(var5.hasNext()) {
                        Setting setting = (Setting)var5.next();
                        this.getNonNull(propertiesObject, setting.getKey(), (element) -> {
                            SettingWrapper.setValue(setting, element);
                        });
                    }
                }

            });
        }

    }
    
    public void setEnabled(final Module module, final boolean enabled) {
        if (enabled) {
            this.enabled.add(module);
        }
        else {
            this.enabled.remove(module);
        }
    }
    
    public boolean isEnabled(final Module module) {
        return this.enabled.contains(module);
    }
    
    public void setPosition(final Module module, final AnchorPoint anchorPoint, final float x, final float y) {
        this.positions.put(module, new Position(anchorPoint, x, y));
    }
    
    public Position getPosition(final Module module) {
        return this.positions.getOrDefault(module, new Position(AnchorPoint.TOP_CENTER, 0.0f, 0.0f));
    }
    
    public float getActualX(final Module module) {
        return this.getPosition(module).getAnchorPoint().getX(new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth()) + this.getPosition(module).getX();
    }
    
    public float getActualY(final Module module) {
        return this.getPosition(module).getAnchorPoint().getY(new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight()) + this.getPosition(module).getY();
    }
    
    public void setClosestAnchorPoint(final Module module) {
        final float actualX = this.getActualX(module);
        final float actualY = this.getActualY(module);
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        final int maxX = sr.getScaledWidth();
        final int maxY = sr.getScaledHeight();
        double shortestDistance = -1.0;
        AnchorPoint closestAnchorPoint = AnchorPoint.CENTER;
        for (final AnchorPoint anchorPoint : AnchorPoint.values()) {
            final double distance = Point2D.distance(actualX, actualY, anchorPoint.getX(maxX), anchorPoint.getY(maxY));
            if (shortestDistance == -1.0 || distance < shortestDistance) {
                closestAnchorPoint = anchorPoint;
                shortestDistance = distance;
            }
        }
        final float x = actualX - closestAnchorPoint.getX(maxX);
        final float y = actualY - closestAnchorPoint.getY(maxY);
        this.setPosition(module, closestAnchorPoint, x, y);
    }
    
    public Float getScale(final Module module) {
        return this.scales.getOrDefault(module, 1.0f);
    }
    
    static {
        INSTANCE = new ModuleConfig();
    }
}
