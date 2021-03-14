package com.replaymod.replaystudio.pathing.impl;

import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TimelineImpl implements Timeline {
  private final List<Path> paths = new ArrayList<>();
  
  private Map<String, Property> properties = new HashMap<>();
  
  private Deque<Change> undoStack = new ArrayDeque<>();
  
  private Deque<Change> redoStack = new ArrayDeque<>();
  
  public List<Path> getPaths() {
    return this.paths;
  }
  
  public Path createPath() {
    Path path = new PathImpl(this);
    this.paths.add(path);
    return path;
  }
  
  public <T> Optional<T> getValue(Property<T> property, long time) {
    for (Path path : this.paths) {
      if (path.isActive()) {
        Optional<T> value = path.getValue(property, time);
        if (value.isPresent())
          return value; 
      } 
    } 
    return Optional.empty();
  }
  
  public void applyToGame(long time, Object replayHandler) {
    for (Property<?> property : this.properties.values())
      applyToGame(time, replayHandler, property); 
  }
  
  private <T> void applyToGame(long time, Object replayHandler, Property<T> property) {
    Optional<T> value = getValue(property, time);
    if (value.isPresent())
      property.applyToGame(value.get(), replayHandler); 
  }
  
  public void registerProperty(Property property) {
    String id = ((property.getGroup() == null) ? "" : (property.getGroup().getId() + ":")) + property.getId();
    this.properties.put(id, property);
  }
  
  public Property getProperty(String id) {
    return this.properties.get(id);
  }
  
  public void applyChange(Change change) {
    change.apply(this);
    pushChange(change);
  }
  
  public void pushChange(Change change) {
    this.undoStack.push(change);
    this.redoStack.clear();
  }
  
  public void undoLastChange() {
    Change change = this.undoStack.pop();
    change.undo(this);
    this.redoStack.push(change);
  }
  
  public void redoLastChange() {
    Change change = this.redoStack.pop();
    change.apply(this);
    this.undoStack.push(change);
  }
  
  public Change peekUndoStack() {
    return this.undoStack.peek();
  }
  
  public Change peekRedoStack() {
    return this.undoStack.peek();
  }
}
