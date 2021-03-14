package com.replaymod.replaystudio.studio;

import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.viaversion.ViaVersionPacketConverter;
import java.util.ServiceLoader;

public class ReplayStudio implements Studio {
  private final ServiceLoader<StreamFilter> streamFilterServiceLoader = ServiceLoader.load(StreamFilter.class);
  
  public String getName() {
    return "ReplayStudio";
  }
  
  public int getVersion() {
    return 1;
  }
  
  public StreamFilter loadStreamFilter(String name) {
    for (StreamFilter filter : this.streamFilterServiceLoader) {
      if (filter.getName().equalsIgnoreCase(name))
        try {
          return (StreamFilter)filter.getClass().newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
          throw new RuntimeException(e);
        }  
    } 
    return null;
  }
  
  public boolean isCompatible(int fileVersion, int protocolVersion, int currentVersion) {
    return ViaVersionPacketConverter.isFileVersionSupported(fileVersion, protocolVersion, currentVersion);
  }
  
  public int getCurrentFileFormatVersion() {
    return 14;
  }
}
