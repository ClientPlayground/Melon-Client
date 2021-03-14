package com.replaymod.replaystudio.pathing.path;

import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.property.Property;
import java.util.List;
import java.util.Optional;

public interface Timeline {
  List<Path> getPaths();
  
  Path createPath();
  
  <T> Optional<T> getValue(Property<T> paramProperty, long paramLong);
  
  void applyToGame(long paramLong, Object paramObject);
  
  void registerProperty(Property paramProperty);
  
  Property getProperty(String paramString);
  
  void applyChange(Change paramChange);
  
  void pushChange(Change paramChange);
  
  void undoLastChange();
  
  void redoLastChange();
  
  Change peekUndoStack();
  
  Change peekRedoStack();
}
