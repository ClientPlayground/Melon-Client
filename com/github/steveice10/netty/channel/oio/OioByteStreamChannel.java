package com.github.steveice10.netty.channel.oio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.WritableByteChannel;

public abstract class OioByteStreamChannel extends AbstractOioByteChannel {
  private static final InputStream CLOSED_IN = new InputStream() {
      public int read() {
        return -1;
      }
    };
  
  private static final OutputStream CLOSED_OUT = new OutputStream() {
      public void write(int b) throws IOException {
        throw new ClosedChannelException();
      }
    };
  
  private InputStream is;
  
  private OutputStream os;
  
  private WritableByteChannel outChannel;
  
  protected OioByteStreamChannel(Channel parent) {
    super(parent);
  }
  
  protected final void activate(InputStream is, OutputStream os) {
    if (this.is != null)
      throw new IllegalStateException("input was set already"); 
    if (this.os != null)
      throw new IllegalStateException("output was set already"); 
    if (is == null)
      throw new NullPointerException("is"); 
    if (os == null)
      throw new NullPointerException("os"); 
    this.is = is;
    this.os = os;
  }
  
  public boolean isActive() {
    InputStream is = this.is;
    if (is == null || is == CLOSED_IN)
      return false; 
    OutputStream os = this.os;
    return (os != null && os != CLOSED_OUT);
  }
  
  protected int available() {
    try {
      return this.is.available();
    } catch (IOException ignored) {
      return 0;
    } 
  }
  
  protected int doReadBytes(ByteBuf buf) throws Exception {
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.attemptedBytesRead(Math.max(1, Math.min(available(), buf.maxWritableBytes())));
    return buf.writeBytes(this.is, allocHandle.attemptedBytesRead());
  }
  
  protected void doWriteBytes(ByteBuf buf) throws Exception {
    OutputStream os = this.os;
    if (os == null)
      throw new NotYetConnectedException(); 
    buf.readBytes(os, buf.readableBytes());
  }
  
  protected void doWriteFileRegion(FileRegion region) throws Exception {
    OutputStream os = this.os;
    if (os == null)
      throw new NotYetConnectedException(); 
    if (this.outChannel == null)
      this.outChannel = Channels.newChannel(os); 
    long written = 0L;
    do {
      long localWritten = region.transferTo(this.outChannel, written);
      if (localWritten == -1L) {
        checkEOF(region);
        return;
      } 
      written += localWritten;
    } while (written < region.count());
  }
  
  private static void checkEOF(FileRegion region) throws IOException {
    if (region.transferred() < region.count())
      throw new EOFException("Expected to be able to write " + region.count() + " bytes, but only wrote " + region
          .transferred()); 
  }
  
  protected void doClose() throws Exception {
    InputStream is = this.is;
    OutputStream os = this.os;
    this.is = CLOSED_IN;
    this.os = CLOSED_OUT;
    try {
      if (is != null)
        is.close(); 
    } finally {
      if (os != null)
        os.close(); 
    } 
  }
}
