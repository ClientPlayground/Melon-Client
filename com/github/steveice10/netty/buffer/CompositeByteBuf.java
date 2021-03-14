package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class CompositeByteBuf extends AbstractReferenceCountedByteBuf implements Iterable<ByteBuf> {
  private static final ByteBuffer EMPTY_NIO_BUFFER = Unpooled.EMPTY_BUFFER.nioBuffer();
  
  private static final Iterator<ByteBuf> EMPTY_ITERATOR = Collections.<ByteBuf>emptyList().iterator();
  
  private final ByteBufAllocator alloc;
  
  private final boolean direct;
  
  private final ComponentList components;
  
  private final int maxNumComponents;
  
  private boolean freed;
  
  public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents) {
    super(2147483647);
    if (alloc == null)
      throw new NullPointerException("alloc"); 
    this.alloc = alloc;
    this.direct = direct;
    this.maxNumComponents = maxNumComponents;
    this.components = newList(maxNumComponents);
  }
  
  public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, ByteBuf... buffers) {
    this(alloc, direct, maxNumComponents, buffers, 0, buffers.length);
  }
  
  CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, ByteBuf[] buffers, int offset, int len) {
    super(2147483647);
    if (alloc == null)
      throw new NullPointerException("alloc"); 
    if (maxNumComponents < 2)
      throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)"); 
    this.alloc = alloc;
    this.direct = direct;
    this.maxNumComponents = maxNumComponents;
    this.components = newList(maxNumComponents);
    addComponents0(false, 0, buffers, offset, len);
    consolidateIfNeeded();
    setIndex(0, capacity());
  }
  
  public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, Iterable<ByteBuf> buffers) {
    super(2147483647);
    if (alloc == null)
      throw new NullPointerException("alloc"); 
    if (maxNumComponents < 2)
      throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)"); 
    this.alloc = alloc;
    this.direct = direct;
    this.maxNumComponents = maxNumComponents;
    this.components = newList(maxNumComponents);
    addComponents0(false, 0, buffers);
    consolidateIfNeeded();
    setIndex(0, capacity());
  }
  
  private static ComponentList newList(int maxNumComponents) {
    return new ComponentList(Math.min(16, maxNumComponents));
  }
  
  CompositeByteBuf(ByteBufAllocator alloc) {
    super(2147483647);
    this.alloc = alloc;
    this.direct = false;
    this.maxNumComponents = 0;
    this.components = null;
  }
  
  public CompositeByteBuf addComponent(ByteBuf buffer) {
    return addComponent(false, buffer);
  }
  
  public CompositeByteBuf addComponents(ByteBuf... buffers) {
    return addComponents(false, buffers);
  }
  
  public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers) {
    return addComponents(false, buffers);
  }
  
  public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer) {
    return addComponent(false, cIndex, buffer);
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer) {
    ObjectUtil.checkNotNull(buffer, "buffer");
    addComponent0(increaseWriterIndex, this.components.size(), buffer);
    consolidateIfNeeded();
    return this;
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers) {
    addComponents0(increaseWriterIndex, this.components.size(), buffers, 0, buffers.length);
    consolidateIfNeeded();
    return this;
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers) {
    addComponents0(increaseWriterIndex, this.components.size(), buffers);
    consolidateIfNeeded();
    return this;
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
    ObjectUtil.checkNotNull(buffer, "buffer");
    addComponent0(increaseWriterIndex, cIndex, buffer);
    consolidateIfNeeded();
    return this;
  }
  
  private int addComponent0(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
    assert buffer != null;
    boolean wasAdded = false;
    try {
      checkComponentIndex(cIndex);
      int readableBytes = buffer.readableBytes();
      Component c = new Component(buffer.order(ByteOrder.BIG_ENDIAN).slice());
      if (cIndex == this.components.size()) {
        wasAdded = this.components.add(c);
        if (cIndex == 0) {
          c.endOffset = readableBytes;
        } else {
          Component prev = this.components.get(cIndex - 1);
          c.offset = prev.endOffset;
          c.endOffset = c.offset + readableBytes;
        } 
      } else {
        this.components.add(cIndex, c);
        wasAdded = true;
        if (readableBytes != 0)
          updateComponentOffsets(cIndex); 
      } 
      if (increaseWriterIndex)
        writerIndex(writerIndex() + buffer.readableBytes()); 
      return cIndex;
    } finally {
      if (!wasAdded)
        buffer.release(); 
    } 
  }
  
  public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers) {
    addComponents0(false, cIndex, buffers, 0, buffers.length);
    consolidateIfNeeded();
    return this;
  }
  
  private int addComponents0(boolean increaseWriterIndex, int cIndex, ByteBuf[] buffers, int offset, int len) {
    ObjectUtil.checkNotNull(buffers, "buffers");
    int i = offset;
    try {
      checkComponentIndex(cIndex);
      while (i < len) {
        ByteBuf b = buffers[i++];
        if (b == null)
          break; 
        cIndex = addComponent0(increaseWriterIndex, cIndex, b) + 1;
        int size = this.components.size();
        if (cIndex > size)
          cIndex = size; 
      } 
      return cIndex;
    } finally {
      for (; i < len; i++) {
        ByteBuf b = buffers[i];
        if (b != null)
          try {
            b.release();
          } catch (Throwable throwable) {} 
      } 
    } 
  }
  
  public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers) {
    addComponents0(false, cIndex, buffers);
    consolidateIfNeeded();
    return this;
  }
  
  private int addComponents0(boolean increaseIndex, int cIndex, Iterable<ByteBuf> buffers) {
    if (buffers instanceof ByteBuf)
      return addComponent0(increaseIndex, cIndex, (ByteBuf)buffers); 
    ObjectUtil.checkNotNull(buffers, "buffers");
    if (!(buffers instanceof Collection)) {
      List<ByteBuf> list = new ArrayList<ByteBuf>();
      try {
        for (ByteBuf b : buffers)
          list.add(b); 
        buffers = list;
      } finally {
        if (buffers != list)
          for (ByteBuf b : buffers) {
            if (b != null)
              try {
                b.release();
              } catch (Throwable throwable) {} 
          }  
      } 
    } 
    Collection<ByteBuf> col = (Collection<ByteBuf>)buffers;
    return addComponents0(increaseIndex, cIndex, col.<ByteBuf>toArray(new ByteBuf[col.size()]), 0, col.size());
  }
  
  private void consolidateIfNeeded() {
    int numComponents = this.components.size();
    if (numComponents > this.maxNumComponents) {
      int capacity = (this.components.get(numComponents - 1)).endOffset;
      ByteBuf consolidated = allocBuffer(capacity);
      for (int i = 0; i < numComponents; i++) {
        Component component = this.components.get(i);
        ByteBuf b = component.buf;
        consolidated.writeBytes(b);
        component.freeIfNecessary();
      } 
      Component c = new Component(consolidated);
      c.endOffset = c.length;
      this.components.clear();
      this.components.add(c);
    } 
  }
  
  private void checkComponentIndex(int cIndex) {
    ensureAccessible();
    if (cIndex < 0 || cIndex > this.components.size())
      throw new IndexOutOfBoundsException(String.format("cIndex: %d (expected: >= 0 && <= numComponents(%d))", new Object[] { Integer.valueOf(cIndex), Integer.valueOf(this.components.size()) })); 
  }
  
  private void checkComponentIndex(int cIndex, int numComponents) {
    ensureAccessible();
    if (cIndex < 0 || cIndex + numComponents > this.components.size())
      throw new IndexOutOfBoundsException(String.format("cIndex: %d, numComponents: %d (expected: cIndex >= 0 && cIndex + numComponents <= totalNumComponents(%d))", new Object[] { Integer.valueOf(cIndex), Integer.valueOf(numComponents), Integer.valueOf(this.components.size()) })); 
  }
  
  private void updateComponentOffsets(int cIndex) {
    int size = this.components.size();
    if (size <= cIndex)
      return; 
    Component c = this.components.get(cIndex);
    if (cIndex == 0) {
      c.offset = 0;
      c.endOffset = c.length;
      cIndex++;
    } 
    for (int i = cIndex; i < size; i++) {
      Component prev = this.components.get(i - 1);
      Component cur = this.components.get(i);
      cur.offset = prev.endOffset;
      cur.endOffset = cur.offset + cur.length;
    } 
  }
  
  public CompositeByteBuf removeComponent(int cIndex) {
    checkComponentIndex(cIndex);
    Component comp = this.components.remove(cIndex);
    comp.freeIfNecessary();
    if (comp.length > 0)
      updateComponentOffsets(cIndex); 
    return this;
  }
  
  public CompositeByteBuf removeComponents(int cIndex, int numComponents) {
    checkComponentIndex(cIndex, numComponents);
    if (numComponents == 0)
      return this; 
    int endIndex = cIndex + numComponents;
    boolean needsUpdate = false;
    for (int i = cIndex; i < endIndex; i++) {
      Component c = this.components.get(i);
      if (c.length > 0)
        needsUpdate = true; 
      c.freeIfNecessary();
    } 
    this.components.removeRange(cIndex, endIndex);
    if (needsUpdate)
      updateComponentOffsets(cIndex); 
    return this;
  }
  
  public Iterator<ByteBuf> iterator() {
    ensureAccessible();
    if (this.components.isEmpty())
      return EMPTY_ITERATOR; 
    return new CompositeByteBufIterator();
  }
  
  public List<ByteBuf> decompose(int offset, int length) {
    checkIndex(offset, length);
    if (length == 0)
      return Collections.emptyList(); 
    int componentId = toComponentIndex(offset);
    List<ByteBuf> slice = new ArrayList<ByteBuf>(this.components.size());
    Component firstC = this.components.get(componentId);
    ByteBuf first = firstC.buf.duplicate();
    first.readerIndex(offset - firstC.offset);
    ByteBuf buf = first;
    int bytesToSlice = length;
    do {
      int readableBytes = buf.readableBytes();
      if (bytesToSlice <= readableBytes) {
        buf.writerIndex(buf.readerIndex() + bytesToSlice);
        slice.add(buf);
        break;
      } 
      slice.add(buf);
      bytesToSlice -= readableBytes;
      componentId++;
      buf = (this.components.get(componentId)).buf.duplicate();
    } while (bytesToSlice > 0);
    for (int i = 0; i < slice.size(); i++)
      slice.set(i, ((ByteBuf)slice.get(i)).slice()); 
    return slice;
  }
  
  public boolean isDirect() {
    int size = this.components.size();
    if (size == 0)
      return false; 
    for (int i = 0; i < size; i++) {
      if (!(this.components.get(i)).buf.isDirect())
        return false; 
    } 
    return true;
  }
  
  public boolean hasArray() {
    switch (this.components.size()) {
      case 0:
        return true;
      case 1:
        return (this.components.get(0)).buf.hasArray();
    } 
    return false;
  }
  
  public byte[] array() {
    switch (this.components.size()) {
      case 0:
        return EmptyArrays.EMPTY_BYTES;
      case 1:
        return (this.components.get(0)).buf.array();
    } 
    throw new UnsupportedOperationException();
  }
  
  public int arrayOffset() {
    switch (this.components.size()) {
      case 0:
        return 0;
      case 1:
        return (this.components.get(0)).buf.arrayOffset();
    } 
    throw new UnsupportedOperationException();
  }
  
  public boolean hasMemoryAddress() {
    switch (this.components.size()) {
      case 0:
        return Unpooled.EMPTY_BUFFER.hasMemoryAddress();
      case 1:
        return (this.components.get(0)).buf.hasMemoryAddress();
    } 
    return false;
  }
  
  public long memoryAddress() {
    switch (this.components.size()) {
      case 0:
        return Unpooled.EMPTY_BUFFER.memoryAddress();
      case 1:
        return (this.components.get(0)).buf.memoryAddress();
    } 
    throw new UnsupportedOperationException();
  }
  
  public int capacity() {
    int numComponents = this.components.size();
    if (numComponents == 0)
      return 0; 
    return (this.components.get(numComponents - 1)).endOffset;
  }
  
  public CompositeByteBuf capacity(int newCapacity) {
    checkNewCapacity(newCapacity);
    int oldCapacity = capacity();
    if (newCapacity > oldCapacity) {
      int paddingLength = newCapacity - oldCapacity;
      int nComponents = this.components.size();
      if (nComponents < this.maxNumComponents) {
        ByteBuf padding = allocBuffer(paddingLength);
        padding.setIndex(0, paddingLength);
        addComponent0(false, this.components.size(), padding);
      } else {
        ByteBuf padding = allocBuffer(paddingLength);
        padding.setIndex(0, paddingLength);
        addComponent0(false, this.components.size(), padding);
        consolidateIfNeeded();
      } 
    } else if (newCapacity < oldCapacity) {
      int bytesToTrim = oldCapacity - newCapacity;
      for (ListIterator<Component> i = this.components.listIterator(this.components.size()); i.hasPrevious(); ) {
        Component c = i.previous();
        if (bytesToTrim >= c.length) {
          bytesToTrim -= c.length;
          i.remove();
          continue;
        } 
        Component newC = new Component(c.buf.slice(0, c.length - bytesToTrim));
        newC.offset = c.offset;
        newC.endOffset = newC.offset + newC.length;
        i.set(newC);
      } 
      if (readerIndex() > newCapacity) {
        setIndex(newCapacity, newCapacity);
      } else if (writerIndex() > newCapacity) {
        writerIndex(newCapacity);
      } 
    } 
    return this;
  }
  
  public ByteBufAllocator alloc() {
    return this.alloc;
  }
  
  public ByteOrder order() {
    return ByteOrder.BIG_ENDIAN;
  }
  
  public int numComponents() {
    return this.components.size();
  }
  
  public int maxNumComponents() {
    return this.maxNumComponents;
  }
  
  public int toComponentIndex(int offset) {
    checkIndex(offset);
    for (int low = 0, high = this.components.size(); low <= high; ) {
      int mid = low + high >>> 1;
      Component c = this.components.get(mid);
      if (offset >= c.endOffset) {
        low = mid + 1;
        continue;
      } 
      if (offset < c.offset) {
        high = mid - 1;
        continue;
      } 
      return mid;
    } 
    throw new Error("should not reach here");
  }
  
  public int toByteIndex(int cIndex) {
    checkComponentIndex(cIndex);
    return (this.components.get(cIndex)).offset;
  }
  
  public byte getByte(int index) {
    return _getByte(index);
  }
  
  protected byte _getByte(int index) {
    Component c = findComponent(index);
    return c.buf.getByte(index - c.offset);
  }
  
  protected short _getShort(int index) {
    Component c = findComponent(index);
    if (index + 2 <= c.endOffset)
      return c.buf.getShort(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF); 
    return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
  }
  
  protected short _getShortLE(int index) {
    Component c = findComponent(index);
    if (index + 2 <= c.endOffset)
      return c.buf.getShortLE(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8); 
    return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
  }
  
  protected int _getUnsignedMedium(int index) {
    Component c = findComponent(index);
    if (index + 3 <= c.endOffset)
      return c.buf.getUnsignedMedium(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return (_getShort(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF; 
    return _getShort(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
  }
  
  protected int _getUnsignedMediumLE(int index) {
    Component c = findComponent(index);
    if (index + 3 <= c.endOffset)
      return c.buf.getUnsignedMediumLE(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return _getShortLE(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16; 
    return (_getShortLE(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
  }
  
  protected int _getInt(int index) {
    Component c = findComponent(index);
    if (index + 4 <= c.endOffset)
      return c.buf.getInt(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return (_getShort(index) & 0xFFFF) << 16 | _getShort(index + 2) & 0xFFFF; 
    return _getShort(index) & 0xFFFF | (_getShort(index + 2) & 0xFFFF) << 16;
  }
  
  protected int _getIntLE(int index) {
    Component c = findComponent(index);
    if (index + 4 <= c.endOffset)
      return c.buf.getIntLE(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return _getShortLE(index) & 0xFFFF | (_getShortLE(index + 2) & 0xFFFF) << 16; 
    return (_getShortLE(index) & 0xFFFF) << 16 | _getShortLE(index + 2) & 0xFFFF;
  }
  
  protected long _getLong(int index) {
    Component c = findComponent(index);
    if (index + 8 <= c.endOffset)
      return c.buf.getLong(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return (_getInt(index) & 0xFFFFFFFFL) << 32L | _getInt(index + 4) & 0xFFFFFFFFL; 
    return _getInt(index) & 0xFFFFFFFFL | (_getInt(index + 4) & 0xFFFFFFFFL) << 32L;
  }
  
  protected long _getLongLE(int index) {
    Component c = findComponent(index);
    if (index + 8 <= c.endOffset)
      return c.buf.getLongLE(index - c.offset); 
    if (order() == ByteOrder.BIG_ENDIAN)
      return _getIntLE(index) & 0xFFFFFFFFL | (_getIntLE(index + 4) & 0xFFFFFFFFL) << 32L; 
    return (_getIntLE(index) & 0xFFFFFFFFL) << 32L | _getIntLE(index + 4) & 0xFFFFFFFFL;
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    checkDstIndex(index, length, dstIndex, dst.length);
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.getBytes(index - adjustment, dst, dstIndex, localLength);
      index += localLength;
      dstIndex += localLength;
      length -= localLength;
      i++;
    } 
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuffer dst) {
    int limit = dst.limit();
    int length = dst.remaining();
    checkIndex(index, length);
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    try {
      while (length > 0) {
        Component c = this.components.get(i);
        ByteBuf s = c.buf;
        int adjustment = c.offset;
        int localLength = Math.min(length, s.capacity() - index - adjustment);
        dst.limit(dst.position() + localLength);
        s.getBytes(index - adjustment, dst);
        index += localLength;
        length -= localLength;
        i++;
      } 
    } finally {
      dst.limit(limit);
    } 
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    checkDstIndex(index, length, dstIndex, dst.capacity());
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.getBytes(index - adjustment, dst, dstIndex, localLength);
      index += localLength;
      dstIndex += localLength;
      length -= localLength;
      i++;
    } 
    return this;
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    int count = nioBufferCount();
    if (count == 1)
      return out.write(internalNioBuffer(index, length)); 
    long writtenBytes = out.write(nioBuffers(index, length));
    if (writtenBytes > 2147483647L)
      return Integer.MAX_VALUE; 
    return (int)writtenBytes;
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    int count = nioBufferCount();
    if (count == 1)
      return out.write(internalNioBuffer(index, length), position); 
    long writtenBytes = 0L;
    for (ByteBuffer buf : nioBuffers(index, length))
      writtenBytes += out.write(buf, position + writtenBytes); 
    if (writtenBytes > 2147483647L)
      return Integer.MAX_VALUE; 
    return (int)writtenBytes;
  }
  
  public CompositeByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    checkIndex(index, length);
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.getBytes(index - adjustment, out, localLength);
      index += localLength;
      length -= localLength;
      i++;
    } 
    return this;
  }
  
  public CompositeByteBuf setByte(int index, int value) {
    Component c = findComponent(index);
    c.buf.setByte(index - c.offset, value);
    return this;
  }
  
  protected void _setByte(int index, int value) {
    setByte(index, value);
  }
  
  public CompositeByteBuf setShort(int index, int value) {
    return (CompositeByteBuf)super.setShort(index, value);
  }
  
  protected void _setShort(int index, int value) {
    Component c = findComponent(index);
    if (index + 2 <= c.endOffset) {
      c.buf.setShort(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setByte(index, (byte)(value >>> 8));
      _setByte(index + 1, (byte)value);
    } else {
      _setByte(index, (byte)value);
      _setByte(index + 1, (byte)(value >>> 8));
    } 
  }
  
  protected void _setShortLE(int index, int value) {
    Component c = findComponent(index);
    if (index + 2 <= c.endOffset) {
      c.buf.setShortLE(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setByte(index, (byte)value);
      _setByte(index + 1, (byte)(value >>> 8));
    } else {
      _setByte(index, (byte)(value >>> 8));
      _setByte(index + 1, (byte)value);
    } 
  }
  
  public CompositeByteBuf setMedium(int index, int value) {
    return (CompositeByteBuf)super.setMedium(index, value);
  }
  
  protected void _setMedium(int index, int value) {
    Component c = findComponent(index);
    if (index + 3 <= c.endOffset) {
      c.buf.setMedium(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setShort(index, (short)(value >> 8));
      _setByte(index + 2, (byte)value);
    } else {
      _setShort(index, (short)value);
      _setByte(index + 2, (byte)(value >>> 16));
    } 
  }
  
  protected void _setMediumLE(int index, int value) {
    Component c = findComponent(index);
    if (index + 3 <= c.endOffset) {
      c.buf.setMediumLE(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setShortLE(index, (short)value);
      _setByte(index + 2, (byte)(value >>> 16));
    } else {
      _setShortLE(index, (short)(value >> 8));
      _setByte(index + 2, (byte)value);
    } 
  }
  
  public CompositeByteBuf setInt(int index, int value) {
    return (CompositeByteBuf)super.setInt(index, value);
  }
  
  protected void _setInt(int index, int value) {
    Component c = findComponent(index);
    if (index + 4 <= c.endOffset) {
      c.buf.setInt(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setShort(index, (short)(value >>> 16));
      _setShort(index + 2, (short)value);
    } else {
      _setShort(index, (short)value);
      _setShort(index + 2, (short)(value >>> 16));
    } 
  }
  
  protected void _setIntLE(int index, int value) {
    Component c = findComponent(index);
    if (index + 4 <= c.endOffset) {
      c.buf.setIntLE(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setShortLE(index, (short)value);
      _setShortLE(index + 2, (short)(value >>> 16));
    } else {
      _setShortLE(index, (short)(value >>> 16));
      _setShortLE(index + 2, (short)value);
    } 
  }
  
  public CompositeByteBuf setLong(int index, long value) {
    return (CompositeByteBuf)super.setLong(index, value);
  }
  
  protected void _setLong(int index, long value) {
    Component c = findComponent(index);
    if (index + 8 <= c.endOffset) {
      c.buf.setLong(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setInt(index, (int)(value >>> 32L));
      _setInt(index + 4, (int)value);
    } else {
      _setInt(index, (int)value);
      _setInt(index + 4, (int)(value >>> 32L));
    } 
  }
  
  protected void _setLongLE(int index, long value) {
    Component c = findComponent(index);
    if (index + 8 <= c.endOffset) {
      c.buf.setLongLE(index - c.offset, value);
    } else if (order() == ByteOrder.BIG_ENDIAN) {
      _setIntLE(index, (int)value);
      _setIntLE(index + 4, (int)(value >>> 32L));
    } else {
      _setIntLE(index, (int)(value >>> 32L));
      _setIntLE(index + 4, (int)value);
    } 
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    checkSrcIndex(index, length, srcIndex, src.length);
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.setBytes(index - adjustment, src, srcIndex, localLength);
      index += localLength;
      srcIndex += localLength;
      length -= localLength;
      i++;
    } 
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuffer src) {
    int limit = src.limit();
    int length = src.remaining();
    checkIndex(index, length);
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    try {
      while (length > 0) {
        Component c = this.components.get(i);
        ByteBuf s = c.buf;
        int adjustment = c.offset;
        int localLength = Math.min(length, s.capacity() - index - adjustment);
        src.limit(src.position() + localLength);
        s.setBytes(index - adjustment, src);
        index += localLength;
        length -= localLength;
        i++;
      } 
    } finally {
      src.limit(limit);
    } 
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    checkSrcIndex(index, length, srcIndex, src.capacity());
    if (length == 0)
      return this; 
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.setBytes(index - adjustment, src, srcIndex, localLength);
      index += localLength;
      srcIndex += localLength;
      length -= localLength;
      i++;
    } 
    return this;
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    checkIndex(index, length);
    if (length == 0)
      return in.read(EmptyArrays.EMPTY_BYTES); 
    int i = toComponentIndex(index);
    int readBytes = 0;
    do {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      if (localLength == 0) {
        i++;
      } else {
        int localReadBytes = s.setBytes(index - adjustment, in, localLength);
        if (localReadBytes < 0) {
          if (readBytes == 0)
            return -1; 
          break;
        } 
        if (localReadBytes == localLength) {
          index += localLength;
          length -= localLength;
          readBytes += localLength;
          i++;
        } else {
          index += localReadBytes;
          length -= localReadBytes;
          readBytes += localReadBytes;
        } 
      } 
    } while (length > 0);
    return readBytes;
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    checkIndex(index, length);
    if (length == 0)
      return in.read(EMPTY_NIO_BUFFER); 
    int i = toComponentIndex(index);
    int readBytes = 0;
    do {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      if (localLength == 0) {
        i++;
      } else {
        int localReadBytes = s.setBytes(index - adjustment, in, localLength);
        if (localReadBytes == 0)
          break; 
        if (localReadBytes < 0) {
          if (readBytes == 0)
            return -1; 
          break;
        } 
        if (localReadBytes == localLength) {
          index += localLength;
          length -= localLength;
          readBytes += localLength;
          i++;
        } else {
          index += localReadBytes;
          length -= localReadBytes;
          readBytes += localReadBytes;
        } 
      } 
    } while (length > 0);
    return readBytes;
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    checkIndex(index, length);
    if (length == 0)
      return in.read(EMPTY_NIO_BUFFER, position); 
    int i = toComponentIndex(index);
    int readBytes = 0;
    do {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      if (localLength == 0) {
        i++;
      } else {
        int localReadBytes = s.setBytes(index - adjustment, in, position + readBytes, localLength);
        if (localReadBytes == 0)
          break; 
        if (localReadBytes < 0) {
          if (readBytes == 0)
            return -1; 
          break;
        } 
        if (localReadBytes == localLength) {
          index += localLength;
          length -= localLength;
          readBytes += localLength;
          i++;
        } else {
          index += localReadBytes;
          length -= localReadBytes;
          readBytes += localReadBytes;
        } 
      } 
    } while (length > 0);
    return readBytes;
  }
  
  public ByteBuf copy(int index, int length) {
    checkIndex(index, length);
    ByteBuf dst = allocBuffer(length);
    if (length != 0)
      copyTo(index, length, toComponentIndex(index), dst); 
    return dst;
  }
  
  private void copyTo(int index, int length, int componentId, ByteBuf dst) {
    int dstIndex = 0;
    int i = componentId;
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      s.getBytes(index - adjustment, dst, dstIndex, localLength);
      index += localLength;
      dstIndex += localLength;
      length -= localLength;
      i++;
    } 
    dst.writerIndex(dst.capacity());
  }
  
  public ByteBuf component(int cIndex) {
    return internalComponent(cIndex).duplicate();
  }
  
  public ByteBuf componentAtOffset(int offset) {
    return internalComponentAtOffset(offset).duplicate();
  }
  
  public ByteBuf internalComponent(int cIndex) {
    checkComponentIndex(cIndex);
    return (this.components.get(cIndex)).buf;
  }
  
  public ByteBuf internalComponentAtOffset(int offset) {
    return (findComponent(offset)).buf;
  }
  
  private Component findComponent(int offset) {
    checkIndex(offset);
    for (int low = 0, high = this.components.size(); low <= high; ) {
      int mid = low + high >>> 1;
      Component c = this.components.get(mid);
      if (offset >= c.endOffset) {
        low = mid + 1;
        continue;
      } 
      if (offset < c.offset) {
        high = mid - 1;
        continue;
      } 
      assert c.length != 0;
      return c;
    } 
    throw new Error("should not reach here");
  }
  
  public int nioBufferCount() {
    switch (this.components.size()) {
      case 0:
        return 1;
      case 1:
        return (this.components.get(0)).buf.nioBufferCount();
    } 
    int count = 0;
    int componentsCount = this.components.size();
    for (int i = 0; i < componentsCount; i++) {
      Component c = this.components.get(i);
      count += c.buf.nioBufferCount();
    } 
    return count;
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    switch (this.components.size()) {
      case 0:
        return EMPTY_NIO_BUFFER;
      case 1:
        return (this.components.get(0)).buf.internalNioBuffer(index, length);
    } 
    throw new UnsupportedOperationException();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    ByteBuf buf;
    checkIndex(index, length);
    switch (this.components.size()) {
      case 0:
        return EMPTY_NIO_BUFFER;
      case 1:
        buf = (this.components.get(0)).buf;
        if (buf.nioBufferCount() == 1)
          return (this.components.get(0)).buf.nioBuffer(index, length); 
        break;
    } 
    ByteBuffer merged = ByteBuffer.allocate(length).order(order());
    ByteBuffer[] buffers = nioBuffers(index, length);
    for (ByteBuffer byteBuffer : buffers)
      merged.put(byteBuffer); 
    merged.flip();
    return merged;
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    checkIndex(index, length);
    if (length == 0)
      return new ByteBuffer[] { EMPTY_NIO_BUFFER }; 
    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>(this.components.size());
    int i = toComponentIndex(index);
    while (length > 0) {
      Component c = this.components.get(i);
      ByteBuf s = c.buf;
      int adjustment = c.offset;
      int localLength = Math.min(length, s.capacity() - index - adjustment);
      switch (s.nioBufferCount()) {
        case 0:
          throw new UnsupportedOperationException();
        case 1:
          buffers.add(s.nioBuffer(index - adjustment, localLength));
          break;
        default:
          Collections.addAll(buffers, s.nioBuffers(index - adjustment, localLength));
          break;
      } 
      index += localLength;
      length -= localLength;
      i++;
    } 
    return buffers.<ByteBuffer>toArray(new ByteBuffer[buffers.size()]);
  }
  
  public CompositeByteBuf consolidate() {
    ensureAccessible();
    int numComponents = numComponents();
    if (numComponents <= 1)
      return this; 
    Component last = this.components.get(numComponents - 1);
    int capacity = last.endOffset;
    ByteBuf consolidated = allocBuffer(capacity);
    for (int i = 0; i < numComponents; i++) {
      Component c = this.components.get(i);
      ByteBuf b = c.buf;
      consolidated.writeBytes(b);
      c.freeIfNecessary();
    } 
    this.components.clear();
    this.components.add(new Component(consolidated));
    updateComponentOffsets(0);
    return this;
  }
  
  public CompositeByteBuf consolidate(int cIndex, int numComponents) {
    checkComponentIndex(cIndex, numComponents);
    if (numComponents <= 1)
      return this; 
    int endCIndex = cIndex + numComponents;
    Component last = this.components.get(endCIndex - 1);
    int capacity = last.endOffset - (this.components.get(cIndex)).offset;
    ByteBuf consolidated = allocBuffer(capacity);
    for (int i = cIndex; i < endCIndex; i++) {
      Component c = this.components.get(i);
      ByteBuf b = c.buf;
      consolidated.writeBytes(b);
      c.freeIfNecessary();
    } 
    this.components.removeRange(cIndex + 1, endCIndex);
    this.components.set(cIndex, new Component(consolidated));
    updateComponentOffsets(cIndex);
    return this;
  }
  
  public CompositeByteBuf discardReadComponents() {
    ensureAccessible();
    int readerIndex = readerIndex();
    if (readerIndex == 0)
      return this; 
    int writerIndex = writerIndex();
    if (readerIndex == writerIndex && writerIndex == capacity()) {
      int size = this.components.size();
      for (int j = 0; j < size; j++)
        this.components.get(j).freeIfNecessary(); 
      this.components.clear();
      setIndex(0, 0);
      adjustMarkers(readerIndex);
      return this;
    } 
    int firstComponentId = toComponentIndex(readerIndex);
    for (int i = 0; i < firstComponentId; i++)
      this.components.get(i).freeIfNecessary(); 
    this.components.removeRange(0, firstComponentId);
    Component first = this.components.get(0);
    int offset = first.offset;
    updateComponentOffsets(0);
    setIndex(readerIndex - offset, writerIndex - offset);
    adjustMarkers(offset);
    return this;
  }
  
  public CompositeByteBuf discardReadBytes() {
    ensureAccessible();
    int readerIndex = readerIndex();
    if (readerIndex == 0)
      return this; 
    int writerIndex = writerIndex();
    if (readerIndex == writerIndex && writerIndex == capacity()) {
      int size = this.components.size();
      for (int j = 0; j < size; j++)
        this.components.get(j).freeIfNecessary(); 
      this.components.clear();
      setIndex(0, 0);
      adjustMarkers(readerIndex);
      return this;
    } 
    int firstComponentId = toComponentIndex(readerIndex);
    for (int i = 0; i < firstComponentId; i++)
      this.components.get(i).freeIfNecessary(); 
    Component c = this.components.get(firstComponentId);
    int adjustment = readerIndex - c.offset;
    if (adjustment == c.length) {
      firstComponentId++;
    } else {
      Component newC = new Component(c.buf.slice(adjustment, c.length - adjustment));
      this.components.set(firstComponentId, newC);
    } 
    this.components.removeRange(0, firstComponentId);
    updateComponentOffsets(0);
    setIndex(0, writerIndex - readerIndex);
    adjustMarkers(readerIndex);
    return this;
  }
  
  private ByteBuf allocBuffer(int capacity) {
    return this.direct ? alloc().directBuffer(capacity) : alloc().heapBuffer(capacity);
  }
  
  public String toString() {
    String result = super.toString();
    result = result.substring(0, result.length() - 1);
    return result + ", components=" + this.components.size() + ')';
  }
  
  private static final class Component {
    final ByteBuf buf;
    
    final int length;
    
    int offset;
    
    int endOffset;
    
    Component(ByteBuf buf) {
      this.buf = buf;
      this.length = buf.readableBytes();
    }
    
    void freeIfNecessary() {
      this.buf.release();
    }
  }
  
  public CompositeByteBuf readerIndex(int readerIndex) {
    return (CompositeByteBuf)super.readerIndex(readerIndex);
  }
  
  public CompositeByteBuf writerIndex(int writerIndex) {
    return (CompositeByteBuf)super.writerIndex(writerIndex);
  }
  
  public CompositeByteBuf setIndex(int readerIndex, int writerIndex) {
    return (CompositeByteBuf)super.setIndex(readerIndex, writerIndex);
  }
  
  public CompositeByteBuf clear() {
    return (CompositeByteBuf)super.clear();
  }
  
  public CompositeByteBuf markReaderIndex() {
    return (CompositeByteBuf)super.markReaderIndex();
  }
  
  public CompositeByteBuf resetReaderIndex() {
    return (CompositeByteBuf)super.resetReaderIndex();
  }
  
  public CompositeByteBuf markWriterIndex() {
    return (CompositeByteBuf)super.markWriterIndex();
  }
  
  public CompositeByteBuf resetWriterIndex() {
    return (CompositeByteBuf)super.resetWriterIndex();
  }
  
  public CompositeByteBuf ensureWritable(int minWritableBytes) {
    return (CompositeByteBuf)super.ensureWritable(minWritableBytes);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst) {
    return (CompositeByteBuf)super.getBytes(index, dst);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int length) {
    return (CompositeByteBuf)super.getBytes(index, dst, length);
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst) {
    return (CompositeByteBuf)super.getBytes(index, dst);
  }
  
  public CompositeByteBuf setBoolean(int index, boolean value) {
    return (CompositeByteBuf)super.setBoolean(index, value);
  }
  
  public CompositeByteBuf setChar(int index, int value) {
    return (CompositeByteBuf)super.setChar(index, value);
  }
  
  public CompositeByteBuf setFloat(int index, float value) {
    return (CompositeByteBuf)super.setFloat(index, value);
  }
  
  public CompositeByteBuf setDouble(int index, double value) {
    return (CompositeByteBuf)super.setDouble(index, value);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src) {
    return (CompositeByteBuf)super.setBytes(index, src);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int length) {
    return (CompositeByteBuf)super.setBytes(index, src, length);
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src) {
    return (CompositeByteBuf)super.setBytes(index, src);
  }
  
  public CompositeByteBuf setZero(int index, int length) {
    return (CompositeByteBuf)super.setZero(index, length);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst) {
    return (CompositeByteBuf)super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int length) {
    return (CompositeByteBuf)super.readBytes(dst, length);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
    return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
  }
  
  public CompositeByteBuf readBytes(byte[] dst) {
    return (CompositeByteBuf)super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length) {
    return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
  }
  
  public CompositeByteBuf readBytes(ByteBuffer dst) {
    return (CompositeByteBuf)super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(OutputStream out, int length) throws IOException {
    return (CompositeByteBuf)super.readBytes(out, length);
  }
  
  public CompositeByteBuf skipBytes(int length) {
    return (CompositeByteBuf)super.skipBytes(length);
  }
  
  public CompositeByteBuf writeBoolean(boolean value) {
    return (CompositeByteBuf)super.writeBoolean(value);
  }
  
  public CompositeByteBuf writeByte(int value) {
    return (CompositeByteBuf)super.writeByte(value);
  }
  
  public CompositeByteBuf writeShort(int value) {
    return (CompositeByteBuf)super.writeShort(value);
  }
  
  public CompositeByteBuf writeMedium(int value) {
    return (CompositeByteBuf)super.writeMedium(value);
  }
  
  public CompositeByteBuf writeInt(int value) {
    return (CompositeByteBuf)super.writeInt(value);
  }
  
  public CompositeByteBuf writeLong(long value) {
    return (CompositeByteBuf)super.writeLong(value);
  }
  
  public CompositeByteBuf writeChar(int value) {
    return (CompositeByteBuf)super.writeChar(value);
  }
  
  public CompositeByteBuf writeFloat(float value) {
    return (CompositeByteBuf)super.writeFloat(value);
  }
  
  public CompositeByteBuf writeDouble(double value) {
    return (CompositeByteBuf)super.writeDouble(value);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src) {
    return (CompositeByteBuf)super.writeBytes(src);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int length) {
    return (CompositeByteBuf)super.writeBytes(src, length);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
    return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
  }
  
  public CompositeByteBuf writeBytes(byte[] src) {
    return (CompositeByteBuf)super.writeBytes(src);
  }
  
  public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length) {
    return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
  }
  
  public CompositeByteBuf writeBytes(ByteBuffer src) {
    return (CompositeByteBuf)super.writeBytes(src);
  }
  
  public CompositeByteBuf writeZero(int length) {
    return (CompositeByteBuf)super.writeZero(length);
  }
  
  public CompositeByteBuf retain(int increment) {
    return (CompositeByteBuf)super.retain(increment);
  }
  
  public CompositeByteBuf retain() {
    return (CompositeByteBuf)super.retain();
  }
  
  public CompositeByteBuf touch() {
    return this;
  }
  
  public CompositeByteBuf touch(Object hint) {
    return this;
  }
  
  public ByteBuffer[] nioBuffers() {
    return nioBuffers(readerIndex(), readableBytes());
  }
  
  public CompositeByteBuf discardSomeReadBytes() {
    return discardReadComponents();
  }
  
  protected void deallocate() {
    if (this.freed)
      return; 
    this.freed = true;
    int size = this.components.size();
    for (int i = 0; i < size; i++)
      this.components.get(i).freeIfNecessary(); 
  }
  
  public ByteBuf unwrap() {
    return null;
  }
  
  private final class CompositeByteBufIterator implements Iterator<ByteBuf> {
    private final int size = CompositeByteBuf.this.components.size();
    
    private int index;
    
    public boolean hasNext() {
      return (this.size > this.index);
    }
    
    public ByteBuf next() {
      if (this.size != CompositeByteBuf.this.components.size())
        throw new ConcurrentModificationException(); 
      if (!hasNext())
        throw new NoSuchElementException(); 
      try {
        return (CompositeByteBuf.this.components.get(this.index++)).buf;
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      } 
    }
    
    public void remove() {
      throw new UnsupportedOperationException("Read-Only");
    }
    
    private CompositeByteBufIterator() {}
  }
  
  private static final class ComponentList extends ArrayList<Component> {
    ComponentList(int initialCapacity) {
      super(initialCapacity);
    }
    
    public void removeRange(int fromIndex, int toIndex) {
      super.removeRange(fromIndex, toIndex);
    }
  }
}
