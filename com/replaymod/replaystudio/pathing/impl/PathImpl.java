package com.replaymod.replaystudio.pathing.impl;

import com.replaymod.replaystudio.pathing.interpolation.InterpolationParameters;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class PathImpl implements Path {
  private final Timeline timeline;
  
  private Map<Long, Keyframe> keyframes = new TreeMap<>();
  
  private List<PathSegment> segments = new LinkedList<>();
  
  private boolean active = true;
  
  public PathImpl(Timeline timeline) {
    this.timeline = timeline;
  }
  
  public Timeline getTimeline() {
    return this.timeline;
  }
  
  public Collection<Keyframe> getKeyframes() {
    return Collections.unmodifiableCollection(this.keyframes.values());
  }
  
  public Collection<PathSegment> getSegments() {
    return Collections.unmodifiableCollection(this.segments);
  }
  
  public void update() {
    update(false);
  }
  
  public void updateAll() {
    update(false);
  }
  
  private void update(boolean force) {
    Interpolator interpolator = null;
    Map<PropertyPart, InterpolationParameters> parameters = new HashMap<>();
    for (PathSegment segment : this.segments) {
      if (segment.getInterpolator() != interpolator) {
        interpolator = segment.getInterpolator();
        if (force || interpolator.isDirty())
          parameters = interpolator.bake(parameters); 
      } 
    } 
  }
  
  public <T> Optional<T> getValue(Property<T> property, long time) {
    PathSegment segment = getSegment(time);
    if (segment != null) {
      Interpolator interpolator = segment.getInterpolator();
      if (interpolator != null && 
        interpolator.getKeyframeProperties().contains(property))
        return interpolator.getValue(property, time); 
    } 
    return Optional.empty();
  }
  
  public Keyframe insert(long time) {
    Keyframe keyframe = new KeyframeImpl(time);
    insert(keyframe);
    return keyframe;
  }
  
  public Keyframe getKeyframe(long time) {
    return this.keyframes.get(Long.valueOf(time));
  }
  
  public void insert(Keyframe keyframe) {
    if (this.keyframes.containsKey(Long.valueOf(keyframe.getTime())))
      throw new IllegalStateException("A keyframe at " + keyframe.getTime() + " already exists."); 
    this.keyframes.put(Long.valueOf(keyframe.getTime()), keyframe);
    if (this.segments.isEmpty()) {
      if (this.keyframes.size() >= 2) {
        Iterator<Keyframe> iterator = this.keyframes.values().iterator();
        this.segments.add(new PathSegmentImpl(iterator.next(), iterator.next()));
      } 
      return;
    } 
    ListIterator<PathSegment> iter = this.segments.listIterator();
    PathSegment next = iter.next();
    if (keyframe.getTime() < next.getStartKeyframe().getTime()) {
      iter.previous();
      iter.add(new PathSegmentImpl(keyframe, next.getStartKeyframe(), next.getInterpolator()));
      return;
    } 
    while (true) {
      if (next.getStartKeyframe().getTime() <= keyframe.getTime() && next
        .getEndKeyframe().getTime() >= keyframe.getTime()) {
        iter.remove();
        iter.add(new PathSegmentImpl(next.getStartKeyframe(), keyframe, next.getInterpolator()));
        iter.add(new PathSegmentImpl(keyframe, next.getEndKeyframe(), next.getInterpolator()));
        next.setInterpolator(null);
        return;
      } 
      if (iter.hasNext()) {
        next = iter.next();
        continue;
      } 
      break;
    } 
    iter.add(new PathSegmentImpl(next.getEndKeyframe(), keyframe, next.getInterpolator()));
  }
  
  public void remove(Keyframe keyframe, boolean useFirstInterpolator) {
    if (this.keyframes.get(Long.valueOf(keyframe.getTime())) != keyframe)
      throw new IllegalArgumentException("The keyframe " + keyframe + " is not part of this path."); 
    this.keyframes.remove(Long.valueOf(keyframe.getTime()));
    if (this.segments.size() < 2) {
      for (PathSegment segment : this.segments)
        segment.setInterpolator(null); 
      this.segments.clear();
      return;
    } 
    ListIterator<PathSegment> iter = this.segments.listIterator();
    while (iter.hasNext()) {
      PathSegment next = iter.next();
      if (next.getEndKeyframe() == keyframe) {
        iter.remove();
        if (iter.hasNext()) {
          PathSegment next2 = iter.next();
          iter.remove();
          iter.add(new PathSegmentImpl(next.getStartKeyframe(), next2.getEndKeyframe(), (useFirstInterpolator ? next : next2)
                .getInterpolator()));
          next2.setInterpolator(null);
        } 
        next.setInterpolator(null);
        return;
      } 
      if (next.getStartKeyframe() == keyframe) {
        next.setInterpolator(null);
        iter.remove();
        return;
      } 
    } 
    throw new AssertionError("No segment for keyframe found!");
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public boolean isActive() {
    return this.active;
  }
  
  private PathSegment getSegment(long time) {
    for (PathSegment segment : this.segments) {
      if (segment.getStartKeyframe().getTime() <= time && segment.getEndKeyframe().getTime() >= time)
        return segment; 
    } 
    return null;
  }
}
