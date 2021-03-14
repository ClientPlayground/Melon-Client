package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;

final class UnsafeHeapSwappedByteBuf extends AbstractUnsafeSwappedByteBuf {
  UnsafeHeapSwappedByteBuf(AbstractByteBuf buf) {
    super(buf);
  }
  
  private static int idx(ByteBuf wrapped, int index) {
    return wrapped.arrayOffset() + index;
  }
  
  protected long _getLong(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getLong(wrapped.array(), idx(wrapped, index));
  }
  
  protected int _getInt(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getInt(wrapped.array(), idx(wrapped, index));
  }
  
  protected short _getShort(AbstractByteBuf wrapped, int index) {
    return PlatformDependent.getShort(wrapped.array(), idx(wrapped, index));
  }
  
  protected void _setShort(AbstractByteBuf wrapped, int index, short value) {
    PlatformDependent.putShort(wrapped.array(), idx(wrapped, index), value);
  }
  
  protected void _setInt(AbstractByteBuf wrapped, int index, int value) {
    PlatformDependent.putInt(wrapped.array(), idx(wrapped, index), value);
  }
  
  protected void _setLong(AbstractByteBuf wrapped, int index, long value) {
    PlatformDependent.putLong(wrapped.array(), idx(wrapped, index), value);
  }
}
