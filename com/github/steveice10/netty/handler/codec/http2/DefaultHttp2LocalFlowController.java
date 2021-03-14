package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;

public class DefaultHttp2LocalFlowController implements Http2LocalFlowController {
  public static final float DEFAULT_WINDOW_UPDATE_RATIO = 0.5F;
  
  private final Http2Connection connection;
  
  private final Http2Connection.PropertyKey stateKey;
  
  private Http2FrameWriter frameWriter;
  
  private ChannelHandlerContext ctx;
  
  private float windowUpdateRatio;
  
  private int initialWindowSize = 65535;
  
  public DefaultHttp2LocalFlowController(Http2Connection connection) {
    this(connection, 0.5F, false);
  }
  
  public DefaultHttp2LocalFlowController(Http2Connection connection, float windowUpdateRatio, boolean autoRefillConnectionWindow) {
    this.connection = (Http2Connection)ObjectUtil.checkNotNull(connection, "connection");
    windowUpdateRatio(windowUpdateRatio);
    this.stateKey = connection.newKey();
    FlowState connectionState = autoRefillConnectionWindow ? new AutoRefillState(connection.connectionStream(), this.initialWindowSize) : new DefaultState(connection.connectionStream(), this.initialWindowSize);
    connection.connectionStream().setProperty(this.stateKey, connectionState);
    connection.addListener(new Http2ConnectionAdapter() {
          public void onStreamAdded(Http2Stream stream) {
            stream.setProperty(DefaultHttp2LocalFlowController.this.stateKey, DefaultHttp2LocalFlowController.REDUCED_FLOW_STATE);
          }
          
          public void onStreamActive(Http2Stream stream) {
            stream.setProperty(DefaultHttp2LocalFlowController.this.stateKey, new DefaultHttp2LocalFlowController.DefaultState(stream, DefaultHttp2LocalFlowController.this.initialWindowSize));
          }
          
          public void onStreamClosed(Http2Stream stream) {
            try {
              DefaultHttp2LocalFlowController.FlowState state = DefaultHttp2LocalFlowController.this.state(stream);
              int unconsumedBytes = state.unconsumedBytes();
              if (DefaultHttp2LocalFlowController.this.ctx != null && unconsumedBytes > 0) {
                DefaultHttp2LocalFlowController.this.connectionState().consumeBytes(unconsumedBytes);
                state.consumeBytes(unconsumedBytes);
              } 
            } catch (Http2Exception e) {
              PlatformDependent.throwException(e);
            } finally {
              stream.setProperty(DefaultHttp2LocalFlowController.this.stateKey, DefaultHttp2LocalFlowController.REDUCED_FLOW_STATE);
            } 
          }
        });
  }
  
  public DefaultHttp2LocalFlowController frameWriter(Http2FrameWriter frameWriter) {
    this.frameWriter = (Http2FrameWriter)ObjectUtil.checkNotNull(frameWriter, "frameWriter");
    return this;
  }
  
  public void channelHandlerContext(ChannelHandlerContext ctx) {
    this.ctx = (ChannelHandlerContext)ObjectUtil.checkNotNull(ctx, "ctx");
  }
  
  public void initialWindowSize(int newWindowSize) throws Http2Exception {
    assert this.ctx == null || this.ctx.executor().inEventLoop();
    int delta = newWindowSize - this.initialWindowSize;
    this.initialWindowSize = newWindowSize;
    WindowUpdateVisitor visitor = new WindowUpdateVisitor(delta);
    this.connection.forEachActiveStream(visitor);
    visitor.throwIfError();
  }
  
  public int initialWindowSize() {
    return this.initialWindowSize;
  }
  
  public int windowSize(Http2Stream stream) {
    return state(stream).windowSize();
  }
  
  public int initialWindowSize(Http2Stream stream) {
    return state(stream).initialWindowSize();
  }
  
  public void incrementWindowSize(Http2Stream stream, int delta) throws Http2Exception {
    assert this.ctx != null && this.ctx.executor().inEventLoop();
    FlowState state = state(stream);
    state.incrementInitialStreamWindow(delta);
    state.writeWindowUpdateIfNeeded();
  }
  
  public boolean consumeBytes(Http2Stream stream, int numBytes) throws Http2Exception {
    assert this.ctx != null && this.ctx.executor().inEventLoop();
    if (numBytes < 0)
      throw new IllegalArgumentException("numBytes must not be negative"); 
    if (numBytes == 0)
      return false; 
    if (stream != null && !isClosed(stream)) {
      if (stream.id() == 0)
        throw new UnsupportedOperationException("Returning bytes for the connection window is not supported"); 
      boolean windowUpdateSent = connectionState().consumeBytes(numBytes);
      windowUpdateSent |= state(stream).consumeBytes(numBytes);
      return windowUpdateSent;
    } 
    return false;
  }
  
