package com.replaymod.replaystudio.us.myles.ViaVersion.api.type;

import com.github.steveice10.netty.buffer.ByteBuf;

public abstract class PartialType<T, X> extends Type<T> {
  private final X param;
  
  public PartialType(X param, Class<T> type) {
    super(type);
    this.param = param;
  }
  
  public abstract T read(ByteBuf paramByteBuf, X paramX) throws Exception;
  
  public abstract void write(ByteBuf paramByteBuf, X paramX, T paramT) throws Exception;
  
  public T read(ByteBuf buffer) throws Exception {
    return read(buffer, this.param);
  }
  
  public void write(ByteBuf buffer, T object) throws Exception {
    write(buffer, this.param, object);
  }
}
