package com.sun.jna;

import java.lang.reflect.Method;

abstract class VarArgsChecker {
  private VarArgsChecker() {}
  
  private static final class RealVarArgsChecker extends VarArgsChecker {
    private RealVarArgsChecker() {}
    
    boolean isVarArgs(Method m) {
      return m.isVarArgs();
    }
    
    int fixedArgs(Method m) {
      return m.isVarArgs() ? ((m.getParameterTypes()).length - 1) : 0;
    }
  }
  
  private static final class NoVarArgsChecker extends VarArgsChecker {
    private NoVarArgsChecker() {}
    
    boolean isVarArgs(Method m) {
      return false;
    }
    
    int fixedArgs(Method m) {
      return 0;
    }
  }
  
  static VarArgsChecker create() {
    try {
      Method isVarArgsMethod = Method.class.getMethod("isVarArgs", new Class[0]);
      if (isVarArgsMethod != null)
        return new RealVarArgsChecker(); 
      return new NoVarArgsChecker();
    } catch (NoSuchMethodException e) {
      return new NoVarArgsChecker();
    } catch (SecurityException e) {
      return new NoVarArgsChecker();
    } 
  }
  
  abstract boolean isVarArgs(Method paramMethod);
  
  abstract int fixedArgs(Method paramMethod);
}
