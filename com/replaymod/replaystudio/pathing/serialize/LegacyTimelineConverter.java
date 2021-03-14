package com.replaymod.replaystudio.pathing.serialize;

import com.google.common.base.Optional;
import com.google.gson.GsonBuilder;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Triple;

public class LegacyTimelineConverter {
  public static Map<String, Timeline> convert(PathingRegistry registry, ReplayFile replayFile) throws IOException {
    KeyframeSet[] keyframeSets = readAndParse(replayFile);
    if (keyframeSets == null)
      return Collections.emptyMap(); 
    Map<String, Timeline> timelines = new LinkedHashMap<>();
    for (KeyframeSet keyframeSet : keyframeSets)
      timelines.put(keyframeSet.name, convert(registry, keyframeSet)); 
    return timelines;
  }
  
  private static Optional<InputStream> read(ReplayFile replayFile) throws IOException {
    Optional<InputStream> in = replayFile.get("paths.json");
    if (!in.isPresent())
      in = replayFile.get("paths"); 
    return in;
  }
  
  private static KeyframeSet[] parse(InputStream in) {
    return (KeyframeSet[])(new GsonBuilder())
      .registerTypeAdapter(KeyframeSet[].class, new LegacyKeyframeSetAdapter())
      .create().fromJson(new InputStreamReader(in), KeyframeSet[].class);
  }
  
  private static KeyframeSet[] readAndParse(ReplayFile replayFile) throws IOException {
    KeyframeSet[] keyframeSets;
    Optional<InputStream> optIn = read(replayFile);
    if (!optIn.isPresent())
      return null; 
    try (InputStream in = (InputStream)optIn.get()) {
      keyframeSets = parse(in);
    } 
    return keyframeSets;
  }
  
  private static Timeline convert(PathingRegistry registry, KeyframeSet keyframeSet) {
    Timeline timeline = registry.createTimeline();
    Property timestamp = timeline.getProperty("timestamp");
    Property cameraPosition = timeline.getProperty("camera:position");
    Property cameraRotation = timeline.getProperty("camera:rotation");
    Path timePath = timeline.createPath();
    Path positionPath = timeline.createPath();
    for (Keyframe<AdvancedPosition> positionKeyframe : keyframeSet.positionKeyframes) {
      AdvancedPosition value = (AdvancedPosition)positionKeyframe.value;
      com.replaymod.replaystudio.pathing.path.Keyframe keyframe = getKeyframe(positionPath, positionKeyframe.realTimestamp);
      keyframe.setValue(cameraPosition, Triple.of(Double.valueOf(value.x), Double.valueOf(value.y), Double.valueOf(value.z)));
      keyframe.setValue(cameraRotation, Triple.of(Float.valueOf(value.yaw), Float.valueOf(value.pitch), Float.valueOf(value.roll)));
      if (value instanceof SpectatorData);
    } 
    for (Keyframe<TimestampValue> timeKeyframe : keyframeSet.timeKeyframes) {
      TimestampValue value = (TimestampValue)timeKeyframe.value;
      com.replaymod.replaystudio.pathing.path.Keyframe keyframe = getKeyframe(timePath, timeKeyframe.realTimestamp);
      keyframe.setValue(timestamp, Integer.valueOf((int)value.value));
    } 
    LinearInterpolator linearInterpolator = new LinearInterpolator();
    linearInterpolator.registerProperty(timestamp);
    timePath.getSegments().forEach(s -> s.setInterpolator(timeInterpolator));
    CubicSplineInterpolator cubicSplineInterpolator = new CubicSplineInterpolator();
    cubicSplineInterpolator.registerProperty(cameraPosition);
    cubicSplineInterpolator.registerProperty(cameraRotation);
    positionPath.getSegments().forEach(s -> s.setInterpolator(positionInterpolator));
    return timeline;
  }
  
  private static com.replaymod.replaystudio.pathing.path.Keyframe getKeyframe(Path path, long time) {
    com.replaymod.replaystudio.pathing.path.Keyframe keyframe = path.getKeyframe(time);
    if (keyframe == null)
      keyframe = path.insert(time); 
    return keyframe;
  }
  
  static class KeyframeSet {
    String name;
    
    LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.AdvancedPosition>[] positionKeyframes;
    
    LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.TimestampValue>[] timeKeyframes;
    
    LegacyTimelineConverter.CustomImageObject[] customObjects;
  }
  
  static class Keyframe<T> {
    int realTimestamp;
    
    T value;
  }
  
  static class Position {
    double x;
    
    double y;
    
    double z;
  }
  
  static class AdvancedPosition extends Position {
    float pitch;
    
    float yaw;
    
    float roll;
  }
  
  static class SpectatorData extends AdvancedPosition {
    Integer spectatedEntityID;
    
    SpectatingMethod spectatingMethod;
    
    LegacyTimelineConverter.SpectatorDataThirdPersonInfo thirdPersonInfo;
    
    enum SpectatingMethod {
      FIRST_PERSON, SHOULDER_CAM;
    }
  }
  
  static class SpectatorDataThirdPersonInfo {
    double shoulderCamDistance;
    
    double shoulderCamPitchOffset;
    
    double shoulderCamYawOffset;
    
    double shoulderCamSmoothness;
  }
  
  static class TimestampValue {
    double value;
  }
  
  static class CustomImageObject {
    String name;
    
    UUID linkedAsset;
    
    float width;
    
    float height;
    
    float textureWidth;
    
    float textureHeight;
    
    LegacyTimelineConverter.Transformations transformations = new LegacyTimelineConverter.Transformations();
  }
  
  static class Transformations {
    LegacyTimelineConverter.Position defaultAnchor;
    
    LegacyTimelineConverter.Position defaultPosition;
    
    LegacyTimelineConverter.Position defaultOrientation;
    
    LegacyTimelineConverter.Position defaultScale;
    
    LegacyTimelineConverter.NumberValue defaultOpacity;
    
    List<LegacyTimelineConverter.Position> anchorKeyframes;
    
    List<LegacyTimelineConverter.Position> positionKeyframes;
    
    List<LegacyTimelineConverter.Position> orientationKeyframes;
    
    List<LegacyTimelineConverter.Position> scaleKeyframes;
    
    List<LegacyTimelineConverter.NumberValue> opacityKeyframes;
  }
  
  static class NumberValue {
    double value;
  }
}
