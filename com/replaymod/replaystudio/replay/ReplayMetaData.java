package com.replaymod.replaystudio.replay;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReplayMetaData {
  public static final int CURRENT_FILE_FORMAT_VERSION = 14;
  
  public static final Map<Integer, Integer> PROTOCOL_FOR_FILE_FORMAT = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {
      
      });
  
  private boolean singleplayer;
  
  private String serverName;
  
  private int duration;
  
  private long date;
  
  private String mcversion;
  
  private String fileFormat;
  
  private int fileFormatVersion;
  
  private Integer protocol;
  
  private String generator;
  
  private int selfId = -1;
  
  private String[] players = new String[0];
  
  public ReplayMetaData() {}
  
  public ReplayMetaData(ReplayMetaData other) {
    this.singleplayer = other.singleplayer;
    this.serverName = other.serverName;
    this.duration = other.duration;
    this.date = other.date;
    this.mcversion = other.mcversion;
    this.fileFormat = other.fileFormat;
    this.fileFormatVersion = other.fileFormatVersion;
    this.generator = other.generator;
    this.selfId = other.selfId;
    this.players = Arrays.<String>copyOf(other.players, other.players.length);
  }
  
  public boolean isSingleplayer() {
    return this.singleplayer;
  }
  
  public String getServerName() {
    return this.serverName;
  }
  
  public int getDuration() {
    return this.duration;
  }
  
  public long getDate() {
    return this.date;
  }
  
  public String getMcVersion() {
    return this.mcversion;
  }
  
  public String getFileFormat() {
    return this.fileFormat;
  }
  
  public int getFileFormatVersion() {
    return this.fileFormatVersion;
  }
  
  public Integer getRawProtocolVersion() {
    return this.protocol;
  }
  
  public int getRawProtocolVersionOr0() {
    return (this.protocol != null) ? this.protocol.intValue() : 0;
  }
  
  public ProtocolVersion getProtocolVersion() {
    Integer protocol = this.protocol;
    if (protocol == null) {
      protocol = PROTOCOL_FOR_FILE_FORMAT.get(Integer.valueOf(this.fileFormatVersion));
      if (protocol == null)
        throw new IllegalStateException("Replay files with version 10+ must provide the `protocol` key."); 
    } 
    return ProtocolVersion.getProtocol(protocol.intValue());
  }
  
  public String getGenerator() {
    return this.generator;
  }
  
  public int getSelfId() {
    return this.selfId;
  }
  
  public String[] getPlayers() {
    return this.players;
  }
  
  public void setSingleplayer(boolean singleplayer) {
    this.singleplayer = singleplayer;
  }
  
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }
  
  public void setDuration(int duration) {
    this.duration = duration;
  }
  
  public void setDate(long date) {
    this.date = date;
  }
  
  public void setMcVersion(String mcVersion) {
    this.mcversion = mcVersion;
  }
  
  public void setFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
  }
  
  public void setFileFormatVersion(int fileFormatVersion) {
    this.fileFormatVersion = fileFormatVersion;
  }
  
  public void setProtocolVersion(int protocol) {
    this.protocol = Integer.valueOf(protocol);
  }
  
  public void setGenerator(String generator) {
    this.generator = generator;
  }
  
  public void setSelfId(int selfId) {
    this.selfId = selfId;
  }
  
  public void setPlayers(String[] players) {
    this.players = players;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof ReplayMetaData))
      return false; 
    ReplayMetaData other = (ReplayMetaData)o;
    if (!other.canEqual(this))
      return false; 
    if (this.singleplayer != other.singleplayer)
      return false; 
    if (!Objects.equals(this.serverName, other.serverName))
      return false; 
    if (this.duration != other.duration)
      return false; 
    if (this.date != other.date)
      return false; 
    if (!Objects.equals(this.mcversion, other.mcversion))
      return false; 
    if (!Objects.equals(this.fileFormat, other.fileFormat))
      return false; 
    if (this.fileFormatVersion != other.fileFormatVersion)
      return false; 
    if (this.protocol != other.protocol)
      return false; 
    if (!Objects.equals(this.generator, other.generator))
      return false; 
    if (this.selfId != other.selfId)
      return false; 
    return Arrays.deepEquals((Object[])this.players, (Object[])other.players);
  }
  
  public int hashCode() {
    int result = 1;
    result = result * 59 + (this.singleplayer ? 79 : 97);
    result = result * 59 + ((this.serverName == null) ? 0 : this.serverName.hashCode());
    result = result * 59 + this.duration;
    result = result * 59 + (int)(this.date >>> 32L ^ this.date);
    result = result * 59 + ((this.mcversion == null) ? 0 : this.mcversion.hashCode());
    result = result * 59 + ((this.fileFormat == null) ? 0 : this.fileFormat.hashCode());
    result = result * 59 + this.fileFormatVersion;
    result = result * 59 + this.protocol.intValue();
    result = result * 59 + ((this.generator == null) ? 0 : this.generator.hashCode());
    result = result * 59 + this.selfId;
    result = result * 59 + Arrays.deepHashCode((Object[])this.players);
    return result;
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof ReplayMetaData;
  }
  
  public String toString() {
    return "ReplayMetaData{singleplayer=" + this.singleplayer + ", serverName='" + this.serverName + '\'' + ", duration=" + this.duration + ", date=" + this.date + ", mcversion='" + this.mcversion + '\'' + ", fileFormat='" + this.fileFormat + '\'' + ", fileFormatVersion=" + this.fileFormatVersion + ", protocol=" + this.protocol + ", generator='" + this.generator + '\'' + ", selfId=" + this.selfId + ", players=" + 
      
      Arrays.toString((Object[])this.players) + '}';
  }
}
