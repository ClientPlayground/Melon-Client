package net.minecraft.client.stream;

import tv.twitch.ErrorCode;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.chat.ChatUserInfo;

public interface IStream {
  void shutdownStream();
  
  void func_152935_j();
  
  void func_152922_k();
  
  boolean func_152936_l();
  
  boolean isReadyToBroadcast();
  
  boolean isBroadcasting();
  
  void func_152911_a(Metadata paramMetadata, long paramLong);
  
  void func_176026_a(Metadata paramMetadata, long paramLong1, long paramLong2);
  
  boolean isPaused();
  
  void requestCommercial();
  
  void pause();
  
  void unpause();
  
  void updateStreamVolume();
  
  void func_152930_t();
  
  void stopBroadcasting();
  
  IngestServer[] func_152925_v();
  
  void func_152909_x();
  
  IngestServerTester func_152932_y();
  
  boolean func_152908_z();
  
  int func_152920_A();
  
  boolean func_152927_B();
  
  String func_152921_C();
  
  ChatUserInfo func_152926_a(String paramString);
  
  void func_152917_b(String paramString);
  
  boolean func_152928_D();
  
  ErrorCode func_152912_E();
  
  boolean func_152913_F();
  
  void muteMicrophone(boolean paramBoolean);
  
  boolean func_152929_G();
  
  AuthFailureReason func_152918_H();
  
  public enum AuthFailureReason {
    ERROR, INVALID_TOKEN;
  }
}
