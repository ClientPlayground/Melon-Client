package com.replaymod.replaystudio.stream;

import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.protocol.Packet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class IteratorStream implements PacketStream {
  private final ListIterator<PacketData> iterator;
  
  private final List<PacketData> added = new ArrayList<>();
  
  private final PacketStream.FilterInfo filter;
  
  private boolean filterActive;
  
  private boolean processing;
  
  private long lastTimestamp = -1L;
  
  public IteratorStream(ListIterator<PacketData> iterator, StreamFilter filter) {
    this(iterator, new PacketStream.FilterInfo(filter, -1L, -1L));
  }
  
  public IteratorStream(ListIterator<PacketData> iterator, PacketStream.FilterInfo filter) {
    this.iterator = iterator;
    this.filter = filter;
  }
  
  public void insert(PacketData packet) {
    if (this.processing) {
      this.added.add(packet);
    } else {
      this.iterator.add(packet);
    } 
    if (packet.getTime() > this.lastTimestamp)
      this.lastTimestamp = packet.getTime(); 
  }
  
  public void insert(long time, Packet packet) {
    insert(new PacketData(time, packet));
  }
  
  public void addFilter(StreamFilter filter) {
    throw new UnsupportedOperationException();
  }
  
  public void addFilter(StreamFilter filter, long from, long to) {
    throw new UnsupportedOperationException();
  }
  
  public void removeFilter(StreamFilter filter) {
    throw new UnsupportedOperationException();
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public PacketData next() {
    throw new UnsupportedOperationException();
  }
  
  public Collection<PacketStream.FilterInfo> getFilters() {
    return Arrays.asList(new PacketStream.FilterInfo[] { this.filter });
  }
  
  public void processNext() throws IOException {
    this.processing = true;
    PacketData next = this.iterator.next();
    boolean keep = true;
    if ((this.filter.getFrom() == -1L || this.filter.getFrom() <= next.getTime()) && (this.filter
      .getTo() == -1L || this.filter.getFrom() >= next.getTime())) {
      if (!this.filterActive) {
        this.filter.getFilter().onStart(this);
        this.filterActive = true;
      } 
      keep = this.filter.getFilter().onPacket(this, next);
    } else if (this.filterActive) {
      this.filter.getFilter().onEnd(this, this.lastTimestamp);
      this.filterActive = false;
    } 
    if (!keep) {
      this.iterator.remove();
      next.getPacket().getBuf().release();
      if (this.lastTimestamp == -1L)
        this.lastTimestamp = next.getTime(); 
    } else if (next.getTime() > this.lastTimestamp) {
      this.lastTimestamp = next.getTime();
    } 
    for (PacketData data : this.added)
      this.iterator.add(data); 
    this.added.clear();
    this.processing = false;
  }
  
  public void processAll() throws IOException {
    while (hasNext())
      processNext(); 
    end();
  }
  
  public void start() {}
  
  public List<PacketData> end() throws IOException {
    if (this.filterActive) {
      this.filterActive = false;
      this.filter.getFilter().onEnd(this, this.lastTimestamp);
    } 
    return Collections.unmodifiableList(this.added);
  }
}
