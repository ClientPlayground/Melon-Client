package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

public final class IovArray implements ChannelOutboundBuffer.MessageProcessor {
  private static final int ADDRESS_SIZE = PlatformDependent.addressSize();
  
  private static final int IOV_SIZE = 2 * ADDRESS_SIZE;
  
  private static final int CAPACITY = Limits.IOV_MAX * IOV_SIZE;
  
  private final long memoryAddress;
  
  private int count;
  
  private long size;
  
  private long maxBytes = Limits.SSIZE_MAX;
  
  public IovArray() {
    this.memoryAddress = PlatformDependent.allocateMemory(CAPACITY);
  }
  
  public void clear() {
    this.count = 0;
    this.size = 0L;
  }
  
  public boolean add(ByteBuf buf) {
    if (this.count == Limits.IOV_MAX)
      return false; 
    if (buf.hasMemoryAddress() && buf.nioBufferCount() == 1) {
      int len = buf.readableBytes();
      return (len == 0 || add(buf.memoryAddress(), buf.readerIndex(), len));
    } 
    ByteBuffer[] buffers = buf.nioBuffers();
    for (ByteBuffer nioBuffer : buffers) {
      int len = nioBuffer.remaining();
      if (len != 0 && (!add(PlatformDependent.directBufferAddress(nioBuffer), nioBuffer.position(), len) || this.count == Limits.IOV_MAX))
        return false; 
    } 
    return true;
  }
  
  private boolean add(long addr, int offset, int len) {
    long baseOffset = memoryAddress(this.count);
    long lengthOffset = baseOffset + ADDRESS_SIZE;
    if (this.maxBytes - len < this.size && this.count > 0)
      return false; 
    this.size += len;
    this.count++;
    if (ADDRESS_SIZE == 8) {
      PlatformDependent.putLong(baseOffset, addr + offset);
      PlatformDependent.putLong(lengthOffset, len);
    } else {
      assert ADDRESS_SIZE == 4;
      PlatformDependent.putInt(baseOffset, (int)addr + offset);
      PlatformDependent.putInt(lengthOffset, len);
    } 
    return true;
  }
  
  public int count() {
    return this.count;
  }
  
  public long size() {
    return this.size;
  }
  
  public void maxBytes(long maxBytes) {
    this.maxBytes = Math.min(Limits.SSIZE_MAX, ObjectUtil.checkPositive(maxBytes, "maxBytes"));
  }
  
  public long maxBytes() {
    return this.maxBytes;
  }
  
  public long memoryAddress(int offset) {
    return this.memoryAddress + (IOV_SIZE * offset);
  }
  
  public void release() {
    PlatformDependent.freeMemory(this.memoryAddress);
  }
  
  public boolean processMessage(Object msg) throws Exception {
    return (msg instanceof ByteBuf && add((ByteBuf)msg));
  }
}
