package com.replaymod.replaystudio.pathing.serialize;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimelineSerialization {
  private static final String FILE_ENTRY = "timelines.json";
  
  private final PathingRegistry registry;
  
  private final ReplayFile replayFile;
  
  public TimelineSerialization(PathingRegistry registry, ReplayFile replayFile) {
    this.registry = registry;
    this.replayFile = replayFile;
  }
  
  public void save(Map<String, Timeline> timelines) throws IOException {
    String serialized = serialize(timelines);
    try (OutputStream out = this.replayFile.write("timelines.json")) {
      out.write(serialized.getBytes(Charsets.UTF_8));
    } 
  }
  
  public Map<String, Timeline> load() throws IOException {
    Map<String, Timeline> timelines = new LinkedHashMap<>(LegacyTimelineConverter.convert(this.registry, this.replayFile));
    Optional<InputStream> optionalIn = this.replayFile.get("timelines.json");
    if (optionalIn.isPresent()) {
      String serialized;
      try (InputStream in = (InputStream)optionalIn.get()) {
        serialized = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
      } 
      Map<String, Timeline> deserialized = deserialize(serialized);
      timelines.putAll(deserialized);
    } 
    return timelines;
  }
  
  public String serialize(Map<String, Timeline> timelines) throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    for (Map.Entry<String, Timeline> entry : timelines.entrySet()) {
      Timeline timeline = entry.getValue();
      writer.name(entry.getKey()).beginArray();
      for (Path path : timeline.getPaths()) {
        writer.beginObject();
        writer.name("keyframes").beginArray();
        for (Keyframe keyframe : path.getKeyframes()) {
          writer.beginObject();
          writer.name("time").value(keyframe.getTime());
          writer.name("properties").beginObject();
          for (Property<?> property : (Iterable<Property<?>>)keyframe.getProperties()) {
            writer.name(((property.getGroup() == null) ? "" : (property.getGroup().getId() + ":")) + property.getId());
            writeProperty(writer, keyframe, property);
          } 
          writer.endObject();
          writer.endObject();
        } 
        writer.endArray();
        Map<Interpolator, Integer> interpolators = new LinkedHashMap<>();
        writer.name("segments").beginArray();
        for (PathSegment segment : path.getSegments()) {
          Interpolator interpolator = segment.getInterpolator();
          if (interpolator == null) {
            writer.nullValue();
            continue;
          } 
          Integer index = interpolators.get(interpolator);
          if (index == null)
            interpolators.put(interpolator, index = Integer.valueOf(interpolators.size())); 
          writer.value(index);
        } 
        writer.endArray();
        writer.name("interpolators").beginArray();
        for (Interpolator interpolator : interpolators.keySet()) {
          writer.beginObject();
          writer.name("type");
          this.registry.serializeInterpolator(writer, interpolator);
          writer.name("properties").beginArray();
          for (Property<?> property : (Iterable<Property<?>>)interpolator.getKeyframeProperties())
            writer.value(((property.getGroup() == null) ? "" : (property.getGroup().getId() + ":")) + property.getId()); 
          writer.endArray();
          writer.endObject();
        } 
        writer.endArray();
        writer.endObject();
      } 
      writer.endArray();
    } 
    writer.endObject();
    writer.flush();
    return stringWriter.toString();
  }
  
  private static <T> void writeProperty(JsonWriter writer, Keyframe keyframe, Property<T> property) throws IOException {
    property.toJson(writer, keyframe.getValue(property).get());
  }
  
  public Map<String, Timeline> deserialize(String serialized) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(serialized));
    Map<String, Timeline> timelines = new LinkedHashMap<>();
    reader.beginObject();
    while (reader.hasNext()) {
      Timeline timeline = this.registry.createTimeline();
      timelines.put(reader.nextName(), timeline);
      reader.beginArray();
      while (reader.hasNext()) {
        Path path = timeline.createPath();
        reader.beginObject();
        List<Integer> segments = new ArrayList<>();
        List<Interpolator> interpolators = new ArrayList<>();
        while (reader.hasNext()) {
          switch (reader.nextName()) {
            case "keyframes":
              reader.beginArray();
              while (reader.hasNext()) {
                long time = 0L;
                Map<Property, Object> properties = new HashMap<>();
                reader.beginObject();
                while (reader.hasNext()) {
                  switch (reader.nextName()) {
                    case "time":
                      time = reader.nextLong();
                    case "properties":
                      reader.beginObject();
                      while (reader.hasNext()) {
                        String id = reader.nextName();
                        Property property = timeline.getProperty(id);
                        if (property == null)
                          throw new IOException("Unknown property: " + id); 
                        Object value = property.fromJson(reader);
                        properties.put(property, value);
                      } 
                      reader.endObject();
                  } 
                } 
                reader.endObject();
                Keyframe keyframe = path.insert(time);
                for (Map.Entry<Property, Object> entry : properties.entrySet())
                  keyframe.setValue(entry.getKey(), entry.getValue()); 
              } 
              reader.endArray();
            case "segments":
              reader.beginArray();
              while (reader.hasNext()) {
                if (reader.peek() == JsonToken.NULL) {
                  reader.nextNull();
                  segments.add(null);
                  continue;
                } 
                segments.add(Integer.valueOf(reader.nextInt()));
              } 
              reader.endArray();
            case "interpolators":
              reader.beginArray();
              while (reader.hasNext()) {
                reader.beginObject();
                Interpolator interpolator = null;
                Set<String> properties = new HashSet<>();
                while (reader.hasNext()) {
                  switch (reader.nextName()) {
                    case "type":
                      interpolator = this.registry.deserializeInterpolator(reader);
                    case "properties":
                      reader.beginArray();
                      while (reader.hasNext())
                        properties.add(reader.nextString()); 
                      reader.endArray();
                  } 
                } 
                if (interpolator == null)
                  throw new IOException("Missing interpolator type"); 
                for (String propertyName : properties) {
                  Property property = timeline.getProperty(propertyName);
                  if (property == null)
                    throw new IOException("Timeline does not know property '" + propertyName + "'"); 
                  interpolator.registerProperty(property);
                } 
                interpolators.add(interpolator);
                reader.endObject();
              } 
              reader.endArray();
          } 
        } 
        Iterator<Integer> iter = segments.iterator();
        for (PathSegment segment : path.getSegments()) {
          Integer next = iter.next();
          if (next != null)
            segment.setInterpolator(interpolators.get(next.intValue())); 
        } 
        reader.endObject();
      } 
      reader.endArray();
    } 
    reader.endObject();
    return timelines;
  }
}
