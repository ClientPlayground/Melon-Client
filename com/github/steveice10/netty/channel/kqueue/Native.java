package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.unix.Errors;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.util.internal.NativeLibraryLoader;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.util.Locale;

final class Native {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Native.class);
  
  static {
    try {
      sizeofKEvent();
    } catch (UnsatisfiedLinkError ignore) {
      loadNativeLibrary();
    } 
    Socket.initialize();
  }
  
  static final short EV_ADD = KQueueStaticallyReferencedJniMethods.evAdd();
  
  static final short EV_ENABLE = KQueueStaticallyReferencedJniMethods.evEnable();
  
  static final short EV_DISABLE = KQueueStaticallyReferencedJniMethods.evDisable();
  
  static final short EV_DELETE = KQueueStaticallyReferencedJniMethods.evDelete();
  
  static final short EV_CLEAR = KQueueStaticallyReferencedJniMethods.evClear();
  
  static final short EV_ERROR = KQueueStaticallyReferencedJniMethods.evError();
  
  static final short EV_EOF = KQueueStaticallyReferencedJniMethods.evEOF();
  
  static final int NOTE_READCLOSED = KQueueStaticallyReferencedJniMethods.noteReadClosed();
  
  static final int NOTE_CONNRESET = KQueueStaticallyReferencedJniMethods.noteConnReset();
  
  static final int NOTE_DISCONNECTED = KQueueStaticallyReferencedJniMethods.noteDisconnected();
  
  static final int NOTE_RDHUP = NOTE_READCLOSED | NOTE_CONNRESET | NOTE_DISCONNECTED;
  
  static final short EV_ADD_CLEAR_ENABLE = (short)(EV_ADD | EV_CLEAR | EV_ENABLE);
  
  static final short EV_DELETE_DISABLE = (short)(EV_DELETE | EV_DISABLE);
  
  static final short EVFILT_READ = KQueueStaticallyReferencedJniMethods.evfiltRead();
  
  static final short EVFILT_WRITE = KQueueStaticallyReferencedJniMethods.evfiltWrite();
  
  static final short EVFILT_USER = KQueueStaticallyReferencedJniMethods.evfiltUser();
  
  static final short EVFILT_SOCK = KQueueStaticallyReferencedJniMethods.evfiltSock();
  
  static FileDescriptor newKQueue() {
    return new FileDescriptor(kqueueCreate());
  }
  
  static int keventWait(int kqueueFd, KQueueEventArray changeList, KQueueEventArray eventList, int tvSec, int tvNsec) throws IOException {
    int ready = keventWait(kqueueFd, changeList.memoryAddress(), changeList.size(), eventList
        .memoryAddress(), eventList.capacity(), tvSec, tvNsec);
    if (ready < 0)
      throw Errors.newIOException("kevent", ready); 
    return ready;
  }
  
  private static void loadNativeLibrary() {
    String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
    if (!name.startsWith("mac") && !name.contains("bsd") && !name.startsWith("darwin"))
      throw new IllegalStateException("Only supported on BSD"); 
    String staticLibName = "netty_transport_native_kqueue";
    String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
    ClassLoader cl = PlatformDependent.getClassLoader(Native.class);
    try {
      NativeLibraryLoader.load(sharedLibName, cl);
    } catch (UnsatisfiedLinkError e1) {
      try {
        NativeLibraryLoader.load(staticLibName, cl);
        logger.debug("Failed to load {}", sharedLibName, e1);
      } catch (UnsatisfiedLinkError e2) {
        ThrowableUtil.addSuppressed(e1, e2);
        throw e1;
      } 
    } 
  }
  
  private static native int kqueueCreate();
  
  private static native int keventWait(int paramInt1, long paramLong1, int paramInt2, long paramLong2, int paramInt3, int paramInt4, int paramInt5);
  
  static native int keventTriggerUserEvent(int paramInt1, int paramInt2);
  
  static native int keventAddUserEvent(int paramInt1, int paramInt2);
  
  static native int sizeofKEvent();
  
  static native int offsetofKEventIdent();
  
  static native int offsetofKEventFlags();
  
  static native int offsetofKEventFFlags();
  
  static native int offsetofKEventFilter();
  
  static native int offsetofKeventData();
}
