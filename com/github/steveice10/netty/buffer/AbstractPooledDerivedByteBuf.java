package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class AbstractPooledDerivedByteBuf extends AbstractReferenceCountedByteBuf {
  private final Recycler.Handle<AbstractPooledDerivedByteBuf> recyclerHandle;
  
  private AbstractByteBuf rootParent;
  
  private ByteBuf parent;
  
  AbstractPooledDerivedByteBuf(Recycler.Handle<? extends AbstractPooledDerivedByteBuf> recyclerHandle) {
    super(0);
    this.recyclerHandle = (Recycler.Handle)recyclerHandle;
  }
  
  final void parent(ByteBuf newParent) {
    assert newParent instanceof SimpleLeakAwareByteBuf;
    this.parent = newParent;
  }
  
  public final AbstractByteBuf unwrap() {
    return this.rootParent;
  }
  
  final <U extends AbstractPooledDerivedByteBuf> U init(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex, int maxCapacity) {
    wrapped.retain();
    this.parent = wrapped;
    this.rootParent = unwrapped;
    try {
      maxCapacity(maxCapacity);
      setIndex0(readerIndex, writerIndex);
      setRefCnt(1);
      AbstractPooledDerivedByteBuf abstractPooledDerivedByteBuf = this;
      wrapped = null;
      return (U)abstractPooledDerivedByteBuf;
    } finally {
      if (wrapped != null) {
        this.parent = this.rootParent = null;
        wrapped.release();
      } 
    } 
  }
  
  protected final void deallocate() {
    ByteBuf parent = this.parent;
    this.recyclerHandle.recycle(this);
    parent.release();
  }
  
  public final ByteBufAllocator alloc() {
    return unwrap().alloc();
  }
  
  @Deprecated
  public final ByteOrder order() {
    return unwrap().order();
  }
  
  public boolean isReadOnly() {
    return unwrap().isReadOnly();
  }
  
  public final boolean isDirect() {
    return unwrap().isDirect();
  }
  
  public boolean hasArray() {
    return unwrap().hasArray();
  }
  
  public byte[] array() {
    return unwrap().array();
  }
  
  public boolean hasMemoryAddress() {
    return unwrap().hasMemoryAddress();
  }
  
  public final int nioBufferCount() {
    return unwrap().nioBufferCount();
  }
  
  public final ByteBuffer internalNioBuffer(int index, int length) {
    return nioBuffer(index, length);
  }
  
  public final ByteBuf retainedSlice() {
    int index = readerIndex();
    return retainedSlice(index, writerIndex() - index);
  }
  
  public ByteBuf slice(int index, int length) {
    ensureAccessible();
    return new PooledNonRetainedSlicedByteBuf(this, unwrap(), index, length);
  }
  
  final ByteBuf duplicate0() {
    ensureAccessible();
    return new PooledNonRetainedDuplicateByteBuf(this, unwrap());
  }
  
  private static final class PooledNonRetainedDuplicateByteBuf extends UnpooledDuplicatedByteBuf {
    private final ReferenceCounted referenceCountDelegate;
    
    PooledNonRetainedDuplicateByteBuf(ReferenceCounted referenceCountDelegate, AbstractByteBuf buffer) {
      super(buffer);
      this.referenceCountDelegate = referenceCountDelegate;
    }
    
    int refCnt0() {
      return this.referenceCountDelegate.refCnt();
    }
    
    ByteBuf retain0() {
      this.referenceCountDelegate.retain();
      return this;
    }
    
    ByteBuf retain0(int increment) {
      this.referenceCountDelegate.retain(increment);
      return this;
    }
    
    ByteBuf touch0() {
      this.referenceCountDelegate.touch();
      return this;
    }
    
    ByteBuf touch0(Object hint) {
      this.referenceCountDelegate.touch(hint);
      return this;
    }
    
    boolean release0() {
      return this.referenceCountDelegate.release();
    }
    
    boolean release0(int decrement) {
      return this.referenceCountDelegate.release(decrement);
    }
    
    public ByteBuf duplicate() {
      ensureAccessible();
      return new PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, this);
    }
    
    public ByteBuf retainedDuplicate() {
      return PooledDuplicatedByteBuf.newInstance(unwrap(), this, readerIndex(), writerIndex());
    }
    
    public ByteBuf slice(int index, int length) {
      checkIndex(index, length);
      return new AbstractPooledDerivedByteBuf.PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, unwrap(), index, length);
    }
    
    public ByteBuf retainedSlice() {
      return retainedSlice(readerIndex(), capacity());
    }
    
    public ByteBuf retainedSlice(int index, int length) {
      return PooledSlicedByteBuf.newInstance(unwrap(), this, index, length);
    }
  }
  
  private static final class PooledNonRetainedSlicedByteBuf extends UnpooledSlicedByteBuf {
    private final ReferenceCounted referenceCountDelegate;
    
    PooledNonRetainedSlicedByteBuf(ReferenceCounted referenceCountDelegate, AbstractByteBuf buffer, int index, int length) {
      super(buffer, index, length);
      this.referenceCountDelegate = referenceCountDelegate;
    }
    
    int refCnt0() {
      return this.referenceCountDelegate.refCnt();
    }
    
    ByteBuf retain0() {
      this.referenceCountDelegate.retain();
      return this;
    }
    
    ByteBuf retain0(int increment) {
      this.referenceCountDelegate.retain(increment);
      return this;
    }
    
    ByteBuf touch0() {
      this.referenceCountDelegate.touch();
      return this;
    }
    
    ByteBuf touch0(Object hint) {
      this.referenceCountDelegate.touch(hint);
      return this;
    }
    
    boolean release0() {
      return this.referenceCountDelegate.release();
    }
    
    boolean release0(int decrement) {
      return this.referenceCountDelegate.release(decrement);
    }
    
    public ByteBuf duplicate() {
      ensureAccessible();
      return (new AbstractPooledDerivedByteBuf.PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, unwrap()))
        .setIndex(idx(readerIndex()), idx(writerIndex()));
    }
    
    public ByteBuf retainedDuplicate() {
      return PooledDuplicatedByteBuf.newInstance(unwrap(), this, idx(readerIndex()), idx(writerIndex()));
    }
    
    public ByteBuf slice(int index, int length) {
      checkIndex(index, length);
      return new PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, unwrap(), idx(index), length);
    }
    
    public ByteBuf retainedSlice() {
      return retainedSlice(0, capacity());
    }
    
    public ByteBuf retainedSlice(int index, int length) {
      return PooledSlicedByteBuf.newInstance(unwrap(), this, idx(index), length);
    }
  }
}
