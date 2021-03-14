package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Unsafe;

final class PlatformDependent0 {
  private static final InternalLogger logger;
  
  private static final long ADDRESS_FIELD_OFFSET;
  
  private static final long BYTE_ARRAY_BASE_OFFSET;
  
  private static final Constructor<?> DIRECT_BUFFER_CONSTRUCTOR;
  
  private static final Throwable EXPLICIT_NO_UNSAFE_CAUSE;
  
  private static final Method ALLOCATE_ARRAY_METHOD;
  
  private static final int JAVA_VERSION;
  
  private static final boolean IS_ANDROID;
  
  private static final Throwable UNSAFE_UNAVAILABILITY_CAUSE;
  
  private static final Object INTERNAL_UNSAFE;
  
  private static final boolean IS_EXPLICIT_TRY_REFLECTION_SET_ACCESSIBLE;
  
  static final Unsafe UNSAFE;
  
  static final int HASH_CODE_ASCII_SEED = -1028477387;
  
  static final int HASH_CODE_C1 = -862048943;
  
  static final int HASH_CODE_C2 = 461845907;
  
  private static final long UNSAFE_COPY_THRESHOLD = 1048576L;
  
  private static final boolean UNALIGNED;
  
  static {
    final ByteBuffer direct;
    Unsafe unsafe;
  }
  
