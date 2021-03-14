package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;

final class UnsafeDirectSwappedByteBuf extends AbstractUnsafeSwappedByteBuf {
  UnsafeDirectSwappedByteBuf(AbstractByteBuf buf) {
    super(buf);
  }
  
  private static long addr(AbstractByteBuf wrapped, int index) {
    return wrapped.memoryAddress() + index;
  }
  
  protected long _getLong(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getLong(addr(wrapped, index));
  }
  
  protected int _getInt(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getInt(addr(wrapped, index));
  }
  
  protected short _getShort(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getShort(addr(wrapped, index));
  }
  
  protected void _setShort(AbstractByteBuf wrapped, int index, short value) {
    PlatformDependent.putShort(addr(wrapped, index), value);
  }
  
  protected void _setInt(AbstractByteBuf wrapped, int index, int value) {
    PlatformDependent.putInt(addr(wrapped, index), value);
  }
  
  protected void _setLong(AbstractByteBuf wrapped, int index, long value) {
    PlatformDependent.putLong(addr(wrapped, index), value);
  }
}
