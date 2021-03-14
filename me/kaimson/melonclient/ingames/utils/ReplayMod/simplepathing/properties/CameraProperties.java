package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.property.AbstractProperty;
import com.replaymod.replaystudio.pathing.property.AbstractPropertyGroup;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyGroup;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import com.replaymod.replaystudio.pathing.property.PropertyParts;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.NonNull;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import org.apache.commons.lang3.tuple.Triple;

public class CameraProperties extends AbstractPropertyGroup {
  public static final CameraProperties GROUP = new CameraProperties();
  
  public static final Position POSITION = new Position();
  
  public static final Rotation ROTATION = new Rotation();
  
  private CameraProperties() {
    super("camera", "replaymod.gui.camera");
  }
  
  public Optional<Callable<Change>> getSetter() {
    return Optional.empty();
  }
  
  public static class Position extends AbstractProperty<Triple<Double, Double, Double>> {
    public final PropertyPart<Triple<Double, Double, Double>> X = (PropertyPart<Triple<Double, Double, Double>>)new PropertyParts.ForDoubleTriple((Property)this, true, PropertyParts.TripleElement.LEFT), Y = (PropertyPart<Triple<Double, Double, Double>>)new PropertyParts.ForDoubleTriple((Property)this, true, PropertyParts.TripleElement.MIDDLE), Z = (PropertyPart<Triple<Double, Double, Double>>)new PropertyParts.ForDoubleTriple((Property)this, true, PropertyParts.TripleElement.RIGHT);
    
    private Position() {
      super("position", "replaymod.gui.position", (PropertyGroup)CameraProperties.GROUP, Triple.of(Double.valueOf(0.0D), Double.valueOf(0.0D), Double.valueOf(0.0D)));
    }
    
    public Collection<PropertyPart<Triple<Double, Double, Double>>> getParts() {
      return Arrays.asList((PropertyPart<Triple<Double, Double, Double>>[])new PropertyPart[] { this.X, this.Y, this.Z });
    }
    
    public void applyToGame(Triple<Double, Double, Double> value, @NonNull Object replayHandler) {
      if (replayHandler == null)
        throw new NullPointerException("replayHandler is marked non-null but is null"); 
      ReplayHandler handler = (ReplayHandler)replayHandler;
      handler.spectateCamera();
      CameraEntity cameraEntity = handler.getCameraEntity();
      if (cameraEntity != null)
        cameraEntity.setCameraPosition(((Double)value.getLeft()).doubleValue(), ((Double)value.getMiddle()).doubleValue(), ((Double)value.getRight()).doubleValue()); 
    }
    
    public void toJson(JsonWriter writer, Triple<Double, Double, Double> value) throws IOException {
      writer.beginArray().value((Number)value.getLeft()).value((Number)value.getMiddle()).value((Number)value.getRight()).endArray();
    }
    
    public Triple<Double, Double, Double> fromJson(JsonReader reader) throws IOException {
      reader.beginArray();
      try {
        return Triple.of(Double.valueOf(reader.nextDouble()), Double.valueOf(reader.nextDouble()), Double.valueOf(reader.nextDouble()));
      } finally {
        reader.endArray();
      } 
    }
  }
  
  public static class Rotation extends AbstractProperty<Triple<Float, Float, Float>> {
    public final PropertyPart<Triple<Float, Float, Float>> YAW = (PropertyPart<Triple<Float, Float, Float>>)new PropertyParts.ForFloatTriple((Property)this, true, 360.0F, PropertyParts.TripleElement.LEFT), PITCH = (PropertyPart<Triple<Float, Float, Float>>)new PropertyParts.ForFloatTriple((Property)this, true, 360.0F, PropertyParts.TripleElement.MIDDLE), ROLL = (PropertyPart<Triple<Float, Float, Float>>)new PropertyParts.ForFloatTriple((Property)this, true, 360.0F, PropertyParts.TripleElement.RIGHT);
    
    private Rotation() {
      super("rotation", "replaymod.gui.rotation", (PropertyGroup)CameraProperties.GROUP, Triple.of(Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)));
    }
    
    public Collection<PropertyPart<Triple<Float, Float, Float>>> getParts() {
      return Arrays.asList((PropertyPart<Triple<Float, Float, Float>>[])new PropertyPart[] { this.YAW, this.PITCH, this.ROLL });
    }
    
    public void applyToGame(Triple<Float, Float, Float> value, @NonNull Object replayHandler) {
      if (replayHandler == null)
        throw new NullPointerException("replayHandler is marked non-null but is null"); 
      ReplayHandler handler = (ReplayHandler)replayHandler;
      handler.spectateCamera();
      CameraEntity cameraEntity = handler.getCameraEntity();
      if (cameraEntity != null)
        cameraEntity.setCameraRotation(((Float)value.getLeft()).floatValue(), ((Float)value.getMiddle()).floatValue(), ((Float)value.getRight()).floatValue()); 
    }
    
    public void toJson(JsonWriter writer, Triple<Float, Float, Float> value) throws IOException {
      writer.beginArray().value((Number)value.getLeft()).value((Number)value.getMiddle()).value((Number)value.getRight()).endArray();
    }
    
    public Triple<Float, Float, Float> fromJson(JsonReader reader) throws IOException {
      reader.beginArray();
      try {
        return Triple.of(Float.valueOf((float)reader.nextDouble()), Float.valueOf((float)reader.nextDouble()), Float.valueOf((float)reader.nextDouble()));
      } finally {
        reader.endArray();
      } 
    }
  }
}
