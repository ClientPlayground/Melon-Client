package com.replaymod.replaystudio.stream;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.protocol.Packet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractPacketStream implements PacketStream {
  public static AbstractPacketStream of(Supplier<PacketData> supplier) {
    return new AbstractPacketStreamImpl(supplier);
  }
  
  private static final class AbstractPacketStreamImpl extends AbstractPacketStream {
    private final Supplier<PacketData> supplier;
    
    public AbstractPacketStreamImpl(Supplier<PacketData> supplier) {
      this.supplier = supplier;
    }
    
    public void start() {}
    
    protected void cleanup() {}
    
    protected PacketData nextInput() {
      return (PacketData)this.supplier.get();
    }
  }
  
  private class PacketStreamContext implements PacketStream {
    private final AbstractPacketStream.StreamElement element;
    
    public PacketStreamContext(AbstractPacketStream.StreamElement element) {
      this.element = (AbstractPacketStream.StreamElement)Preconditions.checkNotNull(element);
    }
    
    public void insert(PacketData packet) {
      this.element.inserted.add(packet);
    }
    
    public void insert(long time, Packet packet) {
      this.element.inserted.add(new PacketData(time, packet));
    }
    
    public void addFilter(StreamFilter filter) {
      AbstractPacketStream.this.addFilter(filter);
    }
    
    public void addFilter(StreamFilter filter, long from, long to) {
      AbstractPacketStream.this.addFilter(filter, from, to);
    }
    
    public void removeFilter(StreamFilter filter) {
      AbstractPacketStream.this.removeFilter(filter);
    }
    
    public Collection<PacketStream.FilterInfo> getFilters() {
      return AbstractPacketStream.this.getFilters();
    }
    
    public PacketData next() {
      throw new IllegalStateException("Cannot get next data from within stream pipeline");
    }
    
    public void start() {
      throw new IllegalStateException("Cannot start from within stream pipeline");
    }
    
    public List<PacketData> end() {
      throw new IllegalStateException("Cannot end from within stream pipeline");
    }
  }
  
  private class StreamElement {
    private final PacketStream.FilterInfo filter;
    
    private final AbstractPacketStream.PacketStreamContext context = new AbstractPacketStream.PacketStreamContext(this);
    
    private final Queue<PacketData> inserted = new LinkedList<>();
    
    private boolean active;
    
    private long lastTimestamp;
    
    private StreamElement next;
    
    protected StreamElement() {
      this.filter = null;
    }
    
    public StreamElement(PacketStream.FilterInfo filter) {
      this.filter = (PacketStream.FilterInfo)Preconditions.checkNotNull(filter);
    }
    
    public void process(PacketData data) throws IOException {
      boolean keep = true;
      if (data != null && this.filter.applies(data.getTime())) {
        if (!this.active) {
          this.filter.getFilter().onStart(this.context);
          this.active = true;
        } 
        keep = this.filter.getFilter().onPacket(this.context, data);
        if (!keep)
          data.getPacket().getBuf().release(); 
      } else if (this.active) {
        this.filter.getFilter().onEnd(this.context, this.lastTimestamp);
        this.active = false;
        for (PacketData d : this.inserted) {
          if (d.getTime() > this.lastTimestamp)
            this.lastTimestamp = d.getTime(); 
          this.next.process(d);
        } 
        this.inserted.clear();
      } 
      if (data != null && keep) {
        if (data.getTime() > this.lastTimestamp)
          this.lastTimestamp = data.getTime(); 
        this.next.process(data);
      } 
      for (PacketData d : this.inserted) {
        if (d.getTime() > this.lastTimestamp)
          this.lastTimestamp = d.getTime(); 
        this.next.process(d);
      } 
      this.inserted.clear();
      if (data == null)
        this.next.process(null); 
    }
    
    public String toString() {
      return (this.active ? "" : "in") + "active " + this.filter;
    }
  }
  
  private class StreamElementEnd extends StreamElement {
    public void process(PacketData data) {
      if (data != null)
        AbstractPacketStream.this.inserted.add(data); 
    }
    
    public String toString() {
      return "Out";
    }
  }
  
  private final Queue<PacketData> inserted = new LinkedList<>();
  
  private final List<StreamElement> filters = new ArrayList<>();
  
  private StreamElement firstElement;
  
  public void insert(PacketData packet) {
    this.inserted.add(packet);
  }
  
  public void insert(long time, Packet packet) {
    this.inserted.add(new PacketData(time, packet));
  }
  
  private void buildPipe() {
    Iterator<StreamElement> iter = this.filters.iterator();
    StreamElement l = null;
    while (iter.hasNext()) {
      StreamElement e = iter.next();
      if (l == null) {
        this.firstElement = e;
      } else {
        l.next = e;
      } 
      l = e;
    } 
    if (l == null) {
      this.firstElement = new StreamElementEnd();
    } else {
      l.next = new StreamElementEnd();
    } 
  }
  
  public void addFilter(StreamFilter filter) {
    addFilter(filter, -1L, -1L);
  }
  
  public void addFilter(StreamFilter filter, long from, long to) {
    this.filters.add(new StreamElement(new PacketStream.FilterInfo(filter, from, to)));
    buildPipe();
  }
  
  public void removeFilter(StreamFilter filter) {
    Iterator<StreamElement> iter = this.filters.iterator();
    while (iter.hasNext()) {
      if (filter == (iter.next()).filter.getFilter())
        iter.remove(); 
    } 
    buildPipe();
  }
  
  public PacketData next() throws IOException {
    while (this.inserted.isEmpty()) {
      PacketData next = nextInput();
      if (next == null)
        break; 
      this.firstElement.process(next);
    } 
    return this.inserted.poll();
  }
  
  public Collection<PacketStream.FilterInfo> getFilters() {
    return Collections.unmodifiableList(Lists.transform(this.filters, e -> e.filter));
  }
  
  public List<PacketData> end() throws IOException {
    this.firstElement.process(null);
    List<PacketData> result = new LinkedList<>(this.inserted);
    this.inserted.clear();
    return result;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("PacketStream[");
    StreamElement e = this.firstElement;
    while (e != null) {
      sb.append(e);
      if (e.next != null)
        sb.append(" -> "); 
      e = e.next;
    } 
    sb.append("]");
    return sb.toString();
  }
  
  protected abstract PacketData nextInput();
  
  protected abstract void cleanup();
}
