package com.github.steveice10.netty.buffer;

class UnpooledSlicedByteBuf extends AbstractUnpooledSlicedByteBuf {
  UnpooledSlicedByteBuf(AbstractByteBuf buffer, int index, int length) {
    super(buffer, index, length);
  }
  
  public int capacity() {
    return maxCapacity();
  }
  
  public AbstractByteBuf unwrap() {
    return (AbstractByteBuf)super.unwrap();
  }
  
  protected byte _getByte(int index) {
    return unwrap()._getByte(idx(index));
  }
  
  protected short _getShort(int index) {
    return unwrap()._getShort(idx(index));
  }
  
  protected short _getShortLE(int index) {
    return unwrap()._getShortLE(idx(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return unwrap()._getUnsignedMedium(idx(index));
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return unwrap()._getUnsignedMediumLE(idx(index));
  }
  
  protected int _getInt(int index) {
    return unwrap()._getInt(idx(index));
  }
  
  protected int _getIntLE(int index) {
    return unwrap()._getIntLE(idx(index));
  }
  
  protected long _getLong(int index) {
    return unwrap()._getLong(idx(index));
  }
  
  protected long _getLongLE(int index) {
    return unwrap()._getLongLE(idx(index));
  }
  
  protected void _setByte(int index, int value) {
    unwrap()._setByte(idx(index), value);
  }
  
  protected void _setShort(int index, int value) {
    unwrap()._setShort(idx(index), value);
  }
  
  protected void _setShortLE(int index, int value) {
    unwrap()._setShortLE(idx(index), value);
  }
  
  protected void _setMedium(int index, int value) {
    unwrap()._setMedium(idx(index), value);
  }
  
  protected void _setMediumLE(int index, int value) {
    unwrap()._setMediumLE(idx(index), value);
  }
  
  protected void _setInt(int index, int value) {
    unwrap()._setInt(idx(index), value);
  }
  
  protected void _setIntLE(int index, int value) {
    unwrap()._setIntLE(idx(index), value);
  }
  
  protected void _setLong(int index, long value) {
    unwrap()._setLong(idx(index), value);
  }
  
  protected void _setLongLE(int index, long value) {
    unwrap()._setLongLE(idx(index), value);
  }
}
