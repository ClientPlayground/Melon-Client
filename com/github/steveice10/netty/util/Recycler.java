package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.ObjectCleaner;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Recycler<T> {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
  
  private static final Handle NOOP_HANDLE = new Handle() {
      public void recycle(Object object) {}
    };
  
  private static final AtomicInteger ID_GENERATOR = new AtomicInteger(-2147483648);
  
  private static final int OWN_THREAD_ID = ID_GENERATOR.getAndIncrement();
  
  private static final int DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD = 4096;
  
  private static final int DEFAULT_MAX_CAPACITY_PER_THREAD;
  
  private static final int INITIAL_CAPACITY;
  
  static {
    int maxCapacityPerThread = SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.maxCapacityPerThread", 
        SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.maxCapacity", 4096));
    if (maxCapacityPerThread < 0)
      maxCapacityPerThread = 4096; 
    DEFAULT_MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
  }
  
  private static final int MAX_SHARED_CAPACITY_FACTOR = Math.max(2, 
      SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.maxSharedCapacityFactor", 2));
  
  private static final int MAX_DELAYED_QUEUES_PER_THREAD = Math.max(0, 
      SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.maxDelayedQueuesPerThread", 
        
        NettyRuntime.availableProcessors() * 2));
  
  private static final int LINK_CAPACITY = MathUtil.safeFindNextPositivePowerOfTwo(
      Math.max(SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.linkCapacity", 16), 16));
  
  private static final int RATIO = MathUtil.safeFindNextPositivePowerOfTwo(SystemPropertyUtil.getInt("com.github.steveice10.netty.recycler.ratio", 8));
  
  private final int maxCapacityPerThread;
  
  private final int maxSharedCapacityFactor;
  
  private final int ratioMask;
  
  private final int maxDelayedQueuesPerThread;
  
  static {
    if (logger.isDebugEnabled())
      if (DEFAULT_MAX_CAPACITY_PER_THREAD == 0) {
        logger.debug("-Dio.netty.recycler.maxCapacityPerThread: disabled");
        logger.debug("-Dio.netty.recycler.maxSharedCapacityFactor: disabled");
        logger.debug("-Dio.netty.recycler.linkCapacity: disabled");
        logger.debug("-Dio.netty.recycler.ratio: disabled");
      } else {
        logger.debug("-Dio.netty.recycler.maxCapacityPerThread: {}", Integer.valueOf(DEFAULT_MAX_CAPACITY_PER_THREAD));
        logger.debug("-Dio.netty.recycler.maxSharedCapacityFactor: {}", Integer.valueOf(MAX_SHARED_CAPACITY_FACTOR));
        logger.debug("-Dio.netty.recycler.linkCapacity: {}", Integer.valueOf(LINK_CAPACITY));
        logger.debug("-Dio.netty.recycler.ratio: {}", Integer.valueOf(RATIO));
      }  
    INITIAL_CAPACITY = Math.min(DEFAULT_MAX_CAPACITY_PER_THREAD, 256);
    DELAYED_RECYCLED = new FastThreadLocal<Map<Stack<?>, WeakOrderQueue>>() {
        protected Map<Recycler.Stack<?>, Recycler.WeakOrderQueue> initialValue() {
          return new WeakHashMap<Recycler.Stack<?>, Recycler.WeakOrderQueue>();
        }
      };
  }
  
  private final FastThreadLocal<Stack<T>> threadLocal = new FastThreadLocal<Stack<T>>() {
      protected Recycler.Stack<T> initialValue() {
        return new Recycler.Stack<T>(Recycler.this, Thread.currentThread(), Recycler.this.maxCapacityPerThread, Recycler.this.maxSharedCapacityFactor, Recycler.this.ratioMask, Recycler.this.maxDelayedQueuesPerThread);
      }
      
      protected void onRemoval(Recycler.Stack<T> value) {
        if (value.threadRef.get() == Thread.currentThread() && Recycler.DELAYED_RECYCLED.isSet())
          ((Map)Recycler.DELAYED_RECYCLED.get()).remove(value); 
      }
    };
  
  private static final FastThreadLocal<Map<Stack<?>, WeakOrderQueue>> DELAYED_RECYCLED;
  
  protected Recycler() {
    this(DEFAULT_MAX_CAPACITY_PER_THREAD);
  }
  
  protected Recycler(int maxCapacityPerThread) {
    this(maxCapacityPerThread, MAX_SHARED_CAPACITY_FACTOR);
  }
  
  protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor) {
    this(maxCapacityPerThread, maxSharedCapacityFactor, RATIO, MAX_DELAYED_QUEUES_PER_THREAD);
  }
  
  protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread) {
    this.ratioMask = MathUtil.safeFindNextPositivePowerOfTwo(ratio) - 1;
    if (maxCapacityPerThread <= 0) {
      this.maxCapacityPerThread = 0;
      this.maxSharedCapacityFactor = 1;
      this.maxDelayedQueuesPerThread = 0;
    } else {
      this.maxCapacityPerThread = maxCapacityPerThread;
      this.maxSharedCapacityFactor = Math.max(1, maxSharedCapacityFactor);
      this.maxDelayedQueuesPerThread = Math.max(0, maxDelayedQueuesPerThread);
    } 
  }
  
  public final T get() {
    if (this.maxCapacityPerThread == 0)
      return newObject(NOOP_HANDLE); 
    Stack<T> stack = (Stack<T>)this.threadLocal.get();
    DefaultHandle<T> handle = stack.pop();
    if (handle == null) {
      handle = stack.newHandle();
      handle.value = newObject(handle);
    } 
    return (T)handle.value;
  }
  
  @Deprecated
  public final boolean recycle(T o, Handle<T> handle) {
    if (handle == NOOP_HANDLE)
      return false; 
    DefaultHandle<T> h = (DefaultHandle<T>)handle;
    if (h.stack.parent != this)
      return false; 
    h.recycle(o);
    return true;
  }
  
  final int threadLocalCapacity() {
    return ((Stack)this.threadLocal.get()).elements.length;
  }
  
  final int threadLocalSize() {
    return ((Stack)this.threadLocal.get()).size;
  }
  
  protected abstract T newObject(Handle<T> paramHandle);
  
  static final class DefaultHandle<T> implements Handle<T> {
    private int lastRecycledId;
    
    private int recycleId;
    
    boolean hasBeenRecycled;
    
    private Recycler.Stack<?> stack;
    
    private Object value;
    
    DefaultHandle(Recycler.Stack<?> stack) {
      this.stack = stack;
    }
    
    public void recycle(Object object) {
      if (object != this.value)
        throw new IllegalArgumentException("object does not belong to handle"); 
      this.stack.push(this);
    }
  }
  
  private static final class WeakOrderQueue {
    static final WeakOrderQueue DUMMY = new WeakOrderQueue();
    
    private final Head head;
    
    private Link tail;
    
    private WeakOrderQueue next;
    
    private final WeakReference<Thread> owner;
    
    static final class Link extends AtomicInteger {
      private final Recycler.DefaultHandle<?>[] elements = (Recycler.DefaultHandle<?>[])new Recycler.DefaultHandle[Recycler.LINK_CAPACITY];
      
      private int readIndex;
      
      Link next;
    }
    
    static final class Head implements Runnable {
      private final AtomicInteger availableSharedCapacity;
      
      Recycler.WeakOrderQueue.Link link;
      
      Head(AtomicInteger availableSharedCapacity) {
        this.availableSharedCapacity = availableSharedCapacity;
      }
      
      public void run() {
        Recycler.WeakOrderQueue.Link head = this.link;
        while (head != null) {
          reclaimSpace(Recycler.LINK_CAPACITY);
          head = head.next;
        } 
      }
      
      void reclaimSpace(int space) {
        assert space >= 0;
        this.availableSharedCapacity.addAndGet(space);
      }
      
      boolean reserveSpace(int space) {
        return reserveSpace(this.availableSharedCapacity, space);
      }
      
      static boolean reserveSpace(AtomicInteger availableSharedCapacity, int space) {
        assert space >= 0;
        while (true) {
          int available = availableSharedCapacity.get();
          if (available < space)
            return false; 
          if (availableSharedCapacity.compareAndSet(available, available - space))
            return true; 
        } 
      }
    }
    
    private final int id = Recycler.ID_GENERATOR.getAndIncrement();
    
    private WeakOrderQueue() {
      this.owner = null;
      this.head = new Head(null);
    }
    
    private WeakOrderQueue(Recycler.Stack<?> stack, Thread thread) {
      this.tail = new Link();
      this.head = new Head(stack.availableSharedCapacity);
      this.head.link = this.tail;
      this.owner = new WeakReference<Thread>(thread);
    }
    
    static WeakOrderQueue newQueue(Recycler.Stack<?> stack, Thread thread) {
      WeakOrderQueue queue = new WeakOrderQueue(stack, thread);
      stack.setHead(queue);
      Head head = queue.head;
      ObjectCleaner.register(queue, head);
      return queue;
    }
    
    private void setNext(WeakOrderQueue next) {
      assert next != this;
      this.next = next;
    }
    
    static WeakOrderQueue allocate(Recycler.Stack<?> stack, Thread thread) {
      return Head.reserveSpace(stack.availableSharedCapacity, Recycler.LINK_CAPACITY) ? 
        newQueue(stack, thread) : null;
    }
    
    void add(Recycler.DefaultHandle<?> handle) {
      handle.lastRecycledId = this.id;
      Link tail = this.tail;
      int writeIndex;
      if ((writeIndex = tail.get()) == Recycler.LINK_CAPACITY) {
        if (!this.head.reserveSpace(Recycler.LINK_CAPACITY))
          return; 
        this.tail = tail = tail.next = new Link();
        writeIndex = tail.get();
      } 
      tail.elements[writeIndex] = handle;
      handle.stack = null;
      tail.lazySet(writeIndex + 1);
    }
    
    boolean hasFinalData() {
      return (this.tail.readIndex != this.tail.get());
    }
    
    boolean transfer(Recycler.Stack<?> dst) {
      Link head = this.head.link;
      if (head == null)
        return false; 
      if (head.readIndex == Recycler.LINK_CAPACITY) {
        if (head.next == null)
          return false; 
        this.head.link = head = head.next;
      } 
      int srcStart = head.readIndex;
      int srcEnd = head.get();
      int srcSize = srcEnd - srcStart;
      if (srcSize == 0)
        return false; 
      int dstSize = dst.size;
      int expectedCapacity = dstSize + srcSize;
      if (expectedCapacity > dst.elements.length) {
        int actualCapacity = dst.increaseCapacity(expectedCapacity);
        srcEnd = Math.min(srcStart + actualCapacity - dstSize, srcEnd);
      } 
      if (srcStart != srcEnd) {
        Recycler.DefaultHandle[] srcElems = (Recycler.DefaultHandle[])head.elements;
        Recycler.DefaultHandle[] dstElems = (Recycler.DefaultHandle[])dst.elements;
        int newDstSize = dstSize;
        for (int i = srcStart; i < srcEnd; i++) {
          Recycler.DefaultHandle<?> element = srcElems[i];
          if (element.recycleId == 0) {
            element.recycleId = element.lastRecycledId;
          } else if (element.recycleId != element.lastRecycledId) {
            throw new IllegalStateException("recycled already");
          } 
          srcElems[i] = null;
          if (!dst.dropHandle(element)) {
            element.stack = dst;
            dstElems[newDstSize++] = element;
          } 
        } 
        if (srcEnd == Recycler.LINK_CAPACITY && head.next != null) {
          this.head.reclaimSpace(Recycler.LINK_CAPACITY);
          this.head.link = head.next;
        } 
        head.readIndex = srcEnd;
        if (dst.size == newDstSize)
          return false; 
        dst.size = newDstSize;
        return true;
      } 
      return false;
    }
  }
  
  static final class Stack<T> {
    final Recycler<T> parent;
    
    final WeakReference<Thread> threadRef;
    
    final AtomicInteger availableSharedCapacity;
    
    final int maxDelayedQueues;
    
    private final int maxCapacity;
    
    private final int ratioMask;
    
    private Recycler.DefaultHandle<?>[] elements;
    
    private int size;
    
    private int handleRecycleCount = -1;
    
    private Recycler.WeakOrderQueue cursor;
    
    private Recycler.WeakOrderQueue prev;
    
    private volatile Recycler.WeakOrderQueue head;
    
    Stack(Recycler<T> parent, Thread thread, int maxCapacity, int maxSharedCapacityFactor, int ratioMask, int maxDelayedQueues) {
      this.parent = parent;
      this.threadRef = new WeakReference<Thread>(thread);
      this.maxCapacity = maxCapacity;
      this.availableSharedCapacity = new AtomicInteger(Math.max(maxCapacity / maxSharedCapacityFactor, Recycler.LINK_CAPACITY));
      this.elements = (Recycler.DefaultHandle<?>[])new Recycler.DefaultHandle[Math.min(Recycler.INITIAL_CAPACITY, maxCapacity)];
      this.ratioMask = ratioMask;
      this.maxDelayedQueues = maxDelayedQueues;
    }
    
    synchronized void setHead(Recycler.WeakOrderQueue queue) {
      queue.setNext(this.head);
      this.head = queue;
    }
    
    int increaseCapacity(int expectedCapacity) {
      int newCapacity = this.elements.length;
      int maxCapacity = this.maxCapacity;
      do {
        newCapacity <<= 1;
      } while (newCapacity < expectedCapacity && newCapacity < maxCapacity);
      newCapacity = Math.min(newCapacity, maxCapacity);
      if (newCapacity != this.elements.length)
        this.elements = (Recycler.DefaultHandle<?>[])Arrays.<Recycler.DefaultHandle>copyOf((Recycler.DefaultHandle[])this.elements, newCapacity); 
      return newCapacity;
    }
    
    Recycler.DefaultHandle<T> pop() {
      int size = this.size;
      if (size == 0) {
        if (!scavenge())
          return null; 
        size = this.size;
      } 
      size--;
      Recycler.DefaultHandle<?> ret = this.elements[size];
      this.elements[size] = null;
      if (ret.lastRecycledId != ret.recycleId)
        throw new IllegalStateException("recycled multiple times"); 
      ret.recycleId = 0;
      ret.lastRecycledId = 0;
      this.size = size;
      return (Recycler.DefaultHandle)ret;
    }
    
    boolean scavenge() {
      if (scavengeSome())
        return true; 
      this.prev = null;
      this.cursor = this.head;
      return false;
    }
    
    boolean scavengeSome() {
      Recycler.WeakOrderQueue prev, cursor = this.cursor;
      if (cursor == null) {
        prev = null;
        cursor = this.head;
        if (cursor == null)
          return false; 
      } else {
        prev = this.prev;
      } 
      boolean success = false;
      do {
        if (cursor.transfer(this)) {
          success = true;
          break;
        } 
        Recycler.WeakOrderQueue next = cursor.next;
        if (cursor.owner.get() == null) {
          if (cursor.hasFinalData())
            while (cursor.transfer(this))
              success = true;  
          if (prev != null)
            prev.setNext(next); 
        } else {
          prev = cursor;
        } 
        cursor = next;
      } while (cursor != null && !success);
      this.prev = prev;
      this.cursor = cursor;
      return success;
    }
    
    void push(Recycler.DefaultHandle<?> item) {
      Thread currentThread = Thread.currentThread();
      if (this.threadRef.get() == currentThread) {
        pushNow(item);
      } else {
        pushLater(item, currentThread);
      } 
    }
    
    private void pushNow(Recycler.DefaultHandle<?> item) {
      if ((item.recycleId | item.lastRecycledId) != 0)
        throw new IllegalStateException("recycled already"); 
      item.recycleId = item.lastRecycledId = Recycler.OWN_THREAD_ID;
      int size = this.size;
      if (size >= this.maxCapacity || dropHandle(item))
        return; 
      if (size == this.elements.length)
        this.elements = (Recycler.DefaultHandle<?>[])Arrays.<Recycler.DefaultHandle>copyOf((Recycler.DefaultHandle[])this.elements, Math.min(size << 1, this.maxCapacity)); 
      this.elements[size] = item;
      this.size = size + 1;
    }
    
    private void pushLater(Recycler.DefaultHandle<?> item, Thread thread) {
      Map<Stack<?>, Recycler.WeakOrderQueue> delayedRecycled = (Map<Stack<?>, Recycler.WeakOrderQueue>)Recycler.DELAYED_RECYCLED.get();
      Recycler.WeakOrderQueue queue = delayedRecycled.get(this);
      if (queue == null) {
        if (delayedRecycled.size() >= this.maxDelayedQueues) {
          delayedRecycled.put(this, Recycler.WeakOrderQueue.DUMMY);
          return;
        } 
        if ((queue = Recycler.WeakOrderQueue.allocate(this, thread)) == null)
          return; 
        delayedRecycled.put(this, queue);
      } else if (queue == Recycler.WeakOrderQueue.DUMMY) {
        return;
      } 
      queue.add(item);
    }
    
    boolean dropHandle(Recycler.DefaultHandle<?> handle) {
      if (!handle.hasBeenRecycled) {
        if ((++this.handleRecycleCount & this.ratioMask) != 0)
          return true; 
        handle.hasBeenRecycled = true;
      } 
      return false;
    }
    
    Recycler.DefaultHandle<T> newHandle() {
      return new Recycler.DefaultHandle<T>(this);
    }
  }
  
  public static interface Handle<T> {
    void recycle(T param1T);
  }
}
