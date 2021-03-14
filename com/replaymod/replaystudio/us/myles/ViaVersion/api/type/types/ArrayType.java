package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.lang.reflect.Array;

public class ArrayType<T> extends Type<T[]> {
  private final Type<T> elementType;
  
  public ArrayType(Type<T> type) {
    super(type.getTypeName() + " Array", getArrayClass(type.getOutputClass()));
    this.elementType = type;
  }
  
  public static Class<?> getArrayClass(Class<?> componentType) {
    return Array.newInstance(componentType, 0).getClass();
  }
  
  public T[] read(ByteBuf buffer) throws Exception {
    int amount = ((Integer)Type.VAR_INT.read(buffer)).intValue();
    T[] array = (T[])Array.newInstance(this.elementType.getOutputClass(), amount);
    for (int i = 0; i < amount; i++)
      array[i] = (T)this.elementType.read(buffer); 
    return array;
  }
  
  public void write(ByteBuf buffer, T[] object) throws Exception {
    Type.VAR_INT.write(buffer, Integer.valueOf(object.length));
    for (T o : object)
      this.elementType.write(buffer, o); 
  }
}
