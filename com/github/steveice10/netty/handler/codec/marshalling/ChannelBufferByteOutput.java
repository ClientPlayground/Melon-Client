package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.buffer.ByteBuf;
import java.io.IOException;
import org.jboss.marshalling.ByteOutput;

class ChannelBufferByteOutput implements ByteOutput {
  private final ByteBuf buffer;
  
  ChannelBufferByteOutput(ByteBuf buffer) {
    this.buffer = buffer;
  }
  
  public void close() throws IOException {}
  
  public void flush() throws IOException {}
  
  public void write(int b) throws IOException {
    this.buffer.writeByte(b);
  }
  
  public void write(byte[] bytes) throws IOException {
    this.buffer.writeBytes(bytes);
  }
  
  public void write(byte[] bytes, int srcIndex, int length) throws IOException {
    this.buffer.writeBytes(bytes, srcIndex, length);
  }
  
  ByteBuf getBuffer() {
    return this.buffer;
  }
}
