package com.replaymod.replaystudio.viaversion;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.ViaVersionConfig;
import java.util.List;

public class CustomViaConfig implements ViaVersionConfig {
  public boolean isCheckForUpdates() {
    return false;
  }
  
  public boolean isPreventCollision() {
    return false;
  }
  
  public boolean isNewEffectIndicator() {
    return false;
  }
  
  public boolean isShowNewDeathMessages() {
    return false;
  }
  
  public boolean isSuppressMetadataErrors() {
    return true;
  }
  
  public boolean isShieldBlocking() {
    return false;
  }
  
  public boolean isHologramPatch() {
    return true;
  }
  
  public boolean isPistonAnimationPatch() {
    return false;
  }
  
  public boolean isBossbarPatch() {
    return true;
  }
  
  public boolean isBossbarAntiflicker() {
    return false;
  }
  
  public boolean isUnknownEntitiesSuppressed() {
    return true;
  }
  
  public double getHologramYOffset() {
    return -0.96D;
  }
  
  public boolean isAutoTeam() {
    return false;
  }
  
  public boolean isBlockBreakPatch() {
    return false;
  }
  
  public int getMaxPPS() {
    return -1;
  }
  
  public String getMaxPPSKickMessage() {
    return null;
  }
  
  public int getTrackingPeriod() {
    return -1;
  }
  
  public int getWarningPPS() {
    return -1;
  }
  
  public int getMaxWarnings() {
    return -1;
  }
  
  public String getMaxWarningsKickMessage() {
    return null;
  }
  
  public boolean isAntiXRay() {
    return false;
  }
  
  public boolean isSendSupportedVersions() {
    return false;
  }
  
  public boolean isStimulatePlayerTick() {
    return false;
  }
  
  public boolean isItemCache() {
    return false;
  }
  
  public boolean isNMSPlayerTicking() {
    return false;
  }
  
  public boolean isReplacePistons() {
    return false;
  }
  
  public int getPistonReplacementId() {
    return -1;
  }
  
  public boolean isForceJsonTransform() {
    return false;
  }
  
  public boolean is1_12NBTArrayFix() {
    return true;
  }
  
  public boolean is1_13TeamColourFix() {
    return true;
  }
  
  public boolean is1_12QuickMoveActionFix() {
    return false;
  }
  
  public List<Integer> getBlockedProtocols() {
    return null;
  }
  
  public String getBlockedDisconnectMsg() {
    return null;
  }
  
  public String getReloadDisconnectMsg() {
    return null;
  }
  
  public boolean isSuppress1_13ConversionErrors() {
    return false;
  }
  
  public boolean isDisable1_13AutoComplete() {
    return false;
  }
  
  public boolean isMinimizeCooldown() {
    return true;
  }
  
  public boolean isServersideBlockConnections() {
    return true;
  }
  
  public String getBlockConnectionMethod() {
    return "packet";
  }
  
  public boolean isReduceBlockStorageMemory() {
    return false;
  }
  
  public boolean isStemWhenBlockAbove() {
    return true;
  }
  
  public boolean isVineClimbFix() {
    return false;
  }
  
  public boolean isSnowCollisionFix() {
    return false;
  }
  
  public boolean isInfestedBlocksFix() {
    return false;
  }
  
  public int get1_13TabCompleteDelay() {
    return 0;
  }
  
  public boolean isTruncate1_14Books() {
    return false;
  }
  
  public boolean isLeftHandedHandling() {
    return true;
  }
  
  public boolean is1_9HitboxFix() {
    return true;
  }
  
  public boolean is1_14HitboxFix() {
    return true;
  }
  
  public boolean isNonFullBlockLightFix() {
    return false;
  }
  
  public boolean is1_14HealthNaNFix() {
    return true;
  }
  
  public boolean is1_15InstantRespawn() {
    return false;
  }
}
