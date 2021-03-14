package me.kaimson.melonclient.Events;

import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventHandler {
  private static final Map<Class<? extends Event>, List<MethodData>> bus = Maps.newHashMap();
  
  public static void register(Object o) {
    for (Method method : o.getClass().getDeclaredMethods()) {
      if ((method.getParameterTypes()).length == 1 && method.isAnnotationPresent((Class)TypeEvent.class))
        register(method, o); 
    } 
  }
  
  private static void register(Method m, Object s) {
    Class<? extends Event> c = (Class)m.getParameterTypes()[0];
    final MethodData d = new MethodData(s, m);
    if (bus.containsKey(c)) {
      if (!((List)bus.get(c)).contains(d))
        ((List<MethodData>)bus.get(c)).add(d); 
    } else {
      bus.put(c, new CopyOnWriteArrayList<MethodData>() {
            private static final long serialVersionUID = 666L;
          });
    } 
  }
  
  public static void unregister(Object object) {
    for (Iterator<List<MethodData>> iterator = bus.values().iterator(); iterator.hasNext(); ) {
      List<MethodData> dataList = iterator.next();
      dataList.removeIf(data -> data.getSource().equals(object));
    } 
  }
  
  public static void unregister(Object object, Class<? extends Event> eventClass) {
    if (bus.containsKey(eventClass))
      ((List)bus.get(eventClass)).removeIf(data -> data.getSource().equals(object)); 
  }
  
  public static boolean call(Event e) {
    List<MethodData> list = bus.get(e.getClass());
    if (list != null)
      for (MethodData m : list)
        invoke(m, e);  
    return (e instanceof Cancellable && ((Cancellable)e).isCancelled());
  }
  
  private static void invoke(MethodData data, Event argument) {
    try {
      data.getTarget().setAccessible(true);
      data.getTarget().invoke(data.getSource(), new Object[] { argument });
    } catch (IllegalAccessException|IllegalArgumentException|java.lang.reflect.InvocationTargetException e) {
      e.printStackTrace();
    } 
  }
  
  private static final class MethodData {
    Object source;
    
    Method target;
    
    public MethodData(Object source, Method target) {
      this.source = source;
      this.target = target;
    }
    
    public Object getSource() {
      return this.source;
    }
    
    public Method getTarget() {
      return this.target;
    }
  }
}
