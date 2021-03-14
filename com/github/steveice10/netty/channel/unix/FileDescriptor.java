package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class FileDescriptor {
  private static final ClosedChannelException WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "write(..)");
  
  private static final ClosedChannelException WRITE_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writeAddress(..)");
  
  private static final ClosedChannelException WRITEV_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writev(..)");
  
  private static final ClosedChannelException WRITEV_ADDRESSES_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writevAddresses(..)");
  
  private static final ClosedChannelException READ_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "read(..)");
  
  private static final ClosedChannelException READ_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "readAddress(..)");
  
  private static final Errors.NativeIoException WRITE_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
      Errors.newConnectionResetException("syscall:write", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "write(..)");
  
  private static final Errors.NativeIoException WRITE_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:write", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writeAddress(..)");
  
  private static final Errors.NativeIoException WRITEV_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
      Errors.newConnectionResetException("syscall:writev", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writev(..)");
  
  private static final Errors.NativeIoException WRITEV_ADDRESSES_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:writev", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writeAddresses(..)");
  
  private static final Errors.NativeIoException READ_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
      Errors.newConnectionResetException("syscall:read", Errors.ERRNO_ECONNRESET_NEGATIVE), FileDescriptor.class, "read(..)");
  
  private static final Errors.NativeIoException READ_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:read", Errors.ERRNO_ECONNRESET_NEGATIVE), FileDescriptor.class, "readAddress(..)");
  
  private static final AtomicIntegerFieldUpdater<FileDescriptor> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(FileDescriptor.class, "state");
  
  private static final int STATE_CLOSED_MASK = 1;
  
  private static final int STATE_INPUT_SHUTDOWN_MASK = 2;
  
  private static final int STATE_OUTPUT_SHUTDOWN_MASK = 4;
  
  private static final int STATE_ALL_MASK = 7;
  
  volatile int state;
  
  final int fd;
  
  public FileDescriptor(int fd) {
    if (fd < 0)
      throw new IllegalArgumentException("fd must be >= 0"); 
    this.fd = fd;
  }
  
  public final int intValue() {
    return this.fd;
  }
  
  public void close() throws IOException {
    int state;
    do {
      state = this.state;
      if (isClosed(state))
        return; 
    } while (!casState(state, state | 0x7));
    int res = close(this.fd);
    if (res < 0)
      throw Errors.newIOException("close", res); 
  }
  
  public boolean isOpen() {
    return !isClosed(this.state);
  }
  
  public final int write(ByteBuffer buf, int pos, int limit) throws IOException {
    int res = write(this.fd, buf, pos, limit);
    if (res >= 0)
      return res; 
    return Errors.ioResult("write", res, WRITE_CONNECTION_RESET_EXCEPTION, WRITE_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final int writeAddress(long address, int pos, int limit) throws IOException {
    int res = writeAddress(this.fd, address, pos, limit);
    if (res >= 0)
      return res; 
    return Errors.ioResult("writeAddress", res, WRITE_ADDRESS_CONNECTION_RESET_EXCEPTION, WRITE_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final long writev(ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite) throws IOException {
    long res = writev(this.fd, buffers, offset, Math.min(Limits.IOV_MAX, length), maxBytesToWrite);
    if (res >= 0L)
      return res; 
    return Errors.ioResult("writev", (int)res, WRITEV_CONNECTION_RESET_EXCEPTION, WRITEV_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final long writevAddresses(long memoryAddress, int length) throws IOException {
    long res = writevAddresses(this.fd, memoryAddress, length);
    if (res >= 0L)
      return res; 
    return Errors.ioResult("writevAddresses", (int)res, WRITEV_ADDRESSES_CONNECTION_RESET_EXCEPTION, WRITEV_ADDRESSES_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final int read(ByteBuffer buf, int pos, int limit) throws IOException {
    int res = read(this.fd, buf, pos, limit);
    if (res > 0)
      return res; 
    if (res == 0)
      return -1; 
    return Errors.ioResult("read", res, READ_CONNECTION_RESET_EXCEPTION, READ_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final int readAddress(long address, int pos, int limit) throws IOException {
    int res = readAddress(this.fd, address, pos, limit);
    if (res > 0)
      return res; 
    if (res == 0)
      return -1; 
    return Errors.ioResult("readAddress", res, READ_ADDRESS_CONNECTION_RESET_EXCEPTION, READ_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public String toString() {
    return "FileDescriptor{fd=" + this.fd + '}';
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof FileDescriptor))
      return false; 
    return (this.fd == ((FileDescriptor)o).fd);
  }
  
  public int hashCode() {
    return this.fd;
  }
  
  public static FileDescriptor from(String path) throws IOException {
    ObjectUtil.checkNotNull(path, "path");
    int res = open(path);
    if (res < 0)
      throw Errors.newIOException("open", res); 
    return new FileDescriptor(res);
  }
  
  public static FileDescriptor from(File file) throws IOException {
    return from(((File)ObjectUtil.checkNotNull(file, "file")).getPath());
  }
  
  public static FileDescriptor[] pipe() throws IOException {
    long res = newPipe();
    if (res < 0L)
      throw Errors.newIOException("newPipe", (int)res); 
    return new FileDescriptor[] { new FileDescriptor((int)(res >>> 32L)), new FileDescriptor((int)res) };
  }
  
  final boolean casState(int expected, int update) {
    return stateUpdater.compareAndSet(this, expected, update);
  }
  
  static boolean isClosed(int state) {
    return ((state & 0x1) != 0);
  }
  
  static boolean isInputShutdown(int state) {
    return ((state & 0x2) != 0);
  }
  
  static boolean isOutputShutdown(int state) {
    return ((state & 0x4) != 0);
  }
  
  static int inputShutdown(int state) {
    return state | 0x2;
  }
  
  static int outputShutdown(int state) {
    return state | 0x4;
  }
  
  private static native int open(String paramString);
  
  private static native int close(int paramInt);
  
  private static native int write(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3);
  
  private static native int writeAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3);
  
  private static native long writev(int paramInt1, ByteBuffer[] paramArrayOfByteBuffer, int paramInt2, int paramInt3, long paramLong);
  
  private static native long writevAddresses(int paramInt1, long paramLong, int paramInt2);
  
  private static native int read(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3);
  
  private static native int readAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3);
  
  private static native long newPipe();
}
