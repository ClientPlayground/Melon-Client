package net.minecraft.block.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;

public class PropertyInteger extends PropertyHelper<Integer> {
  private final ImmutableSet<Integer> allowedValues;
  
  protected PropertyInteger(String name, int min, int max) {
    super(name, Integer.class);
    if (min < 0)
      throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater"); 
    if (max <= min)
      throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")"); 
    Set<Integer> set = Sets.newHashSet();
    for (int i = min; i <= max; i++)
      set.add(Integer.valueOf(i)); 
    this.allowedValues = ImmutableSet.copyOf(set);
  }
  
  public Collection<Integer> getAllowedValues() {
    return (Collection<Integer>)this.allowedValues;
  }
  
  public boolean equals(Object p_equals_1_) {
    if (this == p_equals_1_)
      return true; 
    if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
      if (!super.equals(p_equals_1_))
        return false; 
      PropertyInteger propertyinteger = (PropertyInteger)p_equals_1_;
      return this.allowedValues.equals(propertyinteger.allowedValues);
    } 
    return false;
  }
  
  public int hashCode() {
    int i = super.hashCode();
    i = 31 * i + this.allowedValues.hashCode();
    return i;
  }
  
  public static PropertyInteger create(String name, int min, int max) {
    return new PropertyInteger(name, min, max);
  }
  
  public String getName(Integer value) {
    return value.toString();
  }
}
