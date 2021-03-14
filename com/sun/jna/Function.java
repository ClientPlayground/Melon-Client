package com.sun.jna;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class Function extends Pointer {
  public static final int MAX_NARGS = 256;
  
  public static final int C_CONVENTION = 0;
  
  public static final int ALT_CONVENTION = 63;
  
  private static final int MASK_CC = 63;
  
  public static final int THROW_LAST_ERROR = 64;
  
  public static final int USE_VARARGS = 384;
  
  static final Integer INTEGER_TRUE = Integer.valueOf(-1);
  
  static final Integer INTEGER_FALSE = Integer.valueOf(0);
  
  private NativeLibrary library;
  
  private final String functionName;
  
  final String encoding;
  
  final int callFlags;
  
  final Map<String, ?> options;
  
  static final String OPTION_INVOKING_METHOD = "invoking-method";
  
  public static Function getFunction(String libraryName, String functionName) {
    return NativeLibrary.getInstance(libraryName).getFunction(functionName);
  }
  
  public static Function getFunction(String libraryName, String functionName, int callFlags) {
    return NativeLibrary.getInstance(libraryName).getFunction(functionName, callFlags, null);
  }
  
  public static Function getFunction(String libraryName, String functionName, int callFlags, String encoding) {
    return NativeLibrary.getInstance(libraryName).getFunction(functionName, callFlags, encoding);
  }
  
  public static Function getFunction(Pointer p) {
    return getFunction(p, 0, (String)null);
  }
  
  public static Function getFunction(Pointer p, int callFlags) {
    return getFunction(p, callFlags, (String)null);
  }
  
  public static Function getFunction(Pointer p, int callFlags, String encoding) {
    return new Function(p, callFlags, encoding);
  }
  
  private static final VarArgsChecker IS_VARARGS = VarArgsChecker.create();
  
  Function(NativeLibrary library, String functionName, int callFlags, String encoding) {
    checkCallingConvention(callFlags & 0x3F);
    if (functionName == null)
      throw new NullPointerException("Function name must not be null"); 
    this.library = library;
    this.functionName = functionName;
    this.callFlags = callFlags;
    this.options = library.options;
    this.encoding = (encoding != null) ? encoding : Native.getDefaultStringEncoding();
    try {
      this.peer = library.getSymbolAddress(functionName);
    } catch (UnsatisfiedLinkError e) {
      throw new UnsatisfiedLinkError("Error looking up function '" + functionName + "': " + e
          
          .getMessage());
    } 
  }
  
  Function(Pointer functionAddress, int callFlags, String encoding) {
    checkCallingConvention(callFlags & 0x3F);
    if (functionAddress == null || functionAddress.peer == 0L)
      throw new NullPointerException("Function address may not be null"); 
    this.functionName = functionAddress.toString();
    this.callFlags = callFlags;
    this.peer = functionAddress.peer;
    this.options = Collections.EMPTY_MAP;
    this
      .encoding = (encoding != null) ? encoding : Native.getDefaultStringEncoding();
  }
  
  private void checkCallingConvention(int convention) throws IllegalArgumentException {
    if ((convention & 0x3F) != convention)
      throw new IllegalArgumentException("Unrecognized calling convention: " + convention); 
  }
  
  public String getName() {
    return this.functionName;
  }
  
  public int getCallingConvention() {
    return this.callFlags & 0x3F;
  }
  
  public Object invoke(Class<?> returnType, Object[] inArgs) {
    return invoke(returnType, inArgs, this.options);
  }
  
  public Object invoke(Class<?> returnType, Object[] inArgs, Map<String, ?> options) {
    Method invokingMethod = (Method)options.get("invoking-method");
    Class<?>[] paramTypes = (invokingMethod != null) ? invokingMethod.getParameterTypes() : null;
    return invoke(invokingMethod, paramTypes, returnType, inArgs, options);
  }
  
  Object invoke(Method invokingMethod, Class<?>[] paramTypes, Class<?> returnType, Object[] inArgs, Map<String, ?> options) {
    Object[] args = new Object[0];
    if (inArgs != null) {
      if (inArgs.length > 256)
        throw new UnsupportedOperationException("Maximum argument count is 256"); 
      args = new Object[inArgs.length];
      System.arraycopy(inArgs, 0, args, 0, args.length);
    } 
    TypeMapper mapper = (TypeMapper)options.get("type-mapper");
    boolean allowObjects = Boolean.TRUE.equals(options.get("allow-objects"));
    boolean isVarArgs = (args.length > 0 && invokingMethod != null) ? isVarArgs(invokingMethod) : false;
    int fixedArgs = (args.length > 0 && invokingMethod != null) ? fixedArgs(invokingMethod) : 0;
    for (int i = 0; i < args.length; i++) {
      Class<?> paramType = (invokingMethod != null) ? ((isVarArgs && i >= paramTypes.length - 1) ? paramTypes[paramTypes.length - 1].getComponentType() : paramTypes[i]) : null;
      args[i] = convertArgument(args, i, invokingMethod, mapper, allowObjects, paramType);
    } 
    Class<?> nativeReturnType = returnType;
    FromNativeConverter resultConverter = null;
    if (NativeMapped.class.isAssignableFrom(returnType)) {
      NativeMappedConverter tc = NativeMappedConverter.getInstance(returnType);
      resultConverter = tc;
      nativeReturnType = tc.nativeType();
    } else if (mapper != null) {
      resultConverter = mapper.getFromNativeConverter(returnType);
      if (resultConverter != null)
        nativeReturnType = resultConverter.nativeType(); 
    } 
    Object result = invoke(args, nativeReturnType, allowObjects, fixedArgs);
    if (resultConverter != null) {
      FromNativeContext context;
      if (invokingMethod != null) {
        context = new MethodResultContext(returnType, this, inArgs, invokingMethod);
      } else {
        context = new FunctionResultContext(returnType, this, inArgs);
      } 
      result = resultConverter.fromNative(result, context);
    } 
    if (inArgs != null)
      for (int j = 0; j < inArgs.length; j++) {
        Object inArg = inArgs[j];
        if (inArg != null)
          if (inArg instanceof Structure) {
            if (!(inArg instanceof Structure.ByValue))
              ((Structure)inArg).autoRead(); 
          } else if (args[j] instanceof PostCallRead) {
            ((PostCallRead)args[j]).read();
            if (args[j] instanceof PointerArray) {
              PointerArray array = (PointerArray)args[j];
              if (Structure.ByReference[].class.isAssignableFrom(inArg.getClass())) {
                Class<? extends Structure> type = (Class)inArg.getClass().getComponentType();
                Structure[] ss = (Structure[])inArg;
                for (int si = 0; si < ss.length; si++) {
                  Pointer p = array.getPointer((Native.POINTER_SIZE * si));
                  ss[si] = Structure.updateStructureByReference((Class)type, ss[si], p);
                } 
              } 
            } 
          } else if (Structure[].class.isAssignableFrom(inArg.getClass())) {
            Structure.autoRead((Structure[])inArg);
          }  
      }  
    return result;
  }
  
  Object invoke(Object[] args, Class<?> returnType, boolean allowObjects) {
    return invoke(args, returnType, allowObjects, 0);
  }
  
  Object invoke(Object[] args, Class<?> returnType, boolean allowObjects, int fixedArgs) {
    Object result = null;
    int callFlags = this.callFlags | (fixedArgs & 0x3) << 7;
    if (returnType == null || returnType == void.class || returnType == Void.class) {
      Native.invokeVoid(this, this.peer, callFlags, args);
      result = null;
    } else if (returnType == boolean.class || returnType == Boolean.class) {
      result = valueOf((Native.invokeInt(this, this.peer, callFlags, args) != 0));
    } else if (returnType == byte.class || returnType == Byte.class) {
      result = Byte.valueOf((byte)Native.invokeInt(this, this.peer, callFlags, args));
    } else if (returnType == short.class || returnType == Short.class) {
      result = Short.valueOf((short)Native.invokeInt(this, this.peer, callFlags, args));
    } else if (returnType == char.class || returnType == Character.class) {
      result = Character.valueOf((char)Native.invokeInt(this, this.peer, callFlags, args));
    } else if (returnType == int.class || returnType == Integer.class) {
      result = Integer.valueOf(Native.invokeInt(this, this.peer, callFlags, args));
    } else if (returnType == long.class || returnType == Long.class) {
      result = Long.valueOf(Native.invokeLong(this, this.peer, callFlags, args));
    } else if (returnType == float.class || returnType == Float.class) {
      result = Float.valueOf(Native.invokeFloat(this, this.peer, callFlags, args));
    } else if (returnType == double.class || returnType == Double.class) {
      result = Double.valueOf(Native.invokeDouble(this, this.peer, callFlags, args));
    } else if (returnType == String.class) {
      result = invokeString(callFlags, args, false);
    } else if (returnType == WString.class) {
      String s = invokeString(callFlags, args, true);
      if (s != null)
        result = new WString(s); 
    } else {
      if (Pointer.class.isAssignableFrom(returnType))
        return invokePointer(callFlags, args); 
      if (Structure.class.isAssignableFrom(returnType)) {
        if (Structure.ByValue.class.isAssignableFrom(returnType)) {
          Structure s = Native.invokeStructure(this, this.peer, callFlags, args, 
              (Structure)Structure.newInstance(returnType));
          s.autoRead();
          result = s;
        } else {
          result = invokePointer(callFlags, args);
          if (result != null) {
            Structure s = (Structure)Structure.newInstance(returnType, (Pointer)result);
            s.conditionalAutoRead();
            result = s;
          } 
        } 
      } else if (Callback.class.isAssignableFrom(returnType)) {
        result = invokePointer(callFlags, args);
        if (result != null)
          result = CallbackReference.getCallback(returnType, (Pointer)result); 
      } else if (returnType == String[].class) {
        Pointer p = invokePointer(callFlags, args);
        if (p != null)
          result = p.getStringArray(0L, this.encoding); 
      } else if (returnType == WString[].class) {
        Pointer p = invokePointer(callFlags, args);
        if (p != null) {
          String[] arr = p.getWideStringArray(0L);
          WString[] warr = new WString[arr.length];
          for (int i = 0; i < arr.length; i++)
            warr[i] = new WString(arr[i]); 
          result = warr;
        } 
      } else if (returnType == Pointer[].class) {
        Pointer p = invokePointer(callFlags, args);
        if (p != null)
          result = p.getPointerArray(0L); 
      } else if (allowObjects) {
        result = Native.invokeObject(this, this.peer, callFlags, args);
        if (result != null && 
          !returnType.isAssignableFrom(result.getClass()))
          throw new ClassCastException("Return type " + returnType + " does not match result " + result
              
              .getClass()); 
      } else {
        throw new IllegalArgumentException("Unsupported return type " + returnType + " in function " + getName());
      } 
    } 
    return result;
  }
  
  private Pointer invokePointer(int callFlags, Object[] args) {
    long ptr = Native.invokePointer(this, this.peer, callFlags, args);
    return (ptr == 0L) ? null : new Pointer(ptr);
  }
  
  private Object convertArgument(Object[] args, int index, Method invokingMethod, TypeMapper mapper, boolean allowObjects, Class<?> expectedType) {
    Object arg = args[index];
    if (arg != null) {
      Class<?> type = arg.getClass();
      ToNativeConverter converter = null;
      if (NativeMapped.class.isAssignableFrom(type)) {
        converter = NativeMappedConverter.getInstance(type);
      } else if (mapper != null) {
        converter = mapper.getToNativeConverter(type);
      } 
      if (converter != null) {
        ToNativeContext context;
        if (invokingMethod != null) {
          context = new MethodParameterContext(this, args, index, invokingMethod);
        } else {
          context = new FunctionParameterContext(this, args, index);
        } 
        arg = converter.toNative(arg, context);
      } 
    } 
    if (arg == null || isPrimitiveArray(arg.getClass()))
      return arg; 
    Class<?> argClass = arg.getClass();
    if (arg instanceof Structure) {
      Structure struct = (Structure)arg;
      struct.autoWrite();
      if (struct instanceof Structure.ByValue) {
        Class<?> ptype = struct.getClass();
        if (invokingMethod != null) {
          Class<?>[] ptypes = invokingMethod.getParameterTypes();
          if (IS_VARARGS.isVarArgs(invokingMethod)) {
            if (index < ptypes.length - 1) {
              ptype = ptypes[index];
            } else {
              Class<?> etype = ptypes[ptypes.length - 1].getComponentType();
              if (etype != Object.class)
                ptype = etype; 
            } 
          } else {
            ptype = ptypes[index];
          } 
        } 
        if (Structure.ByValue.class.isAssignableFrom(ptype))
          return struct; 
      } 
      return struct.getPointer();
    } 
    if (arg instanceof Callback)
      return CallbackReference.getFunctionPointer((Callback)arg); 
    if (arg instanceof String)
      return (new NativeString((String)arg, false)).getPointer(); 
    if (arg instanceof WString)
      return (new NativeString(arg.toString(), true)).getPointer(); 
    if (arg instanceof Boolean)
      return Boolean.TRUE.equals(arg) ? INTEGER_TRUE : INTEGER_FALSE; 
    if (String[].class == argClass)
      return new StringArray((String[])arg, this.encoding); 
    if (WString[].class == argClass)
      return new StringArray((WString[])arg); 
    if (Pointer[].class == argClass)
      return new PointerArray((Pointer[])arg); 
    if (NativeMapped[].class.isAssignableFrom(argClass))
      return new NativeMappedArray((NativeMapped[])arg); 
    if (Structure[].class.isAssignableFrom(argClass)) {
      Structure[] ss = (Structure[])arg;
      Class<?> type = argClass.getComponentType();
      boolean byRef = Structure.ByReference.class.isAssignableFrom(type);
      if (expectedType != null && 
        !Structure.ByReference[].class.isAssignableFrom(expectedType)) {
        if (byRef)
          throw new IllegalArgumentException("Function " + getName() + " declared Structure[] at parameter " + index + " but array of " + type + " was passed"); 
        for (int i = 0; i < ss.length; i++) {
          if (ss[i] instanceof Structure.ByReference)
            throw new IllegalArgumentException("Function " + getName() + " declared Structure[] at parameter " + index + " but element " + i + " is of Structure.ByReference type"); 
        } 
      } 
      if (byRef) {
        Structure.autoWrite(ss);
        Pointer[] pointers = new Pointer[ss.length + 1];
        for (int i = 0; i < ss.length; i++)
          pointers[i] = (ss[i] != null) ? ss[i].getPointer() : null; 
        return new PointerArray(pointers);
      } 
      if (ss.length == 0)
        throw new IllegalArgumentException("Structure array must have non-zero length"); 
      if (ss[0] == null) {
        Structure.newInstance(type).toArray(ss);
        return ss[0].getPointer();
      } 
      Structure.autoWrite(ss);
      return ss[0].getPointer();
    } 
    if (argClass.isArray())
      throw new IllegalArgumentException("Unsupported array argument type: " + argClass
          .getComponentType()); 
    if (allowObjects)
      return arg; 
    if (!Native.isSupportedNativeType(arg.getClass()))
      throw new IllegalArgumentException("Unsupported argument type " + arg
          .getClass().getName() + " at parameter " + index + " of function " + 
          
          getName()); 
    return arg;
  }
  
  private boolean isPrimitiveArray(Class<?> argClass) {
    return (argClass.isArray() && argClass
      .getComponentType().isPrimitive());
  }
  
  public void invoke(Object[] args) {
    invoke(Void.class, args);
  }
  
  private String invokeString(int callFlags, Object[] args, boolean wide) {
    Pointer ptr = invokePointer(callFlags, args);
    String s = null;
    if (ptr != null)
      if (wide) {
        s = ptr.getWideString(0L);
      } else {
        s = ptr.getString(0L, this.encoding);
      }  
    return s;
  }
  
  public String toString() {
    if (this.library != null)
      return "native function " + this.functionName + "(" + this.library.getName() + ")@0x" + 
        Long.toHexString(this.peer); 
    return "native function@0x" + Long.toHexString(this.peer);
  }
  
  public Object invokeObject(Object[] args) {
    return invoke(Object.class, args);
  }
  
  public Pointer invokePointer(Object[] args) {
    return (Pointer)invoke(Pointer.class, args);
  }
  
  public String invokeString(Object[] args, boolean wide) {
    Object o = invoke(wide ? WString.class : String.class, args);
    return (o != null) ? o.toString() : null;
  }
  
  public int invokeInt(Object[] args) {
    return ((Integer)invoke(Integer.class, args)).intValue();
  }
  
  public long invokeLong(Object[] args) {
    return ((Long)invoke(Long.class, args)).longValue();
  }
  
  public float invokeFloat(Object[] args) {
    return ((Float)invoke(Float.class, args)).floatValue();
  }
  
  public double invokeDouble(Object[] args) {
    return ((Double)invoke(Double.class, args)).doubleValue();
  }
  
  public void invokeVoid(Object[] args) {
    invoke(Void.class, args);
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (o == null)
      return false; 
    if (o.getClass() == getClass()) {
      Function other = (Function)o;
      return (other.callFlags == this.callFlags && other.options
        .equals(this.options) && other.peer == this.peer);
    } 
    return false;
  }
  
  public int hashCode() {
    return this.callFlags + this.options.hashCode() + super.hashCode();
  }
  
  static Object[] concatenateVarArgs(Object[] inArgs) {
    if (inArgs != null && inArgs.length > 0) {
      Object lastArg = inArgs[inArgs.length - 1];
      Class<?> argType = (lastArg != null) ? lastArg.getClass() : null;
      if (argType != null && argType.isArray()) {
        Object[] varArgs = (Object[])lastArg;
        for (int i = 0; i < varArgs.length; i++) {
          if (varArgs[i] instanceof Float)
            varArgs[i] = Double.valueOf(((Float)varArgs[i]).floatValue()); 
        } 
        Object[] fullArgs = new Object[inArgs.length + varArgs.length];
        System.arraycopy(inArgs, 0, fullArgs, 0, inArgs.length - 1);
        System.arraycopy(varArgs, 0, fullArgs, inArgs.length - 1, varArgs.length);
        fullArgs[fullArgs.length - 1] = null;
        inArgs = fullArgs;
      } 
    } 
    return inArgs;
  }
  
  static boolean isVarArgs(Method m) {
    return IS_VARARGS.isVarArgs(m);
  }
  
  static int fixedArgs(Method m) {
    return IS_VARARGS.fixedArgs(m);
  }
  
  public static interface PostCallRead {
    void read();
  }
  
  private static class NativeMappedArray extends Memory implements PostCallRead {
    private final NativeMapped[] original;
    
    public NativeMappedArray(NativeMapped[] arg) {
      super(Native.getNativeSize(arg.getClass(), arg));
      this.original = arg;
      setValue(0L, this.original, this.original.getClass());
    }
    
    public void read() {
      getValue(0L, this.original.getClass(), this.original);
    }
  }
  
  private static class PointerArray extends Memory implements PostCallRead {
    private final Pointer[] original;
    
    public PointerArray(Pointer[] arg) {
      super((Native.POINTER_SIZE * (arg.length + 1)));
      this.original = arg;
      for (int i = 0; i < arg.length; i++)
        setPointer((i * Native.POINTER_SIZE), arg[i]); 
      setPointer((Native.POINTER_SIZE * arg.length), (Pointer)null);
    }
    
    public void read() {
      read(0L, this.original, 0, this.original.length);
    }
  }
  
  static Boolean valueOf(boolean b) {
    return b ? Boolean.TRUE : Boolean.FALSE;
  }
}
