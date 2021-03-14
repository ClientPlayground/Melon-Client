package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.collection.IntCollections;
import com.github.steveice10.netty.util.collection.IntObjectHashMap;
import com.github.steveice10.netty.util.collection.IntObjectMap;
import com.github.steveice10.netty.util.internal.DefaultPriorityQueue;
import com.github.steveice10.netty.util.internal.EmptyPriorityQueue;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.PriorityQueue;
import com.github.steveice10.netty.util.internal.PriorityQueueNode;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class WeightedFairQueueByteDistributor implements StreamByteDistributor {
  static final int INITIAL_CHILDREN_MAP_SIZE = Math.max(1, SystemPropertyUtil.getInt("com.github.steveice10.netty.http2.childrenMapSize", 2));
  
  private static final int DEFAULT_MAX_STATE_ONLY_SIZE = 5;
  
  private final Http2Connection.PropertyKey stateKey;
  
  private final IntObjectMap<State> stateOnlyMap;
  
  private final PriorityQueue<State> stateOnlyRemovalQueue;
  
  private final Http2Connection connection;
  
  private final State connectionState;
  
  private int allocationQuantum = 1024;
  
  private final int maxStateOnlySize;
  
  public WeightedFairQueueByteDistributor(Http2Connection connection) {
    this(connection, 5);
  }
  
  public WeightedFairQueueByteDistributor(Http2Connection connection, int maxStateOnlySize) {
    if (maxStateOnlySize < 0)
      throw new IllegalArgumentException("maxStateOnlySize: " + maxStateOnlySize + " (expected: >0)"); 
    if (maxStateOnlySize == 0) {
      this.stateOnlyMap = IntCollections.emptyMap();
      this.stateOnlyRemovalQueue = (PriorityQueue<State>)EmptyPriorityQueue.instance();
    } else {
      this.stateOnlyMap = (IntObjectMap<State>)new IntObjectHashMap(maxStateOnlySize);
      this.stateOnlyRemovalQueue = (PriorityQueue<State>)new DefaultPriorityQueue(StateOnlyComparator.INSTANCE, maxStateOnlySize + 2);
    } 
    this.maxStateOnlySize = maxStateOnlySize;
    this.connection = connection;
    this.stateKey = connection.newKey();
    Http2Stream connectionStream = connection.connectionStream();
    connectionStream.setProperty(this.stateKey, this.connectionState = new State(connectionStream, 16));
    connection.addListener(new Http2ConnectionAdapter() {
          public void onStreamAdded(Http2Stream stream) {
            WeightedFairQueueByteDistributor.State state = (WeightedFairQueueByteDistributor.State)WeightedFairQueueByteDistributor.this.stateOnlyMap.remove(stream.id());
            if (state == null) {
              state = new WeightedFairQueueByteDistributor.State(stream);
              List<WeightedFairQueueByteDistributor.ParentChangedEvent> events = new ArrayList<WeightedFairQueueByteDistributor.ParentChangedEvent>(1);
              WeightedFairQueueByteDistributor.this.connectionState.takeChild(state, false, events);
              WeightedFairQueueByteDistributor.this.notifyParentChanged(events);
            } else {
              WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.removeTyped(state);
              state.stream = stream;
            } 
            switch (stream.state()) {
              case RESERVED_REMOTE:
              case RESERVED_LOCAL:
                state.setStreamReservedOrActivated();
                break;
            } 
            stream.setProperty(WeightedFairQueueByteDistributor.this.stateKey, state);
          }
          
          public void onStreamActive(Http2Stream stream) {
            WeightedFairQueueByteDistributor.this.state(stream).setStreamReservedOrActivated();
          }
          
          public void onStreamClosed(Http2Stream stream) {
            WeightedFairQueueByteDistributor.this.state(stream).close();
          }
          
          public void onStreamRemoved(Http2Stream stream) {
            WeightedFairQueueByteDistributor.State state = WeightedFairQueueByteDistributor.this.state(stream);
            state.stream = null;
            if (WeightedFairQueueByteDistributor.this.maxStateOnlySize == 0) {
              state.parent.removeChild(state);
              return;
            } 
            if (WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.size() == WeightedFairQueueByteDistributor.this.maxStateOnlySize) {
              WeightedFairQueueByteDistributor.State stateToRemove = (WeightedFairQueueByteDistributor.State)WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.peek();
              if (WeightedFairQueueByteDistributor.StateOnlyComparator.INSTANCE.compare(stateToRemove, state) >= 0) {
                state.parent.removeChild(state);
                return;
              } 
              WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.poll();
              stateToRemove.parent.removeChild(stateToRemove);
              WeightedFairQueueByteDistributor.this.stateOnlyMap.remove(stateToRemove.streamId);
            } 
            WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.add(state);
            WeightedFairQueueByteDistributor.this.stateOnlyMap.put(state.streamId, state);
          }
        });
  }
  
  public void updateStreamableBytes(StreamByteDistributor.StreamState state) {
    state(state.stream()).updateStreamableBytes(Http2CodecUtil.streamableBytes(state), (state
        .hasFrame() && state.windowSize() >= 0));
  }
  
  public void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive) {
    State state = state(childStreamId);
    if (state == null) {
      if (this.maxStateOnlySize == 0)
        return; 
      state = new State(childStreamId);
      this.stateOnlyRemovalQueue.add(state);
      this.stateOnlyMap.put(childStreamId, state);
    } 
    State newParent = state(parentStreamId);
    if (newParent == null) {
      if (this.maxStateOnlySize == 0)
        return; 
      newParent = new State(parentStreamId);
      this.stateOnlyRemovalQueue.add(newParent);
      this.stateOnlyMap.put(parentStreamId, newParent);
      List<ParentChangedEvent> events = new ArrayList<ParentChangedEvent>(1);
      this.connectionState.takeChild(newParent, false, events);
      notifyParentChanged(events);
    } 
    if (state.activeCountForTree != 0 && state.parent != null)
      state.parent.totalQueuedWeights += (weight - state.weight); 
    state.weight = weight;
    if (newParent != state.parent || (exclusive && newParent.children.size() != 1)) {
      List<ParentChangedEvent> events;
      if (newParent.isDescendantOf(state)) {
        events = new ArrayList<ParentChangedEvent>(2 + (exclusive ? newParent.children.size() : 0));
        state.parent.takeChild(newParent, false, events);
      } else {
        events = new ArrayList<ParentChangedEvent>(1 + (exclusive ? newParent.children.size() : 0));
      } 
      newParent.takeChild(state, exclusive, events);
      notifyParentChanged(events);
    } 
    while (this.stateOnlyRemovalQueue.size() > this.maxStateOnlySize) {
      State stateToRemove = (State)this.stateOnlyRemovalQueue.poll();
      stateToRemove.parent.removeChild(stateToRemove);
      this.stateOnlyMap.remove(stateToRemove.streamId);
    } 
  }
  
  public boolean distribute(int maxBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
    int oldIsActiveCountForTree;
    if (this.connectionState.activeCountForTree == 0)
      return false; 
    do {
      oldIsActiveCountForTree = this.connectionState.activeCountForTree;
      maxBytes -= distributeToChildren(maxBytes, writer, this.connectionState);
    } while (this.connectionState.activeCountForTree != 0 && (maxBytes > 0 || oldIsActiveCountForTree != this.connectionState.activeCountForTree));
    return (this.connectionState.activeCountForTree != 0);
  }
  
  public void allocationQuantum(int allocationQuantum) {
    if (allocationQuantum <= 0)
      throw new IllegalArgumentException("allocationQuantum must be > 0"); 
    this.allocationQuantum = allocationQuantum;
  }
  
  private int distribute(int maxBytes, StreamByteDistributor.Writer writer, State state) throws Http2Exception {
    if (state.isActive()) {
      int nsent = Math.min(maxBytes, state.streamableBytes);
      state.write(nsent, writer);
      if (nsent == 0 && maxBytes != 0)
        state.updateStreamableBytes(state.streamableBytes, false); 
      return nsent;
    } 
    return distributeToChildren(maxBytes, writer, state);
  }
  
  private int distributeToChildren(int maxBytes, StreamByteDistributor.Writer writer, State state) throws Http2Exception {
    long oldTotalQueuedWeights = state.totalQueuedWeights;
    State childState = state.pollPseudoTimeQueue();
    State nextChildState = state.peekPseudoTimeQueue();
    childState.setDistributing();
    try {
      assert nextChildState == null || nextChildState.pseudoTimeToWrite >= childState.pseudoTimeToWrite : "nextChildState[" + nextChildState.streamId + "].pseudoTime(" + nextChildState.pseudoTimeToWrite + ") <  childState[" + childState.streamId + "].pseudoTime(" + childState.pseudoTimeToWrite + ")";
      int nsent = distribute((nextChildState == null) ? maxBytes : 
          Math.min(maxBytes, (int)Math.min((nextChildState.pseudoTimeToWrite - childState.pseudoTimeToWrite) * childState.weight / oldTotalQueuedWeights + this.allocationQuantum, 2147483647L)), writer, childState);
      state.pseudoTime += nsent;
      childState.updatePseudoTime(state, nsent, oldTotalQueuedWeights);
      return nsent;
    } finally {
      childState.unsetDistributing();
      if (childState.activeCountForTree != 0)
        state.offerPseudoTimeQueue(childState); 
    } 
  }
  
  private State state(Http2Stream stream) {
    return stream.<State>getProperty(this.stateKey);
  }
  
  private State state(int streamId) {
    Http2Stream stream = this.connection.stream(streamId);
    return (stream != null) ? state(stream) : (State)this.stateOnlyMap.get(streamId);
  }
  
  boolean isChild(int childId, int parentId, short weight) {
    State parent = state(parentId);
    State child;
    return (parent.children.containsKey(childId) && 
      (child = state(childId)).parent == parent && child.weight == weight);
  }
  
  int numChildren(int streamId) {
    State state = state(streamId);
    return (state == null) ? 0 : state.children.size();
  }
  
  void notifyParentChanged(List<ParentChangedEvent> events) {
    for (int i = 0; i < events.size(); i++) {
      ParentChangedEvent event = events.get(i);
      this.stateOnlyRemovalQueue.priorityChanged(event.state);
      if (event.state.parent != null && event.state.activeCountForTree != 0) {
        event.state.parent.offerAndInitializePseudoTime(event.state);
        event.state.parent.activeCountChangeForTree(event.state.activeCountForTree);
      } 
    } 
  }
  
  private static final class StateOnlyComparator implements Comparator<State>, Serializable {
    private static final long serialVersionUID = -4806936913002105966L;
    
    static final StateOnlyComparator INSTANCE = new StateOnlyComparator();
    
    public int compare(WeightedFairQueueByteDistributor.State o1, WeightedFairQueueByteDistributor.State o2) {
      boolean o1Actived = o1.wasStreamReservedOrActivated();
      if (o1Actived != o2.wasStreamReservedOrActivated())
        return o1Actived ? -1 : 1; 
      int x = o2.dependencyTreeDepth - o1.dependencyTreeDepth;
      return (x != 0) ? x : (o1.streamId - o2.streamId);
    }
  }
  
  private static final class StatePseudoTimeComparator implements Comparator<State>, Serializable {
    private static final long serialVersionUID = -1437548640227161828L;
    
    static final StatePseudoTimeComparator INSTANCE = new StatePseudoTimeComparator();
    
    public int compare(WeightedFairQueueByteDistributor.State o1, WeightedFairQueueByteDistributor.State o2) {
      return MathUtil.compare(o1.pseudoTimeToWrite, o2.pseudoTimeToWrite);
    }
  }
  
  private final class State implements PriorityQueueNode {
    private static final byte STATE_IS_ACTIVE = 1;
    
    private static final byte STATE_IS_DISTRIBUTING = 2;
    
    private static final byte STATE_STREAM_ACTIVATED = 4;
    
    Http2Stream stream;
    
    State parent;
    
    IntObjectMap<State> children = IntCollections.emptyMap();
    
    private final PriorityQueue<State> pseudoTimeQueue;
    
    final int streamId;
    
    int streamableBytes;
    
    int dependencyTreeDepth;
    
    int activeCountForTree;
    
    private int pseudoTimeQueueIndex = -1;
    
    private int stateOnlyQueueIndex = -1;
    
    long pseudoTimeToWrite;
    
    long pseudoTime;
    
    long totalQueuedWeights;
    
    private byte flags;
    
    short weight = 16;
    
    State(int streamId) {
      this(streamId, null, 0);
    }
    
    State(Http2Stream stream) {
      this(stream, 0);
    }
    
    State(Http2Stream stream, int initialSize) {
      this(stream.id(), stream, initialSize);
    }
    
    State(int streamId, Http2Stream stream, int initialSize) {
      this.stream = stream;
      this.streamId = streamId;
      this.pseudoTimeQueue = (PriorityQueue<State>)new DefaultPriorityQueue(WeightedFairQueueByteDistributor.StatePseudoTimeComparator.INSTANCE, initialSize);
    }
    
    boolean isDescendantOf(State state) {
      State next = this.parent;
      while (next != null) {
        if (next == state)
          return true; 
        next = next.parent;
      } 
      return false;
    }
    
    void takeChild(State child, boolean exclusive, List<WeightedFairQueueByteDistributor.ParentChangedEvent> events) {
      takeChild(null, child, exclusive, events);
    }
    
    void takeChild(Iterator<IntObjectMap.PrimitiveEntry<State>> childItr, State child, boolean exclusive, List<WeightedFairQueueByteDistributor.ParentChangedEvent> events) {
      State oldParent = child.parent;
      if (oldParent != this) {
        events.add(new WeightedFairQueueByteDistributor.ParentChangedEvent(child, oldParent));
        child.setParent(this);
        if (childItr != null) {
          childItr.remove();
        } else if (oldParent != null) {
          oldParent.children.remove(child.streamId);
        } 
        initChildrenIfEmpty();
        State oldChild = (State)this.children.put(child.streamId, child);
        assert oldChild == null : "A stream with the same stream ID was already in the child map.";
      } 
      if (exclusive && !this.children.isEmpty()) {
        Iterator<IntObjectMap.PrimitiveEntry<State>> itr = removeAllChildrenExcept(child).entries().iterator();
        while (itr.hasNext())
          child.takeChild(itr, (State)((IntObjectMap.PrimitiveEntry)itr.next()).value(), false, events); 
      } 
    }
    
    void removeChild(State child) {
      if (this.children.remove(child.streamId) != null) {
        List<WeightedFairQueueByteDistributor.ParentChangedEvent> events = new ArrayList<WeightedFairQueueByteDistributor.ParentChangedEvent>(1 + child.children.size());
        events.add(new WeightedFairQueueByteDistributor.ParentChangedEvent(child, child.parent));
        child.setParent(null);
        Iterator<IntObjectMap.PrimitiveEntry<State>> itr = child.children.entries().iterator();
        while (itr.hasNext())
          takeChild(itr, (State)((IntObjectMap.PrimitiveEntry)itr.next()).value(), false, events); 
        WeightedFairQueueByteDistributor.this.notifyParentChanged(events);
      } 
    }
    
    private IntObjectMap<State> removeAllChildrenExcept(State stateToRetain) {
      stateToRetain = (State)this.children.remove(stateToRetain.streamId);
      IntObjectMap<State> prevChildren = this.children;
      initChildren();
      if (stateToRetain != null)
        this.children.put(stateToRetain.streamId, stateToRetain); 
      return prevChildren;
    }
    
    private void setParent(State newParent) {
      if (this.activeCountForTree != 0 && this.parent != null) {
        this.parent.removePseudoTimeQueue(this);
        this.parent.activeCountChangeForTree(-this.activeCountForTree);
      } 
      this.parent = newParent;
      this.dependencyTreeDepth = (newParent == null) ? Integer.MAX_VALUE : (newParent.dependencyTreeDepth + 1);
    }
    
    private void initChildrenIfEmpty() {
      if (this.children == IntCollections.emptyMap())
        initChildren(); 
    }
    
    private void initChildren() {
      this.children = (IntObjectMap<State>)new IntObjectHashMap(WeightedFairQueueByteDistributor.INITIAL_CHILDREN_MAP_SIZE);
    }
    
    void write(int numBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
      assert this.stream != null;
      try {
        writer.write(this.stream, numBytes);
      } catch (Throwable t) {
        throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, t, "byte distribution write error", new Object[0]);
      } 
    }
    
    void activeCountChangeForTree(int increment) {
      assert this.activeCountForTree + increment >= 0;
      this.activeCountForTree += increment;
      if (this.parent != null) {
        assert this.activeCountForTree != increment || this.pseudoTimeQueueIndex == -1 || this.parent.pseudoTimeQueue
          
          .containsTyped(this) : "State[" + this.streamId + "].activeCountForTree changed from 0 to " + increment + " is in a pseudoTimeQueue, but not in parent[ " + this.parent.streamId + "]'s pseudoTimeQueue";
        if (this.activeCountForTree == 0) {
          this.parent.removePseudoTimeQueue(this);
        } else if (this.activeCountForTree == increment && !isDistributing()) {
          this.parent.offerAndInitializePseudoTime(this);
        } 
        this.parent.activeCountChangeForTree(increment);
      } 
    }
    
    void updateStreamableBytes(int newStreamableBytes, boolean isActive) {
      if (isActive() != isActive)
        if (isActive) {
          activeCountChangeForTree(1);
          setActive();
        } else {
          activeCountChangeForTree(-1);
          unsetActive();
        }  
      this.streamableBytes = newStreamableBytes;
    }
    
    void updatePseudoTime(State parentState, int nsent, long totalQueuedWeights) {
      assert this.streamId != 0 && nsent >= 0;
      this.pseudoTimeToWrite = Math.min(this.pseudoTimeToWrite, parentState.pseudoTime) + nsent * totalQueuedWeights / this.weight;
    }
    
    void offerAndInitializePseudoTime(State state) {
      state.pseudoTimeToWrite = this.pseudoTime;
      offerPseudoTimeQueue(state);
    }
    
    void offerPseudoTimeQueue(State state) {
      this.pseudoTimeQueue.offer(state);
      this.totalQueuedWeights += state.weight;
    }
    
    State pollPseudoTimeQueue() {
      State state = (State)this.pseudoTimeQueue.poll();
      this.totalQueuedWeights -= state.weight;
      return state;
    }
    
    void removePseudoTimeQueue(State state) {
      if (this.pseudoTimeQueue.removeTyped(state))
        this.totalQueuedWeights -= state.weight; 
    }
    
    State peekPseudoTimeQueue() {
      return (State)this.pseudoTimeQueue.peek();
    }
    
    void close() {
      updateStreamableBytes(0, false);
      this.stream = null;
    }
    
    boolean wasStreamReservedOrActivated() {
      return ((this.flags & 0x4) != 0);
    }
    
    void setStreamReservedOrActivated() {
      this.flags = (byte)(this.flags | 0x4);
    }
    
    boolean isActive() {
      return ((this.flags & 0x1) != 0);
    }
    
    private void setActive() {
      this.flags = (byte)(this.flags | 0x1);
    }
    
    private void unsetActive() {
      this.flags = (byte)(this.flags & 0xFFFFFFFE);
    }
    
    boolean isDistributing() {
      return ((this.flags & 0x2) != 0);
    }
    
    void setDistributing() {
      this.flags = (byte)(this.flags | 0x2);
    }
    
    void unsetDistributing() {
      this.flags = (byte)(this.flags & 0xFFFFFFFD);
    }
    
    public int priorityQueueIndex(DefaultPriorityQueue<?> queue) {
      return (queue == WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue) ? this.stateOnlyQueueIndex : this.pseudoTimeQueueIndex;
    }
    
    public void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i) {
      if (queue == WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue) {
        this.stateOnlyQueueIndex = i;
      } else {
        this.pseudoTimeQueueIndex = i;
      } 
    }
    
    public String toString() {
      StringBuilder sb = new StringBuilder(256 * ((this.activeCountForTree > 0) ? this.activeCountForTree : 1));
      toString(sb);
      return sb.toString();
    }
    
    private void toString(StringBuilder sb) {
      sb.append("{streamId ").append(this.streamId)
        .append(" streamableBytes ").append(this.streamableBytes)
        .append(" activeCountForTree ").append(this.activeCountForTree)
        .append(" pseudoTimeQueueIndex ").append(this.pseudoTimeQueueIndex)
        .append(" pseudoTimeToWrite ").append(this.pseudoTimeToWrite)
        .append(" pseudoTime ").append(this.pseudoTime)
        .append(" flags ").append(this.flags)
        .append(" pseudoTimeQueue.size() ").append(this.pseudoTimeQueue.size())
        .append(" stateOnlyQueueIndex ").append(this.stateOnlyQueueIndex)
        .append(" parent.streamId ").append((this.parent == null) ? -1 : this.parent.streamId).append("} [");
      if (!this.pseudoTimeQueue.isEmpty()) {
        for (State s : this.pseudoTimeQueue) {
          s.toString(sb);
          sb.append(", ");
        } 
        sb.setLength(sb.length() - 2);
      } 
      sb.append(']');
    }
  }
  
  private static final class ParentChangedEvent {
    final WeightedFairQueueByteDistributor.State state;
    
    final WeightedFairQueueByteDistributor.State oldParent;
    
    ParentChangedEvent(WeightedFairQueueByteDistributor.State state, WeightedFairQueueByteDistributor.State oldParent) {
      this.state = state;
      this.oldParent = oldParent;
    }
  }
}
