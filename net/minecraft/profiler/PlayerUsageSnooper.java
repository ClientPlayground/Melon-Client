package net.minecraft.profiler;

import com.google.common.collect.Maps;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.minecraft.util.HttpUtil;

public class PlayerUsageSnooper {
  private final Map<String, Object> snooperStats = Maps.newHashMap();
  
  private final Map<String, Object> clientStats = Maps.newHashMap();
  
  private final String uniqueID = UUID.randomUUID().toString();
  
  private final URL serverUrl;
  
  private final IPlayerUsage playerStatsCollector;
  
  private final Timer threadTrigger = new Timer("Snooper Timer", true);
  
  private final Object syncLock = new Object();
  
  private final long minecraftStartTimeMilis;
  
  private boolean isRunning;
  
  private int selfCounter;
  
  public PlayerUsageSnooper(String side, IPlayerUsage playerStatCollector, long startTime) {
    try {
      this.serverUrl = new URL("http://snoop.minecraft.net/" + side + "?version=" + '\002');
    } catch (MalformedURLException var6) {
      throw new IllegalArgumentException();
    } 
    this.playerStatsCollector = playerStatCollector;
    this.minecraftStartTimeMilis = startTime;
  }
  
  public void startSnooper() {
    if (!this.isRunning) {
      this.isRunning = true;
      addOSData();
      this.threadTrigger.schedule(new TimerTask() {
            public void run() {
              if (PlayerUsageSnooper.this.playerStatsCollector.isSnooperEnabled()) {
                Map<String, Object> map;
                synchronized (PlayerUsageSnooper.this.syncLock) {
                  map = Maps.newHashMap(PlayerUsageSnooper.this.clientStats);
                  if (PlayerUsageSnooper.this.selfCounter == 0)
                    map.putAll(PlayerUsageSnooper.this.snooperStats); 
                  map.put("snooper_count", Integer.valueOf(PlayerUsageSnooper.this.selfCounter++));
                  map.put("snooper_token", PlayerUsageSnooper.this.uniqueID);
                } 
                HttpUtil.postMap(PlayerUsageSnooper.this.serverUrl, map, true);
              } 
            }
          }0L, 900000L);
    } 
  }
  
  private void addOSData() {
    addJvmArgsToSnooper();
    addClientStat("snooper_token", this.uniqueID);
    addStatToSnooper("snooper_token", this.uniqueID);
    addStatToSnooper("os_name", System.getProperty("os.name"));
    addStatToSnooper("os_version", System.getProperty("os.version"));
    addStatToSnooper("os_architecture", System.getProperty("os.arch"));
    addStatToSnooper("java_version", System.getProperty("java.version"));
    addClientStat("version", "1.8.9");
    this.playerStatsCollector.addServerTypeToSnooper(this);
  }
  
  private void addJvmArgsToSnooper() {
    RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
    List<String> list = runtimemxbean.getInputArguments();
    int i = 0;
    for (String s : list) {
      if (s.startsWith("-X"))
        addClientStat("jvm_arg[" + i++ + "]", s); 
    } 
    addClientStat("jvm_args", Integer.valueOf(i));
  }
  
  public void addMemoryStatsToSnooper() {
    addStatToSnooper("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
    addStatToSnooper("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
    addStatToSnooper("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
    addStatToSnooper("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
    this.playerStatsCollector.addServerStatsToSnooper(this);
  }
  
  public void addClientStat(String statName, Object statValue) {
    synchronized (this.syncLock) {
      this.clientStats.put(statName, statValue);
    } 
  }
  
  public void addStatToSnooper(String statName, Object statValue) {
    synchronized (this.syncLock) {
      this.snooperStats.put(statName, statValue);
    } 
  }
  
  public Map<String, String> getCurrentStats() {
    Map<String, String> map = Maps.newLinkedHashMap();
    synchronized (this.syncLock) {
      addMemoryStatsToSnooper();
      for (Map.Entry<String, Object> entry : this.snooperStats.entrySet())
        map.put(entry.getKey(), entry.getValue().toString()); 
      for (Map.Entry<String, Object> entry1 : this.clientStats.entrySet())
        map.put(entry1.getKey(), entry1.getValue().toString()); 
      return map;
    } 
  }
  
  public boolean isSnooperRunning() {
    return this.isRunning;
  }
  
  public void stopSnooper() {
    this.threadTrigger.cancel();
  }
  
  public String getUniqueID() {
    return this.uniqueID;
  }
  
  public long getMinecraftStartTimeMillis() {
    return this.minecraftStartTimeMilis;
  }
}
