package com.replaymod.replaystudio.us.myles.ViaVersion.util;

import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ReflectionUtil {
  public static Object invokeStatic(Class<?> clazz, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method m = clazz.getDeclaredMethod(method, new Class[0]);
    return m.invoke(null, new Object[0]);
  }
  
  public static Object invoke(Object o, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method m = o.getClass().getDeclaredMethod(method, new Class[0]);
    return m.invoke(o, new Object[0]);
  }
  
  public static <T> T getStatic(Class<?> clazz, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
    Field field = clazz.getDeclaredField(f);
    field.setAccessible(true);
    return (T)field.get((Object)null);
  }
  
  public static void setStatic(Class<?> clazz, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
    Field field = clazz.getDeclaredField(f);
    field.setAccessible(true);
    field.set((Object)null, value);
  }
  
  public static <T> T getSuper(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
    Field field = o.getClass().getSuperclass().getDeclaredField(f);
    field.setAccessible(true);
    return (T)field.get(o);
  }
  
  public static <T> T get(Object instance, Class<?> clazz, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
    Field field = clazz.getDeclaredField(f);
    field.setAccessible(true);
    return (T)field.get(instance);
  }
  
  public static <T> T get(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
    Field field = o.getClass().getDeclaredField(f);
    field.setAccessible(true);
    return (T)field.get(o);
  }
  
  public static <T> T getPublic(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
    Field field = o.getClass().getField(f);
    field.setAccessible(true);
    return (T)field.get(o);
  }
  
  public static void set(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
    Field field = o.getClass().getDeclaredField(f);
    field.setAccessible(true);
    field.set(o, value);
  }
  
  public static final class ClassReflection {
    private final Class<?> handle;
    
    private final Map<String, Field> fields = Maps.newConcurrentMap();
    
    private final Map<String, Method> methods = Maps.newConcurrentMap();
    
    public ClassReflection(Class<?> handle) {
      this(handle, true);
    }
    
    public ClassReflection(Class<?> handle, boolean recursive) {
      this.handle = handle;
      scanFields(handle, recursive);
      scanMethods(handle, recursive);
    }
    
    private void scanFields(Class<?> host, boolean recursive) {
      if (host.getSuperclass() != null && recursive)
        scanFields(host.getSuperclass(), true); 
      for (Field field : host.getDeclaredFields()) {
        field.setAccessible(true);
        this.fields.put(field.getName(), field);
      } 
    }
    
    private void scanMethods(Class<?> host, boolean recursive) {
      if (host.getSuperclass() != null && recursive)
        scanMethods(host.getSuperclass(), true); 
      for (Method method : host.getDeclaredMethods()) {
        method.setAccessible(true);
        this.methods.put(method.getName(), method);
      } 
    }
    
    public Object newInstance() throws IllegalAccessException, InstantiationException {
      return this.handle.newInstance();
    }
    
    public Field getField(String name) {
      return this.fields.get(name);
    }
    
    public void setFieldValue(String fieldName, Object instance, Object value) throws IllegalAccessException {
      getField(fieldName).set(instance, value);
    }
    
    public <T> T getFieldValue(String fieldName, Object instance, Class<T> type) throws IllegalAccessException {
      return type.cast(getField(fieldName).get(instance));
    }
    
    public <T> T invokeMethod(Class<T> type, String methodName, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
      return type.cast(getMethod(methodName).invoke(instance, args));
    }
    
    public Method getMethod(String name) {
      return this.methods.get(name);
    }
    
    public Collection<Field> getFields() {
      return Collections.unmodifiableCollection(this.fields.values());
    }
    
    public Collection<Method> getMethods() {
      return Collections.unmodifiableCollection(this.methods.values());
    }
  }
}
