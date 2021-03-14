package com.google.common.reflect;

import com.google.common.base.Preconditions;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract class TypeCapture<T> {
  final Type capture() {
    Type superclass = getClass().getGenericSuperclass();
    Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", new Object[] { superclass });
    return ((ParameterizedType)superclass).getActualTypeArguments()[0];
  }
}
