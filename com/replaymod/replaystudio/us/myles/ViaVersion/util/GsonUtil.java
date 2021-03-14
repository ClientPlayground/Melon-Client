package com.replaymod.replaystudio.us.myles.ViaVersion.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {
  private GsonUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
  
  private static final Gson gson = getGsonBuilder().create();
  
  public static Gson getGson() {
    return gson;
  }
  
  public static GsonBuilder getGsonBuilder() {
    return new GsonBuilder();
  }
}
