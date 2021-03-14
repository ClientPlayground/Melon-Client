package com.sun.jna;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Pointer {
  public static final Pointer NULL = null;
  
  protected long peer;
  
  public static final Pointer createConstant(long peer) {
    return new Opaque(peer);
  }
  
  public static final Pointer createConstant(int peer) {
    return new Opaque(peer & 0xFFFFFFFFFFFFFFFFL);
  }
  
  Pointer() {}
  
  public Pointer(long peer) {
    this.peer = peer;
  }
  
  public Pointer share(long offset) {
    return share(offset, 0L);
  }
  
  public Pointer share(long offset, long sz) {
    if (offset == 0L)
      return this; 
    return new Pointer(this.peer + offset);
  }
  
  public void clear(long size) {
    setMemory(0L, size, (byte)0);
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (o == null)
      return false; 
    return (o instanceof Pointer && ((Pointer)o).peer == this.peer);
  }
  
  public int hashCode() {
    return (int)((this.peer >>> 32L) + (this.peer & 0xFFFFFFFFFFFFFFFFL));
  }
  
  public long indexOf(long offset, byte value) {
    return Native.indexOf(this, this.peer, offset, value);
  }
  
  public void read(long offset, byte[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, short[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, char[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, int[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, long[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, float[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, double[] buf, int index, int length) {
    Native.read(this, this.peer, offset, buf, index, length);
  }
  
  public void read(long offset, Pointer[] buf, int index, int length) {
    for (int i = 0; i < length; i++) {
      Pointer p = getPointer(offset + (i * Native.POINTER_SIZE));
      Pointer oldp = buf[i + index];
      if (oldp == null || p == null || p.peer != oldp.peer)
        buf[i + index] = p; 
    } 
  }
  
  public void write(long offset, byte[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, short[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, char[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, int[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, long[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, float[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long offset, double[] buf, int index, int length) {
    Native.write(this, this.peer, offset, buf, index, length);
  }
  
  public void write(long bOff, Pointer[] buf, int index, int length) {
    for (int i = 0; i < length; i++)
      setPointer(bOff + (i * Native.POINTER_SIZE), buf[index + i]); 
  }
  
  Object getValue(long offset, Class<?> type, Object currentValue) {
    Object result = null;
    if (Structure.class.isAssignableFrom(type)) {
      Structure s = (Structure)currentValue;
      if (Structure.ByReference.class.isAssignableFrom(type)) {
        s = Structure.updateStructureByReference(type, s, getPointer(offset));
      } else {
        s.useMemory(this, (int)offset, true);
        s.read();
      } 
      result = s;
    } else if (type == boolean.class || type == Boolean.class) {
      result = Function.valueOf((getInt(offset) != 0));
    } else if (type == byte.class || type == Byte.class) {
      result = Byte.valueOf(getByte(offset));
    } else if (type == short.class || type == Short.class) {
      result = Short.valueOf(getShort(offset));
    } else if (type == char.class || type == Character.class) {
      result = Character.valueOf(getChar(offset));
    } else if (type == int.class || type == Integer.class) {
      result = Integer.valueOf(getInt(offset));
    } else if (type == long.class || type == Long.class) {
      result = Long.valueOf(getLong(offset));
    } else if (type == float.class || type == Float.class) {
      result = Float.valueOf(getFloat(offset));
    } else if (type == double.class || type == Double.class) {
      result = Double.valueOf(getDouble(offset));
    } else if (Pointer.class.isAssignableFrom(type)) {
      Pointer p = getPointer(offset);
      if (p != null) {
        Pointer oldp = (currentValue instanceof Pointer) ? (Pointer)currentValue : null;
        if (oldp == null || p.peer != oldp.peer) {
          result = p;
        } else {
          result = oldp;
        } 
      } 
    } else if (type == String.class) {
      Pointer p = getPointer(offset);
      result = (p != null) ? p.getString(0L) : null;
    } else if (type == WString.class) {
      Pointer p = getPointer(offset);
      result = (p != null) ? new WString(p.getWideString(0L)) : null;
    } else if (Callback.class.isAssignableFrom(type)) {
      Pointer fp = getPointer(offset);
      if (fp == null) {
        result = null;
      } else {
        Callback cb = (Callback)currentValue;
        Pointer oldfp = CallbackReference.getFunctionPointer(cb);
        if (!fp.equals(oldfp))
          cb = CallbackReference.getCallback(type, fp); 
        result = cb;
      } 
    } else if (Platform.HAS_BUFFERS && Buffer.class.isAssignableFrom(type)) {
      Pointer bp = getPointer(offset);
      if (bp == null) {
        result = null;
      } else {
        Pointer oldbp = (currentValue == null) ? null : Native.getDirectBufferPointer((Buffer)currentValue);
        if (oldbp == null || !oldbp.equals(bp))
          throw new IllegalStateException("Can't autogenerate a direct buffer on memory read"); 
        result = currentValue;
      } 
    } else if (NativeMapped.class.isAssignableFrom(type)) {
      NativeMapped nm = (NativeMapped)currentValue;
      if (nm != null) {
        Object value = getValue(offset, nm.nativeType(), null);
        result = nm.fromNative(value, new FromNativeContext(type));
        if (nm.equals(result))
          result = nm; 
      } else {
        NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
        Object value = getValue(offset, tc.nativeType(), null);
        result = tc.fromNative(value, new FromNativeContext(type));
      } 
    } else if (type.isArray()) {
      result = currentValue;
      if (result == null)
        throw new IllegalStateException("Need an initialized array"); 
      readArray(offset, result, type.getComponentType());
    } else {
      throw new IllegalArgumentException("Reading \"" + type + "\" from memory is not supported");
    } 
    return result;
  }
  
  private void readArray(long offset, Object o, Class<?> cls) {
    int length = 0;
    length = Array.getLength(o);
    Object result = o;
    if (cls == byte.class) {
      read(offset, (byte[])result, 0, length);
    } else if (cls == short.class) {
      read(offset, (short[])result, 0, length);
    } else if (cls == char.class) {
      read(offset, (char[])result, 0, length);
    } else if (cls == int.class) {
      read(offset, (int[])result, 0, length);
    } else if (cls == long.class) {
      read(offset, (long[])result, 0, length);
    } else if (cls == float.class) {
      read(offset, (float[])result, 0, length);
    } else if (cls == double.class) {
      read(offset, (double[])result, 0, length);
    } else if (Pointer.class.isAssignableFrom(cls)) {
      read(offset, (Pointer[])result, 0, length);
    } else if (Structure.class.isAssignableFrom(cls)) {
      Structure[] sarray = (Structure[])result;
      if (Structure.ByReference.class.isAssignableFrom(cls)) {
        Pointer[] parray = getPointerArray(offset, sarray.length);
        for (int i = 0; i < sarray.length; i++)
          sarray[i] = Structure.updateStructureByReference(cls, sarray[i], parray[i]); 
      } else {
        Structure first = sarray[0];
        if (first == null) {
          first = Structure.newInstance(cls, share(offset));
          first.conditionalAutoRead();
          sarray[0] = first;
        } else {
          first.useMemory(this, (int)offset, true);
          first.read();
        } 
        Structure[] tmp = first.toArray(sarray.length);
        for (int i = 1; i < sarray.length; i++) {
          if (sarray[i] == null) {
            sarray[i] = tmp[i];
          } else {
            sarray[i].useMemory(this, (int)(offset + (i * sarray[i].size())), true);
            sarray[i].read();
          } 
        } 
      } 
    } else if (NativeMapped.class.isAssignableFrom(cls)) {
      NativeMapped[] array = (NativeMapped[])result;
      NativeMappedConverter tc = NativeMappedConverter.getInstance(cls);
      int size = Native.getNativeSize(result.getClass(), result) / array.length;
      for (int i = 0; i < array.length; i++) {
        Object value = getValue(offset + (size * i), tc.nativeType(), array[i]);
        array[i] = (NativeMapped)tc.fromNative(value, new FromNativeContext(cls));
      } 
    } else {
      throw new IllegalArgumentException("Reading array of " + cls + " from memory not supported");
    } 
  }
  
  public byte getByte(long offset) {
    return Native.getByte(this, this.peer, offset);
  }
  
  public char getChar(long offset) {
    return Native.getChar(this, this.peer, offset);
  }
  
  public short getShort(long offset) {
    return Native.getShort(this, this.peer, offset);
  }
  
  public int getInt(long offset) {
    return Native.getInt(this, this.peer, offset);
  }
  
  public long getLong(long offset) {
    return Native.getLong(this, this.peer, offset);
  }
  
  public NativeLong getNativeLong(long offset) {
    return new NativeLong((NativeLong.SIZE == 8) ? getLong(offset) : getInt(offset));
  }
  
  public float getFloat(long offset) {
    return Native.getFloat(this, this.peer, offset);
  }
  
  public double getDouble(long offset) {
    return Native.getDouble(this, this.peer, offset);
  }
  
  public Pointer getPointer(long offset) {
    return Native.getPointer(this.peer + offset);
  }
  
  public ByteBuffer getByteBuffer(long offset, long length) {
    return Native.getDirectByteBuffer(this, this.peer, offset, length).order(ByteOrder.nativeOrder());
  }
  
  public String getWideString(long offset) {
    return Native.getWideString(this, this.peer, offset);
  }
  
  public String getString(long offset) {
    return getString(offset, Native.getDefaultStringEncoding());
  }
  
  public String getString(long offset, String encoding) {
    return Native.getString(this, offset, encoding);
  }
  
  public byte[] getByteArray(long offset, int arraySize) {
    byte[] buf = new byte[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public char[] getCharArray(long offset, int arraySize) {
    char[] buf = new char[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public short[] getShortArray(long offset, int arraySize) {
    short[] buf = new short[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public int[] getIntArray(long offset, int arraySize) {
    int[] buf = new int[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public long[] getLongArray(long offset, int arraySize) {
    long[] buf = new long[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public float[] getFloatArray(long offset, int arraySize) {
    float[] buf = new float[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public double[] getDoubleArray(long offset, int arraySize) {
    double[] buf = new double[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public Pointer[] getPointerArray(long offset) {
    List<Pointer> array = new ArrayList<Pointer>();
    int addOffset = 0;
    Pointer p = getPointer(offset);
    while (p != null) {
      array.add(p);
      addOffset += Native.POINTER_SIZE;
      p = getPointer(offset + addOffset);
    } 
    return array.<Pointer>toArray(new Pointer[0]);
  }
  
  public Pointer[] getPointerArray(long offset, int arraySize) {
    Pointer[] buf = new Pointer[arraySize];
    read(offset, buf, 0, arraySize);
    return buf;
  }
  
  public String[] getStringArray(long offset) {
    return getStringArray(offset, -1, Native.getDefaultStringEncoding());
  }
  
  public String[] getStringArray(long offset, String encoding) {
    return getStringArray(offset, -1, encoding);
  }
  
  public String[] getStringArray(long offset, int length) {
    return getStringArray(offset, length, Native.getDefaultStringEncoding());
  }
  
  public String[] getWideStringArray(long offset) {
    return getWideStringArray(offset, -1);
  }
  
  public String[] getWideStringArray(long offset, int length) {
    return getStringArray(offset, length, "--WIDE-STRING--");
  }
  
  public String[] getStringArray(long offset, int length, String encoding) {
    List<String> strings = new ArrayList<String>();
    int addOffset = 0;
    if (length != -1) {
      Pointer p = getPointer(offset + addOffset);
      int count = 0;
      while (count++ < length) {
        String s = (p == null) ? null : ("--WIDE-STRING--".equals(encoding) ? p.getWideString(0L) : p.getString(0L, encoding));
        strings.add(s);
        if (count < length) {
          addOffset += Native.POINTER_SIZE;
          p = getPointer(offset + addOffset);
        } 
      } 
    } else {
      Pointer p;
      while ((p = getPointer(offset + addOffset)) != null) {
        String s = "--WIDE-STRING--".equals(encoding) ? p.getWideString(0L) : p.getString(0L, encoding);
        strings.add(s);
        addOffset += Native.POINTER_SIZE;
      } 
    } 
    return strings.<String>toArray(new String[0]);
  }
  
  void setValue(long offset, Object value, Class<?> type) {
    if (type == boolean.class || type == Boolean.class) {
      setInt(offset, Boolean.TRUE.equals(value) ? -1 : 0);
    } else if (type == byte.class || type == Byte.class) {
      setByte(offset, (value == null) ? 0 : ((Byte)value).byteValue());
    } else if (type == short.class || type == Short.class) {
      setShort(offset, (value == null) ? 0 : ((Short)value).shortValue());
    } else if (type == char.class || type == Character.class) {
      setChar(offset, (value == null) ? Character.MIN_VALUE : ((Character)value).charValue());
    } else if (type == int.class || type == Integer.class) {
      setInt(offset, (value == null) ? 0 : ((Integer)value).intValue());
    } else if (type == long.class || type == Long.class) {
      setLong(offset, (value == null) ? 0L : ((Long)value).longValue());
    } else if (type == float.class || type == Float.class) {
      setFloat(offset, (value == null) ? 0.0F : ((Float)value).floatValue());
    } else if (type == double.class || type == Double.class) {
      setDouble(offset, (value == null) ? 0.0D : ((Double)value).doubleValue());
    } else if (type == Pointer.class) {
      setPointer(offset, (Pointer)value);
    } else if (type == String.class) {
      setPointer(offset, (Pointer)value);
    } else if (type == WString.class) {
      setPointer(offset, (Pointer)value);
    } else if (Structure.class.isAssignableFrom(type)) {
      Structure s = (Structure)value;
      if (Structure.ByReference.class.isAssignableFrom(type)) {
        setPointer(offset, (s == null) ? null : s.getPointer());
        if (s != null)
          s.autoWrite(); 
      } else {
        s.useMemory(this, (int)offset, true);
        s.write();
      } 
    } else if (Callback.class.isAssignableFrom(type)) {
      setPointer(offset, CallbackReference.getFunctionPointer((Callback)value));
    } else if (Platform.HAS_BUFFERS && Buffer.class.isAssignableFrom(type)) {
      Pointer p = (value == null) ? null : Native.getDirectBufferPointer((Buffer)value);
      setPointer(offset, p);
    } else if (NativeMapped.class.isAssignableFrom(type)) {
      NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
      Class<?> nativeType = tc.nativeType();
      setValue(offset, tc.toNative(value, new ToNativeContext()), nativeType);
    } else if (type.isArray()) {
      writeArray(offset, value, type.getComponentType());
    } else {
      throw new IllegalArgumentException("Writing " + type + " to memory is not supported");
    } 
  }
  
  private void writeArray(long offset, Object value, Class<?> cls) {
    if (cls == byte.class) {
      byte[] buf = (byte[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == short.class) {
      short[] buf = (short[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == char.class) {
      char[] buf = (char[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == int.class) {
      int[] buf = (int[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == long.class) {
      long[] buf = (long[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == float.class) {
      float[] buf = (float[])value;
      write(offset, buf, 0, buf.length);
    } else if (cls == double.class) {
      double[] buf = (double[])value;
      write(offset, buf, 0, buf.length);
    } else if (Pointer.class.isAssignableFrom(cls)) {
      Pointer[] buf = (Pointer[])value;
      write(offset, buf, 0, buf.length);
    } else if (Structure.class.isAssignableFrom(cls)) {
      Structure[] sbuf = (Structure[])value;
      if (Structure.ByReference.class.isAssignableFrom(cls)) {
        Pointer[] buf = new Pointer[sbuf.length];
        for (int i = 0; i < sbuf.length; i++) {
          if (sbuf[i] == null) {
            buf[i] = null;
          } else {
            buf[i] = sbuf[i].getPointer();
            sbuf[i].write();
          } 
        } 
        write(offset, buf, 0, buf.length);
      } else {
        Structure first = sbuf[0];
        if (first == null) {
          first = Structure.newInstance(cls, share(offset));
          sbuf[0] = first;
        } else {
          first.useMemory(this, (int)offset, true);
        } 
        first.write();
        Structure[] tmp = first.toArray(sbuf.length);
        for (int i = 1; i < sbuf.length; i++) {
          if (sbuf[i] == null) {
            sbuf[i] = tmp[i];
          } else {
            sbuf[i].useMemory(this, (int)(offset + (i * sbuf[i].size())), true);
          } 
          sbuf[i].write();
        } 
      } 
    } else if (NativeMapped.class.isAssignableFrom(cls)) {
      NativeMapped[] buf = (NativeMapped[])value;
      NativeMappedConverter tc = NativeMappedConverter.getInstance(cls);
      Class<?> nativeType = tc.nativeType();
      int size = Native.getNativeSize(value.getClass(), value) / buf.length;
      for (int i = 0; i < buf.length; i++) {
        Object element = tc.toNative(buf[i], new ToNativeContext());
        setValue(offset + (i * size), element, nativeType);
      } 
    } else {
      throw new IllegalArgumentException("Writing array of " + cls + " to memory not supported");
    } 
  }
  
  public void setMemory(long offset, long length, byte value) {
    Native.setMemory(this, this.peer, offset, length, value);
  }
  
  public void setByte(long offset, byte value) {
    Native.setByte(this, this.peer, offset, value);
  }
  
  public void setShort(long offset, short value) {
    Native.setShort(this, this.peer, offset, value);
  }
  
  public void setChar(long offset, char value) {
    Native.setChar(this, this.peer, offset, value);
  }
  
  public void setInt(long offset, int value) {
    Native.setInt(this, this.peer, offset, value);
  }
  
  public void setLong(long offset, long value) {
    Native.setLong(this, this.peer, offset, value);
  }
  
  public void setNativeLong(long offset, NativeLong value) {
    if (NativeLong.SIZE == 8) {
      setLong(offset, value.longValue());
    } else {
      setInt(offset, value.intValue());
    } 
  }
  
  public void setFloat(long offset, float value) {
    Native.setFloat(this, this.peer, offset, value);
  }
  
  public void setDouble(long offset, double value) {
    Native.setDouble(this, this.peer, offset, value);
  }
  
  public void setPointer(long offset, Pointer value) {
    Native.setPointer(this, this.peer, offset, (value != null) ? value.peer : 0L);
  }
  
  public void setWideString(long offset, String value) {
    Native.setWideString(this, this.peer, offset, value);
  }
  
  public void setString(long offset, WString value) {
    setWideString(offset, (value == null) ? null : value.toString());
  }
  
  public void setString(long offset, String value) {
    setString(offset, value, Native.getDefaultStringEncoding());
  }
  
  public void setString(long offset, String value, String encoding) {
    byte[] data = Native.getBytes(value, encoding);
    write(offset, data, 0, data.length);
    setByte(offset + data.length, (byte)0);
  }
  
  public String dump(long offset, int size) {
    int BYTES_PER_ROW = 4;
    String TITLE = "memory dump";
    StringWriter sw = new StringWriter("memory dump".length() + 2 + size * 2 + size / 4 * 4);
    PrintWriter out = new PrintWriter(sw);
    out.println("memory dump");
    for (int i = 0; i < size; i++) {
      byte b = getByte(offset + i);
      if (i % 4 == 0)
        out.print("["); 
      if (b >= 0 && b < 16)
        out.print("0"); 
      out.print(Integer.toHexString(b & 0xFF));
      if (i % 4 == 3 && i < size - 1)
        out.println("]"); 
    } 
    if (sw.getBuffer().charAt(sw.getBuffer().length() - 2) != ']')
      out.println("]"); 
    return sw.toString();
  }
  
  public String toString() {
    return "native@0x" + Long.toHexString(this.peer);
  }
  
  public static long nativeValue(Pointer p) {
    return (p == null) ? 0L : p.peer;
  }
  
  public static void nativeValue(Pointer p, long value) {
    p.peer = value;
  }
  
  private static class Opaque extends Pointer {
    private final String MSG;
    
    private Opaque(long peer) {
      super(peer);
      this.MSG = "This pointer is opaque: " + this;
    }
    
    public Pointer share(long offset, long size) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void clear(long size) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public long indexOf(long offset, byte value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, byte[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, char[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, short[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, int[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, long[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, float[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, double[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void read(long bOff, Pointer[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, byte[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, char[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, short[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, int[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, long[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, float[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, double[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void write(long bOff, Pointer[] buf, int index, int length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public ByteBuffer getByteBuffer(long offset, long length) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public byte getByte(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public char getChar(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public short getShort(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public int getInt(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public long getLong(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public float getFloat(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public double getDouble(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public Pointer getPointer(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public String getString(long bOff, String encoding) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public String getWideString(long bOff) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setByte(long bOff, byte value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setChar(long bOff, char value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setShort(long bOff, short value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setInt(long bOff, int value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setLong(long bOff, long value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setFloat(long bOff, float value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setDouble(long bOff, double value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setPointer(long offset, Pointer value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setString(long offset, String value, String encoding) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setWideString(long offset, String value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public void setMemory(long offset, long size, byte value) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public String dump(long offset, int size) {
      throw new UnsupportedOperationException(this.MSG);
    }
    
    public String toString() {
      return "const@0x" + Long.toHexString(this.peer);
    }
  }
}