  public int unconsumedBytes(Http2Stream stream) {
    return state(stream).unconsumedBytes();
  }
  
  private static void checkValidRatio(float ratio) {
    if (Double.compare(ratio, 0.0D) <= 0 || Double.compare(ratio, 1.0D) >= 0)
      throw new IllegalArgumentException("Invalid ratio: " + ratio); 
  }
  
  public void windowUpdateRatio(float ratio) {
    assert this.ctx == null || this.ctx.executor().inEventLoop();
    checkValidRatio(ratio);
    this.windowUpdateRatio = ratio;
  }
  
  public float windowUpdateRatio() {
    return this.windowUpdateRatio;
  }
  
  public void windowUpdateRatio(Http2Stream stream, float ratio) throws Http2Exception {
    assert this.ctx != null && this.ctx.executor().inEventLoop();
    checkValidRatio(ratio);
    FlowState state = state(stream);
    state.windowUpdateRatio(ratio);
    state.writeWindowUpdateIfNeeded();
  }
  
  public float windowUpdateRatio(Http2Stream stream) throws Http2Exception {
    return state(stream).windowUpdateRatio();
  }
  
  public void receiveFlowControlledFrame(Http2Stream stream, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
    assert this.ctx != null && this.ctx.executor().inEventLoop();
    int dataLength = data.readableBytes() + padding;
    FlowState connectionState = connectionState();
    connectionState.receiveFlowControlledFrame(dataLength);
    if (stream != null && !isClosed(stream)) {
      FlowState state = state(stream);
      state.endOfStream(endOfStream);
      state.receiveFlowControlledFrame(dataLength);
    } else if (dataLength > 0) {
      connectionState.consumeBytes(dataLength);
    } 
  }
  
  private FlowState connectionState() {
    return this.connection.connectionStream().<FlowState>getProperty(this.stateKey);
  }
  
  private FlowState state(Http2Stream stream) {
    return stream.<FlowState>getProperty(this.stateKey);
  }
  
  private static boolean isClosed(Http2Stream stream) {
    return (stream.state() == Http2Stream.State.CLOSED);
  }
  
  private final class AutoRefillState extends DefaultState {
    public AutoRefillState(Http2Stream stream, int initialWindowSize) {
      super(stream, initialWindowSize);
    }
    
    public void receiveFlowControlledFrame(int dataLength) throws Http2Exception {
      super.receiveFlowControlledFrame(dataLength);
      super.consumeBytes(dataLength);
    }
    
    public boolean consumeBytes(int numBytes) throws Http2Exception {
      return false;
    }
  }
  
  private class DefaultState implements FlowState {
    private final Http2Stream stream;
    
    private int window;
    
    private int processedWindow;
    
    private int initialStreamWindowSize;
    
    private float streamWindowUpdateRatio;
    
    private int lowerBound;
    
    private boolean endOfStream;
    
    public DefaultState(Http2Stream stream, int initialWindowSize) {
      this.stream = stream;
      window(initialWindowSize);
      this.streamWindowUpdateRatio = DefaultHttp2LocalFlowController.this.windowUpdateRatio;
    }
    
    public void window(int initialWindowSize) {
      assert DefaultHttp2LocalFlowController.this.ctx == null || DefaultHttp2LocalFlowController.this.ctx.executor().inEventLoop();
      this.window = this.processedWindow = this.initialStreamWindowSize = initialWindowSize;
    }
    
    public int windowSize() {
      return this.window;
    }
    
    public int initialWindowSize() {
      return this.initialStreamWindowSize;
    }
    
    public void endOfStream(boolean endOfStream) {
      this.endOfStream = endOfStream;
    }
    
    public float windowUpdateRatio() {
      return this.streamWindowUpdateRatio;
    }
    
    public void windowUpdateRatio(float ratio) {
      assert DefaultHttp2LocalFlowController.this.ctx == null || DefaultHttp2LocalFlowController.this.ctx.executor().inEventLoop();
      this.streamWindowUpdateRatio = ratio;
    }
    
    public void incrementInitialStreamWindow(int delta) {
      int newValue = (int)Math.min(2147483647L, 
          Math.max(0L, this.initialStreamWindowSize + delta));
      delta = newValue - this.initialStreamWindowSize;
      this.initialStreamWindowSize += delta;
    }
    
    public void incrementFlowControlWindows(int delta) throws Http2Exception {
      if (delta > 0 && this.window > Integer.MAX_VALUE - delta)
        throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Flow control window overflowed for stream: %d", new Object[] { Integer.valueOf(this.stream.id()) }); 
      this.window += delta;
      this.processedWindow += delta;
      this.lowerBound = (delta < 0) ? delta : 0;
    }
    
