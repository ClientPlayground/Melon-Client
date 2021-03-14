package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing;

import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import java.util.function.Supplier;

public enum InterpolatorType {
  DEFAULT("default", null, null),
  CATMULL_ROM("catmullrom", (Class)CatmullRomSplineInterpolator.class, () -> new CatmullRomSplineInterpolator(0.5D)),
  CUBIC("cubic", (Class)CubicSplineInterpolator.class, CubicSplineInterpolator::new),
  LINEAR("linear", (Class)LinearInterpolator.class, LinearInterpolator::new);
  
  InterpolatorType(String localizationKey, Class<? extends Interpolator> interpolatorClass, Supplier<Interpolator> interpolatorConstructor) {
    this.localizationKey = localizationKey;
    this.interpolatorClass = interpolatorClass;
    this.interpolatorConstructor = interpolatorConstructor;
  }
  
  private final String localizationKey;
  
  private final Class<? extends Interpolator> interpolatorClass;
  
  private final Supplier<Interpolator> interpolatorConstructor;
  
  public String getLocalizationKey() {
    return this.localizationKey;
  }
  
  public Class<? extends Interpolator> getInterpolatorClass() {
    return this.interpolatorClass;
  }
  
  public String getI18nName() {
    return String.format("replaymod.gui.editkeyframe.interpolator.%1$s.name", new Object[] { this.localizationKey });
  }
  
  public String getI18nDescription() {
    return String.format("replaymod.gui.editkeyframe.interpolator.%1$s.desc", new Object[] { this.localizationKey });
  }
  
  public static InterpolatorType fromString(String string) {
    for (InterpolatorType t : values()) {
      if (t.getI18nName().equals(string))
        return t; 
    } 
    return CATMULL_ROM;
  }
  
  public static InterpolatorType fromClass(Class<? extends Interpolator> cls) {
    for (InterpolatorType type : values()) {
      if (cls.equals(type.getInterpolatorClass()))
        return type; 
    } 
    return DEFAULT;
  }
  
  public Interpolator newInstance() {
    return this.interpolatorConstructor.get();
  }
}
