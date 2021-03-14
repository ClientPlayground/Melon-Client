package com.replaymod.replaystudio.us.myles.ViaVersion.api;

import java.util.List;

public interface ViaVersionConfig {
  boolean isCheckForUpdates();
  
  boolean isPreventCollision();
  
  boolean isNewEffectIndicator();
  
  boolean isShowNewDeathMessages();
  
  boolean isSuppressMetadataErrors();
  
  boolean isShieldBlocking();
  
  boolean isHologramPatch();
  
  boolean isPistonAnimationPatch();
  
  boolean isBossbarPatch();
  
  boolean isBossbarAntiflicker();
  
  @Deprecated
  boolean isUnknownEntitiesSuppressed();
  
  double getHologramYOffset();
  
  boolean isAutoTeam();
  
  @Deprecated
  boolean isBlockBreakPatch();
  
  int getMaxPPS();
  
  String getMaxPPSKickMessage();
  
  int getTrackingPeriod();
  
  int getWarningPPS();
  
  int getMaxWarnings();
  
  String getMaxWarningsKickMessage();
  
  boolean isAntiXRay();
  
  boolean isSendSupportedVersions();
  
  boolean isStimulatePlayerTick();
  
  boolean isItemCache();
  
  boolean isNMSPlayerTicking();
  
  boolean isReplacePistons();
  
  int getPistonReplacementId();
  
  boolean isForceJsonTransform();
  
  boolean is1_12NBTArrayFix();
  
  boolean is1_13TeamColourFix();
  
  boolean is1_12QuickMoveActionFix();
  
  List<Integer> getBlockedProtocols();
  
  String getBlockedDisconnectMsg();
  
  String getReloadDisconnectMsg();
  
  boolean isSuppress1_13ConversionErrors();
  
  boolean isDisable1_13AutoComplete();
  
  boolean isMinimizeCooldown();
  
  boolean isServersideBlockConnections();
  
  String getBlockConnectionMethod();
  
  boolean isReduceBlockStorageMemory();
  
  boolean isStemWhenBlockAbove();
  
  boolean isVineClimbFix();
  
  boolean isSnowCollisionFix();
  
  boolean isInfestedBlocksFix();
  
  int get1_13TabCompleteDelay();
  
  boolean isTruncate1_14Books();
  
  boolean isLeftHandedHandling();
  
  boolean is1_9HitboxFix();
  
  boolean is1_14HitboxFix();
  
  boolean isNonFullBlockLightFix();
  
  boolean is1_14HealthNaNFix();
  
  boolean is1_15InstantRespawn();
}
