package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.property.AbstractProperty;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

public class ExplicitInterpolationProperty extends AbstractProperty<Object> {
  public static final ExplicitInterpolationProperty PROPERTY = new ExplicitInterpolationProperty();
  
  private ExplicitInterpolationProperty() {
    super("interpolationFixed", "<internal>", null, new Object());
  }
  
  public Collection<PropertyPart<Object>> getParts() {
    return Collections.emptyList();
  }
  
  public void applyToGame(Object value, @NonNull Object replayHandler) {
    if (replayHandler == null)
      throw new NullPointerException("replayHandler is marked non-null but is null"); 
  }
  
  public void toJson(JsonWriter writer, Object value) throws IOException {
    writer.nullValue();
  }
  
  public Object fromJson(JsonReader reader) throws IOException {
    reader.nextNull();
    return ObjectUtils.NULL;
  }
}
