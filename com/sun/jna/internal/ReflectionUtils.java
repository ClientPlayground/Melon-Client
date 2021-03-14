package com.sun.jna.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {
  private static final Logger LOG = Logger.getLogger(ReflectionUtils.class.getName());
  
  private static Constructor getConstructorLookupClass() {
    if (CONSTRUCTOR_LOOKUP_CLASS == null) {
      Class lookup = lookupClass("java.lang.invoke.MethodHandles$Lookup");
      CONSTRUCTOR_LOOKUP_CLASS = lookupDeclaredConstructor(lookup, new Class[] { Class.class });
    } 
    return CONSTRUCTOR_LOOKUP_CLASS;
  }
  
  static {
    Class methodHandles = lookupClass("java.lang.invoke.MethodHandles");
    Class methodHandle = lookupClass("java.lang.invoke.MethodHandle");
    Class lookup = lookupClass("java.lang.invoke.MethodHandles$Lookup");
    Class methodType = lookupClass("java.lang.invoke.MethodType");
  }
  
  private static final Method METHOD_IS_DEFAULT = lookupMethod(Method.class, "isDefault", new Class[0]);
  
  private static final Method METHOD_HANDLES_LOOKUP;
  
  private static final Method METHOD_HANDLES_LOOKUP_IN;
  
  private static final Method METHOD_HANDLES_PRIVATE_LOOKUP_IN;
  
  private static final Method METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL;
  
  private static final Method METHOD_HANDLES_LOOKUP_FIND_SPECIAL;
  
  private static final Method METHOD_HANDLES_BIND_TO;
  
  private static final Method METHOD_HANDLES_INVOKE_WITH_ARGUMENTS;
  
  private static final Method METHOD_TYPE;
  
  private static Constructor CONSTRUCTOR_LOOKUP_CLASS;
  
  static {
    METHOD_HANDLES_LOOKUP = lookupMethod(methodHandles, "lookup", new Class[0]);
    METHOD_HANDLES_LOOKUP_IN = lookupMethod(lookup, "in", new Class[] { Class.class });
    METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL = lookupMethod(lookup, "unreflectSpecial", new Class[] { Method.class, Class.class });
    METHOD_HANDLES_LOOKUP_FIND_SPECIAL = lookupMethod(lookup, "findSpecial", new Class[] { Class.class, String.class, methodType, Class.class });
    METHOD_HANDLES_BIND_TO = lookupMethod(methodHandle, "bindTo", new Class[] { Object.class });
    METHOD_HANDLES_INVOKE_WITH_ARGUMENTS = lookupMethod(methodHandle, "invokeWithArguments", new Class[] { Object[].class });
    METHOD_HANDLES_PRIVATE_LOOKUP_IN = lookupMethod(methodHandles, "privateLookupIn", new Class[] { Class.class, lookup });
    METHOD_TYPE = lookupMethod(methodType, "methodType", new Class[] { Class.class, Class[].class });
  }
  
  private static Constructor lookupDeclaredConstructor(Class clazz, Class... arguments) {
    if (clazz == null) {
      LOG.log(Level.FINE, "Failed to lookup method: <init>#{1}({2})", new Object[] { clazz, 
            Arrays.toString((Object[])arguments) });
      return null;
    } 
    try {
      Constructor init = clazz.getDeclaredConstructor(arguments);
      init.setAccessible(true);
      return init;
    } catch (Exception ex) {
      LOG.log(Level.FINE, "Failed to lookup method: <init>#{1}({2})", new Object[] { clazz, 
            Arrays.toString((Object[])arguments) });
      return null;
    } 
  }
  
  private static Method lookupMethod(Class clazz, String methodName, Class... arguments) {
    if (clazz == null) {
      LOG.log(Level.FINE, "Failed to lookup method: {0}#{1}({2})", new Object[] { clazz, methodName, 
            Arrays.toString((Object[])arguments) });
      return null;
    } 
    try {
      return clazz.getMethod(methodName, arguments);
    } catch (Exception ex) {
      LOG.log(Level.FINE, "Failed to lookup method: {0}#{1}({2})", new Object[] { clazz, methodName, 
            Arrays.toString((Object[])arguments) });
      return null;
    } 
  }
  
  private static Class lookupClass(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException ex) {
      LOG.log(Level.FINE, "Failed to lookup class: " + name, ex);
      return null;
    } 
  }
  
  public static boolean isDefault(Method method) {
    if (METHOD_IS_DEFAULT == null)
      return false; 
    try {
      return ((Boolean)METHOD_IS_DEFAULT.invoke(method, new Object[0])).booleanValue();
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof RuntimeException)
        throw (RuntimeException)cause; 
      if (cause instanceof Error)
        throw (Error)cause; 
      throw new RuntimeException(cause);
    } 
  }
  
  public static Object getMethodHandle(Method method) throws Exception {
    assert isDefault(method);
    Object baseLookup = createLookup();
    try {
      Object lookup = createPrivateLookupIn(method.getDeclaringClass(), baseLookup);
      Object mh = mhViaFindSpecial(lookup, method);
      return mh;
    } catch (Exception ex) {
      Object lookup = getConstructorLookupClass().newInstance(new Object[] { method.getDeclaringClass() });
      Object mh = mhViaUnreflectSpecial(lookup, method);
      return mh;
    } 
  }
  
  private static Object mhViaFindSpecial(Object lookup, Method method) throws Exception {
    return METHOD_HANDLES_LOOKUP_FIND_SPECIAL.invoke(lookup, new Object[] { method
          
          .getDeclaringClass(), method
          .getName(), METHOD_TYPE
          .invoke(null, new Object[] { method.getReturnType(), method.getParameterTypes() }), method
          .getDeclaringClass() });
  }
  
  private static Object mhViaUnreflectSpecial(Object lookup, Method method) throws Exception {
    Object l2 = METHOD_HANDLES_LOOKUP_IN.invoke(lookup, new Object[] { method.getDeclaringClass() });
    return METHOD_HANDLES_LOOKUP_UNREFLECT_SPECIAL.invoke(l2, new Object[] { method, method.getDeclaringClass() });
  }
  
  private static Object createPrivateLookupIn(Class type, Object lookup) throws Exception {
    return METHOD_HANDLES_PRIVATE_LOOKUP_IN.invoke(null, new Object[] { type, lookup });
  }
  
  private static Object createLookup() throws Exception {
    return METHOD_HANDLES_LOOKUP.invoke(null, new Object[0]);
  }
  
  public static Object invokeDefaultMethod(Object target, Object methodHandle, Object... args) throws Throwable {
    Object boundMethodHandle = METHOD_HANDLES_BIND_TO.invoke(methodHandle, new Object[] { target });
    return METHOD_HANDLES_INVOKE_WITH_ARGUMENTS.invoke(boundMethodHandle, new Object[] { args });
  }
}
