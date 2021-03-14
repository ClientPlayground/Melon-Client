package com.replaymod.replaystudio;

import com.replaymod.replaystudio.filter.StreamFilter;

public interface Studio {
  String getName();
  
  int getVersion();
  
  StreamFilter loadStreamFilter(String paramString);
  
  boolean isCompatible(int paramInt1, int paramInt2, int paramInt3);
  
  int getCurrentFileFormatVersion();
}
