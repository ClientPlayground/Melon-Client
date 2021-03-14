package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class AbstractComposedGuiElement<T extends AbstractComposedGuiElement<T>> extends AbstractGuiElement<T> implements ComposedGuiElement<T> {
  public AbstractComposedGuiElement() {}
  
  public AbstractComposedGuiElement(GuiContainer container) {
    super(container);
  }
  
  public int getMaxLayer() {
    return getLayer() + ((Integer)Ordering.natural().max(Iterables.concat(Collections.singleton(Integer.valueOf(0)), Iterables.transform(getChildren(), new Function<GuiElement, Integer>() {
              public Integer apply(GuiElement e) {
                return Integer.valueOf((e instanceof ComposedGuiElement) ? ((ComposedGuiElement)e).getMaxLayer() : e.getLayer());
              }
            })))).intValue();
  }
  
  public <C> C forEach(Class<C> ofType) {
    int maxLayer = getMaxLayer();
    final List<C> layers = new ArrayList<>(maxLayer + 1);
    for (int i = maxLayer; i >= 0; i--)
      layers.add(forEach(i, ofType)); 
    return (C)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ofType }, new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isGetter = method.getName().startsWith("get");
            Object handled = method.getReturnType().equals(boolean.class) ? Boolean.valueOf(false) : null;
            Iterator var6 = layers.iterator();
            while (var6.hasNext()) {
              Object layer = var6.next();
              try {
                handled = method.invoke(layer, args);
              } catch (Throwable var11) {
                Throwable e = var11;
                if (var11 instanceof java.lang.reflect.InvocationTargetException)
                  e = var11.getCause(); 
                CrashReport crash = CrashReport.makeCrashReport(e, "Calling Gui method");
                CrashReportCategory category = crash.makeCategory("Gui");
                category.addCrashSectionCallable("Method", method::toString);
                category.addCrashSectionCallable("ComposedElement", AbstractComposedGuiElement.this::toString);
                category.addCrashSectionCallable("Element", AbstractComposedGuiElement.this::toString);
                throw new ReportedException(crash);
              } 
              if (handled != null) {
                if (handled instanceof Boolean) {
                  if (Boolean.TRUE.equals(handled))
                    break; 
                  continue;
                } 
                if (isGetter)
                  return handled; 
              } 
            } 
            return handled;
          }
        });
  }
  
  public <C> C forEach(final int layer, final Class<C> ofType) {
    return (C)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ofType }, new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isGetter = method.getName().startsWith("get");
            Object handled = method.getReturnType().equals(boolean.class) ? Boolean.valueOf(false) : null;
            AbstractComposedGuiElement self = AbstractComposedGuiElement.this;
            if (ofType.isInstance(self) && self.getLayer() == layer) {
              try {
                handled = method.invoke(self, args);
              } catch (Throwable var12) {
                Throwable ex = var12;
                if (var12 instanceof java.lang.reflect.InvocationTargetException)
                  ex = var12.getCause(); 
                CrashReport crash = CrashReport.makeCrashReport(ex, "Calling Gui method");
                CrashReportCategory categoryx = crash.makeCategory("Gui");
                categoryx.addCrashSectionCallable("Method", method::toString);
                categoryx.addCrashSectionCallable("ComposedElement", self::toString);
                categoryx.addCrashSectionCallable("Element", self::toString);
                throw new ReportedException(crash);
              } 
              if (handled != null)
                if (handled instanceof Boolean) {
                  if (Boolean.TRUE.equals(handled))
                    return Boolean.valueOf(true); 
                } else if (isGetter) {
                  return handled;
                }  
            } 
            Iterator<GuiElement> var14 = AbstractComposedGuiElement.this.getChildren().iterator();
            while (var14.hasNext()) {
              GuiElement element = var14.next();
              try {
                if (element instanceof ComposedGuiElement) {
                  ComposedGuiElement composed = (ComposedGuiElement)element;
                  if (layer <= composed.getMaxLayer()) {
                    Object elementProxy = composed.forEach(layer - composed.getLayer(), ofType);
                    handled = method.invoke(elementProxy, args);
                  } 
                } else if (ofType.isInstance(element) && element.getLayer() == layer) {
                  handled = method.invoke(element, args);
                } 
                if (handled != null) {
                  if (handled instanceof Boolean) {
                    if (Boolean.TRUE.equals(handled))
                      break; 
                    continue;
                  } 
                  if (isGetter)
                    return handled; 
                } 
              } catch (Throwable var13) {
                Throwable e = var13;
                if (var13 instanceof java.lang.reflect.InvocationTargetException)
                  e = var13.getCause(); 
                CrashReport crashx = CrashReport.makeCrashReport(e, "Calling Gui method");
                CrashReportCategory category = crashx.makeCategory("Gui");
                category.addCrashSectionCallable("Method", method::toString);
                category.addCrashSectionCallable("ComposedElement", element::toString);
                category.addCrashSectionCallable("Element", element::toString);
                throw new ReportedException(crashx);
              } 
            } 
            return handled;
          }
        });
  }
}
