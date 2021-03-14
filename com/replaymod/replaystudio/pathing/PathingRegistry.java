package com.replaymod.replaystudio.pathing;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Timeline;
import java.io.IOException;

public interface PathingRegistry {
  Timeline createTimeline();
  
  void serializeInterpolator(JsonWriter paramJsonWriter, Interpolator paramInterpolator) throws IOException;
  
  Interpolator deserializeInterpolator(JsonReader paramJsonReader) throws IOException;
}