  static {
    logger = InternalLoggerFactory.getInstance(PlatformDependent0.class);
    EXPLICIT_NO_UNSAFE_CAUSE = explicitNoUnsafeCause0();
    JAVA_VERSION = javaVersion0();
    IS_ANDROID = isAndroid0();
    IS_EXPLICIT_TRY_REFLECTION_SET_ACCESSIBLE = explicitTryReflectionSetAccessible0();
    Field addressField = null;
    Method allocateArrayMethod = null;
    Throwable unsafeUnavailabilityCause = null;
    Object internalUnsafe = null;
    if ((unsafeUnavailabilityCause = EXPLICIT_NO_UNSAFE_CAUSE) != null) {
      direct = null;
      addressField = null;
      unsafe = null;
      internalUnsafe = null;
    } else {
      direct = ByteBuffer.allocateDirect(1);
      Object maybeUnsafe = AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
              try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                Throwable cause = ReflectionUtil.trySetAccessible(unsafeField, false);
                if (cause != null)
                  return cause; 
                return unsafeField.get(null);
              } catch (NoSuchFieldException e) {
                return e;
              } catch (SecurityException e) {
                return e;
              } catch (IllegalAccessException e) {
                return e;
              } catch (NoClassDefFoundError e) {
                return e;
              } 
            }
          });
      if (maybeUnsafe instanceof Throwable) {
        unsafe = null;
        unsafeUnavailabilityCause = (Throwable)maybeUnsafe;
        logger.debug("sun.misc.Unsafe.theUnsafe: unavailable", (Throwable)maybeUnsafe);
      } else {
        unsafe = (Unsafe)maybeUnsafe;
        logger.debug("sun.misc.Unsafe.theUnsafe: available");
      } 
      if (unsafe != null) {
        final Unsafe finalUnsafe = unsafe;
        Object maybeException = AccessController.doPrivileged(new PrivilegedAction() {
              public Object run() {
                try {
                  finalUnsafe.getClass().getDeclaredMethod("copyMemory", new Class[] { Object.class, long.class, Object.class, long.class, long.class });
                  return null;
                } catch (NoSuchMethodException e) {
                  return e;
                } catch (SecurityException e) {
                  return e;
                } 
              }
            });
        if (maybeException == null) {
          logger.debug("sun.misc.Unsafe.copyMemory: available");
        } else {
          unsafe = null;
          unsafeUnavailabilityCause = (Throwable)maybeException;
          logger.debug("sun.misc.Unsafe.copyMemory: unavailable", (Throwable)maybeException);
        } 
      } 
      if (unsafe != null) {
        final Unsafe finalUnsafe = unsafe;
        Object maybeAddressField = AccessController.doPrivileged(new PrivilegedAction() {
              public Object run() {
                try {
                  Field field = Buffer.class.getDeclaredField("address");
                  long offset = finalUnsafe.objectFieldOffset(field);
                  long address = finalUnsafe.getLong(direct, offset);
                  if (address == 0L)
                    return null; 
                  return field;
                } catch (NoSuchFieldException e) {
                  return e;
                } catch (SecurityException e) {
                  return e;
                } 
              }
            });
        if (maybeAddressField instanceof Field) {
          addressField = (Field)maybeAddressField;
          logger.debug("java.nio.Buffer.address: available");
        } else {
          unsafeUnavailabilityCause = (Throwable)maybeAddressField;
          logger.debug("java.nio.Buffer.address: unavailable", (Throwable)maybeAddressField);
          unsafe = null;
        } 
      } 
      if (unsafe != null) {
        long byteArrayIndexScale = unsafe.arrayIndexScale(byte[].class);
        if (byteArrayIndexScale != 1L) {
          logger.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", Long.valueOf(byteArrayIndexScale));
          unsafeUnavailabilityCause = new UnsupportedOperationException("Unexpected unsafe.arrayIndexScale");
          unsafe = null;
        } 
      } 
    } 
    UNSAFE_UNAVAILABILITY_CAUSE = unsafeUnavailabilityCause;
    UNSAFE = unsafe;
    if (unsafe == null) {
      ADDRESS_FIELD_OFFSET = -1L;
      BYTE_ARRAY_BASE_OFFSET = -1L;
      UNALIGNED = false;
      DIRECT_BUFFER_CONSTRUCTOR = null;
      ALLOCATE_ARRAY_METHOD = null;
    } else {
      Constructor<?> directBufferConstructor;
      boolean unaligned;
      long address = -1L;
      try {
        Object maybeDirectBufferConstructor = AccessController.doPrivileged(new PrivilegedAction() {
              public Object run() {
                try {
                  Constructor<?> constructor = direct.getClass().getDeclaredConstructor(new Class[] { long.class, int.class });
                  Throwable cause = ReflectionUtil.trySetAccessible(constructor, true);
                  if (cause != null)
                    return cause; 
                  return constructor;
                } catch (NoSuchMethodException e) {
                  return e;
                } catch (SecurityException e) {
                  return e;
                } 
              }
            });
        if (maybeDirectBufferConstructor instanceof Constructor) {
          address = UNSAFE.allocateMemory(1L);
          try {
            ((Constructor)maybeDirectBufferConstructor).newInstance(new Object[] { Long.valueOf(address), Integer.valueOf(1) });
            directBufferConstructor = (Constructor)maybeDirectBufferConstructor;
            logger.debug("direct buffer constructor: available");
          } catch (InstantiationException e) {
            directBufferConstructor = null;
          } catch (IllegalAccessException e) {
            directBufferConstructor = null;
          } catch (InvocationTargetException e) {
            directBufferConstructor = null;
          } 
        } else {
          logger.debug("direct buffer constructor: unavailable", (Throwable)maybeDirectBufferConstructor);
          directBufferConstructor = null;
        } 
      } finally {
        if (address != -1L)
          UNSAFE.freeMemory(address); 
      } 
      DIRECT_BUFFER_CONSTRUCTOR = directBufferConstructor;
      ADDRESS_FIELD_OFFSET = objectFieldOffset(addressField);
      BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
      Object maybeUnaligned = AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
              try {
                Class<?> bitsClass = Class.forName("java.nio.Bits", false, PlatformDependent0.getSystemClassLoader());
                Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned", new Class[0]);
                Throwable cause = ReflectionUtil.trySetAccessible(unalignedMethod, true);
                if (cause != null)
                  return cause; 
                return unalignedMethod.invoke(null, new Object[0]);
              } catch (NoSuchMethodException e) {
                return e;
              } catch (SecurityException e) {
                return e;
              } catch (IllegalAccessException e) {
                return e;
              } catch (ClassNotFoundException e) {
                return e;
              } catch (InvocationTargetException e) {
                return e;
              } 
            }
          });
      if (maybeUnaligned instanceof Boolean) {
        unaligned = ((Boolean)maybeUnaligned).booleanValue();
        logger.debug("java.nio.Bits.unaligned: available, {}", Boolean.valueOf(unaligned));
      } else {
        String arch = SystemPropertyUtil.get("os.arch", "");
        unaligned = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");
        Throwable t = (Throwable)maybeUnaligned;
        logger.debug("java.nio.Bits.unaligned: unavailable {}", Boolean.valueOf(unaligned), t);
      } 
      UNALIGNED = unaligned;
      if (javaVersion() >= 9) {
        Object maybeException = AccessController.doPrivileged(new PrivilegedAction() {
              public Object run() {
                try {
                  Class<?> internalUnsafeClass = PlatformDependent0.getClassLoader(PlatformDependent0.class).loadClass("jdk.internal.misc.Unsafe");
                  Method method = internalUnsafeClass.getDeclaredMethod("getUnsafe", new Class[0]);
                  return method.invoke(null, new Object[0]);
                } catch (Throwable e) {
                  return e;
                } 
              }
            });
        if (!(maybeException instanceof Throwable)) {
          internalUnsafe = maybeException;
          final Object finalInternalUnsafe = internalUnsafe;
          maybeException = AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                  try {
                    return finalInternalUnsafe.getClass().getDeclaredMethod("allocateUninitializedArray", new Class[] { Class.class, int.class });
                  } catch (NoSuchMethodException e) {
                    return e;
                  } catch (SecurityException e) {
                    return e;
                  } 
                }
              });
          if (maybeException instanceof Method)
            try {
              Method m = (Method)maybeException;
              byte[] bytes = (byte[])m.invoke(finalInternalUnsafe, new Object[] { byte.class, Integer.valueOf(8) });
              assert bytes.length == 8;
              allocateArrayMethod = m;
            } catch (IllegalAccessException e) {
              maybeException = e;
            } catch (InvocationTargetException e) {
              maybeException = e;
            }  
        } 
        if (maybeException instanceof Throwable) {
          logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable", (Throwable)maybeException);
        } else {
          logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): available");
        } 
      } else {
        logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable prior to Java9");
      } 
      ALLOCATE_ARRAY_METHOD = allocateArrayMethod;
    } 
    INTERNAL_UNSAFE = internalUnsafe;
    logger.debug("java.nio.DirectByteBuffer.<init>(long, int): {}", (DIRECT_BUFFER_CONSTRUCTOR != null) ? "available" : "unavailable");
  }
  
  static boolean isExplicitNoUnsafe() {
    return (EXPLICIT_NO_UNSAFE_CAUSE == null);
  }
  
  private static Throwable explicitNoUnsafeCause0() {
    String unsafePropName;
    boolean noUnsafe = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.noUnsafe", false);
    logger.debug("-Dio.netty.noUnsafe: {}", Boolean.valueOf(noUnsafe));
    if (noUnsafe) {
      logger.debug("sun.misc.Unsafe: unavailable (io.netty.noUnsafe)");
      return new UnsupportedOperationException("sun.misc.Unsafe: unavailable (io.netty.noUnsafe)");
    } 
    if (SystemPropertyUtil.contains("com.github.steveice10.netty.tryUnsafe")) {
      unsafePropName = "com.github.steveice10.netty.tryUnsafe";
    } else {
      unsafePropName = "org.jboss.netty.tryUnsafe";
    } 
    if (!SystemPropertyUtil.getBoolean(unsafePropName, true)) {
      String msg = "sun.misc.Unsafe: unavailable (" + unsafePropName + ")";
      logger.debug(msg);
      return new UnsupportedOperationException(msg);
    } 
    return null;
  }
  
  static boolean isUnaligned() {
    return UNALIGNED;
  }
  
  static boolean hasUnsafe() {
    return (UNSAFE != null);
  }
  
  static Throwable getUnsafeUnavailabilityCause() {
    return UNSAFE_UNAVAILABILITY_CAUSE;
  }
  
  static boolean unalignedAccess() {
    return UNALIGNED;
  }
  
  static void throwException(Throwable cause) {
    UNSAFE.throwException(ObjectUtil.<Throwable>checkNotNull(cause, "cause"));
  }
  
  static boolean hasDirectBufferNoCleanerConstructor() {
    return (DIRECT_BUFFER_CONSTRUCTOR != null);
  }
  
  static ByteBuffer reallocateDirectNoCleaner(ByteBuffer buffer, int capacity) {
    return newDirectBuffer(UNSAFE.reallocateMemory(directBufferAddress(buffer), capacity), capacity);
  }
  
  static ByteBuffer allocateDirectNoCleaner(int capacity) {
    return newDirectBuffer(UNSAFE.allocateMemory(capacity), capacity);
  }
  
  static boolean hasAllocateArrayMethod() {
    return (ALLOCATE_ARRAY_METHOD != null);
  }
  
  static byte[] allocateUninitializedArray(int size) {
    try {
      return (byte[])ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, new Object[] { byte.class, Integer.valueOf(size) });
    } catch (IllegalAccessException e) {
      throw new Error(e);
    } catch (InvocationTargetException e) {
      throw new Error(e);
    } 
  }
  
  static ByteBuffer newDirectBuffer(long address, int capacity) {
    ObjectUtil.checkPositiveOrZero(capacity, "capacity");
    try {
      return (ByteBuffer)DIRECT_BUFFER_CONSTRUCTOR.newInstance(new Object[] { Long.valueOf(address), Integer.valueOf(capacity) });
    } catch (Throwable cause) {
      if (cause instanceof Error)
        throw (Error)cause; 
      throw new Error(cause);
    } 
  }
  
  static long directBufferAddress(ByteBuffer buffer) {
    return getLong(buffer, ADDRESS_FIELD_OFFSET);
  }
  
  static long byteArrayBaseOffset() {
    return BYTE_ARRAY_BASE_OFFSET;
  }
  
  static Object getObject(Object object, long fieldOffset) {
    return UNSAFE.getObject(object, fieldOffset);
  }
  
  static int getInt(Object object, long fieldOffset) {
    return UNSAFE.getInt(object, fieldOffset);
  }
  
  private static long getLong(Object object, long fieldOffset) {
    return UNSAFE.getLong(object, fieldOffset);
  }
  
  static long objectFieldOffset(Field field) {
    return UNSAFE.objectFieldOffset(field);
  }
  
  static byte getByte(long address) {
    return UNSAFE.getByte(address);
  }
  
  static short getShort(long address) {
    return UNSAFE.getShort(address);
  }
  
  static int getInt(long address) {
    return UNSAFE.getInt(address);
  }
  
  static long getLong(long address) {
    return UNSAFE.getLong(address);
  }
  
  static byte getByte(byte[] data, int index) {
    return UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index);
  }
  
  static short getShort(byte[] data, int index) {
    return UNSAFE.getShort(data, BYTE_ARRAY_BASE_OFFSET + index);
  }
  
  static int getInt(byte[] data, int index) {
    return UNSAFE.getInt(data, BYTE_ARRAY_BASE_OFFSET + index);
  }
  
  static long getLong(byte[] data, int index) {
    return UNSAFE.getLong(data, BYTE_ARRAY_BASE_OFFSET + index);
  }
  
  static void putByte(long address, byte value) {
    UNSAFE.putByte(address, value);
  }
  
  static void putShort(long address, short value) {
    UNSAFE.putShort(address, value);
  }
  
  static void putInt(long address, int value) {
    UNSAFE.putInt(address, value);
  }
  
  static void putLong(long address, long value) {
    UNSAFE.putLong(address, value);
  }
  
  static void putByte(byte[] data, int index, byte value) {
    UNSAFE.putByte(data, BYTE_ARRAY_BASE_OFFSET + index, value);
  }
  
  static void putShort(byte[] data, int index, short value) {
    UNSAFE.putShort(data, BYTE_ARRAY_BASE_OFFSET + index, value);
  }
  
  static void putInt(byte[] data, int index, int value) {
    UNSAFE.putInt(data, BYTE_ARRAY_BASE_OFFSET + index, value);
  }
  
  static void putLong(byte[] data, int index, long value) {
    UNSAFE.putLong(data, BYTE_ARRAY_BASE_OFFSET + index, value);
  }
  
  static void copyMemory(long srcAddr, long dstAddr, long length) {
    while (length > 0L) {
      long size = Math.min(length, 1048576L);
      UNSAFE.copyMemory(srcAddr, dstAddr, size);
      length -= size;
      srcAddr += size;
      dstAddr += size;
    } 
  }
  
  static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length) {
    while (length > 0L) {
      long size = Math.min(length, 1048576L);
      UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
      length -= size;
      srcOffset += size;
      dstOffset += size;
    } 
  }
  
  static void setMemory(long address, long bytes, byte value) {
    UNSAFE.setMemory(address, bytes, value);
  }
  
  static void setMemory(Object o, long offset, long bytes, byte value) {
    UNSAFE.setMemory(o, offset, bytes, value);
  }
  
  static boolean equals(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length) {
    if (length <= 0)
      return true; 
    long baseOffset1 = BYTE_ARRAY_BASE_OFFSET + startPos1;
    long baseOffset2 = BYTE_ARRAY_BASE_OFFSET + startPos2;
    int remainingBytes = length & 0x7;
    long end = baseOffset1 + remainingBytes;
    long j;
    for (long i = baseOffset1 - 8L + length; i >= end; i -= 8L, j -= 8L) {
      if (UNSAFE.getLong(bytes1, i) != UNSAFE.getLong(bytes2, j))
        return false; 
    } 
    if (remainingBytes >= 4) {
      remainingBytes -= 4;
      if (UNSAFE.getInt(bytes1, baseOffset1 + remainingBytes) != UNSAFE
        .getInt(bytes2, baseOffset2 + remainingBytes))
        return false; 
    } 
    if (remainingBytes >= 2)
      return (UNSAFE.getChar(bytes1, baseOffset1) == UNSAFE.getChar(bytes2, baseOffset2) && (remainingBytes == 2 || bytes1[startPos1 + 2] == bytes2[startPos2 + 2])); 
    return (bytes1[startPos1] == bytes2[startPos2]);
  }
  
  static int equalsConstantTime(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length) {
    long result = 0L;
    long baseOffset1 = BYTE_ARRAY_BASE_OFFSET + startPos1;
    long baseOffset2 = BYTE_ARRAY_BASE_OFFSET + startPos2;
    int remainingBytes = length & 0x7;
    long end = baseOffset1 + remainingBytes;
    long j;
    for (long i = baseOffset1 - 8L + length; i >= end; i -= 8L, j -= 8L)
      result |= UNSAFE.getLong(bytes1, i) ^ UNSAFE.getLong(bytes2, j); 
    switch (remainingBytes) {
      case 7:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getInt(bytes1, baseOffset1 + 3L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 3L)) | (UNSAFE
            .getChar(bytes1, baseOffset1 + 1L) ^ UNSAFE.getChar(bytes2, baseOffset2 + 1L)) | (UNSAFE
            .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2)), 0L);
      case 6:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getInt(bytes1, baseOffset1 + 2L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 2L)) | (UNSAFE
            .getChar(bytes1, baseOffset1) ^ UNSAFE.getChar(bytes2, baseOffset2)), 0L);
      case 5:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getInt(bytes1, baseOffset1 + 1L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 1L)) | (UNSAFE
            .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2)), 0L);
      case 4:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getInt(bytes1, baseOffset1) ^ UNSAFE.getInt(bytes2, baseOffset2)), 0L);
      case 3:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getChar(bytes1, baseOffset1 + 1L) ^ UNSAFE.getChar(bytes2, baseOffset2 + 1L)) | (UNSAFE
            .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2)), 0L);
      case 2:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getChar(bytes1, baseOffset1) ^ UNSAFE.getChar(bytes2, baseOffset2)), 0L);
      case 1:
        return ConstantTimeUtils.equalsConstantTime(result | (UNSAFE
            .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2)), 0L);
    } 
    return ConstantTimeUtils.equalsConstantTime(result, 0L);
  }
  
  static boolean isZero(byte[] bytes, int startPos, int length) {
    if (length <= 0)
      return true; 
    long baseOffset = BYTE_ARRAY_BASE_OFFSET + startPos;
    int remainingBytes = length & 0x7;
    long end = baseOffset + remainingBytes;
    long i;
    for (i = baseOffset - 8L + length; i >= end; i -= 8L) {
      if (UNSAFE.getLong(bytes, i) != 0L)
        return false; 
    } 
    if (remainingBytes >= 4) {
      remainingBytes -= 4;
      if (UNSAFE.getInt(bytes, baseOffset + remainingBytes) != 0)
        return false; 
    } 
    if (remainingBytes >= 2)
      return (UNSAFE.getChar(bytes, baseOffset) == '\000' && (remainingBytes == 2 || bytes[startPos + 2] == 0)); 
    return (bytes[startPos] == 0);
  }
  
  static int hashCodeAscii(byte[] bytes, int startPos, int length) {
    int hash = -1028477387;
    long baseOffset = BYTE_ARRAY_BASE_OFFSET + startPos;
    int remainingBytes = length & 0x7;
    long end = baseOffset + remainingBytes;
    long i;
    for (i = baseOffset - 8L + length; i >= end; i -= 8L)
      hash = hashCodeAsciiCompute(UNSAFE.getLong(bytes, i), hash); 
    switch (remainingBytes) {
      case 7:
        return ((hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + 
          hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset + 1L))) * -862048943 + 
          hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 3L));
      case 6:
        return (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset))) * 461845907 + 
          hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 2L));
      case 5:
        return (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + 
          hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 1L));
      case 4:
        return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset));
      case 3:
        return (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + 
          hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset + 1L));
      case 2:
        return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset));
      case 1:
        return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset));
    } 
    return hash;
  }
  
  static int hashCodeAsciiCompute(long value, int hash) {
    return hash * -862048943 + 
      
      hashCodeAsciiSanitize((int)value) * 461845907 + (int)((value & 0x1F1F1F1F00000000L) >>> 32L);
  }
  
  static int hashCodeAsciiSanitize(int value) {
    return value & 0x1F1F1F1F;
  }
  
  static int hashCodeAsciiSanitize(short value) {
    return value & 0x1F1F;
  }
  
  static int hashCodeAsciiSanitize(byte value) {
    return value & 0x1F;
  }
  
  static ClassLoader getClassLoader(final Class<?> clazz) {
    if (System.getSecurityManager() == null)
      return clazz.getClassLoader(); 
    return AccessController.<ClassLoader>doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return clazz.getClassLoader();
          }
        });
  }
  
  static ClassLoader getContextClassLoader() {
    if (System.getSecurityManager() == null)
      return Thread.currentThread().getContextClassLoader(); 
    return AccessController.<ClassLoader>doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
          }
        });
  }
  
  static ClassLoader getSystemClassLoader() {
    if (System.getSecurityManager() == null)
      return ClassLoader.getSystemClassLoader(); 
    return AccessController.<ClassLoader>doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return ClassLoader.getSystemClassLoader();
          }
        });
  }
  
  static int addressSize() {
    return UNSAFE.addressSize();
  }
  
  static long allocateMemory(long size) {
    return UNSAFE.allocateMemory(size);
  }
  
  static void freeMemory(long address) {
    UNSAFE.freeMemory(address);
  }
  
  static long reallocateMemory(long address, long newSize) {
    return UNSAFE.reallocateMemory(address, newSize);
  }
  
  static boolean isAndroid() {
    return IS_ANDROID;
  }
  
  private static boolean isAndroid0() {
    String vmName = SystemPropertyUtil.get("java.vm.name");
    boolean isAndroid = "Dalvik".equals(vmName);
    if (isAndroid)
      logger.debug("Platform: Android"); 
    return isAndroid;
  }
  
  private static boolean explicitTryReflectionSetAccessible0() {
    return SystemPropertyUtil.getBoolean("com.github.steveice10.netty.tryReflectionSetAccessible", (javaVersion() < 9));
  }
  
  static boolean isExplicitTryReflectionSetAccessible() {
    return IS_EXPLICIT_TRY_REFLECTION_SET_ACCESSIBLE;
  }
  
  static int javaVersion() {
    return JAVA_VERSION;
  }
  
  private static int javaVersion0() {
    int majorVersion;
    if (isAndroid0()) {
      majorVersion = 6;
    } else {
      majorVersion = majorVersionFromJavaSpecificationVersion();
    } 
    logger.debug("Java version: {}", Integer.valueOf(majorVersion));
    return majorVersion;
  }
  
  static int majorVersionFromJavaSpecificationVersion() {
    return majorVersion(SystemPropertyUtil.get("java.specification.version", "1.6"));
  }
  
  static int majorVersion(String javaSpecVersion) {
    String[] components = javaSpecVersion.split("\\.");
    int[] version = new int[components.length];
    for (int i = 0; i < components.length; i++)
      version[i] = Integer.parseInt(components[i]); 
    if (version[0] == 1) {
      assert version[1] >= 6;
      return version[1];
    } 
    return version[0];
  }
}
