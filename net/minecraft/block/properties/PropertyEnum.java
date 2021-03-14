package net.minecraft.block.properties;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.util.IStringSerializable;

public class PropertyEnum<T extends Enum<T> & IStringSerializable> extends PropertyHelper<T> {
  private final ImmutableSet<T> allowedValues;
  
  private final Map<String, T> nameToValue = Maps.newHashMap();
  
  protected PropertyEnum(String name, Class<T> valueClass, Collection<T> allowedValues) {
    super(name, valueClass);
    this.allowedValues = ImmutableSet.copyOf(allowedValues);
    for (Enum enum_ : allowedValues) {
      String s = ((IStringSerializable)enum_).getName();
      if (this.nameToValue.containsKey(s))
        throw new IllegalArgumentException("Multiple values have the same name '" + s + "'"); 
      this.nameToValue.put(s, (T)enum_);
    } 
  }
  
  public Collection<T> getAllowedValues() {
    return (Collection<T>)this.allowedValues;
  }
  
  public String getName(T value) {
    return ((IStringSerializable)value).getName();
  }
  
  public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: invokestatic alwaysTrue : ()Lcom/google/common/base/Predicate;
    //   5: invokestatic create : (Ljava/lang/String;Ljava/lang/Class;Lcom/google/common/base/Predicate;)Lnet/minecraft/block/properties/PropertyEnum;
    //   8: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #51	-> 0
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	9	0	name	Ljava/lang/String;
    //   0	9	1	clazz	Ljava/lang/Class;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   0	9	1	clazz	Ljava/lang/Class<TT;>;
  }
  
  public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, Predicate<T> filter) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: aload_1
    //   3: invokevirtual getEnumConstants : ()[Ljava/lang/Object;
    //   6: invokestatic newArrayList : ([Ljava/lang/Object;)Ljava/util/ArrayList;
    //   9: aload_2
    //   10: invokestatic filter : (Ljava/util/Collection;Lcom/google/common/base/Predicate;)Ljava/util/Collection;
    //   13: invokestatic create : (Ljava/lang/String;Ljava/lang/Class;Ljava/util/Collection;)Lnet/minecraft/block/properties/PropertyEnum;
    //   16: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #56	-> 0
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	17	0	name	Ljava/lang/String;
    //   0	17	1	clazz	Ljava/lang/Class;
    //   0	17	2	filter	Lcom/google/common/base/Predicate;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   0	17	1	clazz	Ljava/lang/Class<TT;>;
    //   0	17	2	filter	Lcom/google/common/base/Predicate<TT;>;
  }
  
  public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, T... values) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: aload_2
    //   3: invokestatic newArrayList : ([Ljava/lang/Object;)Ljava/util/ArrayList;
    //   6: invokestatic create : (Ljava/lang/String;Ljava/lang/Class;Ljava/util/Collection;)Lnet/minecraft/block/properties/PropertyEnum;
    //   9: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #61	-> 0
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	10	0	name	Ljava/lang/String;
    //   0	10	1	clazz	Ljava/lang/Class;
    //   0	10	2	values	[Ljava/lang/Enum;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   0	10	1	clazz	Ljava/lang/Class<TT;>;
    //   0	10	2	values	[TT;
  }
  
  public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String name, Class<T> clazz, Collection<T> values) {
    // Byte code:
    //   0: new net/minecraft/block/properties/PropertyEnum
    //   3: dup
    //   4: aload_0
    //   5: aload_1
    //   6: aload_2
    //   7: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Class;Ljava/util/Collection;)V
    //   10: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #66	-> 0
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	11	0	name	Ljava/lang/String;
    //   0	11	1	clazz	Ljava/lang/Class;
    //   0	11	2	values	Ljava/util/Collection;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   0	11	1	clazz	Ljava/lang/Class<TT;>;
    //   0	11	2	values	Ljava/util/Collection<TT;>;
  }
}