    public void receiveFlowControlledFrame(int dataLength) throws Http2Exception {
      assert dataLength >= 0;
      this.window -= dataLength;
      if (this.window < this.lowerBound)
        throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Flow control window exceeded for stream: %d", new Object[] { Integer.valueOf(this.stream.id()) }); 
    }
    
    private void returnProcessedBytes(int delta) throws Http2Exception {
      if (this.processedWindow - delta < this.window)
        throw Http2Exception.streamError(this.stream.id(), Http2Error.INTERNAL_ERROR, "Attempting to return too many bytes for stream %d", new Object[] { Integer.valueOf(this.stream.id()) }); 
      this.processedWindow -= delta;
    }
    
    public boolean consumeBytes(int numBytes) throws Http2Exception {
      returnProcessedBytes(numBytes);
      return writeWindowUpdateIfNeeded();
    }
    
    public int unconsumedBytes() {
      return this.processedWindow - this.window;
    }
    
    public boolean writeWindowUpdateIfNeeded() throws Http2Exception {
      if (this.endOfStream || this.initialStreamWindowSize <= 0)
        return false; 
      int threshold = (int)(this.initialStreamWindowSize * this.streamWindowUpdateRatio);
      if (this.processedWindow <= threshold) {
        writeWindowUpdate();
        return true;
      } 
      return false;
    }
    
    private void writeWindowUpdate() throws Http2Exception {
      int deltaWindowSize = this.initialStreamWindowSize - this.processedWindow;
      try {
        incrementFlowControlWindows(deltaWindowSize);
      } catch (Throwable t) {
        throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, t, "Attempting to return too many bytes for stream %d", new Object[] { Integer.valueOf(this.stream.id()) });
      } 
      DefaultHttp2LocalFlowController.this.frameWriter.writeWindowUpdate(DefaultHttp2LocalFlowController.this.ctx, this.stream.id(), deltaWindowSize, DefaultHttp2LocalFlowController.this.ctx.newPromise());
    }
  }
  
  private static final FlowState REDUCED_FLOW_STATE = new FlowState() {
      public int windowSize() {
        return 0;
      }
      
      public int initialWindowSize() {
        return 0;
      }
      
      public void window(int initialWindowSize) {
        throw new UnsupportedOperationException();
      }
      
      public void incrementInitialStreamWindow(int delta) {}
      
      public boolean writeWindowUpdateIfNeeded() throws Http2Exception {
        throw new UnsupportedOperationException();
      }
      
      public boolean consumeBytes(int numBytes) throws Http2Exception {
        return false;
      }
      
      public int unconsumedBytes() {
        return 0;
      }
      
      public float windowUpdateRatio() {
        throw new UnsupportedOperationException();
      }
      
      public void windowUpdateRatio(float ratio) {
        throw new UnsupportedOperationException();
      }
      
      public void receiveFlowControlledFrame(int dataLength) throws Http2Exception {
        throw new UnsupportedOperationException();
      }
      
      public void incrementFlowControlWindows(int delta) throws Http2Exception {}
      
      public void endOfStream(boolean endOfStream) {
        throw new UnsupportedOperationException();
      }
    };
  
  private final class WindowUpdateVisitor implements Http2StreamVisitor {
    private Http2Exception.CompositeStreamException compositeException;
    
    private final int delta;
    
    public WindowUpdateVisitor(int delta) {
      this.delta = delta;
    }
    
    public boolean visit(Http2Stream stream) throws Http2Exception {
      try {
        DefaultHttp2LocalFlowController.FlowState state = DefaultHttp2LocalFlowController.this.state(stream);
        state.incrementFlowControlWindows(this.delta);
        state.incrementInitialStreamWindow(this.delta);
      } catch (StreamException e) {
        if (this.compositeException == null)
          this.compositeException = new Http2Exception.CompositeStreamException(e.error(), 4); 
        this.compositeException.add(e);
      } 
      return true;
    }
    
    public void throwIfError() throws Http2Exception.CompositeStreamException {
      if (this.compositeException != null)
        throw this.compositeException; 
    }
  }
  
  private static interface FlowState {
    int windowSize();
    
    int initialWindowSize();
    
    void window(int param1Int);
    
    void incrementInitialStreamWindow(int param1Int);
    
    boolean writeWindowUpdateIfNeeded() throws Http2Exception;
    
    boolean consumeBytes(int param1Int) throws Http2Exception;
    
    int unconsumedBytes();
    
    float windowUpdateRatio();
    
    void windowUpdateRatio(float param1Float);
    
    void receiveFlowControlledFrame(int param1Int) throws Http2Exception;
    
    void incrementFlowControlWindows(int param1Int) throws Http2Exception;
    
    void endOfStream(boolean param1Boolean);
  }
}
