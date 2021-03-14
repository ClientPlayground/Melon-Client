package com.replaymod.replaystudio.pathing.serialize;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LegacyKeyframeSetAdapter extends TypeAdapter<LegacyTimelineConverter.KeyframeSet[]> {
  public LegacyTimelineConverter.KeyframeSet[] read(JsonReader in) throws IOException {
    List<LegacyTimelineConverter.KeyframeSet> sets = new ArrayList<>();
    in.beginArray();
    while (in.hasNext()) {
      LegacyTimelineConverter.KeyframeSet set = new LegacyTimelineConverter.KeyframeSet();
      List<LegacyTimelineConverter.Keyframe> positionKeyframes = new ArrayList<>();
      List<LegacyTimelineConverter.Keyframe> timeKeyframes = new ArrayList<>();
      in.beginObject();
      while (in.hasNext()) {
        String jsonTag = in.nextName();
        if ("name".equals(jsonTag)) {
          set.name = in.nextString();
          continue;
        } 
        if ("positionKeyframes".equals(jsonTag)) {
          in.beginArray();
          while (in.hasNext()) {
            LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.AdvancedPosition> newKeyframe = new LegacyTimelineConverter.Keyframe<>();
            Integer spectatedEntityID = null;
            in.beginObject();
            while (in.hasNext()) {
              String jsonKeyframeTag = in.nextName();
              if ("value".equals(jsonKeyframeTag) || "position".equals(jsonKeyframeTag)) {
                LegacyTimelineConverter.SpectatorData spectatorData = (LegacyTimelineConverter.SpectatorData)(new Gson()).fromJson(in, LegacyTimelineConverter.SpectatorData.class);
                if (spectatorData.spectatedEntityID != null) {
                  newKeyframe.value = (T)spectatorData;
                  continue;
                } 
                newKeyframe.value = (T)new LegacyTimelineConverter.AdvancedPosition();
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).x = spectatorData.x;
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).y = spectatorData.y;
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).z = spectatorData.z;
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).yaw = spectatorData.yaw;
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).pitch = spectatorData.pitch;
                ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).roll = spectatorData.roll;
                continue;
              } 
              if ("realTimestamp".equals(jsonKeyframeTag)) {
                newKeyframe.realTimestamp = in.nextInt();
                continue;
              } 
              if ("spectatedEntityID".equals(jsonKeyframeTag))
                spectatedEntityID = Integer.valueOf(in.nextInt()); 
            } 
            if (spectatedEntityID != null) {
              LegacyTimelineConverter.AdvancedPosition pos = (LegacyTimelineConverter.AdvancedPosition)newKeyframe.value;
              LegacyTimelineConverter.SpectatorData spectatorData = new LegacyTimelineConverter.SpectatorData();
              spectatorData.spectatedEntityID = spectatedEntityID;
              newKeyframe.value = (T)spectatorData;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).x = pos.x;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).y = pos.y;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).z = pos.z;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).yaw = pos.yaw;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).pitch = pos.pitch;
              ((LegacyTimelineConverter.AdvancedPosition)newKeyframe.value).roll = pos.roll;
            } 
            in.endObject();
            positionKeyframes.add(newKeyframe);
          } 
          in.endArray();
          continue;
        } 
        if ("timeKeyframes".equals(jsonTag)) {
          in.beginArray();
          while (in.hasNext()) {
            LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.TimestampValue> newKeyframe = new LegacyTimelineConverter.Keyframe<>();
            in.beginObject();
            while (in.hasNext()) {
              String jsonKeyframeTag = in.nextName();
              if ("timestamp".equals(jsonKeyframeTag)) {
                LegacyTimelineConverter.TimestampValue timestampValue = new LegacyTimelineConverter.TimestampValue();
                timestampValue.value = in.nextInt();
                newKeyframe.value = (T)timestampValue;
                continue;
              } 
              if ("value".equals(jsonKeyframeTag)) {
                newKeyframe.value = (T)(new Gson()).fromJson(in, LegacyTimelineConverter.TimestampValue.class);
                continue;
              } 
              if ("realTimestamp".equals(jsonKeyframeTag))
                newKeyframe.realTimestamp = in.nextInt(); 
            } 
            in.endObject();
            timeKeyframes.add(newKeyframe);
          } 
          in.endArray();
          continue;
        } 
        if ("customObjects".equals(jsonTag))
          set.customObjects = (LegacyTimelineConverter.CustomImageObject[])(new Gson()).fromJson(in, LegacyTimelineConverter.CustomImageObject[].class); 
      } 
      in.endObject();
      set.positionKeyframes = positionKeyframes.<LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.AdvancedPosition>>toArray((LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.AdvancedPosition>[])new LegacyTimelineConverter.Keyframe[positionKeyframes.size()]);
      set.timeKeyframes = timeKeyframes.<LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.TimestampValue>>toArray((LegacyTimelineConverter.Keyframe<LegacyTimelineConverter.TimestampValue>[])new LegacyTimelineConverter.Keyframe[timeKeyframes.size()]);
      sets.add(set);
    } 
    in.endArray();
    return sets.<LegacyTimelineConverter.KeyframeSet>toArray(new LegacyTimelineConverter.KeyframeSet[sets.size()]);
  }
  
  public void write(JsonWriter out, LegacyTimelineConverter.KeyframeSet[] value) throws IOException {}
}
