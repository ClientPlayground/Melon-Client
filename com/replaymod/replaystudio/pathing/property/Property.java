package com.replaymod.replaystudio.pathing.property;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collection;
import lombok.NonNull;

public interface Property<T> {
  @NonNull
  String getLocalizedName();
  
  PropertyGroup getGroup();
  
  @NonNull
  String getId();
  
  T getNewValue();
  
  Collection<PropertyPart<T>> getParts();
  
  void applyToGame(T paramT, Object paramObject);
  
  void toJson(JsonWriter paramJsonWriter, T paramT) throws IOException;
  
  T fromJson(JsonReader paramJsonReader) throws IOException;
}
