package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.collection.IntObjectHashMap;
import com.github.steveice10.netty.util.collection.IntObjectMap;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.UnaryPromiseNotifier;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class DefaultHttp2Connection implements Http2Connection {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultHttp2Connection.class);
  
  final IntObjectMap<Http2Stream> streamMap = (IntObjectMap<Http2Stream>)new IntObjectHashMap();
  
  final PropertyKeyRegistry propertyKeyRegistry = new PropertyKeyRegistry();
  
  final ConnectionStream connectionStream = new ConnectionStream();
  
  final DefaultEndpoint<Http2LocalFlowController> localEndpoint;
  
  final DefaultEndpoint<Http2RemoteFlowController> remoteEndpoint;
  
  final List<Http2Connection.Listener> listeners = new ArrayList<Http2Connection.Listener>(4);
  
  final ActiveStreams activeStreams;
  
  Promise<Void> closePromise;
  
  public DefaultHttp2Connection(boolean server) {
    this(server, 100);
  }
  
  public DefaultHttp2Connection(boolean server, int maxReservedStreams) {
    this.activeStreams = new ActiveStreams(this.listeners);
    this.localEndpoint = new DefaultEndpoint<Http2LocalFlowController>(server, server ? Integer.MAX_VALUE : maxReservedStreams);
    this.remoteEndpoint = new DefaultEndpoint<Http2RemoteFlowController>(!server, maxReservedStreams);
    this.streamMap.put(this.connectionStream.id(), this.connectionStream);
  }
  
  final boolean isClosed() {
    return (this.closePromise != null);
  }
  
  public Future<Void> close(Promise<Void> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    if (this.closePromise != null) {
      if (this.closePromise != promise)
        if (promise instanceof ChannelPromise && ((ChannelPromise)this.closePromise).isVoid()) {
          this.closePromise = promise;
        } else {
          this.closePromise.addListener((GenericFutureListener)new UnaryPromiseNotifier(promise));
        }  
    } else {
      this.closePromise = promise;
    } 
    if (isStreamMapEmpty()) {
      promise.trySuccess(null);
      return (Future<Void>)promise;
    } 
    Iterator<IntObjectMap.PrimitiveEntry<Http2Stream>> itr = this.streamMap.entries().iterator();
    if (this.activeStreams.allowModifications()) {
      this.activeStreams.incrementPendingIterations();
      try {
        while (itr.hasNext()) {
          DefaultStream stream = (DefaultStream)((IntObjectMap.PrimitiveEntry)itr.next()).value();
          if (stream.id() != 0)
            stream.close(itr); 
        } 
      } finally {
        this.activeStreams.decrementPendingIterations();
      } 
    } else {
      while (itr.hasNext()) {
        Http2Stream stream = (Http2Stream)((IntObjectMap.PrimitiveEntry)itr.next()).value();
        if (stream.id() != 0)
          stream.close(); 
      } 
    } 
    return (Future<Void>)this.closePromise;
  }
  
  public void addListener(Http2Connection.Listener listener) {
    this.listeners.add(listener);
  }
  
  public void removeListener(Http2Connection.Listener listener) {
    this.listeners.remove(listener);
  }
  
  public boolean isServer() {
    return this.localEndpoint.isServer();
  }
  
  public Http2Stream connectionStream() {
    return this.connectionStream;
  }
  
  public Http2Stream stream(int streamId) {
    return (Http2Stream)this.streamMap.get(streamId);
  }
  
  public boolean streamMayHaveExisted(int streamId) {
    return (this.remoteEndpoint.mayHaveCreatedStream(streamId) || this.localEndpoint.mayHaveCreatedStream(streamId));
  }
  
  public int numActiveStreams() {
    return this.activeStreams.size();
  }
  
  public Http2Stream forEachActiveStream(Http2StreamVisitor visitor) throws Http2Exception {
    return this.activeStreams.forEachActiveStream(visitor);
  }
  
  public Http2Connection.Endpoint<Http2LocalFlowController> local() {
    return this.localEndpoint;
  }
  
  public Http2Connection.Endpoint<Http2RemoteFlowController> remote() {
    return this.remoteEndpoint;
  }
  
  public boolean goAwayReceived() {
    return (this.localEndpoint.lastStreamKnownByPeer >= 0);
  }
  
  public void goAwayReceived(final int lastKnownStream, long errorCode, ByteBuf debugData) {
    this.localEndpoint.lastStreamKnownByPeer(lastKnownStream);
    for (int i = 0; i < this.listeners.size(); i++) {
      try {
        ((Http2Connection.Listener)this.listeners.get(i)).onGoAwayReceived(lastKnownStream, errorCode, debugData);
      } catch (Throwable cause) {
        logger.error("Caught Throwable from listener onGoAwayReceived.", cause);
      } 
    } 
    try {
      forEachActiveStream(new Http2StreamVisitor() {
            public boolean visit(Http2Stream stream) {
              if (stream.id() > lastKnownStream && DefaultHttp2Connection.this.localEndpoint.isValidStreamId(stream.id()))
                stream.close(); 
              return true;
            }
          });
    } catch (Http2Exception e) {
      PlatformDependent.throwException(e);
    } 
  }
  
  public boolean goAwaySent() {
    return (this.remoteEndpoint.lastStreamKnownByPeer >= 0);
  }
  
  public void goAwaySent(final int lastKnownStream, long errorCode, ByteBuf debugData) {
    this.remoteEndpoint.lastStreamKnownByPeer(lastKnownStream);
    for (int i = 0; i < this.listeners.size(); i++) {
      try {
        ((Http2Connection.Listener)this.listeners.get(i)).onGoAwaySent(lastKnownStream, errorCode, debugData);
      } catch (Throwable cause) {
        logger.error("Caught Throwable from listener onGoAwaySent.", cause);
      } 
    } 
    try {
      forEachActiveStream(new Http2StreamVisitor() {
            public boolean visit(Http2Stream stream) {
              if (stream.id() > lastKnownStream && DefaultHttp2Connection.this.remoteEndpoint.isValidStreamId(stream.id()))
                stream.close(); 
              return true;
            }
          });
    } catch (Http2Exception e) {
      PlatformDependent.throwException(e);
    } 
  }
  
  private boolean isStreamMapEmpty() {
    return (this.streamMap.size() == 1);
  }
  
  void removeStream(DefaultStream stream, Iterator<?> itr) {
    boolean removed;
    if (itr == null) {
      removed = (this.streamMap.remove(stream.id()) != null);
    } else {
      itr.remove();
      removed = true;
    } 
    if (removed) {
      for (int i = 0; i < this.listeners.size(); i++) {
        try {
          ((Http2Connection.Listener)this.listeners.get(i)).onStreamRemoved(stream);
        } catch (Throwable cause) {
          logger.error("Caught Throwable from listener onStreamRemoved.", cause);
        } 
      } 
      if (this.closePromise != null && isStreamMapEmpty())
        this.closePromise.trySuccess(null); 
    } 
  }
  
  static Http2Stream.State activeState(int streamId, Http2Stream.State initialState, boolean isLocal, boolean halfClosed) throws Http2Exception {
    switch (initialState) {
      case IDLE:
        return halfClosed ? (isLocal ? Http2Stream.State.HALF_CLOSED_LOCAL : Http2Stream.State.HALF_CLOSED_REMOTE) : Http2Stream.State.OPEN;
      case RESERVED_LOCAL:
        return Http2Stream.State.HALF_CLOSED_REMOTE;
      case RESERVED_REMOTE:
        return Http2Stream.State.HALF_CLOSED_LOCAL;
    } 
    throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Attempting to open a stream in an invalid state: " + initialState, new Object[0]);
  }
  
  void notifyHalfClosed(Http2Stream stream) {
    for (int i = 0; i < this.listeners.size(); i++) {
      try {
        ((Http2Connection.Listener)this.listeners.get(i)).onStreamHalfClosed(stream);
      } catch (Throwable cause) {
        logger.error("Caught Throwable from listener onStreamHalfClosed.", cause);
      } 
    } 
  }
  
  void notifyClosed(Http2Stream stream) {
    for (int i = 0; i < this.listeners.size(); i++) {
      try {
        ((Http2Connection.Listener)this.listeners.get(i)).onStreamClosed(stream);
      } catch (Throwable cause) {
        logger.error("Caught Throwable from listener onStreamClosed.", cause);
      } 
    } 
  }
  
  public Http2Connection.PropertyKey newKey() {
    return this.propertyKeyRegistry.newKey();
  }
  
  final DefaultPropertyKey verifyKey(Http2Connection.PropertyKey key) {
    return ((DefaultPropertyKey)ObjectUtil.checkNotNull(key, "key")).verifyConnection(this);
  }
  
  private class DefaultStream implements Http2Stream {
    private static final byte META_STATE_SENT_RST = 1;
    
    private static final byte META_STATE_SENT_HEADERS = 2;
    
    private static final byte META_STATE_SENT_TRAILERS = 4;
    
    private static final byte META_STATE_SENT_PUSHPROMISE = 8;
    
    private static final byte META_STATE_RECV_HEADERS = 16;
    
    private static final byte META_STATE_RECV_TRAILERS = 32;
    
    private final int id;
    
    private final PropertyMap properties = new PropertyMap();
    
    private Http2Stream.State state;
    
    private byte metaState;
    
    DefaultStream(int id, Http2Stream.State state) {
      this.id = id;
      this.state = state;
    }
    
    public final int id() {
      return this.id;
    }
    
    public final Http2Stream.State state() {
      return this.state;
    }
    
    public boolean isResetSent() {
      return ((this.metaState & 0x1) != 0);
    }
    
    public Http2Stream resetSent() {
      this.metaState = (byte)(this.metaState | 0x1);
      return this;
    }
    
    public Http2Stream headersSent(boolean isInformational) {
      if (!isInformational)
        this.metaState = (byte)(this.metaState | (isHeadersSent() ? 4 : 2)); 
      return this;
    }
    
    public boolean isHeadersSent() {
      return ((this.metaState & 0x2) != 0);
    }
    
    public boolean isTrailersSent() {
      return ((this.metaState & 0x4) != 0);
    }
    
    public Http2Stream headersReceived(boolean isInformational) {
      if (!isInformational)
        this.metaState = (byte)(this.metaState | (isHeadersReceived() ? 32 : 16)); 
      return this;
    }
    
    public boolean isHeadersReceived() {
      return ((this.metaState & 0x10) != 0);
    }
    
    public boolean isTrailersReceived() {
      return ((this.metaState & 0x20) != 0);
    }
    
    public Http2Stream pushPromiseSent() {
      this.metaState = (byte)(this.metaState | 0x8);
      return this;
    }
    
    public boolean isPushPromiseSent() {
      return ((this.metaState & 0x8) != 0);
    }
    
    public final <V> V setProperty(Http2Connection.PropertyKey key, V value) {
      return this.properties.add(DefaultHttp2Connection.this.verifyKey(key), value);
    }
    
    public final <V> V getProperty(Http2Connection.PropertyKey key) {
      return this.properties.get(DefaultHttp2Connection.this.verifyKey(key));
    }
    
    public final <V> V removeProperty(Http2Connection.PropertyKey key) {
      return this.properties.remove(DefaultHttp2Connection.this.verifyKey(key));
    }
    
    public Http2Stream open(boolean halfClosed) throws Http2Exception {
      this.state = DefaultHttp2Connection.activeState(this.id, this.state, isLocal(), halfClosed);
      if (!createdBy().canOpenStream())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Maximum active streams violated for this endpoint.", new Object[0]); 
      activate();
      return this;
    }
    
    void activate() {
      if (this.state == Http2Stream.State.HALF_CLOSED_LOCAL) {
        headersSent(false);
      } else if (this.state == Http2Stream.State.HALF_CLOSED_REMOTE) {
        headersReceived(false);
      } 
      DefaultHttp2Connection.this.activeStreams.activate(this);
    }
    
    Http2Stream close(Iterator<?> itr) {
      if (this.state == Http2Stream.State.CLOSED)
        return this; 
      this.state = Http2Stream.State.CLOSED;
      (createdBy()).numStreams--;
      DefaultHttp2Connection.this.activeStreams.deactivate(this, itr);
      return this;
    }
    
    public Http2Stream close() {
      return close(null);
    }
    
    public Http2Stream closeLocalSide() {
      switch (this.state) {
        case OPEN:
          this.state = Http2Stream.State.HALF_CLOSED_LOCAL;
          DefaultHttp2Connection.this.notifyHalfClosed(this);
        case HALF_CLOSED_LOCAL:
          return this;
      } 
      close();
    }
    
    public Http2Stream closeRemoteSide() {
      switch (this.state) {
        case OPEN:
          this.state = Http2Stream.State.HALF_CLOSED_REMOTE;
          DefaultHttp2Connection.this.notifyHalfClosed(this);
        case HALF_CLOSED_REMOTE:
          return this;
      } 
      close();
    }
    
    DefaultHttp2Connection.DefaultEndpoint<? extends Http2FlowController> createdBy() {
      return DefaultHttp2Connection.this.localEndpoint.isValidStreamId(this.id) ? (DefaultHttp2Connection.DefaultEndpoint)DefaultHttp2Connection.this.localEndpoint : (DefaultHttp2Connection.DefaultEndpoint)DefaultHttp2Connection.this.remoteEndpoint;
    }
    
    final boolean isLocal() {
      return DefaultHttp2Connection.this.localEndpoint.isValidStreamId(this.id);
    }
    
    private class PropertyMap {
      Object[] values = EmptyArrays.EMPTY_OBJECTS;
      
      <V> V add(DefaultHttp2Connection.DefaultPropertyKey key, V value) {
        resizeIfNecessary(key.index);
        V prevValue = (V)this.values[key.index];
        this.values[key.index] = value;
        return prevValue;
      }
      
      <V> V get(DefaultHttp2Connection.DefaultPropertyKey key) {
        if (key.index >= this.values.length)
          return null; 
        return (V)this.values[key.index];
      }
      
      <V> V remove(DefaultHttp2Connection.DefaultPropertyKey key) {
        V prevValue = null;
        if (key.index < this.values.length) {
          prevValue = (V)this.values[key.index];
          this.values[key.index] = null;
        } 
        return prevValue;
      }
      
      void resizeIfNecessary(int index) {
        if (index >= this.values.length)
          this.values = Arrays.copyOf(this.values, DefaultHttp2Connection.this.propertyKeyRegistry.size()); 
      }
      
      private PropertyMap() {}
    }
  }
  
  private final class ConnectionStream extends DefaultStream {
    ConnectionStream() {
      super(0, Http2Stream.State.IDLE);
    }
    
    public boolean isResetSent() {
      return false;
    }
    
    DefaultHttp2Connection.DefaultEndpoint<? extends Http2FlowController> createdBy() {
      return null;
    }
    
    public Http2Stream resetSent() {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream open(boolean halfClosed) {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream close() {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream closeLocalSide() {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream closeRemoteSide() {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream headersSent(boolean isInformational) {
      throw new UnsupportedOperationException();
    }
    
    public boolean isHeadersSent() {
      throw new UnsupportedOperationException();
    }
    
    public Http2Stream pushPromiseSent() {
      throw new UnsupportedOperationException();
    }
    
    public boolean isPushPromiseSent() {
      throw new UnsupportedOperationException();
    }
  }
  
  private final class DefaultEndpoint<F extends Http2FlowController> implements Http2Connection.Endpoint<F> {
    private final boolean server;
    
    private int nextStreamIdToCreate;
    
    private int nextReservationStreamId;
    
    private int lastStreamKnownByPeer = -1;
    
    private boolean pushToAllowed = true;
    
    private F flowController;
    
    private int maxStreams;
    
    private int maxActiveStreams;
    
    private final int maxReservedStreams;
    
    int numActiveStreams;
    
    int numStreams;
    
    DefaultEndpoint(boolean server, int maxReservedStreams) {
      this.server = server;
      if (server) {
        this.nextStreamIdToCreate = 2;
        this.nextReservationStreamId = 0;
      } else {
        this.nextStreamIdToCreate = 1;
        this.nextReservationStreamId = 1;
      } 
      this.pushToAllowed = !server;
      this.maxActiveStreams = Integer.MAX_VALUE;
      this.maxReservedStreams = ObjectUtil.checkPositiveOrZero(maxReservedStreams, "maxReservedStreams");
      updateMaxStreams();
    }
    
    public int incrementAndGetNextStreamId() {
      return (this.nextReservationStreamId >= 0) ? (this.nextReservationStreamId += 2) : this.nextReservationStreamId;
    }
    
    private void incrementExpectedStreamId(int streamId) {
      if (streamId > this.nextReservationStreamId && this.nextReservationStreamId >= 0)
        this.nextReservationStreamId = streamId; 
      this.nextStreamIdToCreate = streamId + 2;
      this.numStreams++;
    }
    
    public boolean isValidStreamId(int streamId) {
      return (streamId > 0 && this.server == (((streamId & 0x1) == 0)));
    }
    
    public boolean mayHaveCreatedStream(int streamId) {
      return (isValidStreamId(streamId) && streamId <= lastStreamCreated());
    }
    
    public boolean canOpenStream() {
      return (this.numActiveStreams < this.maxActiveStreams);
    }
    
    public DefaultHttp2Connection.DefaultStream createStream(int streamId, boolean halfClosed) throws Http2Exception {
      Http2Stream.State state = DefaultHttp2Connection.activeState(streamId, Http2Stream.State.IDLE, isLocal(), halfClosed);
      checkNewStreamAllowed(streamId, state);
      DefaultHttp2Connection.DefaultStream stream = new DefaultHttp2Connection.DefaultStream(streamId, state);
      incrementExpectedStreamId(streamId);
      addStream(stream);
      stream.activate();
      return stream;
    }
    
    public boolean created(Http2Stream stream) {
      return (stream instanceof DefaultHttp2Connection.DefaultStream && ((DefaultHttp2Connection.DefaultStream)stream).createdBy() == this);
    }
    
    public boolean isServer() {
      return this.server;
    }
    
    public DefaultHttp2Connection.DefaultStream reservePushStream(int streamId, Http2Stream parent) throws Http2Exception {
      if (parent == null)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Parent stream missing", new Object[0]); 
      if (isLocal() ? !parent.state().localSideOpen() : !parent.state().remoteSideOpen())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d is not open for sending push promise", new Object[] { Integer.valueOf(parent.id()) }); 
      if (!opposite().allowPushTo())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server push not allowed to opposite endpoint", new Object[0]); 
      Http2Stream.State state = isLocal() ? Http2Stream.State.RESERVED_LOCAL : Http2Stream.State.RESERVED_REMOTE;
      checkNewStreamAllowed(streamId, state);
      DefaultHttp2Connection.DefaultStream stream = new DefaultHttp2Connection.DefaultStream(streamId, state);
      incrementExpectedStreamId(streamId);
      addStream(stream);
      return stream;
    }
    
    private void addStream(DefaultHttp2Connection.DefaultStream stream) {
      DefaultHttp2Connection.this.streamMap.put(stream.id(), stream);
      for (int i = 0; i < DefaultHttp2Connection.this.listeners.size(); i++) {
        try {
          ((Http2Connection.Listener)DefaultHttp2Connection.this.listeners.get(i)).onStreamAdded(stream);
        } catch (Throwable cause) {
          DefaultHttp2Connection.logger.error("Caught Throwable from listener onStreamAdded.", cause);
        } 
      } 
    }
    
    public void allowPushTo(boolean allow) {
      if (allow && this.server)
        throw new IllegalArgumentException("Servers do not allow push"); 
      this.pushToAllowed = allow;
    }
    
    public boolean allowPushTo() {
      return this.pushToAllowed;
    }
    
    public int numActiveStreams() {
      return this.numActiveStreams;
    }
    
    public int maxActiveStreams() {
      return this.maxActiveStreams;
    }
    
    public void maxActiveStreams(int maxActiveStreams) {
      this.maxActiveStreams = maxActiveStreams;
      updateMaxStreams();
    }
    
    public int lastStreamCreated() {
      return (this.nextStreamIdToCreate > 1) ? (this.nextStreamIdToCreate - 2) : 0;
    }
    
    public int lastStreamKnownByPeer() {
      return this.lastStreamKnownByPeer;
    }
    
    private void lastStreamKnownByPeer(int lastKnownStream) {
      this.lastStreamKnownByPeer = lastKnownStream;
    }
    
    public F flowController() {
      return this.flowController;
    }
    
    public void flowController(F flowController) {
      this.flowController = (F)ObjectUtil.checkNotNull(flowController, "flowController");
    }
    
    public Http2Connection.Endpoint<? extends Http2FlowController> opposite() {
      return isLocal() ? (Http2Connection.Endpoint)DefaultHttp2Connection.this.remoteEndpoint : (Http2Connection.Endpoint)DefaultHttp2Connection.this.localEndpoint;
    }
    
    private void updateMaxStreams() {
      this.maxStreams = (int)Math.min(2147483647L, this.maxActiveStreams + this.maxReservedStreams);
    }
    
    private void checkNewStreamAllowed(int streamId, Http2Stream.State state) throws Http2Exception {
      assert state != Http2Stream.State.IDLE;
      if (DefaultHttp2Connection.this.goAwayReceived() && streamId > DefaultHttp2Connection.this.localEndpoint.lastStreamKnownByPeer())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Cannot create stream %d since this endpoint has received a GOAWAY frame with last stream id %d.", new Object[] { Integer.valueOf(streamId), 
              Integer.valueOf(this.this$0.localEndpoint.lastStreamKnownByPeer()) }); 
      if (!isValidStreamId(streamId)) {
        if (streamId < 0)
          throw new Http2NoMoreStreamIdsException(); 
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Request stream %d is not correct for %s connection", new Object[] { Integer.valueOf(streamId), this.server ? "server" : "client" });
      } 
      if (streamId < this.nextStreamIdToCreate)
        throw Http2Exception.closedStreamError(Http2Error.PROTOCOL_ERROR, "Request stream %d is behind the next expected stream %d", new Object[] { Integer.valueOf(streamId), Integer.valueOf(this.nextStreamIdToCreate) }); 
      if (this.nextStreamIdToCreate <= 0)
        throw Http2Exception.connectionError(Http2Error.REFUSED_STREAM, "Stream IDs are exhausted for this endpoint.", new Object[0]); 
      boolean isReserved = (state == Http2Stream.State.RESERVED_LOCAL || state == Http2Stream.State.RESERVED_REMOTE);
      if ((!isReserved && !canOpenStream()) || (isReserved && this.numStreams >= this.maxStreams))
        throw Http2Exception.streamError(streamId, Http2Error.REFUSED_STREAM, "Maximum active streams violated for this endpoint.", new Object[0]); 
      if (DefaultHttp2Connection.this.isClosed())
        throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Attempted to create stream id %d after connection was closed", new Object[] { Integer.valueOf(streamId) }); 
    }
    
    private boolean isLocal() {
      return (this == DefaultHttp2Connection.this.localEndpoint);
    }
  }
  
  private final class ActiveStreams {
    private final List<Http2Connection.Listener> listeners;
    
    private final Queue<DefaultHttp2Connection.Event> pendingEvents = new ArrayDeque<DefaultHttp2Connection.Event>(4);
    
    private final Set<Http2Stream> streams = new LinkedHashSet<Http2Stream>();
    
    private int pendingIterations;
    
    public ActiveStreams(List<Http2Connection.Listener> listeners) {
      this.listeners = listeners;
    }
    
    public int size() {
      return this.streams.size();
    }
    
    public void activate(final DefaultHttp2Connection.DefaultStream stream) {
      if (allowModifications()) {
        addToActiveStreams(stream);
      } else {
        this.pendingEvents.add(new DefaultHttp2Connection.Event() {
              public void process() {
                DefaultHttp2Connection.ActiveStreams.this.addToActiveStreams(stream);
              }
            });
      } 
    }
    
    public void deactivate(final DefaultHttp2Connection.DefaultStream stream, final Iterator<?> itr) {
      if (allowModifications() || itr != null) {
        removeFromActiveStreams(stream, itr);
      } else {
        this.pendingEvents.add(new DefaultHttp2Connection.Event() {
              public void process() {
                DefaultHttp2Connection.ActiveStreams.this.removeFromActiveStreams(stream, itr);
              }
            });
      } 
    }
    
    public Http2Stream forEachActiveStream(Http2StreamVisitor visitor) throws Http2Exception {
      incrementPendingIterations();
      try {
        for (Http2Stream stream : this.streams) {
          if (!visitor.visit(stream))
            return stream; 
        } 
        return null;
      } finally {
        decrementPendingIterations();
      } 
    }
    
    void addToActiveStreams(DefaultHttp2Connection.DefaultStream stream) {
      if (this.streams.add(stream)) {
        (stream.createdBy()).numActiveStreams++;
        for (int i = 0; i < this.listeners.size(); i++) {
          try {
            ((Http2Connection.Listener)this.listeners.get(i)).onStreamActive(stream);
          } catch (Throwable cause) {
            DefaultHttp2Connection.logger.error("Caught Throwable from listener onStreamActive.", cause);
          } 
        } 
      } 
    }
    
    void removeFromActiveStreams(DefaultHttp2Connection.DefaultStream stream, Iterator<?> itr) {
      if (this.streams.remove(stream)) {
        (stream.createdBy()).numActiveStreams--;
        DefaultHttp2Connection.this.notifyClosed(stream);
      } 
      DefaultHttp2Connection.this.removeStream(stream, itr);
    }
    
    boolean allowModifications() {
      return (this.pendingIterations == 0);
    }
    
    void incrementPendingIterations() {
      this.pendingIterations++;
    }
    
    void decrementPendingIterations() {
      this.pendingIterations--;
      if (allowModifications())
        while (true) {
          DefaultHttp2Connection.Event event = this.pendingEvents.poll();
          if (event == null)
            break; 
          try {
            event.process();
          } catch (Throwable cause) {
            DefaultHttp2Connection.logger.error("Caught Throwable while processing pending ActiveStreams$Event.", cause);
          } 
        }  
    }
  }
  
  final class DefaultPropertyKey implements Http2Connection.PropertyKey {
    final int index;
    
    DefaultPropertyKey(int index) {
      this.index = index;
    }
    
    DefaultPropertyKey verifyConnection(Http2Connection connection) {
      if (connection != DefaultHttp2Connection.this)
        throw new IllegalArgumentException("Using a key that was not created by this connection"); 
      return this;
    }
  }
  
  private final class PropertyKeyRegistry {
    final List<DefaultHttp2Connection.DefaultPropertyKey> keys = new ArrayList<DefaultHttp2Connection.DefaultPropertyKey>(4);
    
    DefaultHttp2Connection.DefaultPropertyKey newKey() {
      DefaultHttp2Connection.DefaultPropertyKey key = new DefaultHttp2Connection.DefaultPropertyKey(this.keys.size());
      this.keys.add(key);
      return key;
    }
    
    int size() {
      return this.keys.size();
    }
    
    private PropertyKeyRegistry() {}
  }
  
  static interface Event {
    void process();
  }
}
