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
import lombok.NonNull;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplaySender;

public class TimestampProperty extends AbstractProperty<Integer> {
  public static final TimestampProperty PROPERTY = new TimestampProperty();
  
  public final PropertyPart<Integer> TIME = (PropertyPart<Integer>)new PropertyParts.ForInteger((Property)this, true);
  
  private TimestampProperty() {
    super("timestamp", "replaymod.gui.editkeyframe.timestamp", null, Integer.valueOf(0));
  }
  
  public Collection<PropertyPart<Integer>> getParts() {
    return Collections.singletonList(this.TIME);
  }
  
  public void applyToGame(Integer value, @NonNull Object replayHandler) {
    if (replayHandler == null)
      throw new NullPointerException("replayHandler is marked non-null but is null"); 
    ReplaySender replaySender = ((ReplayHandler)replayHandler).getReplaySender();
    if (replaySender.isAsyncMode()) {
      replaySender.jumpToTime(value.intValue());
    } else {
      replaySender.sendPacketsTill(value.intValue());
    } 
  }
  
  public void toJson(JsonWriter writer, Integer value) throws IOException {
    writer.value(value);
  }
  
  public Integer fromJson(JsonReader reader) throws IOException {
    return Integer.valueOf(reader.nextInt());
  }
}
