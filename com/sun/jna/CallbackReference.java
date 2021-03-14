package com.sun.jna;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class CallbackReference extends WeakReference<Callback> {
  static final Map<Callback, CallbackReference> callbackMap = new WeakHashMap<Callback, CallbackReference>();
  
  static final Map<Callback, CallbackReference> directCallbackMap = new WeakHashMap<Callback, CallbackReference>();
  
  static final Map<Pointer, Reference<Callback>> pointerCallbackMap = new WeakHashMap<Pointer, Reference<Callback>>();
  
  static final Map<Object, Object> allocations = new WeakHashMap<Object, Object>();
  
  private static final Map<CallbackReference, Reference<CallbackReference>> allocatedMemory = Collections.synchronizedMap(new WeakHashMap<CallbackReference, Reference<CallbackReference>>());
  
  private static final Method PROXY_CALLBACK_METHOD;
  
  static {
    try {
      PROXY_CALLBACK_METHOD = CallbackProxy.class.getMethod("callback", new Class[] { Object[].class });
    } catch (Exception e) {
      throw new Error("Error looking up CallbackProxy.callback() method");
    } 
  }
  
  private static final Map<Callback, CallbackThreadInitializer> initializers = new WeakHashMap<Callback, CallbackThreadInitializer>();
  
  Pointer cbstruct;
  
  Pointer trampoline;
  
  CallbackProxy proxy;
  
  Method method;
  
  int callingConvention;
  
  static CallbackThreadInitializer setCallbackThreadInitializer(Callback cb, CallbackThreadInitializer initializer) {
    synchronized (initializers) {
      if (initializer != null)
        return initializers.put(cb, initializer); 
      return initializers.remove(cb);
    } 
  }
  
  static class AttachOptions extends Structure {
    AttachOptions() {
      setStringEncoding("utf8");
    }
    
    public static final List<String> FIELDS = createFieldsOrder(new String[] { "daemon", "detach", "name" });
    
    public boolean daemon;
    
    public boolean detach;
    
    public String name;
    
    protected List<String> getFieldOrder() {
      return FIELDS;
    }
  }
  
  private static ThreadGroup initializeThread(Callback cb, AttachOptions args) {
    CallbackThreadInitializer init = null;
    if (cb instanceof DefaultCallbackProxy)
      cb = ((DefaultCallbackProxy)cb).getCallback(); 
    synchronized (initializers) {
      init = initializers.get(cb);
    } 
    ThreadGroup group = null;
    if (init != null) {
      group = init.getThreadGroup(cb);
      args.name = init.getName(cb);
      args.daemon = init.isDaemon(cb);
      args.detach = init.detach(cb);
      args.write();
    } 
    return group;
  }
  
  public static Callback getCallback(Class<?> type, Pointer p) {
    return getCallback(type, p, false);
  }
  
  private static Callback getCallback(Class<?> type, Pointer p, boolean direct) {
    if (p == null)
      return null; 
    if (!type.isInterface())
      throw new IllegalArgumentException("Callback type must be an interface"); 
    Map<Callback, CallbackReference> map = direct ? directCallbackMap : callbackMap;
    synchronized (pointerCallbackMap) {
      Callback cb = null;
      Reference<Callback> ref = pointerCallbackMap.get(p);
      if (ref != null) {
        cb = ref.get();
        if (cb != null && !type.isAssignableFrom(cb.getClass()))
          throw new IllegalStateException("Pointer " + p + " already mapped to " + cb + ".\nNative code may be re-using a default function pointer, in which case you may need to use a common Callback class wherever the function pointer is reused."); 
        return cb;
      } 
      int ctype = AltCallingConvention.class.isAssignableFrom(type) ? 63 : 0;
      Map<String, Object> foptions = new HashMap<String, Object>(Native.getLibraryOptions(type));
      foptions.put("invoking-method", getCallbackMethod(type));
      NativeFunctionHandler h = new NativeFunctionHandler(p, ctype, foptions);
      cb = (Callback)Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, h);
      map.remove(cb);
      pointerCallbackMap.put(p, new WeakReference<Callback>(cb));
      return cb;
    } 
  }
  
  private CallbackReference(Callback callback, int callingConvention, boolean direct) {
    super(callback);
    TypeMapper mapper = Native.getTypeMapper(callback.getClass());
    this.callingConvention = callingConvention;
    boolean ppc = Platform.isPPC();
    if (direct) {
      Method m = getCallbackMethod(callback);
      Class<?>[] ptypes = m.getParameterTypes();
      for (int i = 0; i < ptypes.length; i++) {
        if (ppc && (ptypes[i] == float.class || ptypes[i] == double.class)) {
          direct = false;
          break;
        } 
        if (mapper != null && mapper
          .getFromNativeConverter(ptypes[i]) != null) {
          direct = false;
          break;
        } 
      } 
      if (mapper != null && mapper
        .getToNativeConverter(m.getReturnType()) != null)
        direct = false; 
    } 
    String encoding = Native.getStringEncoding(callback.getClass());
    long peer = 0L;
    if (direct) {
      this.method = getCallbackMethod(callback);
      Class<?>[] nativeParamTypes = this.method.getParameterTypes();
      Class<?> returnType = this.method.getReturnType();
      int flags = 1;
      if (callback instanceof com.sun.jna.win32.DLLCallback)
        flags |= 0x2; 
      peer = Native.createNativeCallback(callback, this.method, nativeParamTypes, returnType, callingConvention, flags, encoding);
    } else {
      if (callback instanceof CallbackProxy) {
        this.proxy = (CallbackProxy)callback;
      } else {
        this.proxy = new DefaultCallbackProxy(getCallbackMethod(callback), mapper, encoding);
      } 
      Class<?>[] nativeParamTypes = this.proxy.getParameterTypes();
      Class<?> returnType = this.proxy.getReturnType();
      if (mapper != null) {
        for (int j = 0; j < nativeParamTypes.length; j++) {
          FromNativeConverter rc = mapper.getFromNativeConverter(nativeParamTypes[j]);
          if (rc != null)
            nativeParamTypes[j] = rc.nativeType(); 
        } 
        ToNativeConverter tn = mapper.getToNativeConverter(returnType);
        if (tn != null)
          returnType = tn.nativeType(); 
      } 
      for (int i = 0; i < nativeParamTypes.length; i++) {
        nativeParamTypes[i] = getNativeType(nativeParamTypes[i]);
        if (!isAllowableNativeType(nativeParamTypes[i])) {
          String msg = "Callback argument " + nativeParamTypes[i] + " requires custom type conversion";
          throw new IllegalArgumentException(msg);
        } 
      } 
      returnType = getNativeType(returnType);
      if (!isAllowableNativeType(returnType)) {
        String msg = "Callback return type " + returnType + " requires custom type conversion";
        throw new IllegalArgumentException(msg);
      } 
      int flags = (callback instanceof com.sun.jna.win32.DLLCallback) ? 2 : 0;
      peer = Native.createNativeCallback(this.proxy, PROXY_CALLBACK_METHOD, nativeParamTypes, returnType, callingConvention, flags, encoding);
    } 
    this.cbstruct = (peer != 0L) ? new Pointer(peer) : null;
    allocatedMemory.put(this, new WeakReference<CallbackReference>(this));
  }
  
  private Class<?> getNativeType(Class<?> cls) {
    if (Structure.class.isAssignableFrom(cls)) {
      Structure.validate((Class)cls);
      if (!Structure.ByValue.class.isAssignableFrom(cls))
        return Pointer.class; 
    } else {
      if (NativeMapped.class.isAssignableFrom(cls))
        return NativeMappedConverter.getInstance(cls).nativeType(); 
      if (cls == String.class || cls == WString.class || cls == String[].class || cls == WString[].class || Callback.class
        
        .isAssignableFrom(cls))
        return Pointer.class; 
    } 
    return cls;
  }
  
  private static Method checkMethod(Method m) {
    if ((m.getParameterTypes()).length > 256) {
      String msg = "Method signature exceeds the maximum parameter count: " + m;
      throw new UnsupportedOperationException(msg);
    } 
    return m;
  }
  
  static Class<?> findCallbackClass(Class<?> type) {
    if (!Callback.class.isAssignableFrom(type))
      throw new IllegalArgumentException(type.getName() + " is not derived from com.sun.jna.Callback"); 
    if (type.isInterface())
      return type; 
    Class<?>[] ifaces = type.getInterfaces();
    for (int i = 0; i < ifaces.length; i++) {
      if (Callback.class.isAssignableFrom(ifaces[i]))
        try {
          getCallbackMethod(ifaces[i]);
          return ifaces[i];
        } catch (IllegalArgumentException e) {
          break;
        }  
    } 
    if (Callback.class.isAssignableFrom(type.getSuperclass()))
      return findCallbackClass(type.getSuperclass()); 
    return type;
  }
  
  private static Method getCallbackMethod(Callback callback) {
    return getCallbackMethod(findCallbackClass(callback.getClass()));
  }
  
  private static Method getCallbackMethod(Class<?> cls) {
    Method[] pubMethods = cls.getDeclaredMethods();
    Method[] classMethods = cls.getMethods();
    Set<Method> pmethods = new HashSet<Method>(Arrays.asList(pubMethods));
    pmethods.retainAll(Arrays.asList((Object[])classMethods));
    for (Iterator<Method> i = pmethods.iterator(); i.hasNext(); ) {
      Method m = i.next();
      if (Callback.FORBIDDEN_NAMES.contains(m.getName()))
        i.remove(); 
    } 
    Method[] methods = pmethods.<Method>toArray(new Method[0]);
    if (methods.length == 1)
      return checkMethod(methods[0]); 
    for (int j = 0; j < methods.length; j++) {
      Method m = methods[j];
      if ("callback".equals(m.getName()))
        return checkMethod(m); 
    } 
    String msg = "Callback must implement a single public method, or one public method named 'callback'";
    throw new IllegalArgumentException(msg);
  }
  
  private void setCallbackOptions(int options) {
    this.cbstruct.setInt(Native.POINTER_SIZE, options);
  }
  
  public Pointer getTrampoline() {
    if (this.trampoline == null)
      this.trampoline = this.cbstruct.getPointer(0L); 
    return this.trampoline;
  }
  
  protected void finalize() {
    dispose();
  }
  
  protected synchronized void dispose() {
    if (this.cbstruct != null)
      try {
        Native.freeNativeCallback(this.cbstruct.peer);
      } finally {
        this.cbstruct.peer = 0L;
        this.cbstruct = null;
        allocatedMemory.remove(this);
      }  
  }
  
  static void disposeAll() {
    Collection<CallbackReference> refs = new LinkedList<CallbackReference>(allocatedMemory.keySet());
    for (CallbackReference r : refs)
      r.dispose(); 
  }
  
  private Callback getCallback() {
    return get();
  }
  
  private static Pointer getNativeFunctionPointer(Callback cb) {
    if (Proxy.isProxyClass(cb.getClass())) {
      Object handler = Proxy.getInvocationHandler(cb);
      if (handler instanceof NativeFunctionHandler)
        return ((NativeFunctionHandler)handler).getPointer(); 
    } 
    return null;
  }
  
  public static Pointer getFunctionPointer(Callback cb) {
    return getFunctionPointer(cb, false);
  }
  
  private static Pointer getFunctionPointer(Callback cb, boolean direct) {
    Pointer fp = null;
    if (cb == null)
      return null; 
    if ((fp = getNativeFunctionPointer(cb)) != null)
      return fp; 
    Map<String, ?> options = Native.getLibraryOptions(cb.getClass());
    int callingConvention = (cb instanceof AltCallingConvention) ? 63 : ((options != null && options.containsKey("calling-convention")) ? ((Integer)options.get("calling-convention")).intValue() : 0);
    Map<Callback, CallbackReference> map = direct ? directCallbackMap : callbackMap;
    synchronized (pointerCallbackMap) {
      CallbackReference cbref = map.get(cb);
      if (cbref == null) {
        cbref = new CallbackReference(cb, callingConvention, direct);
        map.put(cb, cbref);
        pointerCallbackMap.put(cbref.getTrampoline(), new WeakReference<Callback>(cb));
        if (initializers.containsKey(cb))
          cbref.setCallbackOptions(1); 
      } 
      return cbref.getTrampoline();
    } 
  }
  
  private class DefaultCallbackProxy implements CallbackProxy {
    private final Method callbackMethod;
    
    private ToNativeConverter toNative;
    
    private final FromNativeConverter[] fromNative;
    
    private final String encoding;
    
    public DefaultCallbackProxy(Method callbackMethod, TypeMapper mapper, String encoding) {
      this.callbackMethod = callbackMethod;
      this.encoding = encoding;
      Class<?>[] argTypes = callbackMethod.getParameterTypes();
      Class<?> returnType = callbackMethod.getReturnType();
      this.fromNative = new FromNativeConverter[argTypes.length];
      if (NativeMapped.class.isAssignableFrom(returnType)) {
        this.toNative = NativeMappedConverter.getInstance(returnType);
      } else if (mapper != null) {
        this.toNative = mapper.getToNativeConverter(returnType);
      } 
      for (int i = 0; i < this.fromNative.length; i++) {
        if (NativeMapped.class.isAssignableFrom(argTypes[i])) {
          this.fromNative[i] = new NativeMappedConverter(argTypes[i]);
        } else if (mapper != null) {
          this.fromNative[i] = mapper.getFromNativeConverter(argTypes[i]);
        } 
      } 
      if (!callbackMethod.isAccessible())
        try {
          callbackMethod.setAccessible(true);
        } catch (SecurityException e) {
          throw new IllegalArgumentException("Callback method is inaccessible, make sure the interface is public: " + callbackMethod);
        }  
    }
    
    public Callback getCallback() {
      return CallbackReference.this.getCallback();
    }
    
    private Object invokeCallback(Object[] args) {
      Class<?>[] paramTypes = this.callbackMethod.getParameterTypes();
      Object[] callbackArgs = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        Class<?> type = paramTypes[i];
        Object arg = args[i];
        if (this.fromNative[i] != null) {
          FromNativeContext context = new CallbackParameterContext(type, this.callbackMethod, args, i);
          callbackArgs[i] = this.fromNative[i].fromNative(arg, context);
        } else {
          callbackArgs[i] = convertArgument(arg, type);
        } 
      } 
      Object result = null;
      Callback cb = getCallback();
      if (cb != null)
        try {
          result = convertResult(this.callbackMethod.invoke(cb, callbackArgs));
        } catch (IllegalArgumentException e) {
          Native.getCallbackExceptionHandler().uncaughtException(cb, e);
        } catch (IllegalAccessException e) {
          Native.getCallbackExceptionHandler().uncaughtException(cb, e);
        } catch (InvocationTargetException e) {
          Native.getCallbackExceptionHandler().uncaughtException(cb, e.getTargetException());
        }  
      for (int j = 0; j < callbackArgs.length; j++) {
        if (callbackArgs[j] instanceof Structure && !(callbackArgs[j] instanceof Structure.ByValue))
          ((Structure)callbackArgs[j]).autoWrite(); 
      } 
      return result;
    }
    
    public Object callback(Object[] args) {
      try {
        return invokeCallback(args);
      } catch (Throwable t) {
        Native.getCallbackExceptionHandler().uncaughtException(getCallback(), t);
        return null;
      } 
    }
    
    private Object convertArgument(Object value, Class<?> dstType) {
      if (value instanceof Pointer) {
        if (dstType == String.class) {
          value = ((Pointer)value).getString(0L, this.encoding);
        } else if (dstType == WString.class) {
          value = new WString(((Pointer)value).getWideString(0L));
        } else if (dstType == String[].class) {
          value = ((Pointer)value).getStringArray(0L, this.encoding);
        } else if (dstType == WString[].class) {
          value = ((Pointer)value).getWideStringArray(0L);
        } else if (Callback.class.isAssignableFrom(dstType)) {
          value = CallbackReference.getCallback(dstType, (Pointer)value);
        } else if (Structure.class.isAssignableFrom(dstType)) {
          if (Structure.ByValue.class.isAssignableFrom(dstType)) {
            Structure s = (Structure)Structure.newInstance(dstType);
            byte[] buf = new byte[s.size()];
            ((Pointer)value).read(0L, buf, 0, buf.length);
            s.getPointer().write(0L, buf, 0, buf.length);
            s.read();
            value = s;
          } else {
            Structure s = (Structure)Structure.newInstance(dstType, (Pointer)value);
            s.conditionalAutoRead();
            value = s;
          } 
        } 
      } else if ((boolean.class == dstType || Boolean.class == dstType) && value instanceof Number) {
        value = Function.valueOf((((Number)value).intValue() != 0));
      } 
      return value;
    }
    
    private Object convertResult(Object value) {
      if (this.toNative != null)
        value = this.toNative.toNative(value, new CallbackResultContext(this.callbackMethod)); 
      if (value == null)
        return null; 
      Class<?> cls = value.getClass();
      if (Structure.class.isAssignableFrom(cls)) {
        if (Structure.ByValue.class.isAssignableFrom(cls))
          return value; 
        return ((Structure)value).getPointer();
      } 
      if (cls == boolean.class || cls == Boolean.class)
        return Boolean.TRUE.equals(value) ? Function.INTEGER_TRUE : Function.INTEGER_FALSE; 
      if (cls == String.class || cls == WString.class)
        return CallbackReference.getNativeString(value, (cls == WString.class)); 
      if (cls == String[].class || cls == WString.class) {
        StringArray sa = (cls == String[].class) ? new StringArray((String[])value, this.encoding) : new StringArray((WString[])value);
        CallbackReference.allocations.put(value, sa);
        return sa;
      } 
      if (Callback.class.isAssignableFrom(cls))
        return CallbackReference.getFunctionPointer((Callback)value); 
      return value;
    }
    
    public Class<?>[] getParameterTypes() {
      return this.callbackMethod.getParameterTypes();
    }
    
    public Class<?> getReturnType() {
      return this.callbackMethod.getReturnType();
    }
  }
  
  private static class NativeFunctionHandler implements InvocationHandler {
    private final Function function;
    
    private final Map<String, ?> options;
    
    public NativeFunctionHandler(Pointer address, int callingConvention, Map<String, ?> options) {
      this.options = options;
      this.function = new Function(address, callingConvention, (String)options.get("string-encoding"));
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (Library.Handler.OBJECT_TOSTRING.equals(method)) {
        String str = "Proxy interface to " + this.function;
        Method m = (Method)this.options.get("invoking-method");
        Class<?> cls = CallbackReference.findCallbackClass(m.getDeclaringClass());
        str = str + " (" + cls.getName() + ")";
        return str;
      } 
      if (Library.Handler.OBJECT_HASHCODE.equals(method))
        return Integer.valueOf(hashCode()); 
      if (Library.Handler.OBJECT_EQUALS.equals(method)) {
        Object o = args[0];
        if (o != null && Proxy.isProxyClass(o.getClass()))
          return Function.valueOf((Proxy.getInvocationHandler(o) == this)); 
        return Boolean.FALSE;
      } 
      if (Function.isVarArgs(method))
        args = Function.concatenateVarArgs(args); 
      return this.function.invoke(method.getReturnType(), args, this.options);
    }
    
    public Pointer getPointer() {
      return this.function;
    }
  }
  
  private static boolean isAllowableNativeType(Class<?> cls) {
    return (cls == void.class || cls == Void.class || cls == boolean.class || cls == Boolean.class || cls == byte.class || cls == Byte.class || cls == short.class || cls == Short.class || cls == char.class || cls == Character.class || cls == int.class || cls == Integer.class || cls == long.class || cls == Long.class || cls == float.class || cls == Float.class || cls == double.class || cls == Double.class || (Structure.ByValue.class
      
      .isAssignableFrom(cls) && Structure.class
      .isAssignableFrom(cls)) || Pointer.class
      .isAssignableFrom(cls));
  }
  
  private static Pointer getNativeString(Object value, boolean wide) {
    if (value != null) {
      NativeString ns = new NativeString(value.toString(), wide);
      allocations.put(value, ns);
      return ns.getPointer();
    } 
    return null;
  }
}
