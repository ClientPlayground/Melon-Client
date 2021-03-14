package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {
  <T extends B> T getInstance(Class<T> paramClass);
  
  <T extends B> T putInstance(Class<T> paramClass, @Nullable T paramT);
}
