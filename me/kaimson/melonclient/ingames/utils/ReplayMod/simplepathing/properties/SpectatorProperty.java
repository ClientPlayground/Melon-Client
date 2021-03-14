package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.property.AbstractProperty;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import com.replaymod.replaystudio.pathing.property.PropertyParts;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class SpectatorProperty extends AbstractProperty<Integer> {
  public static final SpectatorProperty PROPERTY = new SpectatorProperty();
  
  public final PropertyPart<Integer> ENTITY_ID = (PropertyPart<Integer>)new PropertyParts.ForInteger((Property)this, false);
  
  private SpectatorProperty() {
    super("spectate", "replaymod.gui.playeroverview.spectate", null, Integer.valueOf(-1));
  }
  
  public Collection<PropertyPart<Integer>> getParts() {
    return Collections.singletonList(this.ENTITY_ID);
  }
  
  public void applyToGame(Integer value, Object replayHandler) {
    ReplayHandler handler = (ReplayHandler)replayHandler;
    CameraEntity cameraEntity = handler.getCameraEntity();
    if (cameraEntity == null)
      return; 
    World world = cameraEntity.getEntityWorld();
    Entity target = world.getEntityByID(value.intValue());
    handler.spectateEntity(target);
  }
  
  public void toJson(JsonWriter writer, Integer value) throws IOException {
    writer.value(value);
  }
  
  public Integer fromJson(JsonReader reader) throws IOException {
    return Integer.valueOf(reader.nextInt());
  }
}
