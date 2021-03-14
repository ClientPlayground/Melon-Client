package com.replaymod.replaystudio.pathing.property;

import com.replaymod.replaystudio.pathing.change.Change;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.NonNull;

public interface PropertyGroup {
  @NonNull
  String getLocalizedName();
  
  @NonNull
  String getId();
  
  @NonNull
  List<Property> getProperties();
  
  @NonNull
  Optional<Callable<Change>> getSetter();
}
