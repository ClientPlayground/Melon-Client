package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.change.AddKeyframe;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.change.CombinedChange;
import com.replaymod.replaystudio.pathing.change.RemoveKeyframe;
import com.replaymod.replaystudio.pathing.change.SetInterpolator;
import com.replaymod.replaystudio.pathing.change.UpdateKeyframeProperties;
import com.replaymod.replaystudio.pathing.impl.TimelineImpl;
import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.replaystudio.util.Location;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.CameraProperties;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.ExplicitInterpolationProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.SpectatorProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.TimestampProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;

public class SPTimeline implements PathingRegistry {
  private final Timeline timeline;
  
  private final Path timePath;
  
  private final Path positionPath;
  
  private EntityPositionTracker entityTracker;
  
  private InterpolatorType defaultInterpolatorType;
  
  public enum SPPath {
    TIME, POSITION;
  }
  
  public Timeline getTimeline() {
    return this.timeline;
  }
  
  public Path getTimePath() {
    return this.timePath;
  }
  
  public Path getPositionPath() {
    return this.positionPath;
  }
  
  public EntityPositionTracker getEntityTracker() {
    return this.entityTracker;
  }
  
  public SPTimeline() {
    this(createInitialTimeline());
  }
  
  public SPTimeline(Timeline timeline) {
    this.timeline = timeline;
    this.timePath = timeline.getPaths().get(SPPath.TIME.ordinal());
    this.positionPath = timeline.getPaths().get(SPPath.POSITION.ordinal());
  }
  
  public Path getPath(SPPath path) {
    switch (path) {
      case STRING:
        return getTimePath();
      case BEGIN_OBJECT:
        return getPositionPath();
    } 
    throw new IllegalArgumentException("Unknown path " + path);
  }
  
  public Keyframe getKeyframe(SPPath path, long keyframe) {
    return getPath(path).getKeyframe(keyframe);
  }
  
  public void setEntityTracker(EntityPositionTracker entityTracker) {
    Preconditions.checkState((this.entityTracker == null), "Entity tracker already set");
    this.entityTracker = entityTracker;
  }
  
  public void setDefaultInterpolatorType(InterpolatorType defaultInterpolatorType) {
    Validate.isTrue((defaultInterpolatorType != InterpolatorType.DEFAULT), "Must not be DEFAULT", new Object[0]);
    InterpolatorType prevType = this.defaultInterpolatorType;
    this.defaultInterpolatorType = (InterpolatorType)Validate.notNull(defaultInterpolatorType);
    Client.log("Updated default interpolator type to " + defaultInterpolatorType);
    if (prevType != null && prevType != this.defaultInterpolatorType)
      this.timeline.pushChange(updateInterpolators()); 
  }
  
