package org.apache.commons.collections4.functors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;

public class PrototypeFactory {
  public static <T> Factory<T> prototypeFactory(T prototype) {
    if (prototype == null)
      return ConstantFactory.constantFactory(null); 
    try {
      Method method = prototype.getClass().getMethod("clone", (Class[])null);
      return new PrototypeCloneFactory<T>(prototype, method);
    } catch (NoSuchMethodException ex) {
      try {
        prototype.getClass().getConstructor(new Class[] { prototype.getClass() });
        return new InstantiateFactory<T>((Class)prototype.getClass(), new Class[] { prototype.getClass() }, new Object[] { prototype });
      } catch (NoSuchMethodException ex2) {
        if (prototype instanceof Serializable)
          return (Factory)new PrototypeSerializationFactory<Serializable>((Serializable)prototype); 
        throw new IllegalArgumentException("The prototype must be cloneable via a public clone method");
      } 
    } 
  }
  
  static class PrototypeCloneFactory<T> implements Factory<T>, Serializable {
    private static final long serialVersionUID = 5604271422565175555L;
    
    private final T iPrototype;
    
    private transient Method iCloneMethod;
    
    private PrototypeCloneFactory(T prototype, Method method) {
      this.iPrototype = prototype;
      this.iCloneMethod = method;
    }
    
    private void findCloneMethod() {
      try {
        this.iCloneMethod = this.iPrototype.getClass().getMethod("clone", (Class[])null);
      } catch (NoSuchMethodException ex) {
        throw new IllegalArgumentException("PrototypeCloneFactory: The clone method must exist and be public ");
      } 
    }
    
    public T create() {
      if (this.iCloneMethod == null)
        findCloneMethod(); 
      try {
        return (T)this.iCloneMethod.invoke(this.iPrototype, (Object[])null);
      } catch (IllegalAccessException ex) {
        throw new FunctorException("PrototypeCloneFactory: Clone method must be public", ex);
      } catch (InvocationTargetException ex) {
        throw new FunctorException("PrototypeCloneFactory: Clone method threw an exception", ex);
      } 
    }
  }
  
  static class PrototypeSerializationFactory<T extends Serializable> implements Factory<T>, Serializable {
    private static final long serialVersionUID = -8704966966139178833L;
    
    private final T iPrototype;
    
    private PrototypeSerializationFactory(T prototype) {
      this.iPrototype = prototype;
    }
    
    public T create() {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      ByteArrayInputStream bais = null;
      try {
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(this.iPrototype);
        bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        return (T)in.readObject();
      } catch (ClassNotFoundException ex) {
        throw new FunctorException(ex);
      } catch (IOException ex) {
        throw new FunctorException(ex);
      } finally {
        try {
          if (bais != null)
            bais.close(); 
        } catch (IOException ex) {}
        try {
          baos.close();
        } catch (IOException ex) {}
      } 
    }
  }
}
