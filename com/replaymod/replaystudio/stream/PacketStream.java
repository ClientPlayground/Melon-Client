package com.replaymod.replaystudio.stream;

import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.protocol.Packet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface PacketStream {
  void insert(PacketData paramPacketData);
  
  void insert(long paramLong, Packet paramPacket);
  
  void addFilter(StreamFilter paramStreamFilter);
  
  void addFilter(StreamFilter paramStreamFilter, long paramLong1, long paramLong2);
  
  void removeFilter(StreamFilter paramStreamFilter);
  
  Collection<FilterInfo> getFilters();
  
  PacketData next() throws IOException;
  
  void start();
  
  List<PacketData> end() throws IOException;
  
  public static class FilterInfo {
    private final StreamFilter filter;
    
    private final long from;
    
    private final long to;
    
    public FilterInfo(StreamFilter filter, long from, long to) {
      this.filter = filter;
      this.from = from;
      this.to = to;
    }
    
    public boolean applies(long time) {
      return ((this.from == -1L || this.from <= time) && (this.to == -1L || this.to >= time));
    }
    
    public StreamFilter getFilter() {
      return this.filter;
    }
    
    public long getFrom() {
      return this.from;
    }
    
    public long getTo() {
      return this.to;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof FilterInfo))
        return false; 
      FilterInfo other = (FilterInfo)o;
      if (!other.canEqual(this))
        return false; 
      if (!Objects.equals(this.filter, other.filter))
        return false; 
      if (this.from != other.from)
        return false; 
      if (this.to != other.to)
        return false; 
      return true;
    }
    
    public int hashCode() {
      int result = 1;
      result = result * 59 + ((this.filter == null) ? 0 : this.filter.hashCode());
      result = result * 59 + (int)(this.from >>> 32L ^ this.from);
      result = result * 59 + (int)(this.to >>> 32L ^ this.to);
      return result;
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof FilterInfo;
    }
    
    public String toString() {
      return "FilterInfo(filter=" + this.filter + ", from=" + this.from + ", to=" + this.to + ")";
    }
  }
}
