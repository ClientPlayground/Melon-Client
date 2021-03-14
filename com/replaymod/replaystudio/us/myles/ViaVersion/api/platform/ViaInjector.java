package com.replaymod.replaystudio.us.myles.ViaVersion.api.platform;

import com.google.gson.JsonObject;

public interface ViaInjector {
  void inject() throws Exception;
  
  void uninject() throws Exception;
  
  int getServerProtocolVersion() throws Exception;
  
  String getEncoderName();
  
  String getDecoderName();
  
  JsonObject getDump();
}