  public Change setDefaultInterpolator(Interpolator interpolator) {
    Preconditions.checkState((this.defaultInterpolatorType != null), "Default interpolator type not set.");
    Validate.isInstanceOf(this.defaultInterpolatorType.getInterpolatorClass(), interpolator);
    registerPositionInterpolatorProperties(interpolator);
    CombinedChange combinedChange = CombinedChange.create((Change[])this.positionPath
        .getSegments().stream()
        
        .filter(s -> !s.getStartKeyframe().getValue((Property)ExplicitInterpolationProperty.PROPERTY).isPresent())
        
        .filter(s -> !isSpectatorSegment(s))
        
        .map(s -> SetInterpolator.create(s, interpolator)).toArray(x$0 -> new Change[x$0]));
    combinedChange.apply(this.timeline);
    return (Change)CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, updateInterpolators() });
  }
  
  public boolean isTimeKeyframe(long time) {
    return (this.timePath.getKeyframe(time) != null);
  }
  
  public boolean isPositionKeyframe(long time) {
    return (this.positionPath.getKeyframe(time) != null);
  }
  
  public boolean isSpectatorKeyframe(long time) {
    Keyframe keyframe = this.positionPath.getKeyframe(time);
    return (keyframe != null && keyframe.getValue((Property)SpectatorProperty.PROPERTY).isPresent());
  }
  
  public void addPositionKeyframe(long time, double posX, double posY, double posZ, float yaw, float pitch, float roll, int spectated) {
    Client.debug("Adding position keyframe at {} pos {}/{}/{} rot {}/{}/{} entId {}", new Object[] { Long.valueOf(time), Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ), Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll), Integer.valueOf(spectated) });
    Path path = this.positionPath;
    Preconditions.checkState((this.positionPath.getKeyframe(time) == null), "Keyframe already exists");
    AddKeyframe addKeyframe = AddKeyframe.create(path, time);
    addKeyframe.apply(this.timeline);
    Keyframe keyframe = path.getKeyframe(time);
    UpdateKeyframeProperties.Builder builder = UpdateKeyframeProperties.create(path, keyframe);
    builder.setValue((Property)CameraProperties.POSITION, Triple.of(Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ)));
    builder.setValue((Property)CameraProperties.ROTATION, Triple.of(Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll)));
    if (spectated != -1)
      builder.setValue((Property)SpectatorProperty.PROPERTY, Integer.valueOf(spectated)); 
    UpdateKeyframeProperties updateChange = builder.done();
    updateChange.apply(this.timeline);
    CombinedChange combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)addKeyframe, (Change)updateChange });
    if (path.getSegments().size() == 1) {
      PathSegment segment = path.getSegments().iterator().next();
      Interpolator interpolator = createDefaultInterpolator();
      SetInterpolator setInterpolator = SetInterpolator.create(segment, interpolator);
      setInterpolator.apply(this.timeline);
      combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, (Change)setInterpolator });
    } 
    combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, updateInterpolators() });
    Change specPosUpdate = updateSpectatorPositions();
    specPosUpdate.apply(this.timeline);
    combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, specPosUpdate });
    this.timeline.pushChange((Change)combinedChange);
  }
  
  public Change updatePositionKeyframe(long time, double posX, double posY, double posZ, float yaw, float pitch, float roll) {
    Client.debug("Updating position keyframe at {} to pos {}/{}/{} rot {}/{}/{}", new Object[] { Long.valueOf(time), Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ), Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll) });
    Keyframe keyframe = this.positionPath.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "Keyframe does not exists");
    Preconditions.checkState(!keyframe.getValue((Property)SpectatorProperty.PROPERTY).isPresent(), "Cannot update spectator keyframe");
    UpdateKeyframeProperties updateKeyframeProperties = UpdateKeyframeProperties.create(this.positionPath, keyframe).setValue((Property)CameraProperties.POSITION, Triple.of(Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ))).setValue((Property)CameraProperties.ROTATION, Triple.of(Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(roll))).done();
    updateKeyframeProperties.apply(this.timeline);
    return (Change)updateKeyframeProperties;
  }
  
  public void removePositionKeyframe(long time) {
    Client.debug("Removing position keyframe at {}", new Object[] { Long.valueOf(time) });
    Path path = this.positionPath;
    Keyframe keyframe = path.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "No keyframe at that time");
    RemoveKeyframe removeKeyframe = RemoveKeyframe.create(path, keyframe);
    removeKeyframe.apply(this.timeline);
    CombinedChange combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)removeKeyframe, updateInterpolators() });
    Change specPosUpdate = updateSpectatorPositions();
    specPosUpdate.apply(this.timeline);
    combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, specPosUpdate });
    this.timeline.pushChange((Change)combinedChange);
  }
  
  public void addTimeKeyframe(long time, int replayTime) {
    Client.debug("Adding time keyframe at {} time {}", new Object[] { Long.valueOf(time), Integer.valueOf(replayTime) });
    Path path = this.timePath;
    Preconditions.checkState((path.getKeyframe(time) == null), "Keyframe already exists");
    AddKeyframe addKeyframe = AddKeyframe.create(path, time);
    addKeyframe.apply(this.timeline);
    Keyframe keyframe = path.getKeyframe(time);
    UpdateKeyframeProperties updateChange = UpdateKeyframeProperties.create(path, keyframe).setValue((Property)TimestampProperty.PROPERTY, Integer.valueOf(replayTime)).done();
    updateChange.apply(this.timeline);
    CombinedChange combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)addKeyframe, (Change)updateChange });
    if (path.getSegments().size() == 1) {
      PathSegment segment = path.getSegments().iterator().next();
      LinearInterpolator linearInterpolator = new LinearInterpolator();
      linearInterpolator.registerProperty((Property)TimestampProperty.PROPERTY);
      SetInterpolator setInterpolator = SetInterpolator.create(segment, (Interpolator)linearInterpolator);
      setInterpolator.apply(this.timeline);
      combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, (Change)setInterpolator });
    } 
    Change specPosUpdate = updateSpectatorPositions();
    specPosUpdate.apply(this.timeline);
    combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, specPosUpdate });
    this.timeline.pushChange((Change)combinedChange);
  }
  
  public Change updateTimeKeyframe(long time, int replayTime) {
    Client.debug("Updating time keyframe at {} to time {}", new Object[] { Long.valueOf(time), Integer.valueOf(replayTime) });
    Keyframe keyframe = this.timePath.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "Keyframe does not exists");
    UpdateKeyframeProperties updateKeyframeProperties = UpdateKeyframeProperties.create(this.timePath, keyframe).setValue((Property)TimestampProperty.PROPERTY, Integer.valueOf(replayTime)).done();
    updateKeyframeProperties.apply(this.timeline);
    return (Change)updateKeyframeProperties;
  }
  
  public void removeTimeKeyframe(long time) {
    Client.debug("Removing time keyframe at {}", new Object[] { Long.valueOf(time) });
    Path path = this.timePath;
    Keyframe keyframe = path.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "No keyframe at that time");
    RemoveKeyframe removeKeyframe = RemoveKeyframe.create(path, keyframe);
    removeKeyframe.apply(this.timeline);
    Change specPosUpdate = updateSpectatorPositions();
    specPosUpdate.apply(this.timeline);
    CombinedChange combinedChange = CombinedChange.createFromApplied(new Change[] { (Change)removeKeyframe, specPosUpdate });
    this.timeline.pushChange((Change)combinedChange);
  }
  
  public Change setInterpolatorToDefault(long time) {
    Client.debug("Setting interpolator of position keyframe at {} to the default", new Object[] { Long.valueOf(time) });
    Keyframe keyframe = this.positionPath.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "Keyframe does not exists");
    UpdateKeyframeProperties updateKeyframeProperties = UpdateKeyframeProperties.create(this.positionPath, keyframe).removeProperty((Property)ExplicitInterpolationProperty.PROPERTY).done();
    updateKeyframeProperties.apply(this.timeline);
    return (Change)CombinedChange.createFromApplied(new Change[] { (Change)updateKeyframeProperties, updateInterpolators() });
  }
  
  public Change setInterpolator(long time, Interpolator interpolator) {
    Client.debug("Setting interpolator of position keyframe at {} to {}", new Object[] { Long.valueOf(time), interpolator });
    Keyframe keyframe = this.positionPath.getKeyframe(time);
    Preconditions.checkState((keyframe != null), "Keyframe does not exists");
    PathSegment segment = (PathSegment)this.positionPath.getSegments().stream().filter(s -> (s.getStartKeyframe() == keyframe)).findFirst().orElseThrow(() -> new IllegalStateException("Keyframe has no following segment."));
    registerPositionInterpolatorProperties(interpolator);
    CombinedChange combinedChange = CombinedChange.create(new Change[] { (Change)UpdateKeyframeProperties.create(this.positionPath, keyframe)
          .setValue((Property)ExplicitInterpolationProperty.PROPERTY, ObjectUtils.NULL)
          .done(), 
          (Change)SetInterpolator.create(segment, interpolator) });
    combinedChange.apply(this.timeline);
    return (Change)CombinedChange.createFromApplied(new Change[] { (Change)combinedChange, updateInterpolators() });
  }
  
  public Change moveKeyframe(SPPath spPath, long oldTime, long newTime) {
    Change restoreInterpolatorChange;
    CombinedChange combinedChange;
    Client.debug("Moving keyframe on {} from {} to {}", new Object[] { spPath, Long.valueOf(oldTime), Long.valueOf(newTime) });
    Path path = getPath(spPath);
    Keyframe keyframe = path.getKeyframe(oldTime);
    Preconditions.checkState((keyframe != null), "No keyframe at specified time");
    Optional<Interpolator> firstInterpolator = path.getSegments().stream().findFirst().map(PathSegment::getInterpolator);
    Optional<Interpolator> lostInterpolator = path.getSegments().stream().filter(s -> (Iterables.getLast(path.getKeyframes()) == keyframe) ? ((s.getEndKeyframe() == keyframe)) : ((s.getStartKeyframe() == keyframe))).findFirst().map(PathSegment::getInterpolator);
    RemoveKeyframe removeKeyframe = RemoveKeyframe.create(path, keyframe);
    removeKeyframe.apply(this.timeline);
    AddKeyframe addKeyframe = AddKeyframe.create(path, newTime);
    addKeyframe.apply(this.timeline);
    UpdateKeyframeProperties.Builder builder = UpdateKeyframeProperties.create(path, path.getKeyframe(newTime));
    for (Property<?> property : (Iterable<Property<?>>)keyframe.getProperties())
      copyProperty(property, keyframe, builder); 
    UpdateKeyframeProperties updateKeyframeProperties = builder.done();
    updateKeyframeProperties.apply(this.timeline);
    Keyframe newKf = path.getKeyframe(newTime);
    if (Iterables.getLast(path.getKeyframes()) != newKf) {
      restoreInterpolatorChange = lostInterpolator.<Change>flatMap(interpolator -> path.getSegments().stream().filter(()).findFirst().map(())).orElseGet(() -> CombinedChange.create(new Change[0]));
    } else {
      restoreInterpolatorChange = path.getSegments().stream().filter(s -> (s.getEndKeyframe() == newKf)).findFirst().flatMap(segment -> lostInterpolator.map(())).orElseGet(() -> CombinedChange.create(new Change[0]));
    } 
    restoreInterpolatorChange.apply(this.timeline);
    if (spPath == SPPath.POSITION) {
      Change interpolatorUpdateChange = updateInterpolators();
    } else {
      if (path.getSegments().size() == 1) {
        assert firstInterpolator.isPresent() : "One segment should have existed before as well";
        SetInterpolator setInterpolator = SetInterpolator.create(path.getSegments().iterator().next(), firstInterpolator.get());
      } else {
        combinedChange = CombinedChange.create(new Change[0]);
      } 
      combinedChange.apply(this.timeline);
    } 
    Change spectatorChange = updateSpectatorPositions();
    spectatorChange.apply(this.timeline);
    return (Change)CombinedChange.createFromApplied(new Change[] { (Change)removeKeyframe, (Change)addKeyframe, (Change)updateKeyframeProperties, restoreInterpolatorChange, (Change)combinedChange, spectatorChange });
  }
  
  private <T> void copyProperty(Property<T> property, Keyframe from, UpdateKeyframeProperties.Builder to) {
    from.getValue(property).ifPresent(value -> to.setValue(property, value));
  }
  
  private Change updateInterpolators() {
    Collection<PathSegment> pathSegments = this.positionPath.getSegments();
    Map<PathSegment, Interpolator> updates = new HashMap<>();
    Interpolator interpolator = null;
    for (PathSegment segment : pathSegments) {
      if (isSpectatorSegment(segment)) {
        LinearInterpolator linearInterpolator;
        if (interpolator == null) {
          linearInterpolator = new LinearInterpolator();
          linearInterpolator.registerProperty((Property)SpectatorProperty.PROPERTY);
        } 
        updates.put(segment, linearInterpolator);
        continue;
      } 
      interpolator = null;
    } 
    pathSegments.stream()
      
      .filter(s -> !s.getStartKeyframe().getValue((Property)ExplicitInterpolationProperty.PROPERTY).isPresent())
      
      .filter(s -> !isSpectatorSegment(s))
      
      .filter(s -> !s.getInterpolator().getClass().equals(this.defaultInterpolatorType.getInterpolatorClass()))
      
      .forEach(segment -> (Interpolator)updates.put(segment, createDefaultInterpolator()));
    Interpolator lastInterpolator = null;
    Set<Interpolator> used = Collections.newSetFromMap(new IdentityHashMap<>());
    for (PathSegment segment : pathSegments) {
      if (isSpectatorSegment(segment)) {
        lastInterpolator = null;
        continue;
      } 
      Interpolator currentInterpolator = updates.getOrDefault(segment, segment.getInterpolator());
      if (lastInterpolator == currentInterpolator)
        continue; 
      if (!used.add(interpolator)) {
        currentInterpolator = cloneInterpolator(currentInterpolator);
        updates.put(segment, currentInterpolator);
      } 
      lastInterpolator = currentInterpolator;
    } 
    lastInterpolator = null;
    String lastInterpolatorSerialized = null;
    for (PathSegment segment : pathSegments) {
      if (isSpectatorSegment(segment)) {
        lastInterpolator = null;
        lastInterpolatorSerialized = null;
        continue;
      } 
      Interpolator currentInterpolator = updates.getOrDefault(segment, segment.getInterpolator());
      String serialized = serializeInterpolator(currentInterpolator);
      if (lastInterpolator != currentInterpolator && serialized.equals(lastInterpolatorSerialized)) {
        updates.put(segment, lastInterpolator);
        continue;
      } 
      lastInterpolator = currentInterpolator;
      lastInterpolatorSerialized = serialized;
    } 
    CombinedChange combinedChange = CombinedChange.create((Change[])updates.entrySet().stream()
        .map(e -> SetInterpolator.create((PathSegment)e.getKey(), (Interpolator)e.getValue())).toArray(x$0 -> new Change[x$0]));
    combinedChange.apply(this.timeline);
    return (Change)combinedChange;
  }
  
  private boolean isSpectatorSegment(PathSegment segment) {
    return (segment.getStartKeyframe().getValue((Property)SpectatorProperty.PROPERTY).isPresent() && segment
      .getEndKeyframe().getValue((Property)SpectatorProperty.PROPERTY).isPresent());
  }
  
  private Change updateSpectatorPositions() {
    if (this.entityTracker == null)
      return (Change)CombinedChange.create(new Change[0]); 
    List<Change> changes = new ArrayList<>();
    this.timePath.updateAll();
    for (Keyframe keyframe : this.positionPath.getKeyframes()) {
      Optional<Integer> spectator = keyframe.getValue((Property)SpectatorProperty.PROPERTY);
      if (spectator.isPresent()) {
        Optional<Integer> time = this.timePath.getValue((Property)TimestampProperty.PROPERTY, keyframe.getTime());
        if (!time.isPresent())
          continue; 
        Location expected = this.entityTracker.getEntityPositionAtTimestamp(((Integer)spectator.get()).intValue(), ((Integer)time.get()).intValue());
        if (expected == null)
          continue; 
        Triple<Double, Double, Double> pos = keyframe.getValue((Property)CameraProperties.POSITION).orElse(Triple.of(Double.valueOf(0.0D), Double.valueOf(0.0D), Double.valueOf(0.0D)));
        Triple<Float, Float, Float> rot = keyframe.getValue((Property)CameraProperties.ROTATION).orElse(Triple.of(Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)));
        Location actual = new Location(((Double)pos.getLeft()).doubleValue(), ((Double)pos.getMiddle()).doubleValue(), ((Double)pos.getRight()).doubleValue(), ((Float)rot.getLeft()).floatValue(), ((Float)rot.getRight()).floatValue());
        if (!expected.equals(actual))
          changes.add(UpdateKeyframeProperties.create(this.positionPath, keyframe)
              .setValue((Property)CameraProperties.POSITION, Triple.of(Double.valueOf(expected.getX()), Double.valueOf(expected.getY()), Double.valueOf(expected.getZ())))
              .setValue((Property)CameraProperties.ROTATION, Triple.of(Float.valueOf(expected.getYaw()), Float.valueOf(expected.getPitch()), Float.valueOf(0.0F))).done()); 
      } 
    } 
    return (Change)CombinedChange.create(changes.<Change>toArray(new Change[changes.size()]));
  }
  
  private Interpolator createDefaultInterpolator() {
    Client.log(this.defaultInterpolatorType + " Type");
    return registerPositionInterpolatorProperties(this.defaultInterpolatorType.newInstance());
  }
  
  private Interpolator registerPositionInterpolatorProperties(Interpolator interpolator) {
    interpolator.registerProperty((Property)CameraProperties.POSITION);
    interpolator.registerProperty((Property)CameraProperties.ROTATION);
    return interpolator;
  }
  
  public Timeline createTimeline() {
    return createTimelineStatic();
  }
  
  private static Timeline createInitialTimeline() {
    Timeline timeline = createTimelineStatic();
    timeline.createPath();
    timeline.createPath();
    return timeline;
  }
  
  private static Timeline createTimelineStatic() {
    TimelineImpl timelineImpl = new TimelineImpl();
    timelineImpl.registerProperty((Property)TimestampProperty.PROPERTY);
    timelineImpl.registerProperty((Property)CameraProperties.POSITION);
    timelineImpl.registerProperty((Property)CameraProperties.ROTATION);
    timelineImpl.registerProperty((Property)SpectatorProperty.PROPERTY);
    timelineImpl.registerProperty((Property)ExplicitInterpolationProperty.PROPERTY);
    return (Timeline)timelineImpl;
  }
  
  public void serializeInterpolator(JsonWriter writer, Interpolator interpolator) throws IOException {
    if (interpolator instanceof LinearInterpolator) {
      writer.value("linear");
    } else if (interpolator instanceof CubicSplineInterpolator) {
      writer.value("cubic-spline");
    } else if (interpolator instanceof CatmullRomSplineInterpolator) {
      writer.beginObject();
      writer.name("type").value("catmull-rom-spline");
      writer.name("alpha").value(((CatmullRomSplineInterpolator)interpolator).getAlpha());
      writer.endObject();
    } else {
      throw new IOException("Unknown interpolator type: " + interpolator);
    } 
  }
  
  public Interpolator deserializeInterpolator(JsonReader reader) throws IOException {
    String type;
    JsonObject args;
    switch (reader.peek()) {
      case STRING:
        type = reader.nextString();
        args = null;
        break;
      case BEGIN_OBJECT:
        args = (new JsonParser()).parse(reader).getAsJsonObject();
        type = args.get("type").getAsString();
        break;
      default:
        throw new IOException("Unexpected token: " + reader.peek());
    } 
    switch (type) {
      case "linear":
        return (Interpolator)new LinearInterpolator();
      case "cubic-spline":
        return (Interpolator)new CubicSplineInterpolator();
      case "catmull-rom-spline":
        if (args == null || !args.has("alpha"))
          throw new IOException("Missing alpha value for catmull-rom-spline."); 
        return (Interpolator)new CatmullRomSplineInterpolator(args.get("alpha").getAsDouble());
    } 
    throw new IOException("Unknown interpolation type: " + type);
  }
  
  private Interpolator cloneInterpolator(Interpolator interpolator) {
    Interpolator cloned = deserializeInterpolator(serializeInterpolator(interpolator));
    interpolator.getKeyframeProperties().forEach(cloned::registerProperty);
    return cloned;
  }
  
  private String serializeInterpolator(Interpolator interpolator) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter jsonWriter = new JsonWriter(new PrintWriter(baos));
    try {
      jsonWriter.beginArray();
      serializeInterpolator(jsonWriter, interpolator);
      jsonWriter.endArray();
      jsonWriter.flush();
    } catch (IOException e) {
      CrashReport crash = CrashReport.makeCrashReport(e, "Serializing interpolator");
      CrashReportCategory category = crash.makeCategory("Serializing interpolator");
      category.addCrashSectionCallable("Interpolator", interpolator::toString);
      Minecraft.getMinecraft().displayCrashReport(crash);
    } 
    return baos.toString();
  }
  
  private Interpolator deserializeInterpolator(String json) {
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    try {
      jsonReader.beginArray();
      return deserializeInterpolator(jsonReader);
    } catch (IOException e) {
      CrashReport crash = CrashReport.makeCrashReport(e, "De-serializing interpolator");
      CrashReportCategory category = crash.makeCategory("De-serializing interpolator");
      category.addCrashSectionCallable("Interpolator", json::toString);
      Minecraft.getMinecraft().displayCrashReport(crash);
      return null;
    } 
  }
}
