package com.sun.jna;

public abstract class IntegerType extends Number implements NativeMapped {
  private static final long serialVersionUID = 1L;
  
  private int size;
  
  private Number number;
  
  private boolean unsigned;
  
  private long value;
  
  public IntegerType(int size) {
    this(size, 0L, false);
  }
  
  public IntegerType(int size, boolean unsigned) {
    this(size, 0L, unsigned);
  }
  
  public IntegerType(int size, long value) {
    this(size, value, false);
  }
  
  public IntegerType(int size, long value, boolean unsigned) {
    this.size = size;
    this.unsigned = unsigned;
    setValue(value);
  }
  
  public void setValue(long value) {
    long truncated = value;
    this.value = value;
    switch (this.size) {
      case 1:
        if (this.unsigned)
          this.value = value & 0xFFL; 
        truncated = (byte)(int)value;
        this.number = Byte.valueOf((byte)(int)value);
        break;
      case 2:
        if (this.unsigned)
          this.value = value & 0xFFFFL; 
        truncated = (short)(int)value;
        this.number = Short.valueOf((short)(int)value);
        break;
      case 4:
        if (this.unsigned)
          this.value = value & 0xFFFFFFFFL; 
        truncated = (int)value;
        this.number = Integer.valueOf((int)value);
        break;
      case 8:
        this.number = Long.valueOf(value);
        break;
      default:
        throw new IllegalArgumentException("Unsupported size: " + this.size);
    } 
    if (this.size < 8) {
      long mask = (1L << this.size * 8) - 1L ^ 0xFFFFFFFFFFFFFFFFL;
      if ((value < 0L && truncated != value) || (value >= 0L && (mask & value) != 0L))
        throw new IllegalArgumentException("Argument value 0x" + 
            Long.toHexString(value) + " exceeds native capacity (" + this.size + " bytes) mask=0x" + 
            Long.toHexString(mask)); 
    } 
  }
  
  public Object toNative() {
    return this.number;
  }
  
  public Object fromNative(Object nativeValue, FromNativeContext context) {
    long value = (nativeValue == null) ? 0L : ((Number)nativeValue).longValue();
    IntegerType number = (IntegerType)Klass.newInstance(getClass());
    number.setValue(value);
    return number;
  }
  
  public Class<?> nativeType() {
    return this.number.getClass();
  }
  
  public int intValue() {
    return (int)this.value;
  }
  
  public long longValue() {
    return this.value;
  }
  
  public float floatValue() {
    return this.number.floatValue();
  }
  
  public double doubleValue() {
    return this.number.doubleValue();
  }
  
  public boolean equals(Object rhs) {
    return (rhs instanceof IntegerType && this.number
      .equals(((IntegerType)rhs).number));
  }
  
  public String toString() {
    return this.number.toString();
  }
  
  public int hashCode() {
    return this.number.hashCode();
  }
  
  public static <T extends IntegerType> int compare(T v1, T v2) {
    if (v1 == v2)
      return 0; 
    if (v1 == null)
      return 1; 
    if (v2 == null)
      return -1; 
    return compare(v1.longValue(), v2.longValue());
  }
  
  public static int compare(IntegerType v1, long v2) {
    if (v1 == null)
      return 1; 
    return compare(v1.longValue(), v2);
  }
  
  public static final int compare(long v1, long v2) {
    if (v1 == v2)
      return 0; 
    if (v1 < v2)
      return -1; 
    return 1;
  }
}
