package com.github.steveice10.netty.handler.codec;

public final class UnsupportedValueConverter<V> implements ValueConverter<V> {
  private static final UnsupportedValueConverter INSTANCE = new UnsupportedValueConverter();
  
  public static <V> UnsupportedValueConverter<V> instance() {
    return INSTANCE;
  }
  
  public V convertObject(Object value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertBoolean(boolean value) {
    throw new UnsupportedOperationException();
  }
  
  public boolean convertToBoolean(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertByte(byte value) {
    throw new UnsupportedOperationException();
  }
  
  public byte convertToByte(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertChar(char value) {
    throw new UnsupportedOperationException();
  }
  
  public char convertToChar(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertShort(short value) {
    throw new UnsupportedOperationException();
  }
  
  public short convertToShort(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertInt(int value) {
    throw new UnsupportedOperationException();
  }
  
  public int convertToInt(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertLong(long value) {
    throw new UnsupportedOperationException();
  }
  
  public long convertToLong(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertTimeMillis(long value) {
    throw new UnsupportedOperationException();
  }
  
  public long convertToTimeMillis(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertFloat(float value) {
    throw new UnsupportedOperationException();
  }
  
  public float convertToFloat(V value) {
    throw new UnsupportedOperationException();
  }
  
  public V convertDouble(double value) {
    throw new UnsupportedOperationException();
  }
  
  public double convertToDouble(V value) {
    throw new UnsupportedOperationException();
  }
}
