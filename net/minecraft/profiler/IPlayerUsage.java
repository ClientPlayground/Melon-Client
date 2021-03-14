package net.minecraft.profiler;

public interface IPlayerUsage {
  void addServerStatsToSnooper(PlayerUsageSnooper paramPlayerUsageSnooper);
  
  void addServerTypeToSnooper(PlayerUsageSnooper paramPlayerUsageSnooper);
  
  boolean isSnooperEnabled();
}
