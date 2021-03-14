package com.github.steveice10.netty.buffer;

class UnpooledDuplicatedByteBuf extends DuplicatedByteBuf {
  UnpooledDuplicatedByteBuf(AbstractByteBuf buffer) {
    super(buffer);
  }
  
  public AbstractByteBuf unwrap() {
    return (AbstractByteBuf)super.unwrap();
  }
  
  protected byte _getByte(int index) {
    return unwrap()._getByte(index);
  }
  
  protected short _getShort(int index) {
    return unwrap()._getShort(index);
  }
  
  protected short _getShortLE(int index) {
    return unwrap()._getShortLE(index);
  }
  
  protected int _getUnsignedMedium(int index) {
    return unwrap()._getUnsignedMedium(index);
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return unwrap()._getUnsignedMediumLE(index);
  }
  
  protected int _getInt(int index) {
    return unwrap()._getInt(index);
  }
  
  protected int _getIntLE(int index) {
    return unwrap()._getIntLE(index);
  }
  
  protected long _getLong(int index) {
    return unwrap()._getLong(index);
  }
  
  protected long _getLongLE(int index) {
    return unwrap()._getLongLE(index);
  }
  
  protected void _setByte(int index, int value) {
    unwrap()._setByte(index, value);
  }
  
  protected void _setShort(int index, int value) {
    unwrap()._setShort(index, value);
  }
  
  protected void _setShortLE(int index, int value) {
    unwrap()._setShortLE(index, value);
  }
  
  protected void _setMedium(int index, int value) {
    unwrap()._setMedium(index, value);
  }
  
  protected void _setMediumLE(int index, int value) {
    unwrap()._setMediumLE(index, value);
  }
  
  protected void _setInt(int index, int value) {
    unwrap()._setInt(index, value);
  }
  
  protected void _setIntLE(int index, int value) {
    unwrap()._setIntLE(index, value);
  }
  
  protected void _setLong(int index, long value) {
    unwrap()._setLong(index, value);
  }
  
  protected void _setLongLE(int index, long value) {
    unwrap()._setLongLE(index, value);
  }
}
